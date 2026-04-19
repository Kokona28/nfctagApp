package com.example.nfctagapp;

import android.app.PendingIntent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String mode = "";
    NfcAdapter nfcAdapter;
    TextView txtResult;
    Map<String, Boolean> attendanceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        txtResult = findViewById(R.id.txtResult);

        Button btnCheckIn = findViewById(R.id.btnCheckIn);
        Button btnCheckOut = findViewById(R.id.btnCheckOut);
        btnCheckIn.setOnClickListener(v -> {
            mode = "IN";
            Log.d("DEBUG", "出勤モード");
        });

        btnCheckOut.setOnClickListener(v -> {
            mode = "OUT";
            Log.d("DEBUG", "退勤モード");
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent newIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_MUTABLE);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
    private void handleNfcIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag != null) {

            byte[] idBytes = tag.getId();
            StringBuilder id = new StringBuilder();

            for (byte b : idBytes) {
                id.append(String.format("%02X", b));
            }

            // 👇 ① userId作る
            String userId = id.toString();

            // 👇 ② そのあと使う
            boolean isCheckedIn = attendanceMap.getOrDefault(userId, false);

            if (mode.equals("IN")) {

                if (isCheckedIn) {
                    Log.d("ERROR", "すでに出勤しています");
                    txtResult.setText("すでに出勤しています");
                } else {
                    attendanceMap.put(userId, true);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = sdf.format(new Date());

                    Log.d("NFC", "出勤: " + userId + " 時刻: " + time);
                    txtResult.setText("出勤しました\n" + time);
                }

            } else if (mode.equals("OUT")) {

                if (!isCheckedIn) {
                    Log.d("ERROR", "出勤していません");
                    txtResult.setText("出勤していません");

                } else {
                    attendanceMap.put(userId, false);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = sdf.format(new Date());

                    Log.d("NFC", "退勤: " + userId + " 時刻: " + time);
                    txtResult.setText("退勤しました\n" + time);

                }
            }
        }    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNfcIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
}