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

    // 1. 메시지 큐에 메시지를 추가하기 위한 핸들러를 생성한다.
    Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCountTextView = (TextView) findViewById(R.id.count_textview);

        // 2. 10초 동안 1초에 1씩 카운트 하는 스레드 생성 및 시작
        Thread workerThread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    mCount++;

                    // 3. 실행 코드가 담긴 Runnable 객체를 하나 생성한다.
                    Runnable callback = new Runnable() {
                        @Override
                        public void run() {
                            // 현재까지 카운트한 수치를 텍스트 뷰에 출력한다.
                            mCountTextView.setText("Count : " + mCount);
                        }
                    };

                    // 4. 메시지 큐에 담을 메시지 하나를 생성한다. 생성 시 Runnable 객체를 생성자로 전달한다.
                    Message message = Message.obtain(mHandler, callback);

                    // 5. 핸들러를 통해 메시지를 메시지 큐로 보낸다.
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
