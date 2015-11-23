package com.massivcode.threadasynctaskexam;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView mDownloadStateTextView = null;
    FileDownloadTask mFileDownloadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);

        mDownloadStateTextView = (TextView)findViewById(R.id.download_state_textview);

        mFileDownloadTask = new FileDownloadTask();
        mFileDownloadTask.execute("FileUrl_1", "FileUrl_2", "FileUrl_3" , "FileUrl_4", "FileUrl_5" , "FileUrl_6", "FileUrl_7" , "FileUrl_8", "FileUrl_9", "FileUrl_10");


    }

    public void onClick(View view) {

        if(mFileDownloadTask != null && mFileDownloadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mFileDownloadTask.cancel(true);
        }

    }

    private class FileDownloadTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            // 최초 화면에 내려받기 시도를 알리는 텍스트를 출력한다.
            mDownloadStateTextView.setText("FileDownload...");
        }

        @Override
        protected Boolean doInBackground(String... downloadInfos) {
            int totalCount = downloadInfos.length;

            for(int i = 1; i <= totalCount; i++) {
                // 1. 파일 내려받기 처리 상태를 표시하기 위해 호출
                publishProgress(i, totalCount);

                if(isCancelled() == true) {
                    Log.d(TAG, "isCancelled()");
                    return false;
                }

                // 2. 아래를 파일을 내려받는 과정이라고 가정한다.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "InterruptedException");
                    return false;
                }

            }


            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... downloadInfos) {
            int currentCount = downloadInfos[0];
            int totalCount = downloadInfos[1];

            // 현재의 파일 내려받기 상태를 표시한다. 예) Downloading: 3/10
            mDownloadStateTextView.setText("Downloading : " + currentCount + "/" + totalCount);

        }

        @Override
        protected void onCancelled() {
            // 화면에 내려받기가 취소되었다는 텍스트를 출력한다.
            mDownloadStateTextView.setText("Download cancelled");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // 화면에 내려받기 성공/실패 여부를 텍스트로 출력한다.
            if(true == result) {
                mDownloadStateTextView.setText("Download Finished");
            } else {
                mDownloadStateTextView.setText("Download Failed");
            }
        }
    }
}
