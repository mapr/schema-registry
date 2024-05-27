/**
 * Copyright 2018 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.</p>
 **/

package io.confluent.kafka.schemaregistry.leaderelector.kafka;

import com.google.protobuf.ByteString;
import com.mapr.fs.proto.Marlinserver;
import com.mapr.kafka.eventstreams.impl.MarlinCoordinator;
import com.mapr.kafka.eventstreams.impl.listener.MarlinListener;
import io.confluent.kafka.schemaregistry.metrics.SchemaRegistryMetric;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistryIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MarlinSRCoordinator extends MarlinCoordinator
        implements Closeable, GenericSRCoordinator {
  private static final Logger log = LoggerFactory.getLogger(MarlinSRCoordinator.class);

  public static final String SR_SUBPROTOCOL_V0 = "v0";

  private final SchemaRegistryIdentity identity;
  private SchemaRegistryProtocol.Assignment assignmentSnapshot;
  private final SchemaRegistryRebalanceListener listener;
  private final SchemaRegistryMetric nodeCountMetric;
  private final boolean stickyLeaderElection;
  private final SchemaRegistryConfig config;

  public MarlinSRCoordinator(String groupId,
                             SchemaRegistryConfig config,
                             SchemaRegistryIdentity identity,
                             SchemaRegistryRebalanceListener listener,
                             SchemaRegistryMetric nodeCountMetric,
                             boolean stickyLeaderElection) {
    super(groupId);
    this.config = config;
    this.identity = identity;
    this.listener = listener;
    this.stickyLeaderElection = stickyLeaderElection;
    this.nodeCountMetric = nodeCountMetric;
    init();
    log.debug("MarlinSchemaRegistryCoordinator constructor");
  }

  @Override
  protected Logger getLogger() {
    return log;
  }

  @Override
  protected MarlinListener.MarlinJoinCallback getJoinerCallback() {
    return new MarlinSchemaRegistryJoinCallback();
  }

  @Override
  protected String generateSyncTopic(String groupId) {
    return "__mapr__" + groupId + "_assignment";
  }

  @Override
  protected String generateCoordStream() {
    return config.getKafkaStoreStream();
  }

  @Override
  protected Marlinserver.JoinGroupDesc generateJoinDesc() {
    ByteBuffer metadata = SchemaRegistryProtocol.serializeMetadata(identity);
    return Marlinserver.JoinGroupDesc.newBuilder().setProtocolType(SR_SUBPROTOCOL_V0)
            .setMemberId(this.memberId)
            .addMemberProtocols(Marlinserver.MemberProtocol.newBuilder()
                    .setProtocol("default")
                    .setMemberMetadata(ByteString.copyFrom(metadata)).build())
            .build();
  }

  @Override
  protected void revokeAssignments() {
    if (assignmentSnapshot != null) {
      listener.onRevoked();
    }
  }

  @Override
  protected void protocolOnSyncComplete(Marlinserver.MemberState memberState, long generation) {
    assignmentSnapshot = SchemaRegistryProtocol.deserializeAssignment(
            ByteBuffer.wrap(memberState.getMemberAssignment().toByteArray()));
    if (stickyLeaderElection && assignmentSnapshot != null
            && assignmentSnapshot.leaderIdentity() != null) {
      log.info("assignmentLeaderIdentity: {}, myIdentity: {}",
              assignmentSnapshot.leaderIdentity(), identity);
      identity.setLeader(assignmentSnapshot.leaderIdentity().equals(identity));
    }
    listener.onAssigned(assignmentSnapshot, (int)generation);
  }

  @Override
  protected Map<String, ByteBuffer> performProtocolAssignment(
          String leaderId, // Kafka group "leader" who does assignment, *not* the SR leader
          List<Marlinserver.Member> members) {
    log.info("Performing assignment");

    Map<String, SchemaRegistryIdentity> memberConfigs = new HashMap<>();
    for (Marlinserver.Member entry : members) {
      SchemaRegistryIdentity identity
              = SchemaRegistryProtocol.deserializeMetadata(
                      ByteBuffer.wrap(entry.getMemberMetadata().toByteArray()));
      memberConfigs.put(entry.getMemberId(), identity);
    }

    log.info("Member information: {}", memberConfigs);

    if (nodeCountMetric != null) {
      nodeCountMetric.record(memberConfigs.size());
    }

    // Compute the leader as the leader-eligible member with the "smallest" (lexicographically) ID.
    // If the group has an existing leader, this leader takes precedence. If multiple group
    // members are determined to be leaders, fall back to the member with the smallest ID.
    // This guarantees that a member will stay as the leader until it leaves the group or the
    // coordinator detects that the member's heartbeat stopped and evicts the member.
    SchemaRegistryIdentity leaderIdentity = null;
    String leaderKafkaId = null;
    SchemaRegistryIdentity existingLeaderIdentity = null;
    String existingLeaderKafkaId = null;
    boolean multipleLeadersFound = false;
    Set<String> urls = new HashSet<>();
    for (Map.Entry<String, SchemaRegistryIdentity> entry : memberConfigs.entrySet()) {
      String kafkaMemberId = entry.getKey();
      SchemaRegistryIdentity memberIdentity = entry.getValue();
      urls.add(memberIdentity.getUrl());
      boolean eligible = memberIdentity.getLeaderEligibility();
      boolean smallerIdentity = leaderIdentity == null
              || memberIdentity.getUrl().compareTo(leaderIdentity.getUrl()) < 0;
      if (eligible && smallerIdentity) {
        leaderKafkaId = kafkaMemberId;
        leaderIdentity = memberIdentity;
      }
      if (stickyLeaderElection && eligible && memberIdentity.isLeader() && !multipleLeadersFound) {
        if (existingLeaderIdentity != null) {
          log.warn("Multiple leaders found in group [{}, {}].",
                  existingLeaderIdentity, memberIdentity);
          multipleLeadersFound = true;
          existingLeaderKafkaId = null;
          existingLeaderIdentity = null;
        } else {
          existingLeaderKafkaId = kafkaMemberId;
          existingLeaderIdentity = memberIdentity;
        }
      }
    }
    short error = SchemaRegistryProtocol.Assignment.NO_ERROR;

    // Validate that schema registry instances aren't trying to use the same URL
    if (urls.size() != memberConfigs.size()) {
      log.error("Found duplicate URLs for schema registry group members. This indicates a "
              + "misconfiguration and is common when executing in containers. Use the host.name "
              + "configuration to set each instance's advertised host name to a value that is "
              + "routable from all other schema registry instances.");
      error = SchemaRegistryProtocol.Assignment.DUPLICATE_URLS;
    }

    Map<String, ByteBuffer> groupAssignment = new HashMap<>();
    if (stickyLeaderElection && existingLeaderKafkaId != null) {
      leaderKafkaId = existingLeaderKafkaId;
      leaderIdentity = existingLeaderIdentity;
      if (!identity.equals(leaderIdentity) && identity.isLeader()) {
        identity.setLeader(false);
      }
    }
    // All members currently receive the same assignment information since it is just the leader ID
    SchemaRegistryProtocol.Assignment assignment
            = new SchemaRegistryProtocol.Assignment(error, leaderKafkaId, leaderIdentity);
    log.info("Assignment: {}", assignment);
    for (String member : memberConfigs.keySet()) {
      groupAssignment.put(member, SchemaRegistryProtocol.serializeAssignment(assignment));
    }
    return groupAssignment;
  }

  @Override
  protected boolean isProtocolRejoinNeeded() {
    if (assignmentSnapshot != null) {
      log.debug("assignmentSnapshot.failed {}", assignmentSnapshot.failed());
    }
    return (assignmentSnapshot == null || assignmentSnapshot.failed());
  }

  @Override
  public void close() {
    super.close();
    this.assignmentSnapshot = null;
  }

  public class MarlinSchemaRegistryJoinCallback extends MarlinCoordinatorJoinCallback {
    @Override
    public void onJoin(Marlinserver.JoinGroupInfo joinGroupInfo) {
      performOnJoin(joinGroupInfo);
    }
  }

}
