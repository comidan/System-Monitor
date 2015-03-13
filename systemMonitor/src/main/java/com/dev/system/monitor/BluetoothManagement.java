package com.dev.system.monitor;

import it.gmariotti.cardslib.library.view.CardView;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class BluetoothManagement extends Fragment
{
	private BluetoothAdapter bluetoothAdapter;
	private ImageView bluetoothState;
	private BroadcastReceiver bluetoothChanged;
	private View rootView;
	private GeneralInfoCard card;
	private Activity mainActivity;
	
	@Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_bluetooth,container,false);
        mainActivity=getActivity();
        bluetoothState=(ImageView)rootView.findViewById(R.id.bluetooth_state);
        try
        {
        	bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        	ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
        	info.add(getString(R.string.mac));
		    value.add(bluetoothAdapter.getAddress());
		    info.add(getString(R.string.b_device_name));
		    value.add(bluetoothAdapter.getName());
		    info.add(getString(R.string.b_visibility));
        	mainActivity.setTitle("Bluetooth");
        	if(bluetoothAdapter.isEnabled())
        		bluetoothState.setImageResource(R.drawable.bluetooth_on);
        	else
        		bluetoothState.setImageResource(R.drawable.bluetooth_off);
        	switch(bluetoothAdapter.getScanMode())
        	{	
        		case BluetoothAdapter.SCAN_MODE_CONNECTABLE: 
        				value.add(getString(R.string.b_con)); break;
        		case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
        				value.add(getString(R.string.b_vi)); break;
        		case BluetoothAdapter.SCAN_MODE_NONE:
        				value.add(getString(R.string.b_not_vi));
        	}
        	initCard(info,value);
        }
        catch(NullPointerException exc)
        {
			 Toast.makeText(mainActivity,getString(R.string.b_not_av_msg),Toast.LENGTH_LONG)
    		 .show();
			 ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
	         info.add(getString(R.string.mac));
             value.add(bluetoothAdapter.getAddress());
            info.add(getString(R.string.b_device_name));
             value.add(bluetoothAdapter.getName());
			 info.add(getString(R.string.b_visibility));
	         mainActivity.setTitle("Bluetooth");
	         bluetoothState.setImageResource(R.drawable.bluetooth_off);
	         value.add(getString(R.string.b_not_vi));
	         initCard(info,value);
			
        }
        bluetoothState.setOnLongClickListener(new OnLongClickListener() {
			
        		@Override
        		public boolean onLongClick(View v) {
        		 try
        		 {
        			boolean state;
        			if(bluetoothAdapter.isEnabled())
        			{
        				bluetoothState.setImageResource(R.drawable.bluetooth_off);
        				state=bluetoothAdapter.disable();
        			}
        			else
        			{
        				bluetoothState.setImageResource(R.drawable.bluetooth_on);
        				state=bluetoothAdapter.enable();
        			}
        			if(!state)
        			{
        				Toast.makeText(mainActivity,getString(R.string.b_err),Toast.LENGTH_LONG).show();
        				return false;
        			}
        		 }
        		 catch(NullPointerException exc)
        		 {
     				 Toast.makeText(mainActivity,getString(R.string.b_not_av_msg),Toast.LENGTH_LONG)
             		 .show();
        		 }
				return false;
			}
		});
        bluetoothChanged=new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
			 try
			 {
				bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
				ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
                info.add(getString(R.string.mac));
                value.add(bluetoothAdapter.getAddress());
                info.add(getString(R.string.b_device_name));
                value.add(bluetoothAdapter.getName());
                info.add(getString(R.string.b_visibility));
			    switch(bluetoothAdapter.getScanMode())
	        	{	
	        		case BluetoothAdapter.SCAN_MODE_CONNECTABLE: 
	        				value.add(getString(R.string.b_con)); break;
	        		case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
	        				value.add(getString(R.string.b_con)); break;
	        		case BluetoothAdapter.SCAN_MODE_NONE:
	        				value.add(getString(R.string.b_not_vi));
	        	}
				switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1))
				{
					case BluetoothAdapter.STATE_OFF:
						bluetoothState.setImageResource(R.drawable.bluetooth_off); break;
					case BluetoothAdapter.STATE_ON:
						bluetoothState.setImageResource(R.drawable.bluetooth_on);
				}
				initCard(info,value);
			 }
			 catch(NullPointerException exc)
			 {
				 Toast.makeText(mainActivity,getString(R.string.b_not_av_msg),Toast.LENGTH_LONG).show();
				 ArrayList<String> info=new ArrayList<String>(),value=new ArrayList<String>();
                 info.add(getString(R.string.mac));
                 value.add(getString(R.string.b_not_av));
                 info.add(getString(R.string.b_device_name));
                 value.add(getString(R.string.b_not_av));
                 info.add(getString(R.string.b_visibility));
		         bluetoothState.setImageResource(R.drawable.bluetooth_off);
		         value.add(getString(R.string.b_not_vi));
		         initCard(info,value);
			 }
             catch(RuntimeException exc)
             {
                 if(!isAdded())
                 {
                     ArrayList<String> info = new ArrayList<String>(), value = new ArrayList<String>();
                     info.add("MAC Address");
                     value.add("Not available");
                     info.add("Device Name");
                     value.add("Not available");
                     info.add("Visibility");
                     value.add("Not visible");
                     initCard(info, value);
                 }
                 mainActivity.unregisterReceiver(bluetoothChanged);
                 mainActivity.registerReceiver(bluetoothChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
             }
			}
		};
        mainActivity.registerReceiver(bluetoothChanged,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        return rootView;
    }
	
	private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
        if(isAdded())
            card=new GeneralInfoCard(mainActivity,info,value,getString(R.string.bluetooth_info));
        else
            card=new GeneralInfoCard(mainActivity,info,value,"Bluetooth Network Information");
        card.init();
        CardView cardView=(CardView)rootView.findViewById(R.id.card_mobile);
        cardView.setCard(card);
    }

}
