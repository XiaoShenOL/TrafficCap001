package com.wakoo.trafficcap001.tcpip;

public interface ThreadsCommunicator {
    void stopDueToError();
    void addRecievedPacket();
    void enqueuePacket(IPPacket packet);
}
