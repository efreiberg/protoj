package dev.freemountain.protoj.serialize;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufMessage;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProtobufSerializerTest {

    @Test(expected = ProtobufSerializationException.class)
    public void serializeNoAMessageAnnotation() {
        class TestMessage {
            @ProtobufField(fieldNumber = 3, protobufType = ProtobufType.INT32)
            public int foo = 0;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializeInvalidFieldNumber() {
        @ProtobufMessage
        class TestMessage {
            @ProtobufField(fieldNumber = 0, protobufType = ProtobufType.INT32)
            public int foo = 0;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializeFieldTypeMismatch() {
        @ProtobufMessage
        class TestMessage {
            @ProtobufField(fieldNumber = 0, protobufType = ProtobufType.INT32)
            public Boolean foo = true;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }
}