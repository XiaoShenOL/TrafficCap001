package com.wakoo.trafficcap001;

import static com.wakoo.trafficcap001.ErrorBinder.Errors.ERROR_HOST_UNKNOWN;
import static com.wakoo.trafficcap001.ErrorBinder.Errors.ERROR_IO_CREATION;
import static com.wakoo.trafficcap001.ErrorBinder.Errors.ERROR_NAME_NOT_FOUND;
import static com.wakoo.trafficcap001.ErrorBinder.Errors.ERROR_OK;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import com.wakoo.trafficcap001.tcpip.ConnectionsListener;
import com.wakoo.trafficcap001.tcpip.DescriptorListener;
import com.wakoo.trafficcap001.tcpip.IPPacket;
import com.wakoo.trafficcap001.tcpip.ThreadsCommunicator;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MyVpnService extends VpnService implements ThreadsCommunicator {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static final String APP_TO_LISTEN = "com.wakoo.trafficcap001.listenapp";

    private final VpnBinder binder = new VpnBinder();
    private ParcelFileDescriptor pfd;
    private Thread descriptor_thread, connections_thread;
    private ConnectionsListener connections_listener;

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) return super.onBind(intent);
        else {
            Builder builder = new Builder();
            try {
                builder.addAllowedApplication(intent.getStringExtra(APP_TO_LISTEN));
                builder.addAddress(Inet4Address.getByAddress(new byte[]{(byte) 192, 88, 99, 3}), 24);
                builder.addRoute(Inet4Address.getByAddress(new byte[]{0, 0, 0, 0}), 0);
                builder.setBlocking(true);
            } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                return new ErrorBinder() {
                    @Override
                    public Errors getError() {
                        return ERROR_NAME_NOT_FOUND;
                    }
                };
            } catch (UnknownHostException unknownHostException) {
                return new ErrorBinder() {
                    @Override
                    public Errors getError() {
                        return ERROR_HOST_UNKNOWN;
                    }
                };
            }
            pfd = builder.establish();
            FileDescriptor fd = pfd.getFileDescriptor();
            try {
                descriptor_thread = new Thread(new DescriptorListener(new FileInputStream(fd), this), "Поток прослушивания дескриптора");
                connections_listener = new ConnectionsListener(new FileOutputStream(fd), this);
                connections_thread = new Thread(connections_listener, "Поток прослушивания соединений");
                descriptor_thread.start();
                connections_thread.start();
            } catch (IOException ioException) {
                return new ErrorBinder() {
                    @Override
                    public Errors getError() {
                        return ERROR_IO_CREATION;
                    }
                };
            }
            return binder;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            pfd.close();
        } catch (IOException e) {
        }
        return false;
    }

    static class VpnBinder extends ErrorBinder {
        @Override
        public Errors getError() {
            return ERROR_OK;
        }

        private int rec_packets;
        private int sent_packets;

        public int getRecievedPackets() {
            return rec_packets;
        }

        public int getSentPackets() {
            return sent_packets;
        }

        public void addRecievedPacket() {
            rec_packets++;
        }

        public void addSentPacket() {
            sent_packets++;
        }
    }

    @Override
    public void stopDueToError() {
        stopSelf();
    }

    @Override
    public void addRecievedPacket() {
        binder.addRecievedPacket();
    }

    @Override
    public void enqueuePacket(IPPacket packet) {
        connections_listener.enqueuePacket(packet);
    }
}