package com.wakoo.trafficcap001.tcpip;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.ProtocolFamily;

public abstract class IPPacket extends Packet {
    public IPPacket(final byte[] raw, final int length) throws MalformedPacketException {

    }

    public interface IPPacketRawFactory {
        public abstract IPPacket makeFromRaw(final byte[] raw, final int length) throws MalformedPacketException;
    }

    public static final int PROTOCOL_TCP = 6;

    public static int getFamily(final byte b) {
        return (Byte.toUnsignedInt(b) & 0xF0) >>> 4;
    }

    public abstract InetAddress getSourceAddress();
    public abstract InetAddress getDestinationAddress();
    public abstract int getProtocol();
    public abstract byte[] getPayload();
    public abstract byte[] getPseudoHeader();
    public abstract ProtocolFamily getProtocolFamily();
}
