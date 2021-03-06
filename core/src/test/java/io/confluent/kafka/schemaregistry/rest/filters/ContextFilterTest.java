/*
 * Copyright 2021 Confluent Inc.
 */

package io.confluent.kafka.schemaregistry.rest.filters;

import java.net.URI;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriBuilder;
import org.junit.Assert;
import org.junit.Test;

public class ContextFilterTest {

  ContextFilter contextFilter = new ContextFilter();

  @Test
  public void testContextsRoot() {
    String path = "/contexts/";
    Assert.assertEquals(
        "URI most not change",
        "/contexts/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testSubjectPartOfUri() {
    String path = "/contexts/.test-ctx/subjects/test-subject/versions";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/subjects/:.test-ctx:test-subject/versions/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testMissingLeadingDotInContext() {
    String path = "/contexts/test-ctx/subjects/test-subject/versions";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/subjects/:.test-ctx:test-subject/versions/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testUriEndsWithSubject() {
    String path = "/contexts/.test-ctx/subjects/test-subject/";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/subjects/:.test-ctx:test-subject/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testUriWithIds() {
    String path = "/contexts/.test-ctx/schemas/ids/1/";
    URI uri = contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>());
    Assert.assertEquals(
        "URI must not change",
        "/schemas/ids/1/",
        uri.getPath()
    );
    Assert.assertEquals(
        "Query param must change",
        "subject=:.test-ctx:",
        uri.getQuery()
    );
  }

  @Test
  public void testUriWithEncodedSlash() {
    String path = "/contexts/.test-ctx/subjects/slash%2Fin%2Fmiddle/";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/subjects/:.test-ctx:slash%2Fin%2Fmiddle/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getRawPath()
    );
  }

  @Test
  public void testConfigUriWithSubject() {
    String path = "/contexts/.test-ctx/config/test-subject";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/config/:.test-ctx:test-subject/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testConfigUriWithoutSubject() {
    String path = "/contexts/.test-ctx/config";
    Assert.assertEquals(
        "Wildcard must be prefixed",
        "/config/:.test-ctx:/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testUriWithoutModification() {
    String path = "/chc/live";
    Assert.assertEquals(
        "URI must not change",
        "/chc/live/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testModeUriWithSubject() {
    String path = "/contexts/.test-ctx/mode/test-subject";
    Assert.assertEquals(
        "Subject must be prefixed",
        "/mode/:.test-ctx:test-subject/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testModeUriWithoutSubject() {
    String path = "/contexts/.test-ctx/mode";
    Assert.assertEquals(
        "Wildcard must be prefixed",
        "/mode/:.test-ctx:/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

  @Test
  public void testConfigNotRoot() {
    String path = "/other/config/test";
    Assert.assertEquals(
        "Non-root config must be unmodified",
        "/other/config/test/",
        contextFilter.modifyUri(UriBuilder.fromPath(path), path, new MultivaluedHashMap<>()).getPath()
    );
  }

}
