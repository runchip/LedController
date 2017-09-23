package org.runchip.ledcontroller;

import android.app.Service;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.HandlerThread;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "LedController";

    Button btnSend;
    EditText editDeviceIP;
    EditText editDevicePort;
    TextView textOutput;
    TextView textSeek1;
    TextView textSeek2;
    TextView textSeek3;
    SeekBar seekLed1;
    SeekBar seekLed2;
    SeekBar seekLed3;
    ScrollView scrollOutput;

    int ledFreq1;
    int ledFreq2;
    int ledFreq3;

    private Socket deviceConnection;
    private OutputStream deviceSendStream;

    private HandlerThread networkThread;
    private Handler networkHandler;
    private Handler mainHandler;

    DhcpInfo getWiFiDhcpInfo() {
        WifiManager wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
        return wifiManager.getDhcpInfo();
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    void initViews() {
        // bind view objdects
        editDeviceIP = (EditText) findViewById(R.id.edit_device_ip);
        editDevicePort = (EditText) findViewById(R.id.edit_device_port);
        scrollOutput = (ScrollView) findViewById(R.id.scroll_output);
        btnSend = (Button) findViewById(R.id.button_send);
        textOutput = (TextView) findViewById(R.id.text_output);
        seekLed1 = (SeekBar) findViewById(R.id.seek_led1);
        seekLed2 = (SeekBar) findViewById(R.id.seek_led2);
        seekLed3 = (SeekBar) findViewById(R.id.seek_led3);
        textSeek1 = (TextView) findViewById(R.id.text_seek1);
        textSeek2 = (TextView) findViewById(R.id.text_seek2);
        textSeek3 = (TextView) findViewById(R.id.text_seek3);

        // register callbacks
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (seekBar.getId() == R.id.seek_led1) {
                        ledFreq1 = progress;
                        textSeek1.setText(Integer.toString(ledFreq1));
                    } else if (seekBar.getId() == R.id.seek_led2) {
                        ledFreq2 = progress;
                        textSeek2.setText(Integer.toString(ledFreq2));
                    } else if (seekBar.getId() == R.id.seek_led3) {
                        ledFreq3 = progress;
                        textSeek3.setText(Integer.toString(ledFreq3));
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
        seekLed1.setOnSeekBarChangeListener(listener);
        seekLed2.setOnSeekBarChangeListener(listener);
        seekLed3.setOnSeekBarChangeListener(listener);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence devIP = editDeviceIP.getText();
                CharSequence devPort = editDevicePort.getText();
                if (!isValidIP(devIP)) {
                    printLogToUI("IP invalid!");
                    return;
                }
                if (!isValidPort(devPort)) {
                    printLogToUI("Port invalid!");
                    return;
                }

                connectToDevice(devIP.toString(), Integer.parseInt(devPort.toString()));
                sendDeviceControlCommand();
                disconnectDevice();
            }
        });
    }

    private void connectToDevice(final String host, final int port) {
        networkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    deviceConnection = new Socket(host, port);
                    deviceSendStream = deviceConnection.getOutputStream();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            printLogToUI("connect to device success!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            printLogToUI("connect to device failed!");
                        }
                    });
                }
            }
        });
    }

    private void disconnectDevice() {
        networkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (deviceConnection == null) return;
                    deviceConnection.close();
                    deviceConnection = null;
                    deviceSendStream = null;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            printLogToUI("device connection close success!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            printLogToUI("device connection close failed!");
                        }
                    });
                }
            }
        });
    }

    private void sendDeviceControlCommand() {
        networkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (deviceSendStream == null) {
                    Log.d(TAG, "device connection not established!");
                    return;
                }
                byte[] command = {0x03, 0x00, 0x00, 0x00, 0x4D, 0x3C, 0x2B, 0x1A, (byte) ledFreq1, (byte) ledFreq2, (byte) ledFreq3};
                try {
                    deviceSendStream.write(command);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            printLogToUI("control command send success!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isValidPort(CharSequence devPort) {
        if (devPort == null || devPort.length() == 0) return false;
        int port = Integer.parseInt(devPort.toString());
        return 0 <= port && port <= 65535;
    }

    private void printLogToUI(CharSequence line) {
        if (textOutput.getText() == null || line == null) return;
        String newContent = "";
        if (textOutput.getText().length() > 0) {
            newContent += textOutput.getText().toString();
        }
        if (line.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            newContent += "[" + sdf.format(new Date()) + "] " + line.toString() + "\n";
            Log.d(TAG, line.toString());
        }
        textOutput.setText(newContent);
        scrollOutput.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private boolean isValidIP(CharSequence devIP) {
        if (devIP == null || devIP.length() == 0) return false;
        String[] nums = devIP.toString().split("\\.");
        if (nums.length != 4) return false;
        for (int i = 0; i < nums.length; i++) {
            int n = Integer.parseInt(nums[i]);
            if (n < 0 || n > 255) return false;
        }
        return true;
    }

    private void updateDeviceIP() {
        // get dhcp info
        DhcpInfo dhcpInfo = getWiFiDhcpInfo();
        String gateway = intToInetAddress(dhcpInfo.gateway).getHostAddress();
        editDeviceIP.setText(gateway);
        printLogToUI("Gateway: " + gateway);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        textSeek1.requestFocus();

        networkThread = new HandlerThread("AppNetworkHandlerThread");
        networkThread.start();

        networkHandler = new Handler(networkThread.getLooper());
        mainHandler = new Handler();

        updateDeviceIP();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateDeviceIP();
    }
}
