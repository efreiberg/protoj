package dev.freemountain.protoj.serialize;

import static org.junit.Assert.fail;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CircularReferenceTest {

    class TestMessage1 {

        @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
        public TestMessage2 foo = new TestMessage2();
    }

    class TestMessage1WithList {

        public TestMessage1WithList() {
            foo.add(new TestMessage2());
        }

        @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
        public List<TestMessage2> foo = new ArrayList<>();
    }

    class TestMessage2 {

        @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
        public TestMessage3 foo = new TestMessage3();
    }

    class TestMessage3 {

        @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE, getterMethod = "getFoo")
        private TestMessage1 foo;

        public TestMessage1 getFoo() {
            return new TestMessage1();
        }
    }

    @Test(expected = ProtobufSerializationException.class)
    public void circularMessageReferenceThrows() throws Exception {
        ProtobufSerializer.serialize(new TestMessage1());
        fail();
    }

    @Test(expected = ProtobufSerializationException.class)
    public void circularMessageReferenceInListThrows() throws Exception {
        ProtobufSerializer.serialize(new TestMessage1WithList());
        fail();
    }
}
