package com.dev.system.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.LineGraphView;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CPUManagement extends Fragment
{
	private View rootView;
	private int NUMBER_OF_CORES;
	private int[] cur_freqs;
	private double[] x;
	private int min_freq,max_freq;
	private LinearLayout[] graphsIDs;
	private Handler handler,_handler;
	private Runnable drawGraph,tempValue;
	private Timer timer,_timer;
	private TimerTask timerTask,_timerTask;
	private GraphViewSeries[] graphViewData;
	private GraphView[] graphViews;
	private Activity mainActivity,activityBackup;
	private TextView[] cores_title;
	private TextView min,max,cpu_temp;
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
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		  File dir=new File("/sys/devices/system/cpu/");
	      File[] files=dir.listFiles(new FileFilter()
	      {
	    	  @Override
	    	  public boolean accept(File pathname)
	    	  {
	    		  return Pattern.matches("cpu[0-9]+",pathname.getName());
	    	  }
	      });
	      mainActivity=getActivity();
	      NUMBER_OF_CORES=files.length;
	      if(NUMBER_OF_CORES<=4)
	      {
	    	  rootView=inflater.inflate(R.layout.fragment_cpu,container,false);
			  cores_title=new TextView[]{(TextView)rootView.findViewById(R.id.textView1),
					  					 (TextView)rootView.findViewById(R.id.textView2),
					  					 (TextView)rootView.findViewById(R.id.textView3),
					  					 (TextView)rootView.findViewById(R.id.textView4)};
			  graphsIDs=new LinearLayout[]{(LinearLayout)rootView.findViewById(R.id.linearLayout1),
					  					   (LinearLayout)rootView.findViewById(R.id.linearLayout2),
					                       (LinearLayout)rootView.findViewById(R.id.linearLayout3),
					                       (LinearLayout)rootView.findViewById(R.id.linearLayout4)};
			  min=((TextView)rootView.findViewById(R.id.textView6));
			  max=((TextView)rootView.findViewById(R.id.textView5));
			  cpu_temp=((TextView)rootView.findViewById(R.id.textView7));
	      }
	      else
	      {
	    	  rootView=inflater.inflate(R.layout.fragment_cpu_octa,container,false);
	    	  cores_title=new TextView[]{
	    			     (TextView)rootView.findViewById(R.id.textView1),
	  					 (TextView)rootView.findViewById(R.id.textView2),
	  					 (TextView)rootView.findViewById(R.id.textView3),
	  					 (TextView)rootView.findViewById(R.id.textView4),
	  					 (TextView)rootView.findViewById(R.id.textView5),
	  					 (TextView)rootView.findViewById(R.id.textView6),
	  					 (TextView)rootView.findViewById(R.id.textView7),
	  					 (TextView)rootView.findViewById(R.id.textView8)};
              graphsIDs=new LinearLayout[]{
						   (LinearLayout)rootView.findViewById(R.id.linearLayout1),
	  					   (LinearLayout)rootView.findViewById(R.id.linearLayout2),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout3),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout4),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout5),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout6),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout7),
	                       (LinearLayout)rootView.findViewById(R.id.linearLayout8)};
	    	  max=(TextView)rootView.findViewById(R.id.textView9);
	    	  min=(TextView)rootView.findViewById(R.id.textView10);
	    	  cpu_temp=((TextView)rootView.findViewById(R.id.textView11));
	      }
		  mainActivity.setTitle("Central Processing Unit");
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
		  				Integer.parseInt(result.charAt(j)+""); //checking raw char or invalid char, hope no :D
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
          min.setText(getString(R.string.min_freq));
		  min.append(" : " + min_freq / 1000 + " MHz");
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
		  /*try
		  {
		  		String[] args;
		  		args=new String[]{"/system/bin/cat","/sys/devices/system/cpu/cpu0/cpufreq/gpu_cur_freq"};   //testing gathering gpu information
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
		  				System.out.println("GPU_CUR_FREQ : "+result);
		  			}
		  }
		  catch(IOException exc)
		  {

		  }*/
          max.setText(getString(R.string.max_freq));
		  max.append(" : "+max_freq/1000+" MHz");
		  graphViews=new GraphView[NUMBER_OF_CORES];
	      graphViewData=new GraphViewSeries[NUMBER_OF_CORES];
	      x=new double[NUMBER_OF_CORES];
	      if(NUMBER_OF_CORES<4)
	    	  for(int i=3;i>=NUMBER_OF_CORES;i--)
	    		  graphsIDs[i].setVisibility(LinearLayout.GONE);
	      for(int i=0;i<graphViews.length;i++)
	      {
	    	  graphViews[i]=new LineGraphView(mainActivity,"");
	    	  graphViews[i].setManualYAxisBounds(max_freq/1000,0);
	    	  graphViewData[i]=new GraphViewSeries(new GraphViewData[]{new GraphViewData(0,0)});
	    	  ((LineGraphView)graphViews[i]).setDrawBackground(true);
	    	  graphViews[i].addSeries(graphViewData[i]);
	    	  graphViews[i].setViewPort(1,1);
	    	  graphViews[i].setScalable(true);
	    	  graphViews[i].setShowHorizontalLabels(false);
	    	  graphViews[i].setShowVerticalLabels(false);
              graphsIDs[i].addView(graphViews[i]);
              x[i]=0.2;
	      }
	      cur_freqs=new int[NUMBER_OF_CORES];
	      handler=new Handler();
	      timer=new Timer();
	      drawGraph=new Runnable()
          {
              public void run()
              {       
                  try
                  {
                	  CPUFrequency performBackgroundTask=new CPUFrequency();
                      performBackgroundTask.execute();
                  }
                  catch (Exception e)
                  {
                	  
                  }
              }
          };
	      timerTask = new TimerTask() {       
	          @Override
	          public void run()
	          {
	              handler.post(drawGraph);
	          }
	      };
	      timer.schedule(timerTask,0,1250);
	      _handler=new Handler();
	      _timer=new Timer();
	      tempValue=new Runnable() {
			
			@Override
			public void run() {
				try
				{
					CPUTemperature temperature=new CPUTemperature();
					temperature.execute();
				}
				catch(Exception exc)
				{
					
				}
			}
	      };
	      _timerTask=new TimerTask() {
			
			@Override
			public void run() {
				 _handler.post(tempValue);
			}
	      };
	      _timer.schedule(_timerTask,0,5000);
		  return rootView;
	}
	
	private class CPUFrequency extends AsyncTask<Void,Integer,Void>
	{

		private String[] args;
		private String result;
		private ProcessBuilder cmd;
		private Process process;
		private InputStream in;
		private byte[] re;
		
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
			for(int i=0;i<NUMBER_OF_CORES;i++)
			{
					publishProgress(new Integer[]{i});
					x[i]+=0.2;
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			graphViewData[values[0]].appendData(new GraphViewData(x[values[0]],cur_freqs[values[0]]/1000),true,10);
			if(cur_freqs[values[0]]!=0)
				cores_title[values[0]].setText("Core "+values[0]+" "+cur_freqs[values[0]]/1000+" Mhz");
			else
				cores_title[values[0]].setText("Core "+values[0]+" OFFLINE");
			if(cur_freqs.length<NUMBER_OF_CORES)
				for(int i=NUMBER_OF_CORES-cur_freqs.length;i<NUMBER_OF_CORES;i++)
					cores_title[values[0]].setText("Core "+i+" NOT PRESENT");
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
	      if(mainActivity==null)
	    	  mainActivity=activityBackup;
            if(mainActivity==null)
                mainActivity=getActivity();
		  if(!mainActivity.getSharedPreferences("PREFERENCE",mainActivity.MODE_PRIVATE)
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
					mainActivity.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE).edit()
					 .putBoolean("PATH_FOUND",true).commit();
					mainActivity.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE).edit()
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
		  else {
              if (mainActivity==null)
                  mainActivity=getActivity();
              if(mainActivity==null)
                  mainActivity=activityBackup;
              if (mainActivity!=null)
              {
                  args=new String[]{"/system/bin/cat",cpu_temp_paths[mainActivity.getSharedPreferences("PREFERENCE",Activity.MODE_PRIVATE).getInt("PATH_INDEX", 0)]};
                  cmd=new ProcessBuilder(args);
                  try {
                      process = cmd.start();
                      in = process.getInputStream();
                      re = new byte[1024];
                      while (in.read(re) != -1)
                          result += new String(re);
                      in.close();
                  } catch (IOException exc) {

                  }
                  char[] tmp = new char[100];
                  for (int i = 0; i < result.length(); i++)
                      if ((result.charAt(i) <= 57 && result.charAt(i) >= 48) || result.charAt(i) == 46) {
                          tmp[0] = result.charAt(i);
                          tmp[1] = result.charAt(i + 1);
                          break;
                      }
                  cpu_temp = String.valueOf(tmp);
                  if(cpu_temp.equals("")||cpu_temp.equals(" "))
                      cpu_temp="40";
              }
          }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
            if(mainActivity!=null&&cpu_temp!=null&&isAdded())
			    CPUManagement.this.cpu_temp.setText(getString(R.string.temperature)+" : " + cpu_temp + " °C");
			super.onPostExecute(result);
		}
	}
	
	@Override
	public void onPause() {
		timer.cancel();
		_timer.cancel();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		try
		{
			timer=new Timer();
			timer.schedule(timerTask,0,1250);
			_timer=new Timer();
			_timer.schedule(timerTask,0,5000);
		}
		catch(IllegalStateException exc)
		{
			
		}
		super.onResume();
	}
	
	@Override
	public void onAttach(Activity activity) {
		mainActivity=activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		activityBackup=mainActivity;
		super.onDetach();
	}
}