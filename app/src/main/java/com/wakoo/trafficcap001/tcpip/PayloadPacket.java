package com.wakoo.trafficcap001.tcpip;

public abstract class PayloadPacket extends Packet {
    protected IPPacket parent;

    public PayloadPacket(IPPacket ipPacket) throws MalformedPacketException {
        parent = ipPacket;
    }

    public abstract IPPacket getParent();
    public abstract int getProtocol();
    public abstract int getSourcePort();
    public abstract int getDestinationPort();
    public abstract int getFlags();
}
