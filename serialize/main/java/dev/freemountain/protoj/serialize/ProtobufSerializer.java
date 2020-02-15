package dev.freemountain.protoj.serialize;

import dev.freemountain.protoj.api.ProtobufField;
import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import dev.freemountain.protoj.internal.TypeMapper;
import dev.freemountain.protoj.internal.WireType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtobufSerializer {

    private final static Logger logger = LoggerFactory.getLogger(ProtobufSerializer.class);
    private static int MIN_FIELD_NUMBER = 1;
    private static int MAX_FIELD_NUMBER = (2 ^ 29) - 1;

    private ProtobufSerializer() {
        throw new RuntimeException();
    }

    public static <T> ByteBuffer serialize(T message) throws ReflectiveOperationException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        return serialize(byteStream, message, 0, new HashMap<>(), new HashSet<>());
    }

    static <T> ByteBuffer serialize(ByteArrayOutputStream byteStream, T message, int numLevel,
        HashMap<String, List<Integer>> visitedMessages, Set<Integer> visitedFieldNumbers)
        throws ReflectiveOperationException {
        // Circular reference checks
        String className = message.getClass().getName();
        markClassAsVisited(className, numLevel, visitedMessages);
        if (isCircularReference(className, numLevel, visitedMessages)) {
            throw new ProtobufSerializationException("Circular reference found for  " + className);
        }
        // Get serializable fields
        for (Field field : message.getClass().getDeclaredFields()) {
            ProtobufField fieldAnnotation = field.getAnnotation(ProtobufField.class);
            if (fieldAnnotation != null) {
                ProtobufType protobufType = fieldAnnotation.protobufType();
                Integer fieldNumber = fieldAnnotation.fieldNumber();
                if (visitedFieldNumbers.contains(fieldNumber)) {
                    throw new ProtobufSerializationException("Duplicate field number " + fieldNumber);
                }
                // Has a custom getter method?
                Object value;
                if (fieldAnnotation.getterMethod().length() > 0) {
                    value = message.getClass().getMethod(fieldAnnotation.getterMethod()).invoke(message);
                } else {
                    value = field.get(message);
                }
                // Skip adding missing values
                if (value != null) {
                    if (value instanceof Iterable) {
                        /**
                         *  The encoded message has zero or more key-value pairs with the same field number.
                         */
                        if (protobufType == ProtobufType.MESSAGE) {
                            for (Object iteratedValue : (Iterable) value) {
                                ByteBuffer nestedMessage = serialize(new ByteArrayOutputStream(), iteratedValue, numLevel,
                                    visitedMessages, new HashSet<>());
                                if (nestedMessage.hasArray() && nestedMessage.array().length > 0) {
                                    appendPrefix(byteStream, ProtobufType.BYTES, fieldNumber);
                                    appendLengthDelimited(byteStream, nestedMessage.array());
                                }
                            }
                        } else {
                            /**
                             * A packed repeated field containing zero elements does not appear in the encoded message.
                             * Otherwise, all of the elements of the field are packed into a single key-value pair with wire
                             * type 2 (length-delimited). Each element is encoded the same way it would be normally, except
                             * without a key preceding it.
                             *
                             * Repeated fields of scalar numeric types are packed by default
                             */
                            ByteArrayOutputStream iterableBytes = new ByteArrayOutputStream();
                            for (Object iteratedValue : (Iterable) value) {
                                if (iteratedValue != null) {
                                    append(iterableBytes, protobufType, iteratedValue);
                                }
                            }
                            if (iterableBytes.size() > 0) {
                                appendPrefix(byteStream, ProtobufType.BYTES, fieldNumber);
                                appendLengthDelimited(byteStream, iterableBytes.toByteArray());
                            }
                        }
                    } else {
                        /**
                         * Embedded messages are treated in exactly the same way as strings (wire type = 2).
                         */
                        if (protobufType == ProtobufType.MESSAGE) {
                            ByteBuffer nestedMessage = serialize(new ByteArrayOutputStream(), value, ++numLevel,
                                visitedMessages, new HashSet<>());
                            if (nestedMessage.hasArray() && nestedMessage.array().length > 0) {
                                appendPrefix(byteStream, ProtobufType.BYTES, fieldNumber);
                                appendLengthDelimited(byteStream, nestedMessage.array());
                            }
                        } else {
                            appendPrefix(byteStream, protobufType, fieldNumber);
                            append(byteStream, protobufType, value);
                        }
                    }

                }
                visitedFieldNumbers.add(fieldNumber);
            }
        }
        return ByteBuffer.wrap(byteStream.toByteArray());
    }

    private static void markClassAsVisited(String className, int currentLevel,
        Map<String, List<Integer>> visitedMessages) {
        if (!visitedMessages.containsKey(className)) {
            visitedMessages.put(className, new ArrayList<>());
        }
        visitedMessages.get(className).add(currentLevel);
    }

    private static boolean isCircularReference(String className, int currentLevel,
        Map<String, List<Integer>> visitedMessages) {
        // Has this class been seen at another recursion level already?
        if (visitedMessages.containsKey(className)) {
            return visitedMessages.get(className).stream().anyMatch(level -> level != currentLevel);
        }
        return false;
    }

    static <T> void append(ByteArrayOutputStream byteStream, ProtobufType type, Object value) {
        // TODO avoid boxing object instantiation
        switch (type) {
            case DOUBLE:
                appendFixed64(byteStream, (double) value);
                break;
            case FLOAT:
                appendFixed32(byteStream, (float) value);
                break;
            case INT32:
            case UINT32:
            case SINT32:
                appendVarint(byteStream, (int) value);
                break;
            case INT64:
            case UINT64:
            case SINT64:
                appendVarint(byteStream, (long) value);
                break;
            case SFIXED32:
            case FIXED32:
                appendFixed32(byteStream, (int) value);
                break;
            case SFIXED64:
            case FIXED64:
                appendFixed64(byteStream, (long) value);
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
     * Each byte in a varint, except the last byte, has the most significant bit (msb) set – this indicates that there
     * are further bytes to come. The lower 7 bits of each byte are used to store the two's complement representation of
     * the number in groups of 7 bits, least significant group first.
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
