package dev.freemountain.protoj.serialize.integration;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufType;
import java.util.ArrayList;
import java.util.List;

public class IntegrationTestProto {

    /**
     * Schema matches what is set in protoc-it.proto. Values match what is set in protoc-it-values.txt
     *
     * Keep the field numbers in order, protoc tends to process them smallest->largest
     */
    public IntegrationTestProto() {
        list.add(-1);
        list.add(4);
        list.add(171487);
        list.add(-149);
    }

    @ProtobufField(fieldNumber = 1, protobufType = ProtobufType.INT32)
    public int foo1 = 19371283;
    @ProtobufField(fieldNumber = 2, protobufType = ProtobufType.MESSAGE, getterMethod = "getNestedInstance")
    private NestedClass nested1;
    @ProtobufField(fieldNumber = 17, protobufType = ProtobufType.INT32)
    public List<Integer> list = new ArrayList<>();
    @ProtobufField(fieldNumber = 11237, protobufType = ProtobufType.INT64)
    public Long bar1 = 9174917261277L;
    @ProtobufField(fieldNumber = 985691, protobufType = ProtobufType.DOUBLE)
    public double baz1 = 659819.1246;

    public NestedClass getNestedInstance() {
        return new NestedClass();
    }

    public static class NestedClass {

        public int skipMe = -1;
        @ProtobufField(fieldNumber = 2, protobufType = ProtobufType.BOOL)
        public boolean bar2 = true;
        @ProtobufField(fieldNumber = 7, protobufType = ProtobufType.STRING)
        public String baz2 = "H3LlO W0RLd";
        @ProtobufField(fieldNumber = 9, protobufType = ProtobufType.FLOAT)
        public float foo2 = 7.80f;
    }
}
