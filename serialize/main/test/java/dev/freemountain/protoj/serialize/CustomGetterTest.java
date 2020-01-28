package dev.freemountain.protoj.serialize;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static dev.freemountain.protoj.util.TestUtil.printBits;
import static junit.framework.TestCase.assertTrue;

public class CustomGetterTest {
    private final static Logger logger = LoggerFactory.getLogger(CustomGetterTest.class);

    @Test(expected = IllegalAccessException.class)
    public void accessingPrivateThrows() throws Exception {
        class TestMessage {
            @ProtobufField(fieldNumber = 9, protobufType = ProtobufType.INT32)
            private int foo = 0;
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = NoSuchMethodException.class)
    public void wrongGetter() throws Exception {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getBar")
            private int foo = 2;

            public int getFoo() {
                return foo;
            }
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ClassCastException.class)
    public void wrongGetterReturnType() throws Exception {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getFoo")
            private int foo = 2;

            public String getFoo() {
                return "foo";
            }
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test
    public void accessingUsingCustomGetter() throws Exception {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getFoo")
            private int foo = 2;

            public int getFoo() {
                return foo;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{0x08, 0x02}));
    }

    @Test
    public void preferCustomGetter() throws Exception {
        class TestMessage {
            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getFoo")
            public int foo = 0;

            public int getFoo() {
                return 2;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(Arrays.equals(out.array(), new byte[]{0x08, 0x02}));
    }

}
