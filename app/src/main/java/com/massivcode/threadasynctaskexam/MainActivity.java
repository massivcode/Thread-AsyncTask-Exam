package com.massivcode.threadasynctaskexam;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView mCountTextView = null;
    CountDownTimer mCountDownTimer = null;

    class TestCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public TestCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // 매번 틱마다 남은 초를 출력한다.
            mCountTextView.setText(millisUntilFinished/1000 + " 초");
        }

        @Override
        public void onFinish() {
            // 카운트다운이 완료된 경우 카운트다운의 최종 초를 출력한다.
            mCountTextView.setText("0 초");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_timer);

        // 1. 레이아웃을 액티비티에 반영 및 텍스트뷰 객체를 얻어 온다.
        mCountTextView = (TextView)findViewById(R.id.countdown_text);

        // 2. 총 60초 동안 1초씩 카운트다운 객체를 생성한다.
        mCountDownTimer = new TestCountDownTimer(60000, 1000);

        // 3. 카운트다운 초깃값을 출력한다.
        mCountTextView.setText("60 초");

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_countdown_btn: {
                // 총 60초 카운트다운을 시작한다.
                mCountDownTimer.start();
                break;
            }
            case R.id.reset_countdown_btn: {
                // 카운트다운을 중단하고 초를 리셋한다.
                mCountDownTimer.cancel();
                mCountTextView.setText("60 초");
                break;
            }
        }

    }

}
