# protoj
A lightweight, Java-based [Protocol Buffer](https://developers.google.com/protocol-buffers/docs/proto3) serialization implementation (proto3) using reflection to process annotation-based Protobuf schemas and values at runtime.

Implements support for all scalar types, nested messages, and repeated types.  Enums are not currently implemented.

## Usage
Define a class with Protobuf fields marked using the supplied annotations.
```java
class TestMessage {
    @ProtobufField(fieldNumber = 3, protobufType = ProtobufType.INT32)
    public int foo = 0;
    @ProtobufField(fieldNumber = 3, protobufType = ProtobufType.INT64)
    public Long bar = 10L;
}
```
Then run the following generate the serialized Protobuf bytes.
```java
ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
```

### Custom Getters
```java
class TestMessage {
    @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getFoo")
    private int foo = 2;

    public int getFoo() {
        return foo;
    }
}
```
