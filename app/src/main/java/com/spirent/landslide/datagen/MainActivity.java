package com.spirent.landslide.datagen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends AppCompatActivity {
    final int RIGHT = 0;
    final int LEFT = 1;
    private GestureDetector gestureDetector;

    // Cell Information
    private TelephonyManager telephonyManager;
    private TextView mLabelCarrier;
    private TextView mLabelLocation;
    private TextView mLabelNetwork;
    private TextView mLabelDataInfo;
    private TextView mLabelCellId;

    //MyPhoneStateListener类的对象，即设置一个监听器对象
    MyPhoneStateListener phoneListener;

    private IntentFilter mWifiIntentFilter;
    private BroadcastReceiver mWifiIntentReceiver;
    private TextView mLabelWifi;
    private TextView mStatusWiFi;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLabelCarrier = (TextView)findViewById(R.id.txtCarrierInfo);
        mLabelLocation = (TextView)findViewById(R.id.lblLocalInfo);
        mLabelNetwork = (TextView)findViewById(R.id.txtNetworkInfo);
        mLabelDataInfo = (TextView)findViewById(R.id.textDataInfo);
        mLabelCellId = (TextView)findViewById(R.id.lblCellId);
        // Get Cell Id and etc
        telephonyManager =(TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        mLabelCarrier.setText(telephonyManager.getNetworkOperatorName());
        mLabelLocation.setText(telephonyManager.getNetworkCountryIso());

        if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
        {
            String dataInfo = "IP Address:" + getLocalIpAddress() + "\n";
            dataInfo = dataInfo + "DNS:" + getLocalDNS() + "\n";
            mLabelDataInfo.setText(dataInfo);
        }
        else
        {
            mLabelDataInfo.setText("Data Not Connected!");
        }

        phoneListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        // WiFi status update
        mLabelWifi = (TextView)findViewById(R.id.Label_WifiDetail);
        mStatusWiFi = (TextView)findViewById(R.id.lblWiFiState);
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        mWifiIntentReceiver = new WifiIntentReceiver();
        registerReceiver(mWifiIntentReceiver, mWifiIntentFilter);

        mHandler = new Handler();
        mHandler.post(new TimerProcess());

        gestureDetector = new GestureDetector(MainActivity.this,onGestureListener);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
    }

    //监听器类
    private class MyPhoneStateListener extends PhoneStateListener{
        /*得到信号的强度由每个tiome供应商,有更新*/
        TextView txtCdma = (TextView)findViewById(R.id.lblCdmaSignal);
        TextView txtGsm = (TextView)findViewById(R.id.lblGsmSignal);
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength){
            //调用超类的该方法，在网络信号变化时得到回答信号
            super.onSignalStrengthsChanged(signalStrength);

            //cinr：Carrier to Interference plus Noise Ratio（载波与干扰和噪声比）
            txtCdma.setText("RSSI = "+ String.valueOf(signalStrength.getCdmaDbm()));
            txtGsm.setText("Cinr = "+ String.valueOf(signalStrength.getGsmSignalStrength()));
        }
    }

    private class TimerProcess implements Runnable{
        public void run() {
            WifiManager wifiInfo = (WifiManager) getSystemService(WIFI_SERVICE);
            TextView txtWiFiList = (TextView) findViewById(R.id.txtWiFiList);
            if (wifiInfo.getWifiState() == WIFI_STATE_ENABLED) {
                showWIFIDetail();
            }
            else
            {
                txtWiFiList.setText("");
                mLabelWifi.setText("");
            }

            int netWorkType = telephonyManager.getPhoneType();
            String cellId = "";
            try {
                if (netWorkType == TelephonyManager.PHONE_TYPE_CDMA) {
                    CdmaCellLocation location = (CdmaCellLocation) telephonyManager.getCellLocation();
                    cellId = "" + location.getBaseStationId();
                } else {
                    GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
                    cellId = "" + location.getCid();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mLabelCellId.setText(cellId);

            int ntwkType=telephonyManager.getNetworkType();
            String ntwkTypeStr="ERR";
            switch (ntwkType) {
                case 0: ntwkTypeStr = "UNKNOWN";
                    break;
                case 1: ntwkTypeStr = "GPRS";
                    break;
                case 2: ntwkTypeStr = "EDGE";
                    break;
                case 3: ntwkTypeStr = "UMTS";
                    break;
                case 4: ntwkTypeStr = "CDMA";
                    break;
                case 5: ntwkTypeStr = "EVDO_0";
                    break;
                case 6: ntwkTypeStr = "EVDO_A";
                    break;
                case 7: ntwkTypeStr = "1xRTT";
                    break;
                case 8: ntwkTypeStr = "HSDPA";
                    break;
                case 9: ntwkTypeStr = "HSUPA";
                    break;
                case 10: ntwkTypeStr = "HSPA";
                    break;
                case 11: ntwkTypeStr = "IDEN";
                    break;
                case 12: ntwkTypeStr = "EVDO_B";
                    break;
                case 13: ntwkTypeStr = "LTE";
                    break;
                case 14: ntwkTypeStr = "EHRPD";
                    break;
                case 15: ntwkTypeStr = "HSPAP";
                    break;
                case 16: ntwkTypeStr = "GSM";
                    break;
                case 17: ntwkTypeStr = "TD_SCDMA";
                    break;
                case 18: ntwkTypeStr = "IWLAN";
                    break;
            }

            mLabelNetwork.setText(ntwkTypeStr);

            mLabelCarrier.setText(telephonyManager.getNetworkOperatorName());
            mLabelLocation.setText(telephonyManager.getNetworkCountryIso());

            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
            {
                String dataInfo = "IP Address:" + getLocalIpAddress() + "\n";
                dataInfo = dataInfo + "DNS:" + getLocalDNS() + "\n";
                dataInfo = dataInfo + "Network Type:" + ntwkTypeStr + "\n";
                mLabelDataInfo.setText(dataInfo);
            }
            else
            {
                mLabelDataInfo.setText("");
            }

            mHandler.postDelayed(this, 500);
        }
    }

    public String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private String getLocalDNS(){
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop net.dns1");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally{
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }

    public void showWIFIDetail()
    {
        WifiInfo info = ((WifiManager)getSystemService(WIFI_SERVICE)).getConnectionInfo();
         /*
         info.getBSSID()；      获取BSSID地址。
         info.getSSID()；       获取SSID地址。  需要连接网络的ID
         info.getIpAddress()；  获取IP地址。4字节Int, XXX.XXX.XXX.XXX 每个XXX为一个字节
         info.getMacAddress()； 获取MAC地址。
         info.getNetworkId()；  获取网络ID。
         info.getLinkSpeed()；  获取连接速度，可以让用户获知这一信息。
         info.getRssi()；       获取RSSI，RSSI就是接受信号强度指示
          */
        int Ip = info.getIpAddress();
        DhcpInfo dhcpInfo = ((WifiManager)getSystemService(WIFI_SERVICE)).getDhcpInfo();
        String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);
        int dnsIp = dhcpInfo.dns1;
        String strDnsIp = "" + (dnsIp & 0xFF) + "." + ((dnsIp >> 8) & 0xFF) + "." + ((dnsIp >> 16) & 0xFF) + "." + ((dnsIp >> 24) & 0xFF);
        mLabelWifi.setText("BSSID : " + info.getBSSID() + "\nSSID : " + info.getSSID() +
                "\nIpAddress : " + strIp +  "\nDNSAddress : " + strDnsIp + "\nMacAddress : " + info.getMacAddress() +
                "\nNetworkId : " + info.getNetworkId() + "\nLinkSpeed : " + info.getLinkSpeed() + "Mbps" +
                "\nRssi : " + info.getRssi());
        // All WiFi list
        TextView txtWiFiList = (TextView) findViewById(R.id.txtWiFiList);
        List<ScanResult> list = ((WifiManager)getSystemService(WIFI_SERVICE)).getScanResults();
        if (list != null)
        {
            String wifiList = "";
            for (int i=0; i<list.size(); i++)
            {
                ScanResult result = list.get(i);
                wifiList = wifiList + "SSID: " + result.SSID + "\n   BSSID: " + result.BSSID + "\n   Signal: " + result.level + "\n";

                if (i >= 2)
                    break;
            }

            txtWiFiList.setText(wifiList);
        }
        else
        {
            txtWiFiList.setText("");
        }

    }

    private class WifiIntentReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent) {
            WifiInfo info = ((WifiManager)getSystemService(WIFI_SERVICE)).getConnectionInfo();
             /*
             WifiManager.WIFI_STATE_DISABLING   正在停止
             WifiManager.WIFI_STATE_DISABLED    已停止
             WifiManager.WIFI_STATE_ENABLING    正在打开
             WifiManager.WIFI_STATE_ENABLED     已开启
             WifiManager.WIFI_STATE_UNKNOWN     未知
              */
            switch (intent.getIntExtra("wifi_state", 0)) {
                case WifiManager.WIFI_STATE_DISABLING:
                    mStatusWiFi.setText("OFF");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    mStatusWiFi.setText("OFF");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    mStatusWiFi.setText("OFF");
                    break;
                case WIFI_STATE_ENABLED:
                    mStatusWiFi.setText("ON");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    mStatusWiFi.setText("UNKNOWN");
                    break;
            }
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
                break;
            case LEFT:
                // System.out.println("go left");
                Intent next = new Intent(MainActivity.this, BenchMark.class);
                MainActivity.this.startActivity(next);
                MainActivity.this.finish();
                break;
        }
    }
}
