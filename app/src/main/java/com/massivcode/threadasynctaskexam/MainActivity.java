package com.massivcode.threadasynctaskexam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int mCount = 0;
    TextView mCountTextView = null;

    // 해당 메시지가 무엇을 처리하는지 구분하기 위한 상수
    static final private int MESSAGE_DRAW_CONTENT_COUNT = 1;

    // 1. 메시지 큐에 메시지를 추가하기 위한 핸들러를 생성한다.
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_DRAW_CONTENT_COUNT: {
                    int currentCount = msg.arg1;
                    TextView countTextView = (TextView)msg.obj;

                    countTextView.setText("Count : " + currentCount);
                    break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCountTextView = (TextView) findViewById(R.id.count_textview);

        // 10초 동안 1초에 1씩 카운트하는 스레드 생성 및 시작
        Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    mCount++;

                    // 2. 메시지 큐에 담을 메시지를 하나 생성한다.
                    Message message = Message.obtain(mHandler);

                    // 3. 핸들러의 handleMessage로 전달할 값들을 설정한다.

                    // 무엇을 실행하는 메시지인지 구분하기 위해 구분자 설정
                    message.what = MESSAGE_DRAW_CONTENT_COUNT;
                    // 메시지가 실행될 때 참조하는 int형 데이터 설정
                    message.arg1 = mCount;
                    // 메시지가 실행될 때 참조하는 Object형 데이터 설정
                    message.obj = mCountTextView;

                    // 핸들러를 통해 메시지를 메시지 큐로 보낸다.
                    mHandler.sendMessage(message);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        workerThread.start();

    }

    public void onClick(View view) {
        // 현재까지 카운트한 수치를 텍스트 뷰에 출력한다.
        // ==========================================
        mCountTextView.setText("Count : " + mCount);
        // ==========================================
    }
}
