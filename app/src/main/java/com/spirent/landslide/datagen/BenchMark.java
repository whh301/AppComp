package com.spirent.landslide.datagen;

<<<<<<< HEAD
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Bundle;
=======
>>>>>>> origin/master
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class BenchMark extends AppCompatActivity {
    final int RIGHT = 0;
    final int LEFT = 1;
    private GestureDetector gestureDetector;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bench_mark);
        gestureDetector = new GestureDetector(BenchMark.this,onGestureListener);
        mHandler = new Handler();
        mHandler.post(new TimerProcess());
    }

    private class TimerProcess implements Runnable{
        public void run() {
            // get the progress of bench mark and show to GUI
            mHandler.postDelayed(this, 500);
        }
    }

    private class HttpBenchMark extends Thread {
<<<<<<< HEAD
        //Start Time
        private long httpStartTime = 0;
        private int bytesReceived = 0;
        private double currBps = 0;

        public int getBytesReceived()
        {
            return bytesReceived;
        }

        public int getHttpBps()
        {
            return (int) currBps;
        }

        private void httpDataReceived(int bytes)
        {
            bytesReceived = bytesReceived + bytes;
            long currTime  = System.currentTimeMillis();
            long httpTime = currTime - httpStartTime;
            if (httpTime > 0)
            {
                currBps = (bytesReceived * 8.0) / httpTime * 1000.0;
            }
        }

        public void run() {
            // Mark Start Time
            httpStartTime = System.currentTimeMillis();
            bytesReceived = 0;
            currBps = 0;

            // TODO Auto-generated method stub
            HttpURLConnection connection=null;

            try {
                URL url=new URL("http://www.baidu.com");
                connection =(HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in=connection.getInputStream();
                //下面对获取到的输入流进行读取
                BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                StringBuilder response=new StringBuilder();
                String line;
                while((line=reader.readLine())!=null) {
                    bytesReceived += line.length();
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(connection!=null){
                    connection.disconnect();
                }
            }
        }
    }

    private class UdpSendThread extends Thread {
=======
>>>>>>> origin/master
        public void run() {

        }
    }

    private class UdpBenchMark extends Thread {
        public void run() {

        }
    }

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();

                    if (x > 0) {
                        doResult(RIGHT);
                    } else if (x < 0) {
                        doResult(LEFT);
                    }
                    return true;
                }
            };

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void doResult(int action) {
        switch (action) {
            case RIGHT:
                // System.out.println("go right");
                Intent next = new Intent(BenchMark.this, MainActivity.class);
                BenchMark.this.startActivity(next);
                BenchMark.this.finish();
                break;
            case LEFT:
                // System.out.println("go left");
                break;
        }
    }
}
