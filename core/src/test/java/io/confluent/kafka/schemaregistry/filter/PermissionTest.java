package io.confluent.kafka.schemaregistry.filter;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
public class PermissionTest {
  @Test
  public void takesPermissionFromMethodWhenBothClassAndMethodAreAnnotated() {
    Method method = MethodUtils.getAccessibleMethod(ModifyOperations.class, "read");
    Permission actual = Permission.at(ModifyOperations.class, method);
    assertEquals(Permission.READ, actual);
  }

  @Test
  public void takesPermissionFromClassWhenOnlyClassIsAnnotated() {
    Method method = MethodUtils.getAccessibleMethod(ModifyOperations.class, "unannotated");
    Permission actual = Permission.at(ModifyOperations.class, method);
    assertEquals(Permission.MODIFY, actual);
  }

  @Test
  public void takesNonePermissionWhenNeitherClassNorMethodAreAnnotated() {
    Method method = MethodUtils.getAccessibleMethod(Unannotated.class, "unannotated");
    Permission actual = Permission.at(Unannotated.class, method);
    assertEquals(Permission.NONE, actual);
  }

  @RequirePermission(Permission.MODIFY)
  public static class ModifyOperations {
    @RequirePermission(Permission.READ)
    public void read() {
    }
    public void unannotated() {
    }
  }

  public static class Unannotated {
    public void unannotated() {
    }
  }
}