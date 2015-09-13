package com.dev.system.monitor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.faizmalkani.floatingactionbutton.Fab;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.view.CardView;

public class RAMManagement extends Fragment
{
	private View rootView;
	private MemoryInfo mi;
	private PieGraph pg;
	private Activity mainActivity;
	private ShowcaseView sv;
    private ProgressBar progressBar;
    private boolean firstrun;
    private boolean isCleaning;
	private GeneralInfoCard card;
	private ArrayList<String> info,values;
	
	@Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
        rootView=inflater.inflate(R.layout.fragment_ram,container,false);
        firstrun=getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE)
        					   .getBoolean("firstrun_fab",true);
        isCleaning=false;
        Fab mFab = (Fab)rootView.findViewById(R.id.fabbutton);
		info=new ArrayList<>();
		values=new ArrayList<>();
        if(firstrun)
	    {
           getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE).edit()
        			    .putBoolean("firstrun_fab",false).commit();
           try
           {
        	   RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
               lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
               lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
               int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
               if(Build.VERSION.SDK_INT>=19)
            	   lps.setMargins(margin,margin,margin,margin+70);
               else
            	   lps.setMargins(margin,margin,margin,margin);
        	   ViewTarget target=new ViewTarget(mFab);
               sv=new ShowcaseView.Builder(getActivity(),true)
                    	.setTarget(target)
                    	.setContentTitle(getString(R.string.tut_ram))
                    	.setStyle(R.style.CustomShowcaseTheme)
                    	.setShowcaseEventListener(null)
                    	.build();
               sv.setButtonPosition(lps);
        	   sv.show();
           }
           catch(NullPointerException exc)
           {
        	
           }
	    }
        mainActivity=getActivity();
        mainActivity.setTitle("Random Access Memory");
        progressBar=(ProgressBar)rootView.findViewById(R.id.progressBar);
        mi=new MemoryInfo();
		ActivityManager activityManager=(ActivityManager)mainActivity.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        mFab.setFabColor(Color.WHITE);
        mFab.setFabDrawable(getResources().getDrawable(android.R.drawable.ic_menu_delete));
        mFab.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                if(!isCleaning)
                {
                    isCleaning=true;
                    new KillProcesses().execute();
                }
			}
		});
        mFab.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if(firstrun)
					sv.hide();
				AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		        builder.setMessage(getString(R.string.ram_msg)).show();
				return false;
			}
		});
        mFab.showFab();
        new DrawTask().execute();
        return rootView;
    }

    private class KillProcesses extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            if(firstrun)
                sv.hide();
            ((TextView)rootView.findViewById(R.id.textView3)).setText(" ");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ActivityManager activityManager=(ActivityManager)mainActivity.getSystemService("activity");
            List<RunningAppProcessInfo> procInfo=activityManager.getRunningAppProcesses();
            for (int i=0;i<procInfo.size();i++)
            {
                RunningAppProcessInfo process=procInfo.get(i);
                int importance=process.importance;
                String name=process.processName;
                if(!name.equals("com.dev.system.monitor")&&
                        !name.contains("launcher")&&
                        !name.contains("googlequicksearchbox")&&
                        importance>RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                    activityManager.killBackgroundProcesses(name);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(mainActivity,getString(R.string.ram_cls),Toast.LENGTH_LONG).show();
            isCleaning=false;
            pg.removeSlices();
            new DrawTask().execute();
        }
    }

	private class DrawTask extends AsyncTask<Void,Void, Void> {
		private PieSlice slice,_slice;
		private long total=0,free=0,rate=0;
		
		public DrawTask()
		{
			pg=(PieGraph)rootView.findViewById(R.id.graph);
		}
		
		@Override
		synchronized protected Void doInBackground(Void... unused)
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
	        slice.setValue(total-free);
	        _slice=new PieSlice();
	        _slice.setColor(Color.parseColor("#FFBB33"));
	        _slice.setValue(free);
	        publishProgress();
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values)
		{
			pg.addSlice(slice);
			pg.addSlice(_slice);
			pg.setInnerCircleRatio(150);
		    for (PieSlice s : pg.getSlices())
		         s.setGoalValue(s.getValue());
		    pg.setDuration(1000);
		    pg.setInterpolator(new AccelerateDecelerateInterpolator());
		    pg.animateToGoalValues();
		    pg.setPadding(3);
			info.clear();
			RAMManagement.this.values.clear();
			info.add(getString(R.string.total_ram));
			RAMManagement.this.values.add(total+" MB");
			info.add(getString(R.string.available_ram));
			RAMManagement.this.values.add(free+" MB");
			info.add(getString(R.string.used_ram));
			RAMManagement.this.values.add((total-free)+" MB");
			((TextView)rootView.findViewById(R.id.textView3)).setText(rate + " %");
			initCard(info,RAMManagement.this.values);

		}
	}

	private void initCard(ArrayList<String> info,ArrayList<String> value)
	{
		card=new GeneralInfoCard(mainActivity,info,value,"RAM Information");
		card.init();
		CardView cardView = (CardView) rootView.findViewById(R.id.card_ram);
		cardView.setCard(card);
	}
}
