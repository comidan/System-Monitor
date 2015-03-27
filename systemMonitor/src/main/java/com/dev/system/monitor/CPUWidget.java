package com.dev.system.monitor;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.RemoteViews;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import android.os.Handler;
import java.util.regex.Pattern;

public class CPUWidget extends AppWidgetProvider
{
    private static Context context;
    private static RemoteViews remoteViews;
    private static AppWidgetManager appWidgetManager;
    private static ComponentName widget;
    private static int NUMBER_OF_CORES,min_freq,max_freq;
    private static Handler handler;
    private static Runnable runnable;
    private static PowerManager mPower;
    private static boolean loading_done;
    private static final String[] cpu_temp_paths={"/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                                                  "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                                                  "/sys/class/thermal/thermal_zone1/temp",
                                                  "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                                                  "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                                                  "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                                                  "/sys/devices/platform/tegra_tmon/temp1_input",
                                                  "/sys/kernel/debug/tegra_thermal/temp_tj",
                                                  "/sys/devices/platform/s5p-tmu/temperature",
                                                  "/sys/class/thermal/thermal_zone0/temp",
                                                  "/sys/devices/virtual/thermal/thermal_zone0/temp",
                                                  "/sys/class/hwmon/hwmon0/device/temp1_input",
                                                  "/sys/devices/virtual/thermal/thermal_zone1/temp",
                                                  "/sys/devices/platform/s5p-tmu/curr_temp",
                                                  "/sys/htc/cpu_temp",
                                                  "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/ext_temperature",
                                                  "/sys/devices/platform/tegra-tsensor/tsensor_temperature"};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context,appWidgetManager,appWidgetIds);
        this.context=context;
        remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_cpu);
        widget=new ComponentName(context,CPUWidget.class);
        mPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        this.appWidgetManager=appWidgetManager;
        loading_done=false;
        handler=new Handler();
        runnable=new Runnable()
        {
            @Override
            public void run() {
                if(!loading_done)
                        new Init().execute();
                else
                    return;
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable,3000);
    }

    private class Init extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            File dir=new File("/sys/devices/system/cpu/");
            File[] files=dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            });
            NUMBER_OF_CORES=files.length;
            ProcessBuilder cmd;
            String result="";
            InputStream in;
            try
            {
                String[] args;
                args=new String[]{"/system/bin/cat","/sys/devices/system/cpu/cpu0"+"/cpufreq/cpuinfo_min_freq"};
                cmd=new ProcessBuilder(args);
                Process process=cmd.start();
                in=process.getInputStream();
                byte[] re=new byte[1024];
                while(in.read(re)!=-1)
                    result+=new String(re);
                in.close();
                for(int j=0;j<result.length();j++)
                    try
                    {
                        Integer.parseInt(result.charAt(j)+""); //checking raw char or invalid char
                    }
                    catch(NumberFormatException exc)
                    {
                        result=new String(result.substring(0,j));
                        min_freq=Integer.parseInt(result);
                    }
            }
            catch(IOException exc)
            {

            }
            result="";
            try
            {
                String[] args;
                args=new String[]{"/system/bin/cat","/sys/devices/system/cpu/cpu0"+"/cpufreq/cpuinfo_max_freq"};
                cmd=new ProcessBuilder(args);
                Process process=cmd.start();
                in=process.getInputStream();
                byte[] re=new byte[1024];
                while(in.read(re)!=-1)
                    result+=new String(re);
                in.close();
                for(int j=0;j<result.length();j++)
                    try
                    {
                        Integer.parseInt(result.charAt(j)+"");
                    }
                    catch(NumberFormatException exc)
                    {
                        result=new String(result.substring(0,j));
                        max_freq=Integer.parseInt(result);
                    }
            }
            catch(IOException exc)
            {

            }
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            remoteViews.setTextViewText(R.id.widget_cpu_max,"Max : "+max_freq/1000+" MHz");
            remoteViews.setTextViewText(R.id.widget_cpu_min,"Min : "+min_freq/1000+" MHz");
            loading_done=true;
            appWidgetManager.updateAppWidget(widget,remoteViews);
            appWidgetManager.updateAppWidget(widget,remoteViews);
            runnable=new Runnable()
            {
                @Override
                public void run() {
                    if(remoteViews==null||context==null||appWidgetManager==null||!mPower.isScreenOn())
                    {
                        handler.postDelayed(this,6000);
                        return;
                    }
                    handler.removeCallbacks(runnable);
                    new CPUFrequency().execute();
                    new CPUTemperature().execute();
                    handler.postDelayed(this,2000);
                }
            };
            handler.postDelayed(runnable,2000);
        }
    }

    private class CPUFrequency extends AsyncTask<Void,Void,Void>
    {
        private String[] args;
        private String result;
        private ProcessBuilder cmd;
        private Process process;
        private InputStream in;
        private byte[] re;
        private int[] cur_freqs=new int[NUMBER_OF_CORES];
        private int rate;

        @Override
        protected Void doInBackground(Void... params)
        {

            for(int i=0;i<NUMBER_OF_CORES;i++)
            {
                args=new String[]{"/system/bin/cat","/sys/devices/system/cpu/cpu"+i+
                        "/cpufreq/scaling_cur_freq"};
                result="";
                int value=0;
                try
                {
                    cmd=new ProcessBuilder(args);
                    process=cmd.start();
                    in=process.getInputStream();
                    re=new byte[1024];
                    while(in.read(re)!=-1)
                        result+=new String(re);
                    in.close();
                    for(int j=0;j<result.length();j++)
                        try
                        {
                            Integer.parseInt(result.charAt(j)+"");
                        }
                        catch(NumberFormatException exc)
                        {
                            result=new String(result.substring(0,j));
                            value=Integer.parseInt(result);
                        }
                    cur_freqs[i]=value;
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            int cur_total_freq=0;
            for(int i=0;i<cur_freqs.length;i++)
                cur_total_freq+=cur_freqs[i];
            rate=(int)((cur_total_freq/((double)max_freq*4))*100);
            return null;
        }

        @Override
        protected void onPostExecute(Void _void)
        {
            if(appWidgetManager!=null&&remoteViews!=null&&widget!=null)
            {
                remoteViews.setTextViewText(R.id.widget_cpu_cur,context.getString(R.string.usage)+" : "+rate+" %");
                synchronized (appWidgetManager)
                {
                    appWidgetManager.updateAppWidget(widget,remoteViews);
                }
            }
        }
    }

    private class CPUTemperature extends AsyncTask<Void,Void,Void>
    {
        private String[] args;
        private String result="";
        private ProcessBuilder cmd;
        private Process process;
        private InputStream in;
        private byte[] re;
        private String cpu_temp;
        private boolean found=false;
        private int index=0;

        @Override
        protected Void doInBackground(Void... params) {
            if(!context.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE)
                    .getBoolean("PATH_FOUND",false))
                while(!found&&index<14)
                {
                    args=new String[]{"/system/bin/cat",cpu_temp_paths[index]};
                    cmd=new ProcessBuilder(args);
                    try
                    {
                        process=cmd.start();
                        in=process.getInputStream();
                        re=new byte[1024];
                        while(in.read(re)!=-1)
                            result+=new String(re);
                        in.close();
                    }
                    catch(IOException exc)
                    {

                    }
                    char[] tmp=new char[100];
                    for(int i=0;i<result.length();i++)
                        if((result.charAt(i)<=57&&result.charAt(i)>=48)||result.charAt(i)==46)
                        {
                            tmp[0]=result.charAt(i);
                            tmp[1]=result.charAt(i+1);
                            break;
                        }
                    cpu_temp=String.valueOf(tmp);
                    try
                    {
                        Double.parseDouble(cpu_temp);
                        context.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE).edit()
                                .putBoolean("PATH_FOUND",true).commit();
                        context.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE).edit()
                                .putInt("PATH_INDEX",index).commit();
                        found=true;
                        break;
                    }
                    catch(NumberFormatException exc)
                    {

                    }
                    index++;
                    if(index==14)
                        cpu_temp="40";
                }
            else
            {
                args=new String[]{"/system/bin/cat",cpu_temp_paths[context.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE)
                        .getInt("PATH_INDEX",0)]};
                cmd=new ProcessBuilder(args);
                try
                {
                    process=cmd.start();
                    in=process.getInputStream();
                    re=new byte[1024];
                    while(in.read(re)!=-1)
                        result+=new String(re);
                    in.close();
                }
                catch(IOException exc)
                {

                }
                char[] tmp=new char[100];
                for(int i=0;i<result.length();i++)
                    if((result.charAt(i)<=57&&result.charAt(i)>=48)||result.charAt(i)==46)
                    {
                        tmp[0]=result.charAt(i);
                        tmp[1]=result.charAt(i+1);
                        break;
                    }
                cpu_temp=String.valueOf(tmp);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            remoteViews.setTextViewText(R.id.widget_cpu_temperature,context.getString(R.string.temperature)+" : "+cpu_temp+" °C");
            synchronized (appWidgetManager)
            {
                appWidgetManager.updateAppWidget(widget,remoteViews);
            }
            super.onPostExecute(result);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        if(handler!=null&&runnable!=null)
            handler.removeCallbacks(runnable);
    }
}
