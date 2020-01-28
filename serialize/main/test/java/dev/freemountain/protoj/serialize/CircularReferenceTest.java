package dev.freemountain.protoj.serialize;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import org.junit.Test;


import static org.junit.Assert.fail;

public class CircularReferenceTest {

    class TestMessage1 {
        @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.MESSAGE)
        public TestMessage2 foo = new TestMessage2();
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
}
