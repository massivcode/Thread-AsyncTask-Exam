package com.massivcode.threadasynctaskexam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView m100MsCountTv = null;
    TextView m1000MsCountTv = null;

    Timer mTimer = new Timer();

    TimerTask m100MsCountTimerTask = null;
    TimerTask m1000MsCountTimerTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        m100MsCountTv = (TextView)findViewById(R.id.ms_100_countdown_text);
        m1000MsCountTv = (TextView)findViewById(R.id.ms_1000_countdown_text);
    }

    @Override
    protected void onDestroy() {

        // 타이머는 작업 스레드이기 때문에 액티비티가 종료될 때
        // 반드시 중단하여 스레드를 제거시키도록 한다.
        mTimer.cancel();

        super.onDestroy();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_countdown_btn: {
                startTimerTask();
                break;
            }
            case R.id.reset_countdown_btn: {
                stopTimerTask();
                break;

            }
        }
    }

    private void stopTimerTask() {

        // 1. 모든 태스크를 중단한다.
        if(m100MsCountTimerTask != null || m1000MsCountTimerTask != null) {
            m100MsCountTimerTask.cancel();
            m100MsCountTimerTask = null;

            m1000MsCountTimerTask.cancel();
            m1000MsCountTimerTask = null;
        }

        // 2. 카운팅 초기화값을 텍스트뷰에 출력한다.
        m100MsCountTv.setText("100MS Count : 0");
        m1000MsCountTv.setText("1000MS Count : 0");

    }

    private void startTimerTask() {

        // 1. TimerTask 실행 중이라면 중단한다.
        stopTimerTask();

        // 2. 새로운 TimerTask를 생성한다.

        // 1) 0.1초마다 카운팅되는 태스크를 등록한다.
        m100MsCountTimerTask = new TimerTask() {
            int mCount = 0;

            @Override
            public void run() {
                mCount++;

                m100MsCountTv.post(new Runnable() {
                    @Override
                    public void run() {
                        m100MsCountTv.setText("100MS Count : " + mCount);
                    }
                });
            }
        };

        // 2) 1초마다 카운팅되는 태스크를 등록한다.
        m1000MsCountTimerTask = new TimerTask() {
            int mCount = 0;

            @Override
            public void run() {
                mCount++;
                m1000MsCountTv.post(new Runnable() {
                    @Override
                    public void run() {
                        m1000MsCountTv.setText("1000MS Count : " + mCount);
                    }
                });
            }
        };

        // 3. TimerTask를 Timer를 통해 실행시킨다.

        // 1) 즉시 타이머를 구동하고 100밀리세컨드 단위로 반복하라.
        mTimer.schedule(m100MsCountTimerTask, 0, 100);

        // 2) 1초 후에 타이머를 구동하고 1000밀리세컨드 단위로 반복하라.
        mTimer.schedule(m1000MsCountTimerTask, 1000, 1000);

    }

}
