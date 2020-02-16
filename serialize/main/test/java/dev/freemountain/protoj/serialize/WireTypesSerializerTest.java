package dev.freemountain.protoj.serialize;

import static dev.freemountain.protoj.util.TestUtil.printBits;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import dev.freemountain.protoj.api.ProtobufSerializationException;
import dev.freemountain.protoj.api.ProtobufType;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WireTypesSerializerTest {

    private final static Logger logger = LoggerFactory.getLogger(WireTypesSerializerTest.class);
    private ByteArrayOutputStream testOut;

    @Before
    public void setup() {
        testOut = new ByteArrayOutputStream();
    }

    /**
     * Varint tests
     */
    @Test
    public void simpleVarint0() {
        ProtobufSerializer.appendVarint(testOut, 0);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x00}));
    }

    @Test
    public void simpleVarint1() {
        ProtobufSerializer.appendVarint(testOut, 1);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x01}));
    }

    @Test
    public void multiVarint1() {
        ProtobufSerializer.appendVarint(testOut, 300);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{(byte) 0xAC, 0x02}));
    }

    @Test
    public void negativeVarint1() {
        ProtobufSerializer.appendVarint(testOut, -1);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        /**
         *  If you use int32 or int64 as the type for a negative number, the resulting varint is always ten bytes long
         */
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
            , (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01}));
    }

    /**
     * Prefix byte tests
     */
    @Test
    public void simpleWireType0() {
        ProtobufSerializer.appendPrefix(testOut, ProtobufType.INT32, 1);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x08}));
    }

    @Test
    public void simpleWireType1() {
        ProtobufSerializer.appendPrefix(testOut, ProtobufType.FIXED64, 2);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x11}));
    }

    @Test
    public void simpleWireType2() {
        ProtobufSerializer.appendPrefix(testOut, ProtobufType.STRING, 5);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x2a}));
    }

    @Test
    public void simpleWireType5() {
        ProtobufSerializer.appendPrefix(testOut, ProtobufType.FLOAT, 15);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x7d}));
    }

    @Test
    public void invalidFieldNumberMin() {
        try {
            ProtobufSerializer.appendPrefix(testOut, ProtobufType.FLOAT, 0);
            fail();
        } catch (ProtobufSerializationException ex) {
        }
    }

    @Test
    public void invalidFieldNumberMax() {
        try {
            ProtobufSerializer.appendPrefix(testOut, ProtobufType.FLOAT, Integer.MAX_VALUE);
            fail();
        } catch (ProtobufSerializationException ex) {
        }
    }

    /**
     * Length delimited
     */
    @Test
    public void simpleString() {
        ProtobufSerializer.appendLengthDelimited(testOut, "testing");
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x07, 0x74, 0x65, 0x73, 0x74, 0x69, 0x6e, 0x67}));
    }

    @Test
    public void trimsEmptyByteArraySpace() {
        ProtobufSerializer.appendLengthDelimited(testOut, "hello world");
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x0b, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77,
            0x6f, 0x72, 0x6c, 0x64}));
    }

    @Test
    public void emptyString() {
        ProtobufSerializer.appendLengthDelimited(testOut, "");
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{}));
    }

    @Test
    public void nullString() {
        ProtobufSerializer.appendLengthDelimited(testOut, (String) null);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{}));
    }

    /**
     * Fixed Types
     */
    @Test
    public void simpleFixed64Long() {
        ProtobufSerializer.appendFixed64(testOut, 10123982);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays
            .equals(testOut.toByteArray(), new byte[]{(byte) 0xCE, 0x7A, (byte) 0x9A, 0x00, 0x00, 0x00, 0x00, 0x00}));
    }

    @Test
    public void simpleFixed64Double() {
        ProtobufSerializer.appendFixed64(testOut, -1284.123);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(),
            new byte[]{(byte) 0xA2, 0x45, (byte) 0xB6, (byte) 0xF3, 0x7D, 0x10, (byte) 0x94, (byte) 0xC0}));
    }

    @Test
    public void simpleFixed32Int() {
        ProtobufSerializer.appendFixed32(testOut, 2020);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{(byte) 0xe4, 0x07, 0x00, 0x00}));
    }

    @Test
    public void simpleFixed32Float() {
        ProtobufSerializer.appendFixed32(testOut, 47.8721F);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x08, 0x7D, 0x3F, 0x42}));
    }

}
