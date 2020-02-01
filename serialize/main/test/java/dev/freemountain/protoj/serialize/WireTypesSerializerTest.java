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
            .equals(testOut.toByteArray(), new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x9A, 0x7A, (byte) 0xCE}));
    }

    @Test
    public void simpleFixed64Double() {
        ProtobufSerializer.appendFixed64(testOut, -1284.123);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(),
            new byte[]{(byte) 0xC0, (byte) 0x94, 0x10, 0x7D, (byte) 0xF3, (byte) 0xB6, 0x45, (byte) 0xA2}));
    }

    @Test
    public void simpleFixed32Int() {
        ProtobufSerializer.appendFixed32(testOut, 2020);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x00, 0x00, 0x07, (byte) 0xE4}));
    }

    @Test
    public void simpleFixed32Float() {
        ProtobufSerializer.appendFixed32(testOut, 47.8721F);
        logger.debug("result={}", printBits(testOut.toByteArray()));
        assertTrue(Arrays.equals(testOut.toByteArray(), new byte[]{0x42, 0x3F, 0x7D, 0x08}));
    }

}