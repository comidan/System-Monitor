package com.dev.system.monitor;

import it.gmariotti.cardslib.library.view.CardView;
import java.util.ArrayList;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class BatteryManagement extends Fragment
{
	private View rootView;
	private int batteryPct;
	private Intent batteryStatus;
	private PieGraph pieGraph;
	private GeneralInfoCard cardView;
	private BroadcastReceiver batteryInfo;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		rootView=inflater.inflate(R.layout.fragment_battery,container,false);
		getActivity().setTitle(getString(R.string.battery));
		IntentFilter ifilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	batteryPct=-1;
    	pieGraph=(PieGraph)rootView.findViewById(R.id.graph);
    	batteryInfo=new BroadcastReceiver() {
    		   public void onReceive(Context context, Intent intent) {
    				   batteryPct=intent.getIntExtra("level",100);
    				   pieGraph.removeSlices();
    				   PieSlice slice=new PieSlice(),_slice=new PieSlice();
    			       slice.setColor(Color.parseColor("#99CC00"));
    			       slice.setValue(batteryPct);
    			       _slice.setColor(Color.parseColor("#0099CC00"));
    			       _slice.setValue(100-batteryPct+0.00001f);
    			       pieGraph.addSlice(slice);
    			       pieGraph.addSlice(_slice);
    			       pieGraph.setInnerCircleRatio(150);
    			   	   for(PieSlice s : pieGraph.getSlices())
    			   		   s.setGoalValue(s.getValue());
    			   	   pieGraph.setDuration(1000);
    			   	   pieGraph.setInterpolator(new AccelerateDecelerateInterpolator());
    			   	   pieGraph.animateToGoalValues();
    				   ((TextView)rootView.findViewById(R.id.textView1)).setText(batteryPct+" %");
    				   int health=intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
    				   int plugged=intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
    				   String technology=intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
    				   float temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
    				   int voltage=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
    				   ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
    				   info.add(getString(R.string.ba_power_source));
    				   value.add(plugged==0 ? getString(R.string.battery) : getString(R.string.charge));
    				   info.add(getString(R.string.ba_type));
    				   value.add(technology);
    				   info.add(getString(R.string.temperature));
    				   value.add((temperature/10)+" °C");
    				   info.add(getString(R.string.voltage));
    				   value.add(voltage+" mV");
    				   info.add(getString(R.string.health));
    				   value.add(getBatteryHealth(health));
    				   initCard(info,value);
    		     }
    	};
    	getActivity().registerReceiver(batteryInfo,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		return rootView;
	}
	
	@Override
	public void onResume() {
		getActivity().registerReceiver(batteryInfo,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		super.onResume();
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(batteryInfo);
		super.onPause();
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
	
	private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
		cardView=new GeneralInfoCard(getActivity(),info,value,getString(R.string.battery_info));
		cardView.init();
        CardView _cardView=(CardView)rootView.findViewById(R.id.card_battery);
        _cardView.setCard(cardView);
    }
}
