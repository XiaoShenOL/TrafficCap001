package com.wakoo.trafficcap001.tcpip;

import java.net.InetAddress;

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
}
