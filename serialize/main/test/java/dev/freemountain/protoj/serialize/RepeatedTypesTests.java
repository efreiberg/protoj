package dev.freemountain.protoj.serialize;

import static dev.freemountain.protoj.util.TestUtil.printBits;
import static junit.framework.TestCase.assertTrue;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepeatedTypesTests {

    private final static Logger logger = LoggerFactory.getLogger(RepeatedTypesTests.class);

    @Test(expected = ClassCastException.class)
    public void wrongGenericScalarTypeInList() throws Exception {
        class TestMessage {

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getList")
            private List<Integer> foo;

            public List<Float> getList() {
                List<Float> list = new ArrayList<>();
                list.add(1.2f);
                return list;
            }
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test(expected = ClassCastException.class)
    public void notIterableType() throws Exception {
        class TestMessage {

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
            public Map<Integer, Integer> foo = new HashMap<>();
        }
        ProtobufSerializer.serialize(new TestMessage());
    }

    @Test
    public void emptyScalarListIsSkipped() throws Exception {
        class TestMessage {

            @ProtobufField(fieldNumber = 10, protobufType = ProtobufType.INT32, getterMethod = "getList")
            private List<Integer> foo;

            public List<Integer> getList() {
                return new ArrayList<>();
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(out.array().length == 0);
    }

    @Test
    public void scalarRepeatedTypeIsPacked() throws Exception {
        class TestMessage {

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32, getterMethod = "getList")
            private LinkedHashSet<Integer> foo;

            public LinkedHashSet<Integer> getList() {
                LinkedHashSet<Integer> list = new LinkedHashSet<>();
                list.add(1);
                list.add(2);
                list.add(4);
                return list;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        // Wire type 2, 3 bytes long
        assertTrue(Arrays.equals(out.array(), new byte[]{(byte) 0x0A, 0x03, 0x01, 0x02, 0x04}));
    }

    @Test
    // Empty list
    public void repeatedMessageEmpty1() throws Exception {
        class TestMessage {

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
            public List<NestedTestMessage> foo = new ArrayList<>();

            class NestedTestMessage {

                @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
                public int foo = 2;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        // Wire type 2, 3 bytes long
        assertTrue(out.array().length == 0);
    }

    @Test
    // List has message objects with no proto fields
    public void repeatedMessageEmpty2() throws Exception {
        class TestMessage {

            TestMessage() {
                foo.add(new NestedTestMessage());
                foo.add(new NestedTestMessage());
                foo.add(new NestedTestMessage());
            }

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
            public List<NestedTestMessage> foo = new ArrayList<>();


            class NestedTestMessage {

                public int foo = 2;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        assertTrue(out.array().length == 0);
    }

    @Test
    public void repeatedMessageInEncounteredOrder() throws Exception {
        class TestMessage {

            TestMessage() {
                foo.add(new NestedTestMessage(1));
                foo.add(new NestedTestMessage(2));
                foo.add(new NestedTestMessage(3));
            }

            @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
            public List<NestedTestMessage> foo = new ArrayList<>();

            class NestedTestMessage {

                NestedTestMessage(int val) {
                    this.foo = val;
                }

                @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
                public int foo = 2;
            }
        }
        ByteBuffer out = ProtobufSerializer.serialize(new TestMessage());
        assertTrue(out.hasArray());
        logger.debug("result={}", printBits(out.array()));
        // (ID1+WireType2 NumBytes2 ID1+WireType0 Val)x3
        assertTrue(Arrays
            .equals(out.array(), new byte[]{0x0A, 0x02, 0x08, 0x01, 0x0A, 0x02, 0x08, 0x02, 0x0A, 0x02, 0x08, 0x03}));
    }
}
