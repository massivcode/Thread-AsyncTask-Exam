package com.massivcode.threadasynctaskexam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

        for(int i = 0; i < 9 ; i++) {
            mCount++;

            Log.i(TAG, "Current Count : " + mCount);
            // 수행되는 동안 UI가 멈춰있다.
            // UI 스레드에서 시간이 오래 걸리는 작업을 해서는 안된다.
            // 메인 스레드 == UI 스레드
            mCountTextView.setText("Count : " + mCount);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }
}
