package dev.freemountain.protoj.util;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static String printBits(byte[] bytes) {
        List<String> bits = new ArrayList<>();
        for (byte b : bytes) {
            // Append a 1 @ bit 9 to print leading zeros
            bits.add(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
        return bits.toString();
    }
}
