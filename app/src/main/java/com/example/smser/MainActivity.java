package com.example.smser;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private EditText serverUrlEditText;
    private Button saveButton;
    private TextView statusTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlEditText = findViewById(R.id.serverUrlEditText);
        saveButton = findViewById(R.id.saveButton);
        statusTextView = findViewById(R.id.statusTextView);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedUrl = sharedPreferences.getString("serverUrl", "");

        if (!savedUrl.isEmpty()) {
            serverUrlEditText.setText(savedUrl);
            saveButton.setText("Изменить");
        }

        saveButton.setOnClickListener(v -> {
            String serverUrl = serverUrlEditText.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("serverUrl", serverUrl);
            editor.apply();

            Toast.makeText(MainActivity.this, "Сервер сохранён", Toast.LENGTH_SHORT).show();
            saveButton.setText("Изменить");
            statusTextView.setText("URL сохранен");
            statusTextView.setVisibility(View.VISIBLE);
        });

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.INTERNET},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Permissions granted");
                statusTextView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Permissions denied");
                statusTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void updateStatus(String status) {
        runOnUiThread(() -> {
            statusTextView.setText(status);
            statusTextView.setVisibility(View.VISIBLE);
        });
    }
}
