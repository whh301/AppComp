package com.spirent.landslide.datagen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BenchMark extends AppCompatActivity {
    final int RIGHT = 0;
    final int LEFT = 1;
    private GestureDetector gestureDetector;
    private Handler mHandler;

    private HttpBenchMark httpThread = null;
    private UdpSendThread udpSendThread;
    private UdpReceiveThread udpReceiveThread;

    // HTTP Bench Mark
    private TextView httpReqUri;
    private TextView httpBytesRcvd;
    private TextView httpBps;
    private Button btnHttpBench;

    // UDP numbers
    private TextView udpPksSent;
    private TextView udpPksRcvd;
    private TextView udpBytesSent;
    private TextView udpBytesRcvd;
    private TextView udpBps;
    private Button btnUdpBench;
    private Button btnUdpPassive;
    private UdpBenchData udpBenchData;

    private static String targetUrl = "www.sina.com.cn";
    private static int udpLocalPort = 2003;
    private static int udpRemotePort = 2003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bench_mark);
        gestureDetector = new GestureDetector(BenchMark.this,onGestureListener);
        mHandler = new Handler();
        mHandler.post(new TimerProcess());

        httpBytesRcvd = (TextView) findViewById(R.id.httpBytesRcved);
        httpBps = (TextView) findViewById(R.id.txtHttpBps);
        httpReqUri = (TextView) findViewById(R.id.txtReqUri);
        httpReqUri.setText("Target URL:" + targetUrl);
        btnHttpBench = (Button) findViewById(R.id.btnHttp);
        btnHttpBench.setOnClickListener(httpClickListener);

        // UDP Benchmark numbers
        udpPksSent = (TextView) findViewById(R.id.txtPksSent);
        udpPksRcvd = (TextView) findViewById(R.id.txtPksRcvd);
        udpBytesSent = (TextView) findViewById(R.id.txtUdpSent);
        udpBytesRcvd = (TextView) findViewById(R.id.udpRcvd);
        udpBps = (TextView) findViewById(R.id.udpMark);

        udpBenchData = new UdpBenchData();
        httpThread = new HttpBenchMark();
        udpSendThread = new UdpSendThread();
        udpReceiveThread = new UdpReceiveThread();
    }

    private View.OnClickListener httpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (httpThread != null) {
                    httpThread.join();
                }
                httpThread = new HttpBenchMark();
                httpThread.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private class UdpBenchData {
        private int packetsRcved = 0;
        private int packetsSend = 0;
        private int bytesReceived = 0;
        private int bytesSent = 0;
        private double currBps = 0;
        private long startTime = 0;

        public void markStartTime()
        {
            startTime = System.currentTimeMillis();
        }

        public synchronized void handlePacketReceived(int length) {
            packetsRcved++;
            bytesReceived = bytesReceived + length;
            calculateBps();
        }

        public synchronized void handlePacketSent(int length) {
            packetsSend++;
            bytesSent = bytesSent + length;
            calculateBps();
        }

        private void calculateBps() {
            long currTime = System.currentTimeMillis();
            long benchTime = currTime - startTime;
            if (benchTime > 0) {
                currBps = (bytesSent + bytesReceived) * 8.0 / benchTime * 1000;
            }
        }

        public int getPacketsRcved() {
            return packetsRcved;
        }

        public int getPacketsSend() {
            return packetsSend;
        }

        public int getBytesReceived() {
            return bytesReceived;
        }

        public int getBytesSent() {
            return bytesSent;
        }

        public double getCurrBps() {
            return currBps;
        }
    }

    private class TimerProcess implements Runnable{
        public void run() {
            // get the progress of bench mark and show to GUI
            showBenchMark();
            mHandler.postDelayed(this, 500);
        }
    }

    private void showBenchMark()
    {
        if (httpThread != null) {
            httpBytesRcvd.setText(httpThread.getBytesReceived() + "");
            httpBps.setText(httpThread.getHttpBps() + "");
        }
    }

    private class HttpBenchMark extends Thread {
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

            HttpURLConnection connection=null;
            try {
                URL url = new URL("http://www.baidu.com");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while (null != (line = reader.readLine())) {
                    httpDataReceived(line.length());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private class UdpSendThread extends Thread {

        public void run() {

        }
    }

    private class UdpReceiveThread extends Thread {
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
