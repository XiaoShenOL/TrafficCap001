package com.wakoo.trafficcap001.tcpip;

import java.net.InetAddress;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;

public class IPv6Packet extends IPPacket {
    private static final int ADDRESS_LENGTH = 16;

    public IPv6Packet(final byte[] raw, final int length) throws MalformedPacketException {
        super(raw, length);
        assert getFamily(raw[0]) == 6;
    }

    @Override
    public InetAddress getSourceAddress() {
        return null;
    }

    @Override
    public InetAddress getDestinationAddress() {
        return null;
    }

    @Override public int getProtocol() {
        return -1;
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    @Override
    public byte[] getPseudoHeader() {
        return null;
    }

    @Override
    public ProtocolFamily getProtocolFamily() {
        return StandardProtocolFamily.INET6;
    }
}
