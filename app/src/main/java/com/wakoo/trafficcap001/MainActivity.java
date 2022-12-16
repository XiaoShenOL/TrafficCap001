package com.wakoo.trafficcap001;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements MainActivityController {
    Button start_button, stop_button;
    TextView error_view;
    EditText appid_edit;
    MyVpnService.VpnBinder vpn_binder = null;
    EditText recpacks_edit, sentpacks_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_button = findViewById(R.id.start_button);
        stop_button = findViewById(R.id.stop_button);
        error_view = findViewById(R.id.error_msg);
        appid_edit = findViewById(R.id.appid_editor);
        recpacks_edit = findViewById(R.id.recpacks_edit);
        sentpacks_edit = findViewById(R.id.sentpacks_edit);

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
                setButtons(true);
            }
        });

        findViewById(R.id.button1337).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCounters();
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
            ErrorBinder err_binder = (MyVpnService.VpnBinder) service;
            if (err_binder.getError() == ErrorBinder.Errors.ERROR_OK) {
                vpn_binder = (MyVpnService.VpnBinder) err_binder;
                setButtons(false);
            }
            else {
                ErrorBinder.Errors err = err_binder.getError();
                if (err == ErrorBinder.Errors.ERROR_HOST_UNKNOWN) {
                    showError(R.string.unknown_host_error);
                } else if (err == ErrorBinder.Errors.ERROR_NAME_NOT_FOUND) {
                    showError(R.string.unknown_appid);
                }
                unbindService(this);
            }

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

    @Override
    public void updateCounters() {
        recpacks_edit.post(new Runnable() {
            @Override
            public void run() {
                recpacks_edit.setText(Integer.toString(vpn_binder.getRecievedPackets()));
            }
        });
        sentpacks_edit.post(new Runnable() {
            @Override
            public void run() {
                sentpacks_edit.setText(Integer.toString(vpn_binder.getSentPackets()));
            }
        });
    }
}