// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: TestOptionalProto.proto
// Protobuf Java Version: 4.29.3

package io.confluent.kafka.serializers.protobuf.test;

public final class TestMessageOptionalProtos {
  private TestMessageOptionalProtos() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 29,
      /* patch= */ 3,
      /* suffix= */ "",
      TestMessageOptionalProtos.class.getName());
  }
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TestMessageOptionalOrBuilder extends
      // @@protoc_insertion_point(interface_extends:io.confluent.kafka.serializers.protobuf.test.TestMessageOptional)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string test_string = 1;</code>
     * @return The testString.
     */
    java.lang.String getTestString();
    /**
     * <code>string test_string = 1;</code>
     * @return The bytes for testString.
     */
    com.google.protobuf.ByteString
        getTestStringBytes();

    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return Whether the testOptionalString field is set.
     */
    boolean hasTestOptionalString();
    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return The testOptionalString.
     */
    java.lang.String getTestOptionalString();
    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return The bytes for testOptionalString.
     */
    com.google.protobuf.ByteString
        getTestOptionalStringBytes();
  }
  /**
   * Protobuf type {@code io.confluent.kafka.serializers.protobuf.test.TestMessageOptional}
   */
  public static final class TestMessageOptional extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:io.confluent.kafka.serializers.protobuf.test.TestMessageOptional)
      TestMessageOptionalOrBuilder {
  private static final long serialVersionUID = 0L;
    static {
      com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
        com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
        /* major= */ 4,
        /* minor= */ 29,
        /* patch= */ 3,
        /* suffix= */ "",
        TestMessageOptional.class.getName());
    }
    // Use TestMessageOptional.newBuilder() to construct.
    private TestMessageOptional(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private TestMessageOptional() {
      testString_ = "";
      testOptionalString_ = "";
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.class, io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.Builder.class);
    }

    private int bitField0_;
    public static final int TEST_STRING_FIELD_NUMBER = 1;
    @SuppressWarnings("serial")
    private volatile java.lang.Object testString_ = "";
    /**
     * <code>string test_string = 1;</code>
     * @return The testString.
     */
    @java.lang.Override
    public java.lang.String getTestString() {
      java.lang.Object ref = testString_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        testString_ = s;
        return s;
      }
    }
    /**
     * <code>string test_string = 1;</code>
     * @return The bytes for testString.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTestStringBytes() {
      java.lang.Object ref = testString_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        testString_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int TEST_OPTIONAL_STRING_FIELD_NUMBER = 2;
    @SuppressWarnings("serial")
    private volatile java.lang.Object testOptionalString_ = "";
    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return Whether the testOptionalString field is set.
     */
    @java.lang.Override
    public boolean hasTestOptionalString() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return The testOptionalString.
     */
    @java.lang.Override
    public java.lang.String getTestOptionalString() {
      java.lang.Object ref = testOptionalString_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        testOptionalString_ = s;
        return s;
      }
    }
    /**
     * <code>optional string test_optional_string = 2;</code>
     * @return The bytes for testOptionalString.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTestOptionalStringBytes() {
      java.lang.Object ref = testOptionalString_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        testOptionalString_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
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
      if (!com.google.protobuf.GeneratedMessage.isStringEmpty(testString_)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 1, testString_);
      }
      if (((bitField0_ & 0x00000001) != 0)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 2, testOptionalString_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!com.google.protobuf.GeneratedMessage.isStringEmpty(testString_)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(1, testString_);
      }
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(2, testOptionalString_);
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
      if (!(obj instanceof io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional)) {
        return super.equals(obj);
      }
      io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional other = (io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional) obj;

      if (!getTestString()
          .equals(other.getTestString())) return false;
      if (hasTestOptionalString() != other.hasTestOptionalString()) return false;
      if (hasTestOptionalString()) {
        if (!getTestOptionalString()
            .equals(other.getTestOptionalString())) return false;
      }
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
      hash = (37 * hash) + TEST_STRING_FIELD_NUMBER;
      hash = (53 * hash) + getTestString().hashCode();
      if (hasTestOptionalString()) {
        hash = (37 * hash) + TEST_OPTIONAL_STRING_FIELD_NUMBER;
        hash = (53 * hash) + getTestOptionalString().hashCode();
      }
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional parseFrom(
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
    public static Builder newBuilder(io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional prototype) {
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
     * Protobuf type {@code io.confluent.kafka.serializers.protobuf.test.TestMessageOptional}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:io.confluent.kafka.serializers.protobuf.test.TestMessageOptional)
        io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptionalOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.class, io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.Builder.class);
      }

      // Construct using io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.newBuilder()
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
        testString_ = "";
        testOptionalString_ = "";
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor;
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional getDefaultInstanceForType() {
        return io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.getDefaultInstance();
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional build() {
        io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional buildPartial() {
        io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional result = new io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.testString_ = testString_;
        }
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.testOptionalString_ = testOptionalString_;
          to_bitField0_ |= 0x00000001;
        }
        result.bitField0_ |= to_bitField0_;
      }

      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional) {
          return mergeFrom((io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional other) {
        if (other == io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional.getDefaultInstance()) return this;
        if (!other.getTestString().isEmpty()) {
          testString_ = other.testString_;
          bitField0_ |= 0x00000001;
          onChanged();
        }
        if (other.hasTestOptionalString()) {
          testOptionalString_ = other.testOptionalString_;
          bitField0_ |= 0x00000002;
          onChanged();
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
              case 10: {
                testString_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000001;
                break;
              } // case 10
              case 18: {
                testOptionalString_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000002;
                break;
              } // case 18
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

      private java.lang.Object testString_ = "";
      /**
       * <code>string test_string = 1;</code>
       * @return The testString.
       */
      public java.lang.String getTestString() {
        java.lang.Object ref = testString_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          testString_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string test_string = 1;</code>
       * @return The bytes for testString.
       */
      public com.google.protobuf.ByteString
          getTestStringBytes() {
        java.lang.Object ref = testString_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          testString_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string test_string = 1;</code>
       * @param value The testString to set.
       * @return This builder for chaining.
       */
      public Builder setTestString(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        testString_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <code>string test_string = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearTestString() {
        testString_ = getDefaultInstance().getTestString();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }
      /**
       * <code>string test_string = 1;</code>
       * @param value The bytes for testString to set.
       * @return This builder for chaining.
       */
      public Builder setTestStringBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        testString_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }

      private java.lang.Object testOptionalString_ = "";
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @return Whether the testOptionalString field is set.
       */
      public boolean hasTestOptionalString() {
        return ((bitField0_ & 0x00000002) != 0);
      }
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @return The testOptionalString.
       */
      public java.lang.String getTestOptionalString() {
        java.lang.Object ref = testOptionalString_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          testOptionalString_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @return The bytes for testOptionalString.
       */
      public com.google.protobuf.ByteString
          getTestOptionalStringBytes() {
        java.lang.Object ref = testOptionalString_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          testOptionalString_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @param value The testOptionalString to set.
       * @return This builder for chaining.
       */
      public Builder setTestOptionalString(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        testOptionalString_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearTestOptionalString() {
        testOptionalString_ = getDefaultInstance().getTestOptionalString();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>optional string test_optional_string = 2;</code>
       * @param value The bytes for testOptionalString to set.
       * @return This builder for chaining.
       */
      public Builder setTestOptionalStringBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        testOptionalString_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:io.confluent.kafka.serializers.protobuf.test.TestMessageOptional)
    }

    // @@protoc_insertion_point(class_scope:io.confluent.kafka.serializers.protobuf.test.TestMessageOptional)
    private static final io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional();
    }

    public static io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<TestMessageOptional>
        PARSER = new com.google.protobuf.AbstractParser<TestMessageOptional>() {
      @java.lang.Override
      public TestMessageOptional parsePartialFrom(
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

    public static com.google.protobuf.Parser<TestMessageOptional> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<TestMessageOptional> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public io.confluent.kafka.serializers.protobuf.test.TestMessageOptionalProtos.TestMessageOptional getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\027TestOptionalProto.proto\022,io.confluent." +
      "kafka.serializers.protobuf.test\"f\n\023TestM" +
      "essageOptional\022\023\n\013test_string\030\001 \001(\t\022!\n\024t" +
      "est_optional_string\030\002 \001(\tH\000\210\001\001B\027\n\025_test_" +
      "optional_stringBI\n,io.confluent.kafka.se" +
      "rializers.protobuf.testB\031TestMessageOpti" +
      "onalProtosb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_io_confluent_kafka_serializers_protobuf_test_TestMessageOptional_descriptor,
        new java.lang.String[] { "TestString", "TestOptionalString", });
    descriptor.resolveAllFeaturesImmutable();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
