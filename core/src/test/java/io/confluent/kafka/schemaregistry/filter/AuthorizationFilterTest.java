package io.confluent.kafka.schemaregistry.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import io.confluent.kafka.schemaregistry.util.ByteConsumerPool;
import io.confluent.kafka.schemaregistry.util.ByteProducerPool;
import io.confluent.kafka.schemaregistry.util.MaprFSUtils;
import io.confluent.kafka.schemaregistry.utils.UserGroupInformationMockPolicy;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.ExecutionException;

import static io.confluent.kafka.schemaregistry.filter.AuthorizationTestResource.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@MockPolicy(UserGroupInformationMockPolicy.class)
@PrepareOnlyThisForTest(MaprFSUtils.class)
public class AuthorizationFilterTest extends EasyMockSupport {

  private static final String AUXILIARY_TOPIC = "/any-internal-topic:aux-topic";
  private static final String IMPERSONATED_USER = "user";
  private static final String ADMIN_USER = System.getProperty("user.name");

  private ByteConsumerPool byteConsumerPool;
  private ByteProducerPool byteProducerPool;
  private AuthorizationFilter authorizationFilter;

  @Before
  public void setUp() {
    byteConsumerPool = mock(ByteConsumerPool.class);
    byteProducerPool = mock(ByteProducerPool.class);
    authorizationFilter = new AuthorizationFilter(byteConsumerPool, byteProducerPool, AUXILIARY_TOPIC);
  }

  @Test
  public void dummyRecordIsSent() {
    expectUserSendsRecordToAuxiliaryTopicSucceeds(ADMIN_USER);
    replayAll();

    authorizationFilter.initialize();

    verifyAll();
  }

  @Test
  public void handlesFutureExecutionExceptionOnInitialization() {
    RuntimeException exception = new RuntimeException();
    expect(byteProducerPool.send(new ProducerRecord<>(AUXILIARY_TOPIC, anyObject(byte[].class))))
        .andReturn(Futures.immediateFailedFuture(exception));
    replayAll();

    try {
      authorizationFilter.initialize();
      fail("No exception was thrown");
    } catch (KafkaException e) {
      assertThat(e.getCause(), instanceOf(ExecutionException.class));
      assertThat(e.getCause().getCause(), is(exception));
    }

    verifyAll();
  }

  @Test
  public void unauthenticatedRequestIsForbidden() {
    final ContainerRequest request = mock(ContainerRequest.class);
    expect(request.getSecurityContext()).andStubReturn(null);
    final Capture<Response> capturedResponse = Capture.newInstance();
    expectAbortWith(request, capture(capturedResponse));
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
    assertThat(capturedResponse.getValue().getStatusInfo().toEnum(), is(Response.Status.FORBIDDEN));
  }

  @Test
  public void authenticatedRequestSucceeds() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.READ);
    expectUserPollsRecordsToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  public void writeCommandIsForbiddenWithoutWritePermission() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.MODIFY);
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
  public void writeCommandSucceedsWithWritePermission() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.MODIFY);
    expectUserSendsRecordToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  public void readCommandIsForbiddenWithoutReadPermission() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.READ);
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
  public void readCommandSucceedsWithReadPermission() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.READ);
    expectUserPollsRecordsToAuxiliaryTopicSucceeds(IMPERSONATED_USER);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  @Test
  public void readCommandIsForbiddenOnEmptyRecordsWithReadPermission() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.READ);
    final Capture<Response> captured = Capture.newInstance();
    expectAbortWith(request, capture(captured));
    expect(byteConsumerPool.poll(AUXILIARY_TOPIC)).andAnswer(() -> {
      assertUserIs(IMPERSONATED_USER);
      return new ConsumerRecords<>(ImmutableMap.of());
    });
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
    assertThat(captured.getValue().getStatusInfo().toEnum(), is(Response.Status.FORBIDDEN));
    assertThat(captured.getValue().getEntity().toString(), containsString("denied"));
  }

  @Test
  public void otherCommandsSucceedWithoutAnyPermissions() {
    final ContainerRequest request = createAuthenticatedRequest();
    authorizationFilter.resourceInfo = resourceInfo(Permission.NONE);
    replayAll();

    authorizationFilter.filter(request);

    verifyAll();
  }

  private ContainerRequest createAuthenticatedRequest() {
    Principal principal = mock(Principal.class);
    expect(principal.getName()).andReturn("user");
    SecurityContext securityContext = mock(SecurityContext.class);
    expect(securityContext.getUserPrincipal()).andReturn(principal);
    final ContainerRequest request = mock(ContainerRequest.class);
    expect(request.getSecurityContext()).andReturn(securityContext);
    return request;
  }

  private void expectAbortWith(ContainerRequest request, Response response) {
    request.abortWith(response);
    expectLastCall().once();
  }

  private void expectUserSendsRecordToAuxiliaryTopicSucceeds(String user) {
    expect(byteProducerPool.send(new ProducerRecord<>(AUXILIARY_TOPIC, anyObject(byte[].class)))).andAnswer(() -> {
      assertUserIs(user);
      final TopicPartition partition = new TopicPartition(AUXILIARY_TOPIC, 1);
      return completedFuture(new RecordMetadata(partition, 0, 0, -1, -1, -1));
    });
  }

  private void expectUserSendsRecordToAuxiliaryTopicFails(String user) {
    expect(byteProducerPool.send(new ProducerRecord<>(AUXILIARY_TOPIC, anyObject(byte[].class)))).andAnswer(() -> {
      assertUserIs(user);
      return supplyAsync(() -> {
        throw new RuntimeException();
      });
    });
  }

  private void expectUserPollsRecordsToAuxiliaryTopicSucceeds(String user) {
    expect(byteConsumerPool.poll(AUXILIARY_TOPIC)).andAnswer(() -> {
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
    expect(byteConsumerPool.poll(AUXILIARY_TOPIC)).andAnswer(() -> {
      assertUserIs(user);
      throw new ExecutionException(new RuntimeException());
    });
  }

  private void assertUserIs(String user) throws IOException {
    final String actualUser = UserGroupInformation.getCurrentUser().getUserName();
    final String msg = String.format("Expected user is %s while actual is %s", user, actualUser);
    Assert.assertEquals(msg, user, actualUser);
  }
}