package com.wakoo.trafficcap001.tcpip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPConnectionState implements AutoCloseable {
    private TCPState state;
    private SocketChannel channel;

    public TCPConnectionState(TCPPacket packet) throws IOException {
        state = TCPState.STATE_SYN_RCVD;
    }

    public void open(InetSocketAddress addr) throws IOException {
        channel = SocketChannel.open(addr);
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }
}
