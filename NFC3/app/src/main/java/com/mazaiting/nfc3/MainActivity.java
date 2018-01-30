package com.mazaiting.nfc3;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    /**写入内容的文本框*/
    private EditText mEditText;
    /**显示NFC卡中的内容*/
    private TextView mTextView;

    /**NFC相关*/
    /**NFC适配器*/
    private NfcAdapter mNfcAdapter;
    /**延时意图*/
    private PendingIntent mPendingIntent;
    /**检测的卡类型*/
    private static String[][] sTechArr = null;
    /**意图过滤器*/
    private static IntentFilter[] sFilters = null;
    static {
        try {
            sTechArr = new String[][]{
                    {IsoDep.class.getName()}, {NfcV.class.getName()}, {NfcF.class.getName()},
                    {MifareUltralight.class.getName()}, {NfcA.class.getName()}
            };
            // 如果这里不设置过滤器，就必须要在AndroidManifest.xml中为MainActivity设置过滤器
            // 这里的ACTION在NfcAdapter中有四个选中，个人认为是在设置响应优先级
            /**
             *
             <intent-filter>
             <action android:name="android.nfc.action.NDEF_DISCOVERED" />
             <category android:name="android.intent.category.DEFAULT" />
             <data android:mimeType="text/plain" />
             </intent-filter>
             */
            sFilters = new IntentFilter[]{
                    new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*")
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initNfc();
        // 判断当前手机是否支持NFC
        if (null == mNfcAdapter) {
            // 如果为null,则为不支持。
            Toast.makeText(this, "当前设备不支持NFC功能!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * 初始化NFC
     */
    private void initNfc() {
        // 1. 初始化Nfc适配器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // 2. 初始化延时意图
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 3. 允许后台唤醒--此处是指应用处于不可见的状态时，将卡片贴近手机时，唤醒当前App。
        // 如果sFilters为null,则为不过滤任何意图；如果sTechArr为null时，则为接收所有类型的卡片。
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, sFilters, sTechArr);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 4. 禁止后台唤醒
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 5. 重写OnIntent方法, 读取到的数据存储在intent中
        // 获取到意图中的Tag数据
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // 可打印出当前NFC芯片支持的NFC类型
//        onNewIntent: TAG: Tech [android.nfc.tech.NfcA, android.nfc.tech.MifareUltralight, android.nfc.tech.NdefFormatable]
        Log.e(TAG, "onNewIntent: " + tag);
        // 此处判断输入框mEditText的内容是否为空，如果为空，则为读取数据；如果不为空，则为写入数据
        if (TextUtils.isEmpty(mEditText.getText().toString())) {
            // 数据为空，读取数据
            readData(tag);
        } else {
            // 数据不为空，写入数据
            writeData(tag);
        }
    }

    /**
     * 写数据
     * @param tag Tag
     */
    private void writeData(Tag tag) {
        // 获取到MifareUltralight
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            // 连接
            ultralight.connect();
            // Write 1 page (4 bytes).
            // 往每一页写数据，最多写四个字节
            ultralight.writePage(4,"中国".getBytes(Charset.forName("gb2312")));
            ultralight.writePage(5,"美国".getBytes(Charset.forName("gb2312")));
            ultralight.writePage(6,"英国".getBytes(Charset.forName("gb2312")));
            ultralight.writePage(7,"法国".getBytes(Charset.forName("gb2312")));
            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            mEditText.setText("");
            // 关闭连接
            ultralight.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读数据
     * @param tag Tag
     */
    private void readData(Tag tag) {
        // 获取连接
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            // 连接
            ultralight.connect();
            byte[] bytes = ultralight.readPages(4);
            mTextView.setText(new String(bytes, Charset.forName("gb2312")));
            // 断开连接
            ultralight.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mEditText = (EditText) this.findViewById(R.id.editText);
        mTextView = (TextView) this.findViewById(R.id.textView);
    }
}
