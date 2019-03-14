package com.dfxh.wang.serialport_test;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

import android_serialport_api.SerialPort;
import utils.SerialPortFinder;
import utils.SerialPortUtils;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private Button button_open;
    private Button button_close;
    private EditText editText_send;
    private Button button_send;
    private TextView textView_status;
    private Button button_status;
    private Spinner spinner_one;

    private SerialPortUtils serialPortUtils = new SerialPortUtils();
    private SerialPort serialPort;

    private Handler handler;
    private byte[] mBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(); //创建主线程的handler  用于更新UI

        button_open = (Button) findViewById(R.id.button_open);
        button_close = (Button) findViewById(R.id.button_close);
        button_send = (Button) findViewById(R.id.button_send);
        editText_send = (EditText) findViewById(R.id.editText_send);
        textView_status = (TextView) findViewById(R.id.textView_status);
        button_status = (Button) findViewById(R.id.button_status);
        spinner_one = (Spinner) findViewById(R.id.spinner_one);

        editText_send.setText("D");

        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //serialPortUtils = new SerialPortUtils();
                serialPort = serialPortUtils.openSerialPort();
                if (serialPort == null) {
                    Log.e(TAG, "串口打开失败");
                    Toast.makeText(MainActivity.this, "串口打开失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                textView_status.setText("串口已打开");
                Toast.makeText(MainActivity.this, "串口已打开", Toast.LENGTH_SHORT).show();

            }
        });
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialPortUtils.closeSerialPort();
                textView_status.setText("串口已关闭");
                Toast.makeText(MainActivity.this, "串口关闭成功", Toast.LENGTH_SHORT).show();
            }
        });
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                byte[] sendBuffer = new byte[8];
                sendBuffer[0] = (byte) 0xAB;
                sendBuffer[1] = (byte) 0x03;
                sendBuffer[2] = (byte) 0x00;
                sendBuffer[3] = (byte) 0x00;
                sendBuffer[4] = (byte) 0x00;
                sendBuffer[5] = (byte) 0x06;
                sendBuffer[6] = (byte) 0xDD;
                sendBuffer[7] = (byte) 0xC2;
                //  0xAB 0x03 0x00 0x00 0x00 0x06 0xDD 0xC2

                //计算字节数组的前6位数据的CRC值
//                sendBuffer = setParamCRC(sendBuffer);
//                sendBuffer[6]=(byte)CrcData;
//                sendBuffer[7]=(byte)(CrcData>>8);


                serialPortUtils.sendSerialPort(sendBuffer);
                String data_ = "";
                for(int i = 0 ; i< sendBuffer.length ;i ++) {
                    data_ += sendBuffer[i] + "  ";
                }
                textView_status.setText("串口发送指令：" + data_);

//                sendBuffer = setParamCRC(sendBuffer);
                Toast.makeText(MainActivity.this, "发送指令：" + sendBuffer.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        button_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //boolean status = serialPortUtils.serialPortStatus;
                //textView_status.setText(String.valueOf(status));
                FileDescriptor fileDescriptor = serialPort.mFd;
                String result = fileDescriptor.toString();
                textView_status.setText(result);
            }
        });
        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                Log.d(TAG, "进入数据监听事件中。。。" + new String(buffer));
                //
                //在线程中直接操作UI会报异常：ViewRootImpl$CalledFromWrongThreadException
                //解决方法：handler
                //
                mBuffer = buffer;
                handler.post(runnable);
            }

            //开线程更新UI
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    textView_status.setText("size：" + String.valueOf(mBuffer.length) + "数据监听：" + new String(mBuffer));
                    editText_send.setText("size：" + String.valueOf(mBuffer.length) + "数据监听：" + new String(mBuffer));
                    TextView tvSer = (TextView) findViewById(R.id.tv_ser);
                    tvSer.setText("size：" + String.valueOf(mBuffer.length) + "数据监听：" + new String(mBuffer));
                }
            };
        });


//        //定义一个下拉列表适配器
//        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this,R.array.data,R.layout.support_simple_spinner_dropdown_item);
//        spinner_one.setAdapter(arrayAdapter); //将适配器传入spinner
//        //设置选中事件
//        spinner_one.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                //获取选中数据
//                String a = spinner_one.getSelectedItem().toString();
//                Toast.makeText(MainActivity.this,"选中了"+a,Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        String[] entries = new mSerialPortFinder.getAllDevices();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        Log.e("lt--", entryValues.toString());

        editText_send.setText(entryValues.length + "f");

        TextView tvSer = (TextView) findViewById(R.id.tv_ser);

        String ser = "";
        for (int i = 0; i < entryValues.length; i++) {
            ser += entryValues[i] + "     ";
        }
        tvSer.setText(ser);

        serialPortUtils.setOnSendSuccessListener(new SerialPortUtils.OnSendSuccessListener() {
            @Override
            public void onSendSuccess(String value) {
                Toast.makeText(MainActivity.this, value, Toast.LENGTH_LONG).show();
                editText_send.setText(value);
            }
        });

    }

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();

    /**
     * 为Byte数组添加两位CRC校验
     *
     * @param buf
     * @return
     */
    public static byte[] setParamCRC(byte[] buf) {
        int MASK = 0x0001, CRCSEED = 0x0810;
        int remain = 0;

        byte val;
        for (int i = 0; i < buf.length; i++) {
            val = buf[i];
            for (int j = 0; j < 8; j++) {
                if (((val ^ remain) & MASK) != 0) {
                    remain ^= CRCSEED;
                    remain >>= 1;
                    remain |= 0x8000;
                } else {
                    remain >>= 1;
                }
                val >>= 1;
            }
        }

//            byte[] crcByte = new byte[2];
//            crcByte[0] = (byte) ((remain >> 8) & 0xff);
//            crcByte[1] = (byte) (remain & 0xff);

        buf[6] = (byte) (byte) (remain & 0xff);
        buf[7] = (byte) (byte) ((remain >> 8) & 0xff);

        // 将新生成的byte数组添加到原数据结尾并返回
        return buf;
    }

}
