package dev.freemountain.protoj.internal;

import java.util.HashMap;
import java.util.Map;

import dev.freemountain.protoj.api.ProtobufType;

public class TypeMapper {

    private static Map<ProtobufType, WireType> protobufToWireTypeMap = new HashMap<>();

    static {
        protobufToWireTypeMap.put(ProtobufType.BOOL, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.BYTES, WireType.LENGTH_DELIMITED);
        protobufToWireTypeMap.put(ProtobufType.DOUBLE, WireType.FIXED_64);
        protobufToWireTypeMap.put(ProtobufType.FIXED32, WireType.FIXED_32);
        protobufToWireTypeMap.put(ProtobufType.FIXED64, WireType.FIXED_64);
        protobufToWireTypeMap.put(ProtobufType.FLOAT, WireType.FIXED_32);
        protobufToWireTypeMap.put(ProtobufType.INT32, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.INT64, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.SINT32, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.SINT64, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.SFIXED32, WireType.FIXED_32);
        protobufToWireTypeMap.put(ProtobufType.SFIXED64, WireType.FIXED_64);
        protobufToWireTypeMap.put(ProtobufType.STRING, WireType.LENGTH_DELIMITED);
        protobufToWireTypeMap.put(ProtobufType.UINT32, WireType.VARINT);
        protobufToWireTypeMap.put(ProtobufType.UINT64, WireType.VARINT);
    }

    public static WireType getWireType(ProtobufType protobufType) {
        return protobufToWireTypeMap.get(protobufType);
    }
}
