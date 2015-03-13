 package com.dev.system.monitor;

 import java.util.ArrayList;
 import com.dev.system.monitor.R;
 import com.github.amlcurran.showcaseview.ShowcaseView;
 import com.github.amlcurran.showcaseview.targets.ViewTarget;
 import it.gmariotti.cardslib.library.view.CardView;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.DhcpInfo;
 import android.net.NetworkInfo;
 import android.net.TrafficStats;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.content.Intent;

 public class WiFiManagement extends Fragment
 {
 	private BroadcastReceiver RSSIListener,WiFiListener;
 	private View rootView;
 	private WifiManager wifiManager;
 	private WifiInfo wifiInfo;
 	private DhcpInfo DHCPInfo;
 	private ImageView signalImg;
 	private GeneralInfoCard card;
 	private Handler handler;
 	private long startRX=0,startTX=0;
 	private TextView RX,TX;
 	private Runnable runnable;
 	private Activity mainActivity;
 	private ShowcaseView sv;
 	private ArrayList<String> valuesBackup;

 	@Override
     public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
 	{
 		rootView=inflater.inflate(R.layout.fragment_wifi,container,false);
 		signalImg=(ImageView)rootView.findViewById(R.id.signalImg);
 		final boolean firstrun=getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE)
 							   .getBoolean("firstrun_fgr",true);
 		if(firstrun)
 	    {
 			getActivity().getSharedPreferences("PREFERENCE",getActivity().MODE_PRIVATE).edit()
 						 .putBoolean("firstrun_fgr",false).commit();

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
         	   ViewTarget target=new ViewTarget(signalImg);
                sv=new ShowcaseView.Builder(getActivity(),true)
                    	.setTarget(target)
                    	.setContentTitle(getString(R.string.tut_wifi))
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
 		startRX=TrafficStats.getTotalRxBytes();
 	    startTX=TrafficStats.getTotalTxBytes();
 	    RX=(TextView)rootView.findViewById(R.id.dataDown);
 	    TX=(TextView)rootView.findViewById(R.id.dataUp);
 	    mainActivity=getActivity();
 	    runnable=new Runnable()
 	    {
 		      public void run()
 		      {
 		    	try
 		    	{
 		    	  ConnectivityManager connManager=(ConnectivityManager)mainActivity
 		    			  						  .getSystemService(Context.CONNECTIVITY_SERVICE);
 		    	  NetworkInfo wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		    	  if(wifi.isConnected())
 			       {
 			    	    long rxBytes=TrafficStats.getTotalRxBytes()-startRX;
 			       		if(rxBytes/(1024*1024*1024)>0)
 			       			RX.setText(Long.toString(rxBytes/(1024*1024*1024))+ " GB/s");
                         else if(rxBytes/(1024*1024)>0)
                             RX.setText(Long.toString(rxBytes/(1024*1024))+ " MB/s");
                         else if(rxBytes/1024>0)
                            RX.setText(Long.toString(rxBytes/1024)+ " KB/s");
 			       		else
 			       			RX.setText(Long.toString(rxBytes)+ " B/s");
 			       		long txBytes=TrafficStats.getTotalTxBytes()-startTX;
 			       		startRX+=rxBytes;
 			       		startTX+=txBytes;
                        if(txBytes/(1024*1024*1024)>0)
                            TX.setText(Long.toString(txBytes/(1024*1024*1024))+ " GB/s");
                        else if(txBytes/(1024*1024)>0)
                            TX.setText(Long.toString(txBytes/(1024*1024))+ " MB/s");
                        else if(txBytes/1024>0)
                            TX.setText(Long.toString(txBytes/1024)+ " KB/s");
 			       		else
 			       			TX.setText(Long.toString(txBytes)+ " B/s");
 			       }
 		    	}
 		    	catch(NullPointerException exc)
 		    	{
 		    		//if caught, due to no data avaiable yet or no network connected to :/
 		    		TX.setText("");
 					RX.setText("");
 		    	}
 		    	handler.postDelayed(this,1000);
 		  }
 		};
 		if(startRX!=TrafficStats.UNSUPPORTED&&startTX!=TrafficStats.UNSUPPORTED)
 		{
 			handler=new Handler();
 			handler.postDelayed(runnable,1000);
 		}
         new GeneralWiFiInfoTask().execute();
         RSSIListener=new BroadcastReceiver(){
             @Override
             public void onReceive(Context context, Intent intent) {
               try
               {
             	  new RSSIBroadcastReceiverTask().execute();
               }
               catch(NullPointerException exc)
               {

               }
             }
         };
         WiFiListener=new BroadcastReceiver()
         {
         	public void onReceive(Context context, Intent intent)
         	{
         		try
         		{
         			new WiFiBroadcastReceiverTask().execute();
         		}
         		catch(NullPointerException exc)
         		{

         		}
         	}
         };
         mainActivity.registerReceiver(RSSIListener,new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
         mainActivity.registerReceiver(WiFiListener,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
         signalImg.setOnLongClickListener(new OnLongClickListener() {

 			@Override
 			public boolean onLongClick(View v) {
 				if(firstrun)
 					sv.hide();
 				try
                 {
                     boolean stat;
                     wifiManager.setWifiEnabled(stat=!wifiManager.isWifiEnabled());
                     if(!stat)
                     {
                         signalImg.setImageResource(R.drawable.wifi_off);
                         Toast.makeText(getActivity(),getString(R.string.wifi_off),Toast.LENGTH_LONG).show();
                         TX.setText("");
                         RX.setText("");
                     }
                     else
                     {
                         Toast.makeText(getActivity(),getString(R.string.wifi_on),Toast.LENGTH_LONG).show();
                         wifiManager=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
                         wifiInfo=wifiManager.getConnectionInfo();
                         DHCPInfo=wifiManager.getDhcpInfo();
                         switch(WifiManager.calculateSignalLevel(wifiInfo.getRssi(),3))
                         {
                             case 0: signalImg.setImageResource(R.drawable.wifi_low); break;
                             case 1:	signalImg.setImageResource(R.drawable.wifi_med); break;
                             case 2:	signalImg.setImageResource(R.drawable.wifi_full); break;
                         }
                     }
                 }
                 catch(Exception exc)
                 {

                 }
 				return false;
 			}
 		});
         valuesBackup=new ArrayList();
         return rootView;
     }

 	private String intToIP(int address)
 	{
         return (address&0xFF)+"."+((address>>8)&0xFF)+"."+((address>>16)&0xFF)+"."+((address>>24)&0xFF);
     }

 	@Override
 	public void onPause() {
 		mainActivity.unregisterReceiver(RSSIListener);
 		mainActivity.unregisterReceiver(WiFiListener);
 		try
 		{
 			if(card!=null)
 				card.unregisterDataSetObserver();
 		}
 		catch(IllegalStateException exc)
 		{
 			//due to no datasetobserver registered
 		}
 		super.onPause();
 	}

 	@Override
 	public void onResume() {
 		mainActivity.registerReceiver(RSSIListener,new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
 		mainActivity.registerReceiver(WiFiListener,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
 		super.onResume();
 	}

 	private class GeneralWiFiInfoTask extends AsyncTask<Void,Void,Void>
 	{
 		ArrayList<String> info,value;

 		public GeneralWiFiInfoTask()
 		{
 			info=new ArrayList<String>();
 			value=new ArrayList<String>();
 		}

 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			wifiManager=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
 	        wifiInfo=wifiManager.getConnectionInfo();
 	        DHCPInfo=wifiManager.getDhcpInfo();
 	        info.add(getString(R.string.ip));
 	        value.add(intToIP(wifiInfo.getIpAddress()));
 	        info.add(getString(R.string.mac));
 	        value.add(wifiInfo.getMacAddress());
 	        info.add("DNS 1");
 	        value.add(intToIP(DHCPInfo.dns1));
 	        info.add("DNS 2");
 	        value.add(intToIP(DHCPInfo.dns2));
 	        info.add("Gateway");
 	        value.add(intToIP(DHCPInfo.gateway));
 	        info.add(getString(R.string.bandwidth));
 	        value.add(wifiInfo.getLinkSpeed()+" Mbps");
 	        publishProgress();
 			return null;
 		}

 		@Override
 		protected void onProgressUpdate(Void... values) {
 			ConnectivityManager connManager=(ConnectivityManager)mainActivity
 					  .getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 			try
 			{
 				mainActivity.setTitle(wifiInfo.getSSID().replace("\"","")+" "+wifiInfo.getRssi()+" dB");
 			}
 			catch(NullPointerException exc)
 			{
 				mainActivity.setTitle(getString(R.string.wifi_not_conn));
 			}
       	    if(wifi!=null&&wifi.isConnected()&&wifiManager.isWifiEnabled())
 		        	switch(WifiManager.calculateSignalLevel(wifiInfo.getRssi(),3))
 		        	{
 		        		case 0: signalImg.setImageResource(R.drawable.wifi_low); break;
 		        		case 1:	signalImg.setImageResource(R.drawable.wifi_med); break;
 		        		case 2:	signalImg.setImageResource(R.drawable.wifi_full); break;
 		        	}
 		     else
 		     {
 		    		signalImg.setImageResource(R.drawable.wifi_off);
 		    		TX.setText("");
 					RX.setText("");
 		     }
 		    initCard(info,value);
 		}
 	}

 	private class RSSIBroadcastReceiverTask extends AsyncTask<Void,Void,Void>
 	{
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			wifiManager=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
   			wifiInfo=wifiManager.getConnectionInfo();
   			DHCPInfo=wifiManager.getDhcpInfo();
   			publishProgress();
 			return null;
 		}

 		@Override
 		protected void onProgressUpdate(Void... values)
 		{
 			ConnectivityManager connManager=(ConnectivityManager)mainActivity
 					  .getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo wifi=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 			mainActivity.setTitle(wifiInfo.getSSID().replace("\"","")+" "+wifiInfo.getRssi()+" dB");
         	if(wifi!=null&&wifi.isConnected()&&wifiManager.isWifiEnabled())
               switch(WifiManager.calculateSignalLevel(wifiInfo.getRssi(),3))
               {
               	case 0: signalImg.setImageResource(R.drawable.wifi_low); break;
               	case 1:	signalImg.setImageResource(R.drawable.wifi_med); break;
               	case 2:	signalImg.setImageResource(R.drawable.wifi_full); break;
               }
         	else
         	{
         		signalImg.setImageResource(R.drawable.wifi_off);
         		TX.setText("");
     			RX.setText("");
         	}
 		}
 	}

 	private class WiFiBroadcastReceiverTask extends AsyncTask<Void,Void,Void>
 	{

 		ArrayList<String> info,value;

 		public WiFiBroadcastReceiverTask()
 		{
 			info=new ArrayList<String>();
 			value=new ArrayList<String>();
 		}

 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			wifiManager=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
 			wifiInfo=wifiManager.getConnectionInfo();
 			DHCPInfo=wifiManager.getDhcpInfo();
 		    info.add(getString(R.string.ip));
 		    value.add(intToIP(wifiInfo.getIpAddress()));
 		    info.add(getString(R.string.mac));
 		    value.add(wifiInfo.getMacAddress());
 		    info.add("DNS 1");
 		    value.add(intToIP(DHCPInfo.dns1));
 		    info.add("DNS 2");
 		    value.add(intToIP(DHCPInfo.dns2));
 		    info.add("Gateway");
 		    value.add(intToIP(DHCPInfo.gateway));
 		    info.add(getString(R.string.bandwidth));
 		    value.add(wifiInfo.getLinkSpeed()+" Mbps");
 		    for(int i=0;i<valuesBackup.size();i++)
 	        	if(!valuesBackup.get(i).equals(value.get(i)))
 	        	{
 	        		publishProgress();
 	        		break;
 	        	}
 	        valuesBackup=value;
 			return null;
 		}

 		@Override
 		protected void onProgressUpdate(Void... values)
 		{
 			try
 			{
 				mainActivity.setTitle(wifiInfo.getSSID().replace("\"","")+" "+wifiInfo.getRssi()+" dB");
 			}
 			catch(NullPointerException exc)
 			{
 				mainActivity.setTitle(getString(R.string.wifi_not_conn));
 			}
 		    initCard(info,value);
 		}
 	}

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }

     private void initCard(ArrayList<String> info,ArrayList<String> value)
     {
         card=new GeneralInfoCard(getActivity(),info,value,getString(R.string.wifi_info));
         card.init();
         CardView cardView=(CardView)rootView.findViewById(R.id.carddemo_weathercard);
         cardView.setCard(card);
     }
 }