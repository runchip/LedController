package org.runchip.ledcontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "LedController";

    boolean isDeviceConnected;
    Button btnConnect;
    EditText editDeviceIP;
    EditText editDevicePort;
    TextView textStatus;
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


    void initViews() {
        // bind view objdects
        editDeviceIP = (EditText) findViewById(R.id.edit_device_ip);
        editDevicePort = (EditText) findViewById(R.id.edit_device_port);
        scrollOutput = (ScrollView) findViewById(R.id.scroll_output);
        btnConnect = (Button) findViewById(R.id.button_connect);
        textStatus = (TextView) findViewById(R.id.text_status_value);
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
                    sendDeviceControlCommand();
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

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence devIP = editDeviceIP.getText();
                CharSequence devPort = editDevicePort.getText();
                if (!isValidIP(devIP) || !isValidPort(devPort)) {
                    printLogToUI("IP or port invalid!");
                    return;
                }

                if (!isDeviceConnected) { // no device connected, do connect flow
                    if (connectToDevice(devIP.toString(), Integer.parseInt(devPort.toString()))) {
                        btnConnect.setText("Disconnect");
                        textStatus.setText("Connected");
                        isDeviceConnected = true;
                    }
                } else { // has device connected, do disconnect flow
                    if (disconnectCurrentDevice()) {
                        isDeviceConnected = false;
                        btnConnect.setText("Connect");
                        textStatus.setText("Offline");
                    }
                }
            }
        });
    }

    private boolean disconnectCurrentDevice() {
        try {
            deviceConnection.close();
            deviceConnection = null;
            deviceSendStream = null;
            printLogToUI("current device disconnect success!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            printLogToUI("current device disconnect failed");
            return false;
        }
    }

    private void sendDeviceControlCommand() {
        if (deviceSendStream == null) {
            printLogToUI("no device connected!");
            return;
        }
        byte[] command = {0x03, 0x00, 0x00, 0x00, 0x4D, 0x3C, 0x2B, 0x1A, (byte) ledFreq1, (byte) ledFreq2, (byte) ledFreq3};
        try {
            deviceSendStream.write(command);
            printLogToUI("control command send success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private boolean connectToDevice(String host, int port) {
        try {
            deviceConnection = new Socket(host, port);
            deviceSendStream = deviceConnection.getOutputStream();
            printLogToUI("connect to device success!");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            printLogToUI("connect to device failed!");
            return false;
        }
    }

    private boolean isValidIP(CharSequence devIP) {
        if (devIP == null || devIP.length() == 0) return false;
        String[] nums = devIP.toString().split(".");
        if (nums.length != 4) return false;
        for (int i = 0; i < nums.length; i++) {
            int n = Integer.parseInt(nums[i]);
            if (n < 0 || n > 255) return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }
}
