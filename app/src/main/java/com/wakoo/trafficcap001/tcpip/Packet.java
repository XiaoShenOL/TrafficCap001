package com.wakoo.trafficcap001.tcpip;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Packet {
    public static int get16bit(@NonNull final byte[] bytes, final int offset) {
        return (Byte.toUnsignedInt(bytes[offset]) << 8) | Byte.toUnsignedInt(bytes[offset + 1]);
    }

    public static int get32bit(@NonNull final byte[] bytes, final int offset) {
        return (Byte.toUnsignedInt(bytes[offset]) << 24) | (Byte.toUnsignedInt(bytes[offset + 1]) << 16) | (Byte.toUnsignedInt(bytes[offset + 2]) << 8) | Byte.toUnsignedInt(bytes[offset + 3]);
    }

    public static int computeCheckSum(int acc, final byte[] bytes) {
        if (bytes.length % 2 == 0) {
            for (int i = 0; i < bytes.length; i += 2) {
                acc += get16bit(bytes, i);
            }
        } else {
            for (int i = 0; i < (bytes.length - 1); i += 2) {
                acc += get16bit(bytes, i);
            }
            acc += bytes[bytes.length - 1] << 16;
        }
        return acc;
    }

    public static int foldCheckSum(int acc) {
        while ((acc >>> 16) != 0) {
            final int hi = acc >>> 16;
            final int lo = acc & 0xFFFF;
            acc = hi + lo;
        }
        return (~acc) & 0xFFFF;
    }

    public static void set16bit(@NonNull final byte[] bytes, final int value, final int offset) {
        final byte hi = (byte) ((value >>> 8) & 0xFF);
        final byte lo = (byte) (value & 0xFF);

        bytes[offset] = hi;
        bytes[offset + 1] = lo;
    }

    public static int align4(int x) {
        return ((x & 3) != 0) ? (x + 4) & (-4) : x;
    }
}
