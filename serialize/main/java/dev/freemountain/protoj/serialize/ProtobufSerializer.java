package dev.freemountain.protoj.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.internal.TypeCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.freemountain.protoj.api.ProtobufMessage;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import dev.freemountain.protoj.internal.TypeMapper;
import dev.freemountain.protoj.internal.WireType;

public class ProtobufSerializer {
    private final static Logger logger = LoggerFactory.getLogger(ProtobufSerializer.class);
    private static int MIN_FIELD_NUMBER = 1;
    private static int MAX_FIELD_NUMBER = (2 ^ 29) - 1;

    public static <T> ByteBuffer serialize(T message) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        serialize(byteStream, message);
        return serialize(byteStream, message);
    }

    static <T> ByteBuffer serialize(ByteArrayOutputStream byteStream, T message) {
        if (!isMessageClass(message.getClass())) {
            throw new ProtobufSerializationException("Attempting to serialize non-message class " + message.getClass().getName());
        }
        // Get serializable fields
        for (Field field : message.getClass().getDeclaredFields()) {
            ProtobufField fieldAnnotation = field.getAnnotation(ProtobufField.class);
            ProtobufMessage messageAnnotation = field.getAnnotation(ProtobufMessage.class);
            if (fieldAnnotation != null) {
                ProtobufType protobufType = fieldAnnotation.protobufType();
                // Check if field type is compatible with the protobuf type
                if (!TypeCompatibility.check(protobufType, field.getType())) {
                    throw new ProtobufSerializationException("Incompatable field type and and protobuf type found: " +
                            field.getType() + " " + protobufType);
                }
                // Serialize value
                try {
                    appendPrefix(byteStream, protobufType, fieldAnnotation.fieldNumber());
                    append(byteStream, protobufType, field.get(message));
                } catch (IllegalAccessException e) {
                    throw new ProtobufSerializationException(e.getMessage());
                }

            }
        }
        /**
         * embedded messages are treated in exactly the same way as strings (wire type = 2).
         */

        /**
         * A packed repeated field containing zero elements does not appear in the encoded message. Otherwise, all of the elements of the field are packed
         * into a single key-value pair with wire type 2 (length-delimited). Each element is encoded the same way it would be normally,
         * except without a key preceding it.
         */
        return null;
    }

    private static boolean isMessageClass(Class clazz) {
        return clazz.getAnnotationsByType(ProtobufMessage.class).length > 0;
    }

    static <T> void append(ByteArrayOutputStream byteStream, ProtobufType type, Object value) {
        switch (type) {
            case DOUBLE:
                appendFixed64(byteStream, (Double) value);
                break;
            case FLOAT:
                appendFixed32(byteStream, (Float) value);
                break;
            case INT32:
                appendFixed32(byteStream, (Integer) value);
                break;
            case INT64:
                appendFixed64(byteStream, (Long) value);
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
            case BOOL:
                appendVarint(byteStream, (boolean) value == true ? 1 : 0);
                break;
            case STRING:
                appendLengthDelimited(byteStream, (String) value);
                break;
            case BYTES:
                appendLengthDelimited(byteStream, (byte[]) value);
                break;
        }
    }

    /**
     * varint encoded length followed by the specified number of bytes of data.
     */
    static void appendLengthDelimited(ByteArrayOutputStream byteStream, String in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        if (in == null || in.length() == 0) {
            return;
        }
        appendLengthDelimited(byteStream, StandardCharsets.UTF_8.encode(in).array());
    }

    static void appendLengthDelimited(ByteArrayOutputStream byteStream, byte[] in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        // Add # of bytes
        appendVarint(byteStream, in.length);
        // Add content
        try {
            byteStream.write(in);
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    static void appendFixed32(ByteArrayOutputStream byteStream, int in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        try {
            byteStream.write(ByteBuffer.allocate(4).putInt(in).array());
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    static void appendFixed32(ByteArrayOutputStream byteStream, float in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        try {
            byteStream.write(ByteBuffer.allocate(4).putFloat(in).array());
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    static void appendFixed64(ByteArrayOutputStream byteStream, long in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        try {
            byteStream.write(ByteBuffer.allocate(8).putLong(in).array());
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    static void appendFixed64(ByteArrayOutputStream byteStream, double in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        try {
            byteStream.write(ByteBuffer.allocate(8).putDouble(in).array());
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    // Each key in the streamed message is a varint with the value (field_number << 3) | wire_type
    static void appendPrefix(ByteArrayOutputStream byteStream, ProtobufType type, int fieldNumber) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        if (fieldNumber > MAX_FIELD_NUMBER || fieldNumber < MIN_FIELD_NUMBER) {
            throw new ProtobufSerializationException("Invalid field number " + fieldNumber);
        }
        WireType wireType = TypeMapper.getWireType(type);
        int valueToEncode = ((fieldNumber << 3) + wireType.getWireTypeId());
        appendVarint(byteStream, valueToEncode);
    }

    /**
     * Each byte in a varint, except the last byte, has the most significant bit (msb) set – this indicates that there are further bytes to come.
     * The lower 7 bits of each byte are used to store the two's complement representation of the number in groups of 7 bits, least significant group first.
     */
    static void appendVarint(ByteArrayOutputStream byteStream, long in) {
        if (byteStream == null) {
            byteStream = new ByteArrayOutputStream();
        }
        // Wrap all byte stream writing in try/catch
        try {
            long inputBitMask = 1L;
            // Just exit early if zero
            if (in == 0) {
                byteStream.write(new byte[]{0x00});
                return;
            }
            int numLeadingZeros = getLeadingZeros(in);
            int lastIdx = (64 - numLeadingZeros);
            // Iterate over bits of input, skipping leading zeros
            BitSet curByte = getEmptyBitSet();
            for (int i = 0; i < lastIdx; i++) {
                int curByteOffset = i % 7;
                boolean isLastBit = (i == lastIdx - 1);
                // get bit at the given index
                long maskedInput = in & inputBitMask;
                // set corresponding, zero-indexed bit on current byte.
                curByte.set(curByteOffset, maskedInput != 0);
                inputBitMask <<= 1;
                // Write last byte, set MSB to 0
                if (isLastBit) {
                    curByte.set(7, false);
                    byteStream.write(curByte.toByteArray());
                }
                // Write full byte (7 bits + indicator)
                else if (curByteOffset == 6) {
                    byteStream.write(curByte.toByteArray());
                    curByte = getEmptyBitSet();
                }
            }
        } catch (IOException e) {
            throw new ProtobufSerializationException(e.getMessage());
        }
    }

    private static int getLeadingZeros(long value) {
        // TODO can probably do this as a binary search
        long bitMask = Long.MIN_VALUE;
        int count = 0;
        for (int i = 0; i < 64; i++) {
            boolean isZero = (value & bitMask) == 0;
            if (isZero) {
                count++;
                // Shift in zeros
                bitMask >>>= 1;
            } else {
                break;
            }
        }
        return count;
    }

    private static BitSet getEmptyBitSet() {
        return BitSet.valueOf(new byte[]{(byte) 0x80});
    }

}
