package com.wakoo.trafficcap001.tcpip;

import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsListener implements Runnable {
    private final FileOutputStream output;
    private final ThreadsCommunicator tc;
    private ConcurrentLinkedQueue<IPPacket> packets;

    public ConnectionsListener(FileOutputStream output, ThreadsCommunicator tc) {
        this.output = output;
        this.tc = tc;
        packets = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {

    }

    public void enqueuePacket(IPPacket packet) {
        packets.add(packet);
    };
}
