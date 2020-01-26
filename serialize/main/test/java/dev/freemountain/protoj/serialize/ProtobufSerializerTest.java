package dev.freemountain.protoj.serialize;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufMessage;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import static dev.freemountain.protoj.util.TestUtil.printBits;
import static junit.framework.TestCase.assertTrue;


public class ProtobufSerializerTest {
    private final static Logger logger = LoggerFactory.getLogger(ProtobufSerializer.class);

    @Test(expected = ProtobufSerializationException.class)
    public void serializDuplicateFieldNumbers1() {
        class TestMessage {
            @ProtobufField(fieldNumber = 3, protobufType = ProtobufType.INT32)
            public int foo = 0;
            @ProtobufField(fieldNumber = 3, protobufType = ProtobufType.INT64)
            public Long bar = 0L;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializDuplicateFieldNumbers2() {
        class TestMessage {
            @ProtobufField(fieldNumber = 10, protobufType = ProtobufType.INT32)
            public int foo = 10;
            @ProtobufMessage(fieldNumber = 10)
            public NestedTestMessage bar;

            class NestedTestMessage {
                @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
                public int foo = 10;
            }
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializeInvalidFieldNumber() {
        class TestMessage {
            @ProtobufField(fieldNumber = 0, protobufType = ProtobufType.INT32)
            public int foo = 10;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializeFieldTypeMismatch() {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public Boolean foo = true;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ProtobufSerializationException.class)
    public void serializeInvalidFieldType() {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public UUID foo = UUID.randomUUID();
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test
    public void serializeSimple() {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public int foo = 2;
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{0x08, 0x02}));
    }

    @Test
    public void serializeSimpleNested() {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public int foo = 2;
            @ProtobufMessage(fieldNumber = 2)
            public NestedTestMessage bar = new NestedTestMessage();

            class NestedTestMessage {
                @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
                public int foo = 2;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{0x08, 0x02, 0x12, 0x02, 0x08, 0x02}));
    }

    @Test
    public void serializeSkipsNullValues1() {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public Integer foo = null;
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{}));
    }

    @Test
    public void serializeSkipsNullValues2() {
        class TestMessage {
            @ProtobufMessage(fieldNumber = 9)
            public NestedTestMessage bar = null;

            class NestedTestMessage {
                @ProtobufField(fieldNumber = 2, protobufType = ProtobufType.INT32)
                public int foo = 10;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{}));
    }
}