package org.runchip.ledcontroller;

import android.app.Service;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.runchip.utils.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static String TAG = "LedController";

    // views
    private Button mSendButton;
    private EditText mEditDeviceIp;
    private EditText mEditDevicePort;
    private TextView mTextOutput;
    private TextView mTextSeek1;
    private TextView mTextSeek2;
    private TextView mTextSeek3;
    private SeekBar mSeekbarLed1;
    private SeekBar mSeekbarLed2;
    private SeekBar mSeekbarLed3;
    private ScrollView mScrollOutput;

    private NetworkThread mNetworkThread;

    private int mFreqLed1;
    private int mFreqLed2;
    private int mFreqLed3;

    DhcpInfo getWiFiDhcpInfo() {
        WifiManager wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
        return wifiManager.getDhcpInfo();
    }

    void bindViews() {
        // bind view objdects
        mEditDeviceIp = (EditText) findViewById(R.id.edit_device_ip);
        mEditDevicePort = (EditText) findViewById(R.id.edit_device_port);
        mScrollOutput = (ScrollView) findViewById(R.id.scroll_output);
        mSendButton = (Button) findViewById(R.id.button_send);
        mTextOutput = (TextView) findViewById(R.id.text_output);
        mSeekbarLed1 = (SeekBar) findViewById(R.id.seek_led1);
        mSeekbarLed2 = (SeekBar) findViewById(R.id.seek_led2);
        mSeekbarLed3 = (SeekBar) findViewById(R.id.seek_led3);
        mTextSeek1 = (TextView) findViewById(R.id.text_seek1);
        mTextSeek2 = (TextView) findViewById(R.id.text_seek2);
        mTextSeek3 = (TextView) findViewById(R.id.text_seek3);

        // register callbacks
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (seekBar.getId() == R.id.seek_led1) {
                        mFreqLed1 = progress;
                        mTextSeek1.setText(Integer.toString(mFreqLed1));
                    } else if (seekBar.getId() == R.id.seek_led2) {
                        mFreqLed2 = progress;
                        mTextSeek2.setText(Integer.toString(mFreqLed2));
                    } else if (seekBar.getId() == R.id.seek_led3) {
                        mFreqLed3 = progress;
                        mTextSeek3.setText(Integer.toString(mFreqLed3));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        mSeekbarLed1.setOnSeekBarChangeListener(listener);
        mSeekbarLed2.setOnSeekBarChangeListener(listener);
        mSeekbarLed3.setOnSeekBarChangeListener(listener);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence devIP = mEditDeviceIp.getText();
                CharSequence devPort = mEditDevicePort.getText();
                if (!NetUtils.isValidIP(devIP)) {
                    printLogToUI("IP invalid!");
                    return;
                }
                if (!NetUtils.isValidPort(devPort)) {
                    printLogToUI("Port invalid!");
                    return;
                }

                mNetworkThread.connect(devIP.toString(), Integer.parseInt(devPort.toString()), new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("connect to device success!");
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("connect to device failed!");
                    }
                });

                byte[] command = {0x03, 0x00, 0x00, 0x00, 0x4D, 0x3C, 0x2B, 0x1A, (byte) mFreqLed1, (byte) mFreqLed2, (byte) mFreqLed3};
                mNetworkThread.send(command, new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("control command send success!");
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("control command send failed!");
                    }
                });

                mNetworkThread.disconnect(new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("device connection close success!");
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        printLogToUI("device connection close failed!");
                    }
                });
            }
        });
    }

    private void printLogToUI(CharSequence line) {
        if (mTextOutput.getText() == null || line == null) return;
        String newContent = "";
        if (mTextOutput.getText().length() > 0) {
            newContent += mTextOutput.getText().toString();
        }
        if (line.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            newContent += "[" + sdf.format(new Date()) + "] " + line.toString() + "\n";
            Log.d(TAG, line.toString());
        }
        mTextOutput.setText(newContent);
        mScrollOutput.fullScroll(ScrollView.FOCUS_DOWN);
    }


    private void updateDeviceIP() {
        // get dhcp info
        DhcpInfo dhcpInfo = getWiFiDhcpInfo();
        String gateway = NetUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
        mEditDeviceIp.setText(gateway);
        printLogToUI("Gateway: " + gateway);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        mTextSeek1.requestFocus();

        mNetworkThread = new NetworkThread(new Handler());

        updateDeviceIP();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNetworkThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNetworkThread.quit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateDeviceIP();
    }
}
