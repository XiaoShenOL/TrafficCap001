package com.wakoo.trafficcap001.tcpip;

import static com.wakoo.trafficcap001.tcpip.IPPacket.PROTOCOL_TCP;
import static java.lang.Thread.currentThread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.wakoo.trafficcap001.tcpip.TCPConnectionState;

public class ConnectionsListener implements Runnable {
    private final FileOutputStream output;
    private final ThreadsCommunicator tc;
    private final ConcurrentLinkedQueue<IPPacket> packets;
    private final Selector selector;

    public ConnectionsListener(FileOutputStream output, ThreadsCommunicator tc) throws IOException {
        this.output = output;
        this.tc = tc;
        this.packets = new ConcurrentLinkedQueue<>();
        this.connections = new HashMap<>();
        this.selector = Selector.open();
    }

    private final static Map<Integer, PacketProcessor> protocols;
    static {
        protocols = new HashMap<>();
        protocols.put(PROTOCOL_TCP, new PacketProcessor() {
            @Override
            public PayloadPacket makePacket(IPPacket packet) {
                PayloadPacket payloadPacket;
                try {
                    payloadPacket = new TCPPacket(packet);
                } catch (MalformedPacketException malformedPacketException) {
                    payloadPacket = null;
                }
                return payloadPacket;
            }
        });
    }

    private final Map<TCPConnectionEndpoints,TCPConnectionState> connections;

    @Override
    public void run() {
        try {
            while (!currentThread().isInterrupted()) {
                selector.select();
                IPPacket inserted_packet;
                inserted_packet = packets.poll();
                if (inserted_packet != null) {
                    final int protocol = inserted_packet.getProtocol();
                    PacketProcessor processor = protocols.get(protocol);
                    if (processor != null) {
                        final PayloadPacket payloadPacket = processor.makePacket(inserted_packet);
                        if (payloadPacket != null) {
                            if (payloadPacket instanceof TCPPacket) {
                                final TCPPacket tcpPacket = (TCPPacket) payloadPacket;
                                assert tcpPacket.getProtocol() == PROTOCOL_TCP;
                                if (tcpPacket.getFlags() == TCPPacket.FLAG_SYNCH) {
                                    TCPConnectionEndpoints endpoints;
                                    endpoints = new TCPConnectionEndpoints(tcpPacket);
                                    TCPConnectionState state;
                                    state = new TCPConnectionState(tcpPacket);
                                    connections.put(endpoints, state);
                                    try {
                                        state.open(endpoints.getLocalSocketAddress());
                                    } catch (IOException ioException) {
                                        // Если соединение установить не удалось, то нужно сообщить об этом другой стороне
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ioException) {

        }
    }

    public void enqueuePacket(IPPacket packet) {
        packets.add(packet);
        selector.wakeup();
    };

    public final class TCPConnectionEndpoints {
        public TCPConnectionEndpoints(PayloadPacket packet) {
            local_addr = packet.getParent().getDestinationAddress();
            remote_addr = packet.getParent().getSourceAddress();
            local_port = packet.getDestinationPort();
            remote_port = packet.getSourcePort();
        }

        private InetAddress local_addr, remote_addr;
        private int local_port, remote_port;

        @Override public int hashCode() {
            return local_addr.hashCode() ^ remote_addr.hashCode() ^ ((local_port << 16) | (remote_port & 0xFFFF));
        }

        @Override public boolean equals(Object o) {
            if (o instanceof TCPConnectionEndpoints) {
                TCPConnectionEndpoints s = (TCPConnectionEndpoints) o;
                return s.remote_addr.equals(this.remote_addr) && s.local_addr.equals(this.local_addr)
                         && (s.local_port == this.local_port) && (s.remote_port == this.remote_port);
            } else return false;
        }

        public InetAddress getLocalAddress() {
            return local_addr;
        }

        public InetAddress getRemoteAddress() {
            return remote_addr;
        }

        public int getLocalPort() {
            return local_port;
        }

        public int getRemotePort() {
            return remote_port;
        }

        public InetSocketAddress getLocalSocketAddress() {
            return new InetSocketAddress(local_addr, local_port);
        }
    }

    public interface PacketProcessor {
        PayloadPacket makePacket(IPPacket packet);
    }
}
