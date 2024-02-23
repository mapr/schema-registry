package io.confluent.kafka.schemaregistry.filter;


import org.apache.commons.lang3.reflect.MethodUtils;

import javax.ws.rs.container.ResourceInfo;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class AuthorizationTestResource {
  @RequirePermission(Permission.MODIFY)
  public void modify() {
  }
  @RequirePermission(Permission.READ)
  public void read() {
  }
  @RequirePermission(Permission.NONE)
  public void none() {
  }

  public static ResourceInfo resourceInfo(Permission onMethod) {
    Method method = MethodUtils.getAccessibleMethod(AuthorizationTestResource.class, selectMethod(onMethod));
    return new ResourceInfo() {
      @Override
      public Method getResourceMethod() {
        return method;
      }

      @Override
      public Class<?> getResourceClass() {
        return AuthorizationTestResource.class;
      }
    };
  }

  private static String selectMethod(Permission onMethod) {
    switch (onMethod) {
      case READ:
        return "read";
      case MODIFY:
        return "modify";
      default:
        return "none";
    }
  }
}
