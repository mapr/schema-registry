package io.confluent.kafka.schemaregistry.client.rest.utils;

import org.I0Itec.zkclient.ZkClient;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.confluent.kafka.schemaregistry.client.rest.utils.SchemaRegistryDiscoveryConfig.SERVICE_ID_DEFAULT;
import static io.confluent.kafka.schemaregistry.client.rest.utils.SchemaRegistryDiscoveryClient.SCHEMAREGISTRY_ZK_URLS_DIR;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SchemaRegistryDiscoveryClientTest extends EasyMockSupport {

  public static final String ZK_URL_MOCK = "zk-url";

  @Test(expected = IllegalStateException.class)
  public void whenSrUrlsDirectoryIsMissingThrowsAnException() {
    ZkClient zkClient = mock(ZkClient.class);
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(false).once();
    zkClient.close();
    expectLastCall();
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient, SERVICE_ID_DEFAULT);
    replayAll();

    try {
      extractor.discoverUrls();
    } finally {
      verifyAll();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void whenUrlsDirectoryIsEmptyThrowsAnException() {
    ZkClient zkClient = mock(ZkClient.class);
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).once();
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(Collections.emptyList());
    zkClient.close();
    expectLastCall();
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient, SERVICE_ID_DEFAULT);
    replayAll();

    try {
      extractor.discoverUrls();
    } finally {
      verifyAll();
    }
  }

  @Test
  public void whenUrlRecordIsAvailable() {
    ZkClient zkClient = mock(ZkClient.class);
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).once();
    String child = "url1";
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(Collections.singletonList(child));
    String url = "localhost";
    expect(zkClient.readData(SCHEMAREGISTRY_ZK_URLS_DIR + "/" + child)).andReturn(url);
    zkClient.close();
    expectLastCall();
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient, SERVICE_ID_DEFAULT);
    replayAll();

    List<String> result = extractor.discoverUrls();

    verifyAll();

    assertEquals(Collections.singletonList(url), result);
  }

  @Test
  public void whenFewUrlRecordsAreAvailable() {
    ZkClient zkClient = mock(ZkClient.class);
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).once();
    String child1 = "url1";
    String child2 = "url2";
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(Arrays.asList(child1, child2));
    String url1 = "localhost";
    String url2 = "localhost2";
    expect(zkClient.readData(SCHEMAREGISTRY_ZK_URLS_DIR + "/" + child1)).andReturn(url1);
    expect(zkClient.readData(SCHEMAREGISTRY_ZK_URLS_DIR + "/" + child2)).andReturn(url2);
    zkClient.close();
    expectLastCall();
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient, SERVICE_ID_DEFAULT);
    replayAll();

    List<String> result = extractor.discoverUrls();

    verifyAll();

    assertEquals(Arrays.asList(url1, url2), result);
  }

  @Test
  public void whenServiceIdIsNotDefaultBuildsCorrectUrl() {
    ZkClient zkClient = mock(ZkClient.class);
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).once();
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(Collections.singletonList("url1"));
    expect(zkClient.readData(SCHEMAREGISTRY_ZK_URLS_DIR + "/url1")).andReturn("localhost");
    zkClient.close();
    expectLastCall();
    String newServiceId = "newService";
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient, newServiceId).serviceId(newServiceId);
    replayAll();

    extractor.discoverUrls();

    verifyAll();
  }

  @Test
  public void whenRetriesFailThrowsLastException() {
    ZkClient zkClient = mock(ZkClient.class);
    int retries = 2;
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).times(retries);
    RuntimeException expectedException = new RuntimeException("expected");
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR))
            .andThrow(new RuntimeException("unexpected"))
            .andThrow(expectedException);
    zkClient.close();
    expectLastCall().times(retries);
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient,
                                                             SERVICE_ID_DEFAULT).retries(retries);
    replayAll();

    try {
      extractor.discoverUrls();
      fail("Expected exception: " + expectedException);
    } catch (Exception e) {
      assertEquals(expectedException, e);
    }

    verifyAll();
  }


  @Test
  public void stopsRetryingWhenUrlsAreAvailable() {
    ZkClient zkClient = mock(ZkClient.class);
    int expectedNumberOfRetries = 3;
    expect(zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)).andReturn(true).times(expectedNumberOfRetries);
    expect(zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR))
            .andThrow(new RuntimeException("unexpected"))
            .andReturn(Collections.emptyList())
            .andReturn(Collections.singletonList("expected"));
    String expectedUrl = "expected-url";
    expect(zkClient.readData(SCHEMAREGISTRY_ZK_URLS_DIR + "/expected")).andReturn(expectedUrl);
    zkClient.close();
    expectLastCall().times(expectedNumberOfRetries);
    SchemaRegistryDiscoveryClient extractor = spiedExtractor(zkClient,
                                                             SERVICE_ID_DEFAULT).retries(expectedNumberOfRetries + 1);
    replayAll();

    List<String> result = extractor.discoverUrls();

    verifyAll();

    assertEquals(Collections.singletonList(expectedUrl), result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deniesEmptyServiceId() {
    new SchemaRegistryDiscoveryClient().serviceId("");
  }

  private SchemaRegistryDiscoveryClient spiedExtractor(ZkClient zkClient, String serviceId) {
    SchemaRegistryDiscoveryClient extractor = partialMockBuilder(SchemaRegistryDiscoveryClient.class)
            .withConstructor()
            .addMockedMethods("getZkUrl", "createZkClient")
            .createMock();
    expect(extractor.getZkUrl()).andReturn(ZK_URL_MOCK).atLeastOnce();
    String expectedZkUrl = ZK_URL_MOCK + "/" + SchemaRegistryDiscoveryClient.SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX + serviceId;
    expect(extractor.createZkClient(expectedZkUrl)).andReturn(zkClient).atLeastOnce();
    return extractor.retries(1).retryInterval(1);
  }
}