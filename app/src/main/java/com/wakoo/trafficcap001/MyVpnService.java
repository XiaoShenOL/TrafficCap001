package com.wakoo.trafficcap001;

import static android.system.OsConstants.AF_INET;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MyVpnService extends VpnService {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static final String APP_TO_LISTEN = "com.wakoo.trafficcap001.listenapp";

    private final Binder binder = new VpnBinder();
    private ParcelFileDescriptor pfd;

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) return super.onBind(intent);
        else {
            try {
                Builder builder = new Builder();
                builder.addAllowedApplication(intent.getStringExtra(APP_TO_LISTEN));
                builder.addAddress(Inet4Address.getByAddress(new byte[]{(byte) 192, (byte) 168, 1, (byte) 243}), 32);
                pfd = builder.establish();
            } catch (Exception ex) {
                ex.printStackTrace();
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

    static class VpnBinder extends Binder {

    }
}