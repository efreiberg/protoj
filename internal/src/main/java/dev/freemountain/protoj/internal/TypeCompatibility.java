package dev.freemountain.protoj.internal;

import dev.freemountain.protoj.api.ProtobufType;

public class TypeCompatibility {

    public static boolean check(ProtobufType protobufType, Class<?> fieldType) {
        switch (protobufType) {
            case BOOL:
                return (fieldType == Boolean.class || fieldType.getName() == "boolean");
            case BYTES:
                break;
            case DOUBLE:
                return (fieldType == Double.class || fieldType.getName() == "double");
            case FLOAT:
                return (fieldType == Float.class || fieldType.getName() == "float");
            case INT32:
            case UINT32:
            case SINT32:
            case SFIXED32:
            case FIXED32:
                return (fieldType == Integer.class || fieldType.getName() == "int");
            case INT64:
            case UINT64:
            case SINT64:
            case SFIXED64:
            case FIXED64:
                return (fieldType == Long.class || fieldType.getName() == "long");
            case STRING:
                return (fieldType == String.class);
            case MESSAGE:
                return (fieldType instanceof Object);
        }
        return false;
    }
}
