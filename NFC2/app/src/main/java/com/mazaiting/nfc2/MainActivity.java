package com.mazaiting.nfc2;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
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
//        TAG: Tech [android.nfc.tech.NfcV, android.nfc.tech.Ndef]
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
     * 写入Uri
     * http://www.baidu.com/
     * @param tag
     */
    private void writeData(Tag tag) {
        // 创建一个Uri
        Uri uri = Uri.parse(mEditText.getText().toString());
        // 创建一个NdefMessage格式数据
        NdefMessage ndefMessage = new NdefMessage(NdefRecord.createUri(uri));
        if (writeTag(ndefMessage, tag)) {
            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            mEditText.setText("");
        }
    }

    /**
     * 写入数据
     * @param ndefMessage Ndef格式数据
     * @param tag Tag
     * @return true，写入成功；false，写入失败
     */
    private boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        // 计算写入数据的长度
        int size = ndefMessage.toByteArray().length;
        try {
            // 获取Ndef
            Ndef ndef = Ndef.get(tag);
            if (null != ndef) {
                // 连接NFC卡片
                ndef.connect();
                // 判断NFC芯片是否可写
                if (!ndef.isWritable()) {
                    Toast.makeText(this, "卡片不可写", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // 判断要写入的数据是否大于NFC芯片最大字节数
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this, "写入数据过大", Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(ndefMessage);
                return true;
            } else {
                // 获取一个格式化工具
                NdefFormatable formatable = NdefFormatable.get(tag);
                // 判断是否为空
                if (null != formatable){
                    // 连接
                    formatable.connect();
                    // 格式化并写入数据
                    formatable.format(ndefMessage);
                    Toast.makeText(this, "格式化成功，并成功写入数据", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "无法格式化", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取数据
     * @param tag Tag
     */
    private void readData(Tag tag) {
        try {
            // 获取Ndef
            Ndef ndef = Ndef.get(tag);
            // 连接
            ndef.connect();
            Log.e(TAG, "readData: type: " + ndef.getType() + ", size: " + ndef.getMaxSize()
                    + ", message: " + ndef.getNdefMessage().toString());
            // 获取Ndef格式数据
            NdefMessage ndefMessage = ndef.getNdefMessage();
            // 获取信息中的记录
            NdefRecord[] records = ndefMessage.getRecords();
            // 判断是否有数据
            if (null != records && records.length > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                // 迭代
                for (NdefRecord ndefRecord : records) {
                    Uri uri = ndefRecord.toUri();
                    if (null != uri) {
                        stringBuilder.append(uri.toString());
                    }
                }
                String string = stringBuilder.toString();
                mTextView.setText(string);
                if (string.startsWith("http://")) {
                    // 跳转网页
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuilder.toString())));
                }
            }
            // 断开连接
            ndef.close();
        } catch (IOException | FormatException e) {
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
