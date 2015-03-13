package com.dev.system.monitor;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WifiWidget extends AppWidgetProvider
{
    private static Context context;
    private static AppWidgetManager appWidgetManager;
    private static RemoteViews remoteViews;
    private static ComponentName widget;
    private static Handler handler;
    private static Runnable runnable;
    private static WifiManager wifiManager;
    private static WifiInfo wifiInfo;
    private static RSSIBroadcastReceiverTask rssiBroadcastReceiverTask;
    private static Intent service;
    private static PowerManager mPower;
    private static ConnectivityManager connManager;
    private static NetworkInfo wifi;
    public static final String ACTION_CHANGE_WIFI_STATUS="CHANGE_WIFI_STATUS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.context=context;
        this.appWidgetManager=appWidgetManager;
        connManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_wifi);
        widget=new ComponentName(context,WifiWidget.class);
        mPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        rssiBroadcastReceiverTask=new RSSIBroadcastReceiverTask();
        Intent intent=new Intent(context,WifiWidget.class);
        intent.setAction(ACTION_CHANGE_WIFI_STATUS);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,0);
        remoteViews.setOnClickPendingIntent(R.id.widget_wifi_status,pendingIntent);
        context.startService(service=new Intent(context,NetworkLog.class));
    }

    @Override
    public void onReceive(Context context,Intent intent)
    {
        super.onReceive(context, intent);
        if(remoteViews==null||context==null||appWidgetManager==null)
            return;
        if(intent.getAction().equals(ACTION_CHANGE_WIFI_STATUS))
        {
            try
            {
                boolean stat;
                wifiManager.setWifiEnabled(stat=!wifiManager.isWifiEnabled());
                if(!stat&&remoteViews!=null&&context!=null)
                {
                    remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_off);
                    Toast.makeText(context,context.getString(R.string.wifi_off),Toast.LENGTH_LONG).show();
                    remoteViews.setTextViewText(R.id.widget_wifi_download,"");
                    remoteViews.setTextViewText(R.id.widget_wifi_upload,"");
                }
                else if(remoteViews!=null&&context!=null)
                {
                    Toast.makeText(context,context.getString(R.string.wifi_on),Toast.LENGTH_LONG).show();
                    wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    wifiInfo=wifiManager.getConnectionInfo();
                    switch(WifiManager.calculateSignalLevel(wifiInfo.getRssi(),3))
                    {
                        case 0: remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_low); break;
                        case 1:	remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_med); break;
                        case 2:	remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_full); break;
                    }
                }
            }
            catch(Exception exc)
            {

            }
            if(appWidgetManager!=null&&remoteViews!=null)
                appWidgetManager.updateAppWidget(widget,remoteViews);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
        if(context!=null)
            try
            {
                context.getApplicationContext().unregisterReceiver(NetworkLog.broadcastReceiver);
                context.stopService(service);
            }
            catch (IllegalArgumentException exc)
            {

            }
        super.onDeleted(context, appWidgetIds);
    }

    public static class NetworkLog extends Service
    {
        private Long startRX,startTX;
        public static BroadcastReceiver broadcastReceiver;
        @Override
        public void onStart(Intent intent, int startId)
        {
          if(context!=null)
            context.getApplicationContext().registerReceiver(broadcastReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(remoteViews==null||context==null||appWidgetManager==null)
                        return;
                    try {
                        rssiBroadcastReceiverTask.execute();
                        rssiBroadcastReceiverTask=new RSSIBroadcastReceiverTask();
                    } catch (NullPointerException exc) {

                    }
                }
            }, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
            startRX=new Long(TrafficStats.getTotalRxBytes());
            startTX=new Long(TrafficStats.getTotalTxBytes());
            handler=new Handler();
            handler.postDelayed(runnable=new Runnable() {
                @Override
                public void run() {
                    if (remoteViews==null||context==null||appWidgetManager==null)
                        return;
                    if (!mPower.isScreenOn())
                        handler.postDelayed(this,3000);
                    else
                    {
                        handler.removeCallbacks(runnable);
                        new NetworkStat().execute(new Long[]{startRX,startTX});
                    }
                }
            },1000);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    private static class NetworkStat extends AsyncTask<Long,String,Long[]>
    {
        private String[] tmp=new String[2];
        private static long startRX=TrafficStats.getTotalRxBytes();
        private static long startTX=TrafficStats.getTotalTxBytes();
        @Override
        protected Long[] doInBackground(Long[] params)
        {
            try
            {
                if (wifi.isConnected()&&remoteViews != null) {
                    long rxBytes=TrafficStats.getTotalRxBytes()-startRX;
                    if (rxBytes / (1024 * 1024 * 1024) > 0)
                        tmp[0]=(rxBytes/(1024 * 1024 * 1024)+" GB/s");
                    else if (rxBytes / (1024 * 1024) > 0)
                        tmp[0]=(rxBytes / (1024 * 1024) + " MB/s");
                    else if (rxBytes / 1024 > 0)
                        tmp[0]=(rxBytes/(1024)+" KB/s");
                    else
                        tmp[0]=rxBytes+" B/s";
                    long txBytes=TrafficStats.getTotalTxBytes()-startTX;
                    startRX+=rxBytes;
                    startTX+=txBytes;
                    if (txBytes / (1024 * 1024 * 1024) > 0)
                        tmp[1]=(txBytes/(1024 * 1024 * 1024)+" GB/s");
                    else if (txBytes / (1024 * 1024) > 0)
                        tmp[1]=(txBytes/(1024 * 1024)+" MB/s");
                    else if (txBytes / 1024 > 0)
                        tmp[1]=(txBytes/(1024)+" KB/s");
                    else
                        tmp[1]=txBytes+" B/s";
                    publishProgress(tmp);
                }
            }
            catch (NullPointerException exc)
            {
                //if caught, due to no data avaiable yet or no network connected to :/
                if (remoteViews!=null)
                {
                    tmp[0]="";
                    tmp[1]="";
                    publishProgress(tmp);
                }
            }

            return params;
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            remoteViews.setTextViewText(R.id.widget_wifi_download,values[0]);
            remoteViews.setTextViewText(R.id.widget_wifi_upload,values[1]);
        }

        @Override
        protected void onPostExecute(Long[] params)
        {
            if (appWidgetManager!=null&&remoteViews!=null)
                appWidgetManager.updateAppWidget(widget, remoteViews);
            if (params[0]!=TrafficStats.UNSUPPORTED&&params[1]!=TrafficStats.UNSUPPORTED)
            {
                if (remoteViews!=null&&appWidgetManager!=null&&context!=null)
                    handler.postDelayed(runnable,1000);
            }
        }
    }

    private static class RSSIBroadcastReceiverTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            wifiInfo=wifiManager.getConnectionInfo();
            connManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            ConnectivityManager connManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(wifi!=null&&wifi.isConnected()&&wifiManager.isWifiEnabled()&&remoteViews!=null)
            {
                switch (WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 3))
                {
                    case 0:
                        remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_low);
                        break;
                    case 1:
                        remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_med);
                        break;
                    case 2:
                        remoteViews.setImageViewResource(R.id.widget_wifi_status, R.drawable.wifi_full);
                        break;
                }
            }
            else if(remoteViews!=null)
            {
                remoteViews.setImageViewResource(R.id.widget_wifi_status,R.drawable.wifi_off);
                remoteViews.setTextViewText(R.id.widget_wifi_download,"");
                remoteViews.setTextViewText(R.id.widget_wifi_upload,"");
            }
            if(remoteViews!=null&&appWidgetManager!=null)
                appWidgetManager.updateAppWidget(widget,remoteViews);
        }
    }
}
