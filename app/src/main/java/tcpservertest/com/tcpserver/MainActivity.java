package tcpservertest.com.tcpserver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private TCPServer socket;
    private Handler mHandler;

    private int port = 60124;

    private TextView status;
    private EditText editPort;
    private Button btn;
    private LinearLayout touchLayout;
    private ImageView dotImage;

    private Thread thread;
    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case TCPServer.MessageTypeClass.SOCKET_CONNECTED:
                        String connectedMsg = (String) inputMessage.obj;
                        if (connectedMsg.isEmpty()) {
                            setStatus("Connected Client");
                        } else {
                            setStatus(connectedMsg);
                        }
                        break;

                    case TCPServer.MessageTypeClass.SOCKET_DATA:
                        String dataMsg = (String) inputMessage.obj;
                        setStatus("Recive Data: " + dataMsg);
                        Log.d("_LOG_", "Recive Data: " + dataMsg);
                        setTouchLayout(dataMsg);
                        break;

                    case TCPServer.MessageTypeClass.SOCKET_DISCONNECTED:
                        setStatus("Disconnected");
                }
            }
        };

        status = (TextView) findViewById(R.id.textView01);
        editPort = (EditText) findViewById(R.id.editText01);
        btn = (Button) findViewById(R.id.button01);
        touchLayout = (LinearLayout) findViewById(R.id.touchLayout);
        dotImage = (ImageView) findViewById(R.id.dotImage);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) {
                    if (socket != null) {
                        socket.setPaused();
                        isStart = false;
                        btn.setText("SERVER START");
                        dotImage.setX(0.0f);
                        dotImage.setY(0.0f);
                        dotImage.setBackgroundColor(getResources().getColor(R.color.colorNoting));
                    }
                } else {
                    if (!editPort.getText().toString().isEmpty()) {
                        port = Integer.parseInt(editPort.getText().toString());
                    }
                    socket = new TCPServer(port, mHandler);
                    socket.start();
                    isStart = true;
                    btn.setText("SERVER STOP");
                }
            }
        });

        touchLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dotImage.setBackgroundColor(getResources().getColor(R.color.colorPad));
                        dotImage.setX(event.getX());
                        dotImage.setY(event.getY());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        dotImage.setX(event.getX());
                        dotImage.setY(event.getY());
                        return true;
                    case MotionEvent.ACTION_UP:
                        dotImage.setBackgroundColor(getResources().getColor(R.color.colorNoting));
                        return true;
                }
                return false;
            }
        });

    }

    public void setStatus(String text) {
        status.setText(text);
    }

    public void setTouchLayout(String str) { // 전송 받은 터치 이벤트 입력

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int action;
        float x = 0.0f;
        float y = 0.0f;
        int metaState = 0;

        if (str == null)
            return;

        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(str, "/");

        action = Integer.parseInt(tokenizer.nextToken());
        x = Float.parseFloat(tokenizer.nextToken());
        y = Float.parseFloat(tokenizer.nextToken());

        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                metaState);

        touchLayout.dispatchTouchEvent(motionEvent);
    }
}