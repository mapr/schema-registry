package io.confluent.kafka.schemaregistry.util;

import io.confluent.kafka.schemaregistry.utils.UserGroupInformationMockPolicy;
import io.confluent.rest.exceptions.RestServerErrorException;
import io.confluent.rest.impersonation.Errors;
import org.apache.hadoop.security.IdMappingServiceProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.easymock.EasyMockSupport;
import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@MockPolicy(UserGroupInformationMockPolicy.class)
public class ByteConsumerPoolTest extends EasyMockSupport {
  private static final String ANY_TOPIC = "/some-stream:topic";
  private static final String USER_NAME = System.getProperty("user.name");
  private static final int USER_ID = 1000;

  private Map<String, Object> consumerConfig = new HashMap<>();
  private KafkaClientSupplier clientSupplier;
  private ByteConsumerPool consumerPool;
  private IdMappingServiceProvider idMapper;

  @Before
  public void setUp() {
    consumerConfig.put("any.config", "has-to-be-copied");
    clientSupplier = mock(KafkaClientSupplier.class);
    idMapper = mock(IdMappingServiceProvider.class);
    consumerPool = new ByteConsumerPool(consumerConfig, clientSupplier, idMapper);
  }

  @Test
  public void consumesRecordsFromCreatedConsumer() throws IOException {
    expect(idMapper.getUid(USER_NAME)).andReturn(USER_ID);
    KafkaConsumer<byte[], byte[]> consumer = mockConsumerForCurrentThreadAndUser(USER_ID);
    expectConsumerAssignTopic(consumer, ANY_TOPIC);

    ConsumerRecords<byte[], byte[]> expectedRecords = new ConsumerRecords<>(Collections.emptyMap());
    consumer.seekToBeginning(anyObject());
    expectLastCall();
    expect(consumer.poll(anyObject(Duration.class))).andReturn(expectedRecords);
    replayAll();

    ByteConsumerPool consumerPool = this.consumerPool;

    assertThat(consumerPool.poll(ANY_TOPIC), sameInstance(expectedRecords));
    verifyAll();
  }

  @Test
  public void cachesConsumerByUser() throws IOException {
    ConsumerRecords<byte[], byte[]> expectedU1C1 = new ConsumerRecords<>(Collections.emptyMap());
    ConsumerRecords<byte[], byte[]> expectedU1C2 = new ConsumerRecords<>(Collections.emptyMap());
    ConsumerRecords<byte[], byte[]> expectedU2C1 = new ConsumerRecords<>(Collections.emptyMap());

    UserGroupInformation u1 = UserGroupInformation.createRemoteUser("U1");
    expect(idMapper.getUid(u1.getUserName())).andReturn(1001).times(2);
    KafkaConsumer<byte[], byte[]> consumerU1 = mockConsumerForCurrentThreadAndUser(1001);
    consumerU1.seekToBeginning(anyObject());
    expectLastCall().times(2);
    expect(consumerU1.poll(anyObject(Duration.class))).andReturn(expectedU1C1).andReturn(expectedU1C2);
    expectConsumerAssignTopic(consumerU1, ANY_TOPIC).times(2);

    UserGroupInformation u2 = UserGroupInformation.createRemoteUser("U2");
    expect(idMapper.getUid(u2.getUserName())).andReturn(1002);
    KafkaConsumer<byte[], byte[]> consumerU2 = mockConsumerForCurrentThreadAndUser(1002);
    consumerU2.seekToBeginning(anyObject());
    expectLastCall();
    expect(consumerU2.poll(anyObject(Duration.class))).andReturn(expectedU2C1);
    expectConsumerAssignTopic(consumerU2, ANY_TOPIC);

    replayAll();

    ByteConsumerPool consumerPool = this.consumerPool;

    final PrivilegedAction<ConsumerRecords<byte[], byte[]>> poll = () -> consumerPool.poll(ANY_TOPIC);
    assertThat(u1.doAs(poll), sameInstance(expectedU1C1));
    assertThat(u2.doAs(poll), sameInstance(expectedU2C1));
    assertThat(u1.doAs(poll), sameInstance(expectedU1C2));

    verifyAll();
  }

  @Test
  public void cachesConsumerByThread() throws Exception {
    expect(idMapper.getUid(USER_NAME)).andReturn(USER_ID).times(3);
    ConsumerRecords<byte[], byte[]> expectedT1C1 = new ConsumerRecords<>(Collections.emptyMap());
    ConsumerRecords<byte[], byte[]> expectedT1C2 = new ConsumerRecords<>(Collections.emptyMap());
    ConsumerRecords<byte[], byte[]> expectedT2C1 = new ConsumerRecords<>(Collections.emptyMap());

    ExecutorService t1 = Executors.newSingleThreadExecutor();
    Long idT1 = t1.submit(() -> Thread.currentThread().getId()).get();
    KafkaConsumer<byte[], byte[]> consumerT1 = mockConsumerForUserAndThread(USER_ID, idT1);
    consumerT1.seekToBeginning(anyObject());
    expectLastCall().times(2);
    expect(consumerT1.poll(anyObject(Duration.class))).andReturn(expectedT1C1).andReturn(expectedT1C2);
    expectConsumerAssignTopic(consumerT1, ANY_TOPIC).times(2);

    ExecutorService t2 = Executors.newSingleThreadExecutor();
    Long idT2 = t2.submit(() -> Thread.currentThread().getId()).get();
    KafkaConsumer<byte[], byte[]> consumerT2 = mockConsumerForUserAndThread(USER_ID, idT2);
    consumerT2.seekToBeginning(anyObject());
    expectLastCall();
    expect(consumerT2.poll(anyObject(Duration.class))).andReturn(expectedT2C1);
    expectConsumerAssignTopic(consumerT2, ANY_TOPIC);

    replayAll();

    Callable<ConsumerRecords<byte[], byte[]>> poll = () -> consumerPool.poll(ANY_TOPIC);
    assertThat(t1.submit(poll).get(), sameInstance(expectedT1C1));
    assertThat(t2.submit(poll).get(), sameInstance(expectedT2C1));
    assertThat(t1.submit(poll).get(), sameInstance(expectedT1C2));

    verifyAll();
  }

  @Test
  public void throwsServerLoginExceptionOnConsumerFailure() throws IOException {
    expect(idMapper.getUid(USER_NAME)).andReturn(USER_ID);
    final Exception cause = new RuntimeException("Why not");
    final KafkaConsumer<byte[], byte[]> consumer = mockConsumerForCurrentThreadAndUser(USER_ID);
    consumer.seekToBeginning(anyObject());
    expectLastCall();
    expect(consumer.poll(anyObject())).andThrow(cause);
    expectConsumerAssignTopic(consumer, ANY_TOPIC);
    replayAll();

    try {
      consumerPool.poll(ANY_TOPIC);
      fail();
    } catch (RestServerErrorException e) {
      RestServerErrorException expected = Errors.serverLoginException(cause);
      assertThat(e.getCause(), is(expected.getCause()));
      assertThat(e.getErrorCode(), is(expected.getErrorCode()));
      assertThat(e.getMessage(), is(expected.getMessage()));
    }

    verifyAll();
  }

  private KafkaConsumer<byte[], byte[]> mockConsumerForCurrentThreadAndUser(int userId) {
    return mockConsumerForUserAndThread(userId, Thread.currentThread().getId());
  }

  @SuppressWarnings("unchecked")
  private KafkaConsumer<byte[], byte[]> mockConsumerForUserAndThread(long userId, long threadId) {
    KafkaConsumer<byte[], byte[]> consumer = mock(KafkaConsumer.class);
    Map<String, Object> expectedProps = new HashMap<>(consumerConfig);
    expectedProps.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("t_%d_u%d", threadId, userId));
    expect(clientSupplier.getConsumer(expectedProps)).andReturn(consumer).once();
    return consumer;
  }

  private IExpectationSetters<Object> expectConsumerAssignTopic(KafkaConsumer<byte[], byte[]> consumer, String topic) {
    consumer.assign(Collections.singletonList(new TopicPartition(topic, 0)));
    return expectLastCall();
  }
}