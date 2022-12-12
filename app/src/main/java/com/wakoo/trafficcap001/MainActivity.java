package com.wakoo.trafficcap001;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button start_button, stop_button;
    TextView error_view;
    EditText appid_edit;
    MyVpnService.VpnBinder vpn_binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_button = findViewById(R.id.start_button);
        stop_button = findViewById(R.id.stop_button);
        error_view = findViewById(R.id.error_msg);
        appid_edit = findViewById(R.id.appid_editor);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appid = appid_edit.getText().toString();
                if (appid.equals("")) {
                    showError(R.string.empty_appid_error);
                    return;
                }
                Intent prepare_intent = MyVpnService.prepare(MainActivity.this);
                if (prepare_intent != null) {
                    vpn_request_launcher.launch(prepare_intent);
                } else startVPN();
            }
        });

        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
unbindService(vpn_service_connection);
            }
        });
    }

    private void setButtons(boolean state) {
        start_button.setEnabled(state);
        stop_button.setEnabled(!state);
    }

    private void showError(final int id) {
        showError(getString(id));
    }

    private void showError(final String str) {
        error_view.setText(str);
        error_view.setVisibility(View.VISIBLE);
    }

    private void removeError() {
        error_view.setText("");
        error_view.setVisibility(View.GONE);
    }

    ActivityResultLauncher<Intent> vpn_request_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                startVPN();
            } else {
                showError(R.string.vpn_forbidden_text);
            }
        }
    });

    private final ServiceConnection vpn_service_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            vpn_binder = (MyVpnService.VpnBinder) service;
            setButtons(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            vpn_binder = null;
            setButtons(true);
        }
    };

    private void startVPN() {
        Intent vpnstart_intent = new Intent(this, MyVpnService.class);
        vpnstart_intent.putExtra(MyVpnService.APP_TO_LISTEN, appid_edit.getText().toString());
        final boolean bind_result = bindService(vpnstart_intent, vpn_service_connection, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);
        removeError();
    }
}