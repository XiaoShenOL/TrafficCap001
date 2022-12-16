package com.wakoo.trafficcap001.tcpip;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DescriptorListener implements Runnable {
    private FileInputStream input;
    private ThreadsCommunicator tc;

    public DescriptorListener(FileInputStream input, ThreadsCommunicator tc) {
        this.input = input;
        this.tc = tc;
    }

    private static final Map<Integer, IPPacket.IPPacketRawFactory> ip_protocols;

    static {
        ip_protocols = new HashMap<>();
        ip_protocols.put(4, new IPPacket.IPPacketRawFactory() {
            @Override
            public IPPacket makeFromRaw(byte[] bytes, int length) throws MalformedPacketException {
                return new IPv4Packet(bytes, length);
            }
        });
        ip_protocols.put(6, new IPPacket.IPPacketRawFactory() {
            @Override
            public IPPacket makeFromRaw(byte[] bytes, int length) throws MalformedPacketException {
                return new IPv6Packet(bytes, length);
            }
        });
    }

    @Override
    public void run() {
        try {
            while (true) {
                final byte[] raw_packet = new byte[65536];
                final int in_readed = input.read(raw_packet);

                final int family = IPPacket.getFamily(raw_packet[0]);
                final IPPacket.IPPacketRawFactory factory = ip_protocols.get(family);
                if (factory != null) {
                    IPPacket packet = factory.makeFromRaw(raw_packet, in_readed);
                    tc.addRecievedPacket();
                }
            }
        } catch (IOException ioException) {
            tc.stopDueToError();
        } catch (MalformedPacketException malformedPacketException) {
            Log.i("Обработка пакетов", "Получен пакет с ошибкой", malformedPacketException);
        }
    }
}
