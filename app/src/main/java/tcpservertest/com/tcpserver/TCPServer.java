package tcpservertest.com.tcpserver;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPServer extends Thread {

    private int port = 0;

    private Handler mHandler = null;

    private ServerSocket serverSocket;
    private Socket mSocket;

    private BufferedReader buffRecv;

    private boolean isRun = true;


    public TCPServer(int port, Handler mHandler) {
        this.port = port;
        this.mHandler = mHandler;
    }
    static class MessageTypeClass {
        public static final int SOCKET_CONNECTED = 0;
        public static final int SOCKET_DATA = 1;
        public static final int SOCKET_DISCONNECTED = 2;
    };
    public enum MessageType { SOCKET_CONNECTED, SOCKET_DATA, SOCKET_DISCONNECTED };

    private void makeMessage(MessageType what, Object obj)
    {
        Message msg = Message.obtain();
        msg.what = what.ordinal();
        msg.obj  = obj;
        mHandler.sendMessage(msg);
    }

    private boolean startSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isRun = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        Log.d("_LOG_", "S: Connecting... Port: " + Integer.toString(port));
        try {
            if (startSocket(port)) {
                makeMessage(MessageType.SOCKET_CONNECTED, "Server Start");
            }
            while (isRun) {
                mSocket = serverSocket.accept();
                try {
                    buffRecv = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    String str = "";
                    while (true) {
                        if ((str = buffRecv.readLine()) != null) {
                            makeMessage(MessageType.SOCKET_DATA, str);
                        } else {
                            break;
                        }
                    }
                    makeMessage(MessageType.SOCKET_DATA, str);

                } catch (Exception e) {
                    Log.d("_LOG_", "S: Error");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPaused() {
        try {
            isRun = false;
            if (buffRecv != null) {
                buffRecv.close();
                buffRecv = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
            makeMessage(MessageType.SOCKET_DISCONNECTED, "");
            Log.d("_LOG_", "S: Done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}