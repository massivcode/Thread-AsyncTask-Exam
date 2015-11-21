package com.massivcode.threadasynctaskexam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int mCount = 0;
    TextView mCountTextView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCountTextView = (TextView) findViewById(R.id.count_textview);

        Thread workerThread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 10; i++) {
                    mCount++;

                    Log.i(TAG, "Current Count : " + mCount);
                    mCountTextView.setText("Count : " + mCount);

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
