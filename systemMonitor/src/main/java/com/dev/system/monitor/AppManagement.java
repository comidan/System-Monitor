package com.dev.system.monitor;

import java.util.ArrayList;
import java.util.List;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AppManagement extends Fragment
{
	private static final int REQUEST_CODE_SETTINGS=0;
    private PackageAdapter adapter;
    private ArrayList<PackageItem> data;
    private SwipeListView swipeListView;
    private View rootView;
	private ProgressBar progressBar;
    private ShowcaseView sv;
    private ListAppTask task;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
    {
    	rootView=inflater.inflate(R.layout.swipe_list_view_activity,container,false);
    	getActivity().setTitle("Running Applications");
    	progressBar=(ProgressBar)rootView.findViewById(R.id.progressBar1);
    	swipeListView=(SwipeListView)rootView.findViewById(R.id.example_lv_list);
    	data=new ArrayList<PackageItem>();
        adapter=new PackageAdapter(getActivity(),data,swipeListView);
        if(Build.VERSION.SDK_INT>=11)
            swipeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        reload();
        task=new ListAppTask();
        task.execute();
        return rootView;
    }
   
    private void reload()
    {
        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_BOTH);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_MODE_NONE);
        swipeListView.setSwipeActionRight(SwipeListView.SWIPE_MODE_NONE);
        swipeListView.setOffsetLeft(12);
        swipeListView.setOffsetRight(12);
        swipeListView.setAnimationTime(250);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQUEST_CODE_SETTINGS:
                reload();
        }
    }
    
    @Override
    public void onPause()
    {
    	task.cancel(true);
    	super.onPause();
    }
    
    public class ListAppTask extends AsyncTask<Void,Void,ArrayList<PackageItem>>
    {
        protected ArrayList<PackageItem> doInBackground(Void... args)
        {
            PackageManager appInfo=getActivity().getPackageManager();
            ArrayList<PackageItem> data=new ArrayList<PackageItem>();
            ActivityManager activityManager=(ActivityManager)getActivity().getSystemService("activity");
			List<RunningAppProcessInfo> procInfo=activityManager.getRunningAppProcesses();
			progressBar.setMax(procInfo.size()-1); //keep out my app :D
            progressBar.setProgress(0);
			for (int i=0;i<procInfo.size();i++)
			{
			    RunningAppProcessInfo process=procInfo.get(i);
			    String name=process.processName;
			    if(!name.equals("com.dev.system.monitor"))
			      try
			      {
			    	PackageItem item=new PackageItem();
                    item.setName(appInfo.getApplicationLabel(appInfo
                    					.getApplicationInfo(process.processName,PackageManager.GET_META_DATA))
                    					.toString());
                    item.setPackageName(name);
                    item.setIcon(appInfo.getApplicationIcon(name));       
    			    int[] pids = new int[1];
    			    pids[0]=process.pid;
    			    android.os.Debug.MemoryInfo[] mi=activityManager.getProcessMemoryInfo(pids);
    			    int totalMemoryInKByte=mi[0].dalvikPrivateDirty+
    			    					   mi[0].dalvikSharedDirty+
    			    					   mi[0].dalvikPss+        
    			    					   mi[0].nativePrivateDirty+
    			    					   mi[0].nativeSharedDirty+
    			    					   mi[0].nativePss;
    			    item.setMemoryInKByte(totalMemoryInKByte);       
                    data.add(item);
                    publishProgress();
			      }
			      catch(NameNotFoundException exc)
			      {
			    	  
			      }
		    }
            return data;
        }
        
        @Override
        protected void onProgressUpdate(Void... values)
        {
        	progressBar.setProgress(progressBar.getProgress()+1);
        }
        
        protected void onPostExecute(ArrayList<PackageItem> result)
        {
        	View headerView;
        	progressBar.setProgress(progressBar.getMax());
        	progressBar.setVisibility(View.GONE);
            data.clear();
            data.addAll(result);
            adapter.notifyDataSetChanged();
            swipeListView.addHeaderView(headerView=getActivity().getLayoutInflater().inflate(R.layout.listview_header
					  ,null
					  ,false));
            ((TextView)headerView.findViewById(R.id.ram_header)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					for(int i=0;i<data.size();i++)
						for(int j=i+1;j<data.size();j++)
							if(data.get(i).getMemoryInKByte()<data.get(j).getMemoryInKByte())
							{
								PackageItem temp=data.get(j),_temp=data.get(i);
								data.remove(i);
								data.add(i,temp);
								data.remove(j);
								data.add(j,_temp);
							}
					adapter.notifyDataSetChanged();
				}
			});;
			((TextView)headerView.findViewById(R.id.app_header)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					for(int i=0;i<data.size();i++)
						for(int j=i+1;j<data.size();j++)
							if(data.get(i).getName().compareTo(data.get(j).getName())>0)
							{
								PackageItem temp=data.get(j),_temp=data.get(i);
								data.remove(i);
								data.add(i,temp);
								data.remove(j);
								data.add(j,_temp);
							}
					adapter.notifyDataSetChanged();
				}
			});;
            swipeListView.setAdapter(adapter);
            final boolean firstrun=getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE)
					   .getBoolean("firstrun_apprun",true);
            if(firstrun)
            {
            	getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE).edit()
            		   .putBoolean("firstrun_apprun",false).commit();
            	RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
                if(Build.VERSION.SDK_INT>=19)
             	   lps.setMargins(margin,margin,margin,margin+70);
                else
             	   lps.setMargins(margin,margin,margin,margin);
            	ViewTarget target=new ViewTarget(swipeListView);
            	sv=new ShowcaseView.Builder(getActivity(),true)
            		   .setTarget(target)
            		   .setContentTitle(getString(R.string.tut_apps))
            		   .setStyle(R.style.CustomShowcaseTheme)
            		   .setShowcaseEventListener(null)
            		   .build();
            	try
            	{
            		sv.setButtonPosition(lps);
            		sv.show();
            	}
            	catch(NullPointerException exc)
            	{
	
            	}
            }
        }
    }
}
