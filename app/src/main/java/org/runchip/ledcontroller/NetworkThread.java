package org.runchip.ledcontroller;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class NetworkThread {
    private static String TAG = "NetworkThread";

    private Socket mConnection;
    private OutputStream mSendStream;

    private Handler mNetworkHandler;
    private Handler mCallbackHandler;
    private HandlerThread mWorkerThread;

    NetworkThread(Handler handler) {
        mCallbackHandler = handler;
        mWorkerThread = new HandlerThread("NetworkWorkerThread");
    }

    public void start() {
        mWorkerThread.start();
        mNetworkHandler = new Handler(mWorkerThread.getLooper());
    }

    public boolean quit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mWorkerThread.quitSafely();
        } else {
            return mWorkerThread.quit();
        }
    }

    public void connect(final String host, final int port, final Runnable onSucess, final Runnable onFailed) {
        mNetworkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnection = new Socket(host, port);
                    mSendStream = mConnection.getOutputStream();
                    mCallbackHandler.post(onSucess);
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallbackHandler.post(onFailed);
                }
            }
        });
    }

    public void disconnect(final Runnable onSucess, final Runnable onFailed) {
        mNetworkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mConnection == null) return;
                    mConnection.close();
                    mConnection = null;
                    mSendStream = null;
                    mCallbackHandler.post(onSucess);
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallbackHandler.post(onFailed);
                }
            }
        });
    }

    public void send(final byte[] command, final Runnable onSuccess, final Runnable onFailed) {
        mNetworkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSendStream == null) {
                    Log.d(TAG, "device connection not established!");
                    return;
                }
                try {
                    mSendStream.write(command);
                    mCallbackHandler.post(onSuccess);
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallbackHandler.post(onFailed);
                }
            }
        });
    }
}
