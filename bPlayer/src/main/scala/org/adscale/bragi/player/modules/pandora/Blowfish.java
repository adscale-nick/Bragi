package org.adscale.bragi.player.modules.pandora;

import java.util.ArrayList;
import java.util.List;

public class Blowfish {

    // Cipher directions
    private static final int ENCRYPT = 0;

    private static final int DECRYPT = 1;

    // For the roundFunc()
    private static final long modulus = (long) Math.pow(2L, 32);

    private long[] p_boxes;

    private long[][] s_boxes;


    public Blowfish(long[] p_boxes, long[][] s_boxes) {
        this.p_boxes = p_boxes;
        this.s_boxes = s_boxes;
    }


    protected long[] cipher(long xl, long xr, int direction) {
        long temp_x;
        long[] result = { 0L, 0L };

        if (direction == Blowfish.ENCRYPT) {
            for (int i = 0; i < 16; i++) {
                xl = xl ^ this.p_boxes[i];
                xr = this.roundFunc(xl) ^ xr;
                temp_x = xl;
                xl = xr;
                xr = temp_x;
            }
            temp_x = xl;
            xl = xr;
            xr = temp_x;
            xr = xr ^ this.p_boxes[16];
            xl = xl ^ this.p_boxes[17];
        }
        else if (direction == Blowfish.DECRYPT) {
            for (int i = 17; i > 1; i--) {
                xl = xl ^ this.p_boxes[i];
                xr = this.roundFunc(xl) ^ xr;
                temp_x = xl;
                xl = xr;
                xr = temp_x;
            }
            temp_x = xl;
            xl = xr;
            xr = temp_x;
            xr = xr ^ this.p_boxes[1];
            xl = xl ^ this.p_boxes[0];
        }

        result[0] = xl;
        result[1] = xr;
        return result;
    }


    private long roundFunc(long xl) {
        long a = (xl & 0xff000000) >> 24;
        long b = (xl & 0x00ff0000) >> 16;
        long c = (xl & 0x0000ff00) >> 8;
        long d = xl & 0x000000ff;

        // Perform all ops as longs then and out the last 32-bits to obtain the integer
        long f = ((long) this.s_boxes[0][(int) a] + (long) this.s_boxes[1][(int) b]) % Blowfish.modulus;
        f = f ^ (long) this.s_boxes[2][(int) c];
        f = f + (long) this.s_boxes[3][(int) d];
        f = (f % Blowfish.modulus) & 0xffffffff;

        return f;
    }


    public long[] encrypt(char[] data) {
        long[] chars = new long[8];

        if (data.length != 8) {
            throw new RuntimeException("Attempted to encrypt data of invalid block length: " + data.length);
        }

        // Use big endianess since that's what everyone else uses
        long xl = (long) data[3] | ((long) data[2] << 8) | ((long) data[1] << 16) | ((long) data[0] << 24);
        long xr = (long) data[7] | ((long) data[6] << 8) | ((long) data[5] << 16) | ((long) data[4] << 24);

        long[] temp_x = this.cipher(xl, xr, Blowfish.ENCRYPT);
        long cl = temp_x[0];
        long cr = temp_x[1];

        chars[0] = ((cl >> 24) & 0xff);
        chars[1] = ((cl >> 16) & 0xff);
        chars[2] = ((cl >> 8) & 0xff);
        chars[3] = (cl & 0xff);
        chars[4] = ((cr >> 24) & 0xff);
        chars[5] = ((cr >> 16) & 0xff);
        chars[6] = ((cr >> 8) & 0xff);
        chars[7] = (cr & 0xff);

        return chars;
    }


    public String decrypt(char[] data) {
        List<Character> characters = decryptToBytes(data);
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : characters) {
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }


    public List<Character> decryptToBytes(char[] data) {
        long[] chars = new long[8];

        if (data.length != 8) {
            throw new RuntimeException("Attempted to encrypt data of invalid block length: " + data.length);
        }

        // Use big endianess since that's what everyone else uses
        long cl = (long) data[3] | ((long) data[2] << 8) | ((long) data[1] << 16) | ((long) data[0] << 24);
        long cr = (long) data[7] | ((long) data[6] << 8) | ((long) data[5] << 16) | ((long) data[4] << 24);

        long[] temp_x = this.cipher(cl, cr, Blowfish.DECRYPT);
        long xl = temp_x[0];
        long xr = temp_x[1];

        chars[0] = ((xl >> 24) & 0xff);
        chars[1] = ((xl >> 16) & 0xff);
        chars[2] = ((xl >> 8) & 0xff);
        chars[3] = (xl & 0xff);
        chars[4] = ((xr >> 24) & 0xff);
        chars[5] = ((xr >> 16) & 0xff);
        chars[6] = ((xr >> 8) & 0xff);
        chars[7] = (xr & 0xff);

        List<Character> results = new ArrayList<>();
        for (long character : chars) {
            results.add((char) character);
        }

        return results;
    }


    public int blocksize() {
        return 8;
    }


    public int keyLength() {
        return 56;
    }


    public int keyBits() {
        return 56 * 8;
    }
}
