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
                return (fieldType == Integer.class || fieldType.getName() == "int");
            case INT64:
                return (fieldType == Long.class || fieldType.getName() == "long");
            case UINT32:
                break;
            case UINT64:
                break;
            case SINT32:
                break;
            case SINT64:
                break;
            case SFIXED32:
                break;
            case SFIXED64:
                break;
            case FIXED32:
                break;
            case FIXED64:
                break;
            case STRING:
                return (fieldType == String.class);
        }
        return false;
    }
}
