 package com.dev.system.monitor;

 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.BatteryManager;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.widget.RemoteViews;
 import com.echo.holographlibrary.PieGraph;
 import com.echo.holographlibrary.PieSlice;

 public class BatteryWidget extends AppWidgetProvider
 {
     private static Context context;
     private static AppWidgetManager appWidgetManager;
     private static RemoteViews remoteViews;
     private static ComponentName widget;
     private static Intent service;
     private static PowerManager mPower;
     @Override
     public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
     {
         super.onUpdate(context,appWidgetManager,appWidgetIds);
         this.context=context;
         this.appWidgetManager=appWidgetManager;
         remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_battery);
         widget=new ComponentName(context,BatteryWidget.class);
         mPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
         context.startService(service=new Intent(context,UpdateBattery.class));
     }

     public static class UpdateBattery extends Service
     {
         public static BroadcastReceiver broadcastReceiver;
         private PieGraph pieGraph;

         @Override
         public void onStart(Intent intent, int startId)
         {
             broadcastReceiver=new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     if(remoteViews==null||context==null||appWidgetManager==null||!mPower.isScreenOn())
                         return;
                     int batteryPct=intent.getIntExtra("level",100);
                     PieSlice slice=new PieSlice(),_slice=new PieSlice();
                     slice.setColor(Color.parseColor("#99CC00"));
                     slice.setValue(batteryPct);
                     _slice.setColor(Color.parseColor("#0099CC00"));
                     _slice.setValue(100 - batteryPct + 0.00001f);
                     pieGraph=new PieGraph(context);
                     pieGraph.measure(200, 200);
                     pieGraph.layout(0, 0, 200, 200);
                     pieGraph.setDrawingCacheEnabled(true);
                     pieGraph.addSlice(slice);
                     pieGraph.addSlice(_slice);
                     pieGraph.setInnerCircleRatio(150);
                     for (PieSlice s : pieGraph.getSlices())
                         s.setGoalValue(s.getValue());
                     pieGraph.setDuration(1000);
                     pieGraph.setInterpolator(new AccelerateDecelerateInterpolator());
                     pieGraph.animateToGoalValues();
                     pieGraph.setPadding(3);
                     Bitmap bitmap=pieGraph.getDrawingCache();
                     Canvas canvas=new Canvas(bitmap);
                     Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
                     paint.setColor(Color.BLACK);
                     paint.setTextSize(18);
                     paint.setShadowLayer(1f,0f,1f,Color.WHITE);
                     Rect bounds=new Rect();
                     paint.getTextBounds(batteryPct+" %",0,new String(batteryPct+" %").length(),bounds);
                     int x=(bitmap.getWidth()-bounds.width())/2;
                     int y=(bitmap.getHeight()+bounds.height())/2;
                     canvas.drawText(batteryPct+" %",x,y,paint);
                     if(remoteViews!=null&&appWidgetManager!=null&&context!=null) {
                         remoteViews.setImageViewBitmap(R.id.widget_battery_graph, bitmap);
                         remoteViews.setTextViewText(R.id.widget_battery_voltage, getString(R.string.voltage)+" : " + intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) + " mV");
                         remoteViews.setTextViewText(R.id.widget_battery_source, getString(R.string.ba_power_source)+" : " + (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == 0 ? "Battery" : "Charge"));
                         remoteViews.setTextViewText(R.id.widget_battery_type, getString(R.string.ba_type)+" : "+ intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY));
                         remoteViews.setTextViewText(R.id.widget_battery_temperature, getString(R.string.temperature)+" : " + ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10) + " °C");
                         remoteViews.setTextViewText(R.id.widget_battery_health, getString(R.string.health)+" : " + getBatteryHealth(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)));
                         appWidgetManager.updateAppWidget(widget, remoteViews);
                     }
                     pieGraph.removeSlices();
                 }
             };
             if(context!=null)
                 context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
         }

         private String getBatteryHealth(int health)
         {
             switch(health)
             {
                 case BatteryManager.BATTERY_HEALTH_COLD: return getString(R.string.cold);
                 case BatteryManager.BATTERY_HEALTH_GOOD: return getString(R.string.good);
                 case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return getString(R.string.over_volt);
                 case BatteryManager.BATTERY_HEALTH_OVERHEAT: return getString(R.string.over_heat);
                 case BatteryManager.BATTERY_HEALTH_UNKNOWN: return getString(R.string.unknown);
                 default : return getString(R.string.unknown);
             }
         }

         @Override
         public IBinder onBind(Intent intent)
         {
             return null;
         }
     }

     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         if(context!=null)
             try
             {
                 context.getApplicationContext().unregisterReceiver(UpdateBattery.broadcastReceiver);
                 context.stopService(service);
             }
             catch (IllegalArgumentException exc)
             {

             }
         super.onDeleted(context, appWidgetIds);
     }
 }
