// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: Int8Value.proto
// Protobuf Java Version: 4.29.3

package io.confluent.kafka.serializers.protobuf.test;

public final class Int8ValueOuterClass {
  private Int8ValueOuterClass() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      Int8ValueOuterClass.class.getName());
  }
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface Int8ValueOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Int8Value)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 value = 1 [(.confluent.field_meta) = { ... }</code>
     * @return The value.
     */
    int getValue();
  }
  /**
   * <pre>
   * Wrapper message for `Int8`.
   * </pre>
   *
   * Protobuf type {@code Int8Value}
   */
  public static final class Int8Value extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:Int8Value)
      Int8ValueOrBuilder {
  private static final long serialVersionUID = 0L;
    static {
      com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
        com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
        /* major= */ 4,
        /* minor= */ 29,
        /* patch= */ 3,
        /* suffix= */ "",
        Int8Value.class.getName());
    }
    // Use Int8Value.newBuilder() to construct.
    private Int8Value(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private Int8Value() {
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.internal_static_Int8Value_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.internal_static_Int8Value_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.class, io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.Builder.class);
    }

    public static final int VALUE_FIELD_NUMBER = 1;
    private int value_ = 0;
    /**
     * <code>int32 value = 1 [(.confluent.field_meta) = { ... }</code>
     * @return The value.
     */
    @java.lang.Override
    public int getValue() {
      return value_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (value_ != 0) {
        output.writeInt32(1, value_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (value_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value)) {
        return super.equals(obj);
      }
      io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value other = (io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value) obj;

      if (getValue()
          != other.getValue()) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getValue();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * Wrapper message for `Int8`.
     * </pre>
     *
     * Protobuf type {@code Int8Value}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Int8Value)
        io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8ValueOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.internal_static_Int8Value_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.internal_static_Int8Value_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.class, io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.Builder.class);
      }

      // Construct using io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        value_ = 0;
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.internal_static_Int8Value_descriptor;
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value getDefaultInstanceForType() {
        return io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.getDefaultInstance();
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value build() {
        io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value buildPartial() {
        io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value result = new io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.value_ = value_;
        }
      }

      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value) {
          return mergeFrom((io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value other) {
        if (other == io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value.getDefaultInstance()) return this;
        if (other.getValue() != 0) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 8: {
                value_ = input.readInt32();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private int value_ ;
      /**
       * <code>int32 value = 1 [(.confluent.field_meta) = { ... }</code>
       * @return The value.
       */
      @java.lang.Override
      public int getValue() {
        return value_;
      }
      /**
       * <code>int32 value = 1 [(.confluent.field_meta) = { ... }</code>
       * @param value The value to set.
       * @return This builder for chaining.
       */
      public Builder setValue(int value) {

        value_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>int32 value = 1 [(.confluent.field_meta) = { ... }</code>
       * @return This builder for chaining.
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = 0;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:Int8Value)
    }

    // @@protoc_insertion_point(class_scope:Int8Value)
    private static final io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value();
    }

    public static io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Int8Value>
        PARSER = new com.google.protobuf.AbstractParser<Int8Value>() {
      @java.lang.Override
      public Int8Value parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<Int8Value> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Int8Value> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public io.confluent.kafka.serializers.protobuf.test.Int8ValueOuterClass.Int8Value getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Int8Value_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_Int8Value_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017Int8Value.proto\032\024confluent/meta.proto\"" +
      "5\n\tInt8Value\022(\n\005value\030\001 \001(\005B\031\202D\026\022\024\n\014conn" +
      "ect.type\022\004int8B.\n,io.confluent.kafka.ser" +
      "ializers.protobuf.testb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          io.confluent.protobuf.MetaProto.getDescriptor(),
        });
    internal_static_Int8Value_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Int8Value_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_Int8Value_descriptor,
        new java.lang.String[] { "Value", });
    descriptor.resolveAllFeaturesImmutable();
    io.confluent.protobuf.MetaProto.getDescriptor();
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(io.confluent.protobuf.MetaProto.fieldMeta);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
