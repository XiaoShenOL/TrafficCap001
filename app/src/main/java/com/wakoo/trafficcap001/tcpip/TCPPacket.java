package com.wakoo.trafficcap001.tcpip;

import static com.wakoo.trafficcap001.tcpip.IPPacket.PROTOCOL_TCP;

public class TCPPacket extends PayloadPacket {
    private final int src_port;
    private final int dst_port;
    private final int seq_num;
    private final int ack_num;
    private final int data_offset;
    private final int flags;
    private final int window;
    private final int checksum;
    private final int urgent;
    private int maxseg = 32768;
    private int scale = 1;

    public static final int FLAG_URGENT = 32;
    public static final int FLAG_ACKNOWLEDGE = 16;
    public static final int FLAG_PUSH = 8;
    public static final int FLAG_RESET = 4;
    public static final int FLAG_SYNCH = 2;
    public static final int FLAG_FINISH = 1;

    private static final int OPTION_END = 0;
    private static final int OPTION_NOOP = 1;
    private static final int OPTION_MAXSEG = 2;
    private static final int OPTION_WINSCALE = 3;

    private final byte[] payload;

    public TCPPacket(IPPacket ipPacket) throws MalformedPacketException {
        super(ipPacket);
        final byte[] raw_packet = ipPacket.getPayload();
        if (raw_packet.length < 20) throw new MalformedPacketException("Заголовок слишком мал");
        src_port = get16bit(raw_packet, 0);
        dst_port = get16bit(raw_packet, 2);
        seq_num = get32bit(raw_packet, 4);
        ack_num = get32bit(raw_packet, 8);
        data_offset = Byte.toUnsignedInt(raw_packet[12]) >>> 4;
        flags = get16bit(raw_packet, 12) & 63;
        window = get16bit(raw_packet, 14);
        checksum = get16bit(raw_packet, 16);
        final int payload_len = raw_packet.length - (4 * data_offset);
        payload = new byte[payload_len];
        System.arraycopy(raw_packet, 0, payload, 0, payload_len);
        final byte[] header = new byte[data_offset * 4];
        System.arraycopy(raw_packet, 0, header, 0, data_offset * 4);
        header[16] = header[17] = 0;
        int cs_acc;
        cs_acc = computeCheckSum(0, parent.getPseudoHeader());
        cs_acc = computeCheckSum(cs_acc, header);
        cs_acc = computeCheckSum(cs_acc, payload);
        cs_acc = foldCheckSum(cs_acc);
        if (cs_acc != checksum) throw new MalformedPacketException("Неверная контрольная сумма");
        urgent = get16bit(raw_packet, 18);
        int option_offset = 20;
        boolean in_options = true;
        while (in_options && (option_offset < (data_offset * 4)) && (option_offset < raw_packet.length)) {
            final byte option_type = raw_packet[option_offset];
            switch (option_type) {
                case OPTION_END:
                    in_options = false;
                case OPTION_NOOP:
                    option_offset++;
                    break;
                case OPTION_MAXSEG:
                    if ((flags & FLAG_SYNCH) != 0) {
                        maxseg = get16bit(raw_packet, option_offset + 2);
                    }
                    option_offset += Byte.toUnsignedInt(raw_packet[option_offset + 1]);
                    break;
                case OPTION_WINSCALE:
                    if ((flags & FLAG_SYNCH) != 0) {
                        scale = Byte.toUnsignedInt(raw_packet[option_offset + 2]);
                    }
                    option_offset += Byte.toUnsignedInt(raw_packet[option_offset + 1]);
                    break;
                default:
                    option_offset += Byte.toUnsignedInt(raw_packet[option_offset + 1]);
                    break;
            }
        }
        option_offset = align4(option_offset);
        if (option_offset != (data_offset * 4))
            throw new MalformedPacketException("Что-то странное с длинами пакета, заголовка и данных");
    }

    @Override
    public IPPacket getParent() {
        return parent;
    }

    @Override
    public int getProtocol() {
        return PROTOCOL_TCP;
    }

    @Override
    public int getSourcePort() {
        return src_port;
    }

    @Override
    public int getDestinationPort() {
        return dst_port;
    }

    @Override
    public int getFlags() {
        return flags;
    }
}
