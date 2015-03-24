package com.dev.system.monitor;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class RAMWidget extends AppWidgetProvider
{
    private static final String ACTION_CLEAN_RAM="CLEAN_RAM";

    private static PieGraph pg;
    private static Context context;
    private static RemoteViews remoteViews;
    private static AppWidgetManager appWidgetManager;
    private static ComponentName widget;
    private static Handler handler;
    private static PowerManager mPower;
    private static CustomRunnable runnable;
    private static Canvas canvas;
    private static boolean isCleaning;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        this.context=context;
        this.appWidgetManager=appWidgetManager;
        remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_ram);
        isCleaning=false;
        widget=new ComponentName(context,RAMWidget.class);
        mPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        Intent intent=new Intent(context,RAMWidget.class);
        intent.setAction(ACTION_CLEAN_RAM);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ram_clean,pendingIntent);
        new DrawTask().execute();
        handler=new Handler();
        runnable=new CustomRunnable();
        handler.postDelayed(runnable,3000);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context,intent);
        if(!isCleaning&&intent.getAction().equals(ACTION_CLEAN_RAM))
        {
            if(remoteViews==null)
                remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_ram);
            isCleaning=true;
            new KillProcesses().execute();
        }

    }

    private class KillProcesses extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (remoteViews == null || context == null || appWidgetManager == null)
                return null;
                ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService("activity");
                List<ActivityManager.RunningAppProcessInfo> procInfo = activityManager.getRunningAppProcesses();
                for (int i = 0; i < procInfo.size(); i++) {
                    ActivityManager.RunningAppProcessInfo process = procInfo.get(i);
                    int importance = process.importance;
                    String name = process.processName;
                    if (!name.equals("com.dev.system.monitor") &&
                            !name.contains("launcher") &&
                            !name.contains("googlequicksearchbox") &&
                            importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                        activityManager.killBackgroundProcesses(name);
                }
            return null;
        }

            @Override
            protected void onPostExecute (Void Void)
            {
                if(context!=null)
                    Toast.makeText(context,context.getString(R.string.ram_cls),Toast.LENGTH_LONG).show();
                if(pg!=null)
                    pg.removeSlices();
                else if(context!=null)
                    pg=new PieGraph(context);
                new DrawTask().execute();
            }
    }

    private class CustomRunnable implements Runnable
    {
        private boolean stop;

        public CustomRunnable()
        {
            stop=false;
        }

        @Override
        public void run()
        {
            if(!stop&&remoteViews!=null&&appWidgetManager!=null&&context!=null)
            {
                if(mPower.isScreenOn())
                {
                    handler.removeCallbacks(this);
                    new DrawTask().execute();
                    handler.postDelayed(this,3000);
                }
                else
                    handler.postDelayed(this,6000);
            }
             else
                return;
        }

        void killThread()
        {
            stop=true;
        }
    }

    private class DrawTask extends AsyncTask<Void,Void, Void>
    {
        private PieSlice slice,_slice;
        private long total=0,free=0,rate=0;

        @Override
        protected Void doInBackground(Void... unused)
        {
            RandomAccessFile reader=null;
            try
            {
                reader=new RandomAccessFile("/proc/meminfo","r");
                long[] mems=new long[4];
                for(int i=0;i<4;i++)
                {
                    String load = reader.readLine();
                    String[] toks = load.split(":");
                    mems[i] = Long.parseLong(toks[1].replace("kB","").trim());
                }
                total=mems[0]/1024;
                free=(mems[1]+mems[2]+mems[3])/1024;
                rate=(int)((float)(total-free)/total*100);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if(reader!=null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            slice=new PieSlice();
            slice.setColor(Color.parseColor("#99CC00"));
            slice.setValue(total - free);
            _slice=new PieSlice();
            _slice.setColor(Color.parseColor("#FFBB33"));
            _slice.setValue(free);
            pg=new PieGraph(context);
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            try
            {
                pg.measure(200, 200);
                pg.layout(0, 0, 200, 200);
                pg.setDrawingCacheEnabled(true);
                pg.addSlice(slice);
                pg.addSlice(_slice);
                pg.setInnerCircleRatio(150);
                for (PieSlice s : pg.getSlices())
                    s.setGoalValue(s.getValue());
                pg.setDuration(1000);
                pg.setInterpolator(new AccelerateDecelerateInterpolator());
                pg.animateToGoalValues();
                pg.setPadding(3);
                Bitmap bitmap = pg.getDrawingCache();
                if (canvas == null)
                    canvas = new Canvas(bitmap);
                else
                    canvas.setBitmap(bitmap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLACK);
                paint.setTextSize(18);
                paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
                Rect bounds = new Rect();
                paint.getTextBounds(rate + " %", 0, new String(rate + " %").length(), bounds);
                int x = (bitmap.getWidth() - bounds.width()) / 2;
                int y = (bitmap.getHeight() + bounds.height()) / 2;
                canvas.drawText(rate + " %", x, y, paint);
                if (remoteViews != null || appWidgetManager != null || context != null) {
                    remoteViews.setTextViewText(R.id.widget_ram_text,context.getString(R.string.total_ram)+" "+ total + " MB");
                    remoteViews.setTextViewText(R.id.widget_ram_text1,context.getString(R.string.free_ram)+" "+ free + " MB");
                    remoteViews.setTextViewText(R.id.widget_ram_text2,context.getString(R.string.used_ram)+" "+ + (total - free) + " MB");
                    remoteViews.setImageViewBitmap(R.id.graph_widget,bitmap);
                    appWidgetManager.updateAppWidget(widget, remoteViews);
                }
            }
            catch(Exception exc)
            {

            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if(runnable!=null)
            runnable.killThread();
        if(handler!=null&&runnable!=null)
            handler.removeCallbacks(runnable);
        super.onDeleted(context, appWidgetIds);
    }
}
