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
 import android.net.TrafficStats;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;

 public class MobileWidget extends AppWidgetProvider
 {
     private static Context context;
     private static AppWidgetManager appWidgetManager;
     private static RemoteViews remoteViews;
     private static ComponentName widget;
     private static Handler handler;
     private static Runnable runnable;
     private static ConnectivityManager connectivityManager;
     private static Intent service;
     private static PowerManager mPower;
     private static final String ACTION_CHANGE_MOBILE_DATA_STATUS="CHANGE_MOBILE_DATA_STATUS";

     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
     {
         super.onUpdate(context, appWidgetManager, appWidgetIds);
         this.context=context;
         this.appWidgetManager=appWidgetManager;
         remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_mobile);
         widget=new ComponentName(context,MobileWidget.class);
         mPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
         Intent intent=new Intent(context,MobileWidget.class);
         intent.setAction(ACTION_CHANGE_MOBILE_DATA_STATUS);
         PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,0);
         remoteViews.setOnClickPendingIntent(R.id.widget_mobile_status,pendingIntent);
         context.startService(service=new Intent(context,MobileNetworkLog.class));
     }

     @Override
     public void onDeleted(Context context, int[] appWidgetIds)
     {
         if(context!=null)
             try
             {
                 context.getApplicationContext().unregisterReceiver(MobileNetworkLog.mobileData);
                 context.stopService(service);
                 if(handler!=null&&runnable!=null)
                     handler.removeCallbacks(runnable);
             }
             catch (IllegalArgumentException exc)
             {

             }
         super.onDeleted(context, appWidgetIds);
     }

     @Override
     public void onReceive(Context context, Intent intent) {
         super.onReceive(context, intent);
         if (intent.getAction().equals(ACTION_CHANGE_MOBILE_DATA_STATUS)) {
             if(remoteViews==null||context==null||appWidgetManager==null)
                 return;
             try {
                 connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                 boolean active;
                 Method setMobileDataEnabledMethod;
                 if (Build.VERSION.SDK_INT < 21) {
                     try {
                         setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                     } catch (NoSuchMethodException exc) {
                         Class[] cArg = new Class[2];  //for cyano
                         cArg[0] = String.class;
                         cArg[1] = Boolean.TYPE;
                         setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                     }
                     if (connectivityManager == null || connectivityManager.getActiveNetworkInfo() == null)
                         connectivityManager = (ConnectivityManager) context.getSystemService(Context
                                 .CONNECTIVITY_SERVICE);
                     setMobileDataEnabledMethod.setAccessible(true);
                     if (connectivityManager.getActiveNetworkInfo() != null)
                         setMobileDataEnabledMethod.invoke(connectivityManager, active = connectivityManager.getActiveNetworkInfo()
                                 .isConnected() ? false : true);
                     else
                         setMobileDataEnabledMethod.invoke(connectivityManager, active = true);
                     Toast.makeText(context,active ? context.getString(R.string.mobile_on):context.getString(R.string.mobile_off), Toast.LENGTH_LONG)
                             .show();
                     try {
                         if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE)
                             switch (connectivityManager.getActiveNetworkInfo().getSubtype()) {
                                 case TelephonyManager.NETWORK_TYPE_1xRTT:
                                 case TelephonyManager.NETWORK_TYPE_CDMA:
                                 case TelephonyManager.NETWORK_TYPE_EDGE:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobilee);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_GPRS:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobileg);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_HSPA:
                                 case TelephonyManager.NETWORK_TYPE_UMTS:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobilehspa);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_EHRPD:
                                 case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                 case TelephonyManager.NETWORK_TYPE_HSDPA:
                                 case TelephonyManager.NETWORK_TYPE_HSUPA:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobile3g);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_HSPAP:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobilehspaplus);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_LTE:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobile4g);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                                 default:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobileoff);
                                     remoteViews.setTextViewText(R.id.widget_mobile_download, "");
                                     remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
                             }
                     } catch (NullPointerException exc) {
                         remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileoff);
                     }
                 }
                 else
                 {
                     TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                     setMobileDataEnabledMethod = Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                     setMobileDataEnabledMethod.invoke(tm, true);
                     if (connectivityManager == null || connectivityManager.getActiveNetworkInfo() == null)
                         connectivityManager = (ConnectivityManager) context.getSystemService(Context
                                 .CONNECTIVITY_SERVICE);
                     setMobileDataEnabledMethod.setAccessible(true);
                     if (connectivityManager.getActiveNetworkInfo() != null)
                         setMobileDataEnabledMethod.invoke(tm, active = connectivityManager.getActiveNetworkInfo().isConnected() ? false : true);
                     else
                         setMobileDataEnabledMethod.invoke(tm, active = true);
                     Toast.makeText(context,active ? context.getString(R.string.mobile_on):context.getString(R.string.mobile_off), Toast.LENGTH_LONG)
                             .show();
                     try {
                         if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE)
                             switch (connectivityManager.getActiveNetworkInfo().getSubtype()) {
                                 case TelephonyManager.NETWORK_TYPE_1xRTT:
                                 case TelephonyManager.NETWORK_TYPE_CDMA:
                                 case TelephonyManager.NETWORK_TYPE_EDGE:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilee);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_GPRS:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileg);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_HSPA:
                                 case TelephonyManager.NETWORK_TYPE_UMTS:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilehspa);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_EHRPD:
                                 case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                 case TelephonyManager.NETWORK_TYPE_HSDPA:
                                 case TelephonyManager.NETWORK_TYPE_HSUPA:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobile3g);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_HSPAP:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilehspaplus);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_LTE:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobile4g);
                                     break;
                                 case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                                 default:
                                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileoff);
                                     remoteViews.setTextViewText(R.id.widget_mobile_download, "");
                                     remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
                             }
                     } catch (NullPointerException exc) {
                         remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileoff);
                         remoteViews.setTextViewText(R.id.widget_mobile_download, "");
                         remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
                     }
                 }

             } catch (IllegalAccessException exc) {
                 exc.printStackTrace();
             } catch (InvocationTargetException _exc) {
                 setDataManually();
             } catch (NoSuchMethodException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException exc) {
                 exc.printStackTrace();
             } catch (SecurityException exc) {
                 setDataManually();
             }
         }
     }

     private void setDataManually()
     {
           Intent intent=new Intent();
           intent.setAction(android.provider.Settings.ACTION_SETTINGS);
           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.getApplicationContext().startActivity(intent);
           Toast.makeText(context,context.getString(R.string.mobile_msg), Toast.LENGTH_LONG).show();
     }

     public static class MobileNetworkLog extends Service
     {
         public static BroadcastReceiver mobileData;
         @Override
         public void onStart(Intent intent, int startId)
         {
             handler=new Handler();
             handler.postDelayed(runnable=new Runnable() {
                 @Override
                 public void run()
                 {
                     if(remoteViews==null||appWidgetManager==null||context==null)
                         return;
                     if (!mPower.isScreenOn())
                         handler.postDelayed(this,3000);
                     else
                     {
                         handler.removeCallbacks(runnable);
                         new NetworkStat().execute();
                     }
                 }
             },1000);
             mobileData=new BroadcastReceiver()
             {
                 @Override
                 public void onReceive(Context context,Intent intent)
                 {
                     if(remoteViews==null||appWidgetManager==null||context==null)
                         return;
                     try
                     {
                         final ConnectivityManager connMgr=(ConnectivityManager)context
                                 .getSystemService(Context.CONNECTIVITY_SERVICE);
                         if(connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable())
                             setMobileData();
                         else
                             remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobileoff);
                     }
                     catch(Exception exc)
                     {
                         remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobileoff);
                     }
                 }
             };
             if(context!=null)
                context.getApplicationContext().registerReceiver(mobileData, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
             setMobileData();
         }

         @Override
         public IBinder onBind(Intent intent) {
             return null;
         }

         private void setMobileData()
         {
             if(remoteViews==null||context==null||appWidgetManager==null)
                 return;
             try
             {
                 connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                 if(connectivityManager.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
                     switch(connectivityManager.getActiveNetworkInfo().getSubtype())
                     {
                         case TelephonyManager.NETWORK_TYPE_1xRTT:
                         case TelephonyManager.NETWORK_TYPE_CDMA:
                         case TelephonyManager.NETWORK_TYPE_EDGE:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilee); break;
                         case TelephonyManager.NETWORK_TYPE_GPRS:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileg); break;
                         case TelephonyManager.NETWORK_TYPE_HSPA:
                         case TelephonyManager.NETWORK_TYPE_UMTS:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilehspa); break;
                         case TelephonyManager.NETWORK_TYPE_EHRPD:
                         case TelephonyManager.NETWORK_TYPE_EVDO_B:
                         case TelephonyManager.NETWORK_TYPE_HSDPA:
                         case TelephonyManager.NETWORK_TYPE_HSUPA:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobile3g); break;
                         case TelephonyManager.NETWORK_TYPE_HSPAP:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobilehspaplus); break;
                         case TelephonyManager.NETWORK_TYPE_LTE:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobile4g); break;
                         case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                         default:
                             remoteViews.setImageViewResource(R.id.widget_mobile_status,R.drawable.mobileoff);
                             remoteViews.setTextViewText(R.id.widget_mobile_download,"");
                             remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
                     }
                 else
                 {
                     remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileoff);
                     remoteViews.setTextViewText(R.id.widget_mobile_download, "");
                     remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
                 }
             }
             catch(Exception exc)
             {
                 remoteViews.setImageViewResource(R.id.widget_mobile_status, R.drawable.mobileoff);
                 remoteViews.setTextViewText(R.id.widget_mobile_download, "");
                 remoteViews.setTextViewText(R.id.widget_mobile_upload, "");
             }
         }
     }

     private static class NetworkStat extends AsyncTask<Void,String,Long[]>
     {
         private String[] tmp=new String[2];
         private static long startRX=TrafficStats.getTotalRxBytes();
         private static long startTX=TrafficStats.getTotalTxBytes();

         @Override
         protected Long[] doInBackground(Void... params)
         {
             try
             {
                 if(connectivityManager.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
                 {
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
                     Log.i("Stat", tmp[0] + " " + tmp[1]);
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

             return new Long[]{startRX,startTX};
         }

         @Override
         protected void onProgressUpdate(String... values)
         {
             remoteViews.setTextViewText(R.id.widget_mobile_download,values[0]);
             remoteViews.setTextViewText(R.id.widget_mobile_upload,values[1]);
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
 }
