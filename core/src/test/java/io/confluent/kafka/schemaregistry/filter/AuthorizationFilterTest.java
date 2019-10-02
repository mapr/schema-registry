package io.confluent.kafka.schemaregistry.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.confluent.kafka.schemaregistry.filter.util.KafkaConsumerPool;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.storage.KafkaProducerPool;
import io.confluent.kafka.schemaregistry.util.MaprFSUtils;
import io.confluent.kafka.schemaregistry.utils.UserGroupInformationMockPolicy;
import io.confluent.rest.RestConfigException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.ImmutableList.of;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@MockPolicy(UserGroupInformationMockPolicy.class)
@PowerMockRunnerDelegate(JUnitParamsRunner.class)
@PrepareOnlyThisForTest(MaprFSUtils.class)
public class AuthorizationFilterTest extends EasyMockSupport {

  private static final String AUXILIARY_TOPIC = "/stream:auxiliary-topic";
  private static final String NONE = "NONE";
  private static final String BASIC_AUTH = "Basic dXNlcjp1c2Vy";
  private static final String COOKIE_AUTH = "hadoop.auth=smt&u=user";
  private static final String IMPERSONATED_USER = "user";
  private static final String ADMIN_USER = System.getProperty("user.name");
  private KafkaConsumerPool consumerPool;
  private KafkaProducerPool producerPool;
  private AuthorizationFilter authorizationFilter;
  private SchemaRegistryConfig config;

  @Before
  public void setUp() throws RestConfigException {
    PowerMock.mockStaticPartial(MaprFSUtils.class, "getZKQuorum");
    EasyMock.expect(MaprFSUtils.getZKQuorum()).andStubReturn("none");
    PowerMock.replay(MaprFSUtils.class);

    consumerPool = mock(KafkaConsumerPool.class);
    producerPool = mock(KafkaProducerPool.class);
    expectUserSendsRecordToAuxiliaryTopicSucceeds(ADMIN_USER);
    replayAll();
    config = new SchemaRegistryConfig(new Properties());
    authorizationFilter = new AuthorizationFilter(config, consumerPool, producerPool);
    resetAll();
  }

  @Test
  public void dummyRecordIsSent() {
    expectUserSendsRecordToAuxiliaryTopicSucceeds(ADMIN_USER);
    replayAll();

    new AuthorizationFilter(config, consumerPool, producerPool);

    verifyAll();
  }

  @Test
  public void unauthenticatedRequestIsForbidden() {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, null, null);
    final Capture<Response> capturedResponse = Capture.newInstance();
    expectAbortWith(request, capture(capturedResponse));
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
    assertThat(capturedResponse.getValue().getStatusInfo().toEnum(), is(Response.Status.FORBIDDEN));
  }

  @Test
  @Parameters(value = {
          BASIC_AUTH + " | " + NONE,
          NONE + " | " + COOKIE_AUTH
  })
  public void authenticatedRequestSucceeds(String authorizationHeader, String cookie) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, authorizationHeader.equals(NONE) ? null : authorizationHeader, cookie.equals(NONE) ? null : of(cookie));
    expect(request.getMethod()).andReturn(HttpMethod.GET);
    expectUserPollsRecordsToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  @Parameters(value = {
          HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT
  })
  public void writeCommandIsForbiddenWithoutWritePermission(final String method) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, BASIC_AUTH, of(COOKIE_AUTH));
    expect(request.getMethod()).andReturn(method);
    final Capture<Response> captured = Capture.newInstance();
    expectAbortWith(request, capture(captured));
    expectUserSendsRecordToAuxiliaryTopicFails(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
    assertThat(captured.getValue().getStatusInfo().toEnum(), is(Response.Status.FORBIDDEN));
    assertThat(captured.getValue().getEntity().toString(), containsString("denied"));
  }


  @Test
  @Parameters(value = {
          HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT
  })
  public void writeCommandSucceedsWithWritePermission(final String method) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, BASIC_AUTH, of(COOKIE_AUTH));
    expect(request.getMethod()).andReturn(method);
    expectUserSendsRecordToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  @Parameters(value = {
          HttpMethod.GET
  })
  public void readCommandIsForbiddenWithoutReadPermission(String method) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, BASIC_AUTH, of(COOKIE_AUTH));
    expect(request.getMethod()).andReturn(method);
    final Capture<Response> captured = Capture.newInstance();
    expectAbortWith(request, capture(captured));
    expectUserPollsRecordsToAuxiliaryTopicFails(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
    assertThat(captured.getValue().getStatusInfo().toEnum(), is(Response.Status.FORBIDDEN));
    assertThat(captured.getValue().getEntity().toString(), containsString("denied"));
  }

  @Test
  @Parameters(value = {
          HttpMethod.GET
  })
  public void readCommandSucceedsWithReadPermission(String method) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, BASIC_AUTH, of(COOKIE_AUTH));
    expect(request.getMethod()).andReturn(method);
    expectUserPollsRecordsToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  @Parameters(value = {
          HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.OPTIONS,
  })
  public void otherCommandsSucceedWithoutAnyPermissions(String method) {
    final ContainerRequest request = mock(ContainerRequest.class);
    expectGatherAuthData(request, BASIC_AUTH, of(COOKIE_AUTH));
    expect(request.getMethod()).andReturn(method);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  private void expectGatherAuthData(ContainerRequest request, String header, List<String> cookie) {
    expect(request.getHeaderString(HttpHeaders.AUTHORIZATION)).andReturn(header);
    expect(request.getRequestHeader(HttpHeaders.COOKIE)).andReturn(cookie);
  }

  private void expectAbortWith(ContainerRequest request, Response response) {
    request.abortWith(response);
    expectLastCall().once();
  }

  private void expectUserSendsRecordToAuxiliaryTopicSucceeds(String user) {
    expect(producerPool.send(new ProducerRecord<>(AUXILIARY_TOPIC, anyObject(byte[].class)))).andAnswer(() -> {
      assertUserIs(user);
      final TopicPartition partition = new TopicPartition(AUXILIARY_TOPIC, 1);
      return completedFuture(new RecordMetadata(partition, 0, 0));
    });
  }

  private void expectUserSendsRecordToAuxiliaryTopicFails(String user) {
    expect(producerPool.send(new ProducerRecord<>(AUXILIARY_TOPIC, anyObject(byte[].class)))).andAnswer(() -> {
      assertUserIs(user);
      return supplyAsync(() -> {
        throw new RuntimeException();
      });
    });
  }

  private void expectUserPollsRecordsToAuxiliaryTopicSucceeds(String user) {
    expect(consumerPool.poll()).andAnswer(() -> {
      assertUserIs(user);
      return new ConsumerRecords<>(ImmutableMap.of(
              new TopicPartition("topic", 0),
              ImmutableList.of(new ConsumerRecord<>(
                      "topic", 0, 0,
                      "key".getBytes(), "value".getBytes()))
      ));
    });
  }

  private void expectUserPollsRecordsToAuxiliaryTopicFails(String user) {
    expect(consumerPool.poll()).andAnswer(() -> {
      assertUserIs(user);
      throw new RuntimeException();
    });
  }

  private void assertUserIs(String user) throws IOException {
    final String actualUser = UserGroupInformation.getCurrentUser().getUserName();
    final String msg = String.format("Expected user is %s while actual is %s", user, actualUser);
    Assert.assertEquals(msg, user, actualUser);
  }
}