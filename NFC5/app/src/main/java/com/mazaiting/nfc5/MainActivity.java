package com.mazaiting.nfc5;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements
        NfcAdapter.CreateBeamUrisCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private static final String TAG = "MainActivity";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    private final String targetFilename = "/sdcard/image.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this,0,
                new Intent(this,getClass()),0);
        try{
            InputStream is = getResources().getAssets().open("image.jpg");
            FileOutputStream fos = new FileOutputStream(targetFilename);
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = is.read(buffer))!=-1){
                fos.write(buffer,0,len);
            }
            fos.close();
            is.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        // 设置回调
        mNfcAdapter.setBeamPushUrisCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        Uri[] uris = new Uri[1];
        Uri uri =  Uri.parse("file://" + targetFilename);
        uris[0]=uri;
        return uris;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "接触", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this,mPendingIntent,
                null,null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.e(TAG, "onNdefPushComplete: ");
    }
}
