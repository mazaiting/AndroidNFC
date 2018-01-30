package com.mazaiting.nfc4;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 测试方法， 将两部手机靠近，AndroidBeam接触时，两部手机界面同时缩小居中，轻触处理数据的手机一方，即可完成发送，
 * 放松数据期间手机不能离开，离开之后即会断开连接，终止发送。
 */
public class MainActivity extends AppCompatActivity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback{
    private static final String TAG = "MainActivity";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);

        // 设置发送消息的回调
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // 设置发送完成回调
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "NFC靠近", Toast.LENGTH_SHORT).show();
    }

    /**
     * 当有NFC靠近时调用该方法
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // 将"com.mazaiting.nfc2"更换为另一部手机中已存在的应用包名
        return new NdefMessage(NdefRecord.createApplicationRecord("com.mazaiting.nfc2"));
    }

    /**
     * 数据发送完成时调用方法
     */
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Toast.makeText(this, "发送完成", Toast.LENGTH_SHORT).show();
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
}
