package com.wakoo.trafficcap001.tcpip;

import java.net.InetAddress;

public abstract class IPPacket {
    public IPPacket(final byte[] raw, final int length) throws MalformedPacketException {

    }

    public interface IPPacketRawFactory {
        public abstract IPPacket makeFromRaw(final byte[] raw, final int length) throws MalformedPacketException;
    }

    public static int getFamily(final byte b) {
        return (Byte.toUnsignedInt(b) & 0xF0) >>> 4;
    }
    public static int get16bit(final byte[] bytes, final int offset) {
        return (Byte.toUnsignedInt(bytes[offset]) << 8) | Byte.toUnsignedInt(bytes[offset+1]);
    };

    public abstract InetAddress getSourceAddress();
    public abstract InetAddress getDestinationAddress();
}
