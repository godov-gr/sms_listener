package com.example.smser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = smsMessage.getMessageBody();

                SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                String serverUrl = sharedPreferences.getString("serverUrl", "");

                if (!serverUrl.isEmpty()) {
                    new SendPostRequest().execute(serverUrl, messageBody);
                }
            }
        }
    }

    private static class SendPostRequest extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String serverUrl = params[0];
            String messageBody = params[1];
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "message=" + messageBody;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("SmsReceiver", "POST Response Code :: " + responseCode);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
