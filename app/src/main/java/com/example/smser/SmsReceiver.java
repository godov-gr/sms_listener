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

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = smsMessage.getMessageBody();
                Log.d(TAG, "SMS received: " + messageBody);

                SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                String serverUrl = sharedPreferences.getString("serverUrl", "");
                Log.d(TAG, "Server URL: " + serverUrl);

                if (!serverUrl.isEmpty()) {
                    new SendPostRequest(context).execute(serverUrl, messageBody);
                } else {
                    Log.d(TAG, "Server URL is empty");
                }
            }
        } else {
            Log.d(TAG, "pdus is null");
        }
    }

    private static class SendPostRequest extends AsyncTask<String, Void, String> {

        private Context context;

        public SendPostRequest(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            String serverUrl = params[0];
            String messageBody = params[1];
            Log.d(TAG, "Sending POST request to: " + serverUrl);
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
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == 200) {
                    return "Request successful";
                } else {
                    return "Request failed with code: " + responseCode;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (context instanceof MainActivity) {
                ((MainActivity) context).updateStatus(result);
            }
            Log.d(TAG, "Request result: " + result);
        }
    }
}
