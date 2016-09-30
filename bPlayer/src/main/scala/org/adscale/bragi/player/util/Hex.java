package org.adscale.bragi.player.util;

public class Hex {

    public static byte[] toBytes(String hex) {
        if (!isHex(hex)) {
            throw new IllegalArgumentException("Input string is not a valid hexadecimal string!");
        }

        byte[] bytes = new byte[hex.length() / 2];

        /* Loop over two characters at a time and convert them to a byte. */
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return bytes;
    }


    public static String toHex(byte[] bytes) {
        String hex = "";

        for (byte aByte : bytes) {
            hex += String.format("%02x", aByte);
        }

        return hex;
    }


    public static boolean isHex(String hex) {
        return (hex.length() % 2 == 0) && hex.matches("[0-9A-Fa-f]+");
    }
}