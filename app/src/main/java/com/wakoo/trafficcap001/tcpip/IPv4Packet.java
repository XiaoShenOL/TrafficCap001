package com.wakoo.trafficcap001.tcpip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IPv4Packet extends IPPacket {
    private static final int ADDRESS_LENGTH = 4;
    private final int header_length;
    private final int type_of_service;
    private final int total_length;
    private final int identification;
    private final boolean res_flag, df_flag, mf_flag;
    private final int fragment_offset;
    private final int ttl;
    private final int protocol;
    private final int header_cs;
    private final byte[] srcaddr_bytes;
    private final byte[] dstaddr_bytes;

    private static final byte OPTION_END = 0;
    private static final byte OPTION_NOOP = 1;
    private static final byte OPTION_SECURITY = (byte) 130;
    private static final byte OPTION_LSRR = (byte) 131;
    private static final byte OPTION_SSRR = (byte) 137;
    private static final byte OPTION_RECORDROUTE = 7;
    private static final byte OPTION_STRID = (byte) 136;
    private static final byte OPTIION_TIMESTAMP = 68;

    private final byte[] payload;

    public IPv4Packet(final byte[] raw, final int length) throws MalformedPacketException {
        super(raw, length);
        assert getFamily(raw[0]) == 4;
        if (length < 20) throw new MalformedPacketException("Пакет слишком мал");
        header_length = Byte.toUnsignedInt(raw[0]) & 15;
        if (header_length < 5) throw new MalformedPacketException("Длина заголовка в пакете слишком мала");
        type_of_service = Byte.toUnsignedInt(raw[1]);
        total_length = get16bit(raw, 2);
        identification = get16bit(raw, 4);
        final byte flags = raw[6];
        res_flag = (flags & 128) != 0;
        df_flag = (flags & 64) != 0;
        mf_flag = (flags & 32) != 0;
        if (mf_flag) throw new MalformedPacketException("Фрагментация не поддерживается");
        fragment_offset = get16bit(raw, 6) & 8191;
        ttl = raw[8];
        protocol = raw[9];
        header_cs = get16bit(raw, 10);
        byte[] header = new byte[header_length*4];
        System.arraycopy(raw, 0, header, 0, header_length*4);
        header[10] = header[11] = 0;
        final int computed_cs = foldCheckSum(computeCheckSum(0, header));
        if (computed_cs != header_cs) throw new MalformedPacketException("Неверная контрольная сумма заголовка");
        srcaddr_bytes = new byte[4];
        System.arraycopy(raw, 12, srcaddr_bytes, 0, 4);
        dstaddr_bytes = new byte[4];
        System.arraycopy(raw, 16, dstaddr_bytes, 0, 4);
        int option_offset = 20;
        boolean in_options = true;
        while (in_options && (option_offset < (header_length*4)) && (option_offset < length)) {
            final byte option_type = raw[option_offset];
            switch (option_type) {
                case OPTION_END:
                    in_options = false;
                case OPTION_NOOP:
                    option_offset++;
                    break;
                case OPTION_SECURITY:
                case OPTION_LSRR:
                case OPTION_SSRR:
                case OPTION_RECORDROUTE:
                case OPTION_STRID:
                case OPTIION_TIMESTAMP:
                default:
                    option_offset += Byte.toUnsignedInt(raw[option_offset+1]);
                    break;
            }
        }
        option_offset = align4(option_offset);
        final int payload_length = total_length - (header_length*4);
        if (payload_length != (length - (header_length*4))) throw new MalformedPacketException("Что-то странное с длинами пакета, заголовка и данных");
        payload = new byte[payload_length];
        System.arraycopy(raw, option_offset, payload, 0, payload_length);
    }

    @Override
    public InetAddress getSourceAddress() {
        try {
            return Inet4Address.getByAddress(srcaddr_bytes);
        } catch (UnknownHostException unknownHostException) {
            return null;
        }
    }

    @Override
    public InetAddress getDestinationAddress() {
        try {
            return Inet4Address.getByAddress(dstaddr_bytes);
        } catch (UnknownHostException unknownHostException) {
            return null;
        }
    }

    @Override public int getProtocol() {
        return protocol;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public byte[] getPseudoHeader() {
        final byte ph[] = new byte[12];
        System.arraycopy(srcaddr_bytes, 0, ph, 0, 4);
        System.arraycopy(dstaddr_bytes, 0, ph, 4, 4);
        ph[9] = (byte) protocol;
        set16bit(ph, payload.length, 10);
        return ph;
    }

    @Override
    public ProtocolFamily getProtocolFamily() {
        return StandardProtocolFamily.INET;
    }
}
