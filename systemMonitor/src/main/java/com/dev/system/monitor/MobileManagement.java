 package com.dev.system.monitor;

  import com.dev.system.monitor.R;
  import it.gmariotti.cardslib.library.view.CardView;
  import java.lang.reflect.InvocationTargetException;
  import java.lang.reflect.Method;
  import java.net.InetAddress;
  import java.net.NetworkInterface;
  import java.net.SocketException;
  import java.util.ArrayList;
  import java.util.Enumeration;
  import org.apache.http.conn.util.InetAddressUtils;
  import android.app.AlertDialog;
  import android.app.Fragment;
  import android.app.AlertDialog.Builder;
  import android.content.BroadcastReceiver;
  import android.content.Context;
  import android.content.DialogInterface;
  import android.content.Intent;
  import android.content.IntentFilter;
  import android.content.DialogInterface.OnClickListener;
  import android.net.ConnectivityManager;
  import android.net.TrafficStats;
  import android.os.AsyncTask;
  import android.os.Build;
  import android.os.Bundle;
  import android.os.Handler;
  import android.telephony.TelephonyManager;
  import android.view.LayoutInflater;
  import android.view.View;
  import android.view.View.OnLongClickListener;
  import android.view.ViewGroup;
  import android.widget.ImageView;
  import android.widget.TextView;
  import android.widget.Toast;

  public class MobileManagement extends Fragment
  {
	private View rootView;
	private ImageView mobileImg;
	private BroadcastReceiver mobileData;
	private GeneralInfoCard card;
	private TelephonyManager data;
	private Handler handler;
	private long startRX=0;
	private long startTX=0;
	private TextView RX,TX;
	private ConnectivityManager conman;
	private Runnable runnable;
	
	@Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
        rootView=inflater.inflate(R.layout.fragment_mobile,container,false);
        startRX=TrafficStats.getTotalRxBytes();
	    startTX=TrafficStats.getTotalTxBytes();
	    RX=(TextView)rootView.findViewById(R.id.dataDown);
	    TX=(TextView)rootView.findViewById(R.id.dataUp);
	    runnable=new Runnable()
	    {
	    	public void run()
		    { 
	    	  try
	    	  {
		       if(conman.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
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
	    		  
	    	  }
	    	  handler.postDelayed(this,1000);
		  }
		};
		if (startRX!=TrafficStats.UNSUPPORTED&&startTX!=TrafficStats.UNSUPPORTED)
		{
			handler=new Handler();
			handler.postDelayed(runnable,1000);
		}
        mobileImg=(ImageView)rootView.findViewById(R.id.signalMobileImg);
        setMobileData();
        mobileData=new BroadcastReceiver()
        {
        	@Override
        	public void onReceive(Context context,Intent intent)
        	{
        	  try
        	  {
        		final ConnectivityManager connMgr=(ConnectivityManager)context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        		if(connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable())
        			setMobileData();
        		else
        			mobileImg.setImageResource(R.drawable.mobileoff);
        	  }
        	  catch(Exception exc)
        	  {
        		  mobileImg.setImageResource(R.drawable.mobileoff);
        	  }
        	}
        };
        getActivity().registerReceiver(mobileData,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mobileImg.setOnLongClickListener(new OnLongClickListener()
        {	
			@Override
			public boolean onLongClick(View arg0)
			{
				try
				{
				  conman=(ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				  boolean active;
				  Method setMobileDataEnabledMethod;
				  if(Build.VERSION.SDK_INT<21)
				  {
					try
				  	{
						setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
				  	}
				  	catch(NoSuchMethodException exc)
				  	{
				  		Class[] cArg = new Class[2];  //for cyano
				  		cArg[0] = String.class;
				  		cArg[1] = Boolean.TYPE;
				  		setMobileDataEnabledMethod=ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
				  	}
					if(conman==null||conman.getActiveNetworkInfo()==null)
		            	  conman=(ConnectivityManager)getActivity().getSystemService(Context
								     .CONNECTIVITY_SERVICE);
					  setMobileDataEnabledMethod.setAccessible(true);
					  if(conman.getActiveNetworkInfo()!=null)
						  setMobileDataEnabledMethod.invoke(conman,active=conman.getActiveNetworkInfo()
						  														.isConnected() ? false : true);
					  else
						  setMobileDataEnabledMethod.invoke(conman,active=true);
		              Toast.makeText(getActivity(),active ? getString(R.string.mobile_on):getString(R.string.mobile_off),Toast.LENGTH_LONG)
		              	   .show();
		              try
		              {
		              if(conman.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
		                  switch(conman.getActiveNetworkInfo().getSubtype())
		                  {
		                  	case TelephonyManager.NETWORK_TYPE_1xRTT:
		                  	case TelephonyManager.NETWORK_TYPE_CDMA:
		                  	case TelephonyManager.NETWORK_TYPE_EDGE:
		                  		mobileImg.setImageResource(R.drawable.mobilee); break;
		                  	case TelephonyManager.NETWORK_TYPE_GPRS:
		                  		mobileImg.setImageResource(R.drawable.mobileg); break;
		                  	case TelephonyManager.NETWORK_TYPE_HSPA:
		                  	case TelephonyManager.NETWORK_TYPE_UMTS:
		                  		mobileImg.setImageResource(R.drawable.mobilehspa); break;
		                  	case TelephonyManager.NETWORK_TYPE_EHRPD:
		                  	case TelephonyManager.NETWORK_TYPE_EVDO_B:
		                  	case TelephonyManager.NETWORK_TYPE_HSDPA:
		                  	case TelephonyManager.NETWORK_TYPE_HSUPA:
		                  		mobileImg.setImageResource(R.drawable.mobile3g); break;
		                  	case TelephonyManager.NETWORK_TYPE_HSPAP:
		                  		mobileImg.setImageResource(R.drawable.mobilehspaplus); break;
		                  	case TelephonyManager.NETWORK_TYPE_LTE:
		                  		mobileImg.setImageResource(R.drawable.mobile4g); break;
		                  	case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		                  	default:
		                  		mobileImg.setImageResource(R.drawable.mobileoff);
		                  		TX.setText("");
		            			RX.setText("");
		                  }
		              }
		              catch(NullPointerException exc)
		              {
		            	  mobileImg.setImageResource(R.drawable.mobileoff);
		              }
		              TelephonyManager data=(TelephonyManager)getActivity()
		            		  			    .getSystemService(Context.TELEPHONY_SERVICE);
		              try
		              {
		            	  if(data.getLine1Number().equals(""))
		            		  getActivity().setTitle(getString(R.string.mobile));
		            	  else
		            		  getActivity().setTitle(data.getLine1Number());
		              }
		              catch(NullPointerException exc)
		              {
		            	  getActivity().setTitle(getString(R.string.mobile));
		              }
                      new MobileDataGeneralTask().execute();
				  }
				  else
				  {
					  TelephonyManager tm=(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
					  setMobileDataEnabledMethod=Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled",Boolean.TYPE);
					  setMobileDataEnabledMethod.invoke(tm,true);
					  if(conman==null||conman.getActiveNetworkInfo()==null)
			            	  conman=(ConnectivityManager)getActivity().getSystemService(Context
									     .CONNECTIVITY_SERVICE);
						  setMobileDataEnabledMethod.setAccessible(true);
						  if(conman.getActiveNetworkInfo()!=null)
							  setMobileDataEnabledMethod.invoke(tm,active=conman.getActiveNetworkInfo().isConnected() ? false : true);
						  else
							  setMobileDataEnabledMethod.invoke(tm,active=true);
			              Toast.makeText(getActivity(),active ? getString(R.string.mobile_on):getString(R.string.mobile_off),Toast.LENGTH_LONG)
			              	   .show();
			              try
			              {
			              if(conman.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
			                  switch(conman.getActiveNetworkInfo().getSubtype())
			                  {
			                  	case TelephonyManager.NETWORK_TYPE_1xRTT:
			                  	case TelephonyManager.NETWORK_TYPE_CDMA:
			                  	case TelephonyManager.NETWORK_TYPE_EDGE:
			                  		mobileImg.setImageResource(R.drawable.mobilee); break;
			                  	case TelephonyManager.NETWORK_TYPE_GPRS:
			                  		mobileImg.setImageResource(R.drawable.mobileg); break;
			                  	case TelephonyManager.NETWORK_TYPE_HSPA:
			                  	case TelephonyManager.NETWORK_TYPE_UMTS:
			                  		mobileImg.setImageResource(R.drawable.mobilehspa); break;
			                  	case TelephonyManager.NETWORK_TYPE_EHRPD:
			                  	case TelephonyManager.NETWORK_TYPE_EVDO_B:
			                  	case TelephonyManager.NETWORK_TYPE_HSDPA:
			                  	case TelephonyManager.NETWORK_TYPE_HSUPA:
			                  		mobileImg.setImageResource(R.drawable.mobile3g); break;
			                  	case TelephonyManager.NETWORK_TYPE_HSPAP:
			                  		mobileImg.setImageResource(R.drawable.mobilehspaplus); break;
			                  	case TelephonyManager.NETWORK_TYPE_LTE:
			                  		mobileImg.setImageResource(R.drawable.mobile4g); break;
			                  	case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			                  	default:
			                  		mobileImg.setImageResource(R.drawable.mobileoff);
			                  		TX.setText("");
			            			RX.setText("");
			                  }
			              }
			              catch(NullPointerException exc)
			              {
			            	  mobileImg.setImageResource(R.drawable.mobileoff);
			            	  TX.setText("");
			      			  RX.setText("");
			              }

			              TelephonyManager data=(TelephonyManager)getActivity()
			            		  			    .getSystemService(Context.TELEPHONY_SERVICE);
                          new MobileDataGeneralTask().execute();
			              try
			              {
			            	  if(data.getLine1Number().equals(""))
			            		  getActivity().setTitle(getString(R.string.mobile));
			            	  else
			            		  getActivity().setTitle(data.getLine1Number());
			              }
			              catch(NullPointerException exc)
			              {
			            	  getActivity().setTitle(getString(R.string.mobile));
			              }
				  }
				  
				}
				catch(IllegalAccessException exc)
				{
					exc.printStackTrace();
				}
				catch (InvocationTargetException _exc)
				{
					setDataManually();
				}
				catch (NoSuchMethodException e)
				{
					e.printStackTrace();
				}
				catch(ClassNotFoundException exc)
				{
					exc.printStackTrace();
				}
				catch(SecurityException exc)
				{
					setDataManually();
				}
				return false;
			}
		});
        return rootView;
    }
	
	private void setMobileData()
	{
		try
        {
		conman=(ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conman.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE)
            switch(conman.getActiveNetworkInfo().getSubtype())
            {
            	case TelephonyManager.NETWORK_TYPE_1xRTT:
            	case TelephonyManager.NETWORK_TYPE_CDMA:
            	case TelephonyManager.NETWORK_TYPE_EDGE:
            		mobileImg.setImageResource(R.drawable.mobilee); break;
            	case TelephonyManager.NETWORK_TYPE_GPRS:
            		mobileImg.setImageResource(R.drawable.mobileg); break;
            	case TelephonyManager.NETWORK_TYPE_HSPA:
            	case TelephonyManager.NETWORK_TYPE_UMTS:
            		mobileImg.setImageResource(R.drawable.mobilehspa); break;
            	case TelephonyManager.NETWORK_TYPE_EHRPD:
            	case TelephonyManager.NETWORK_TYPE_EVDO_B:
            	case TelephonyManager.NETWORK_TYPE_HSDPA:
            	case TelephonyManager.NETWORK_TYPE_HSUPA:
            		mobileImg.setImageResource(R.drawable.mobile3g); break;
            	case TelephonyManager.NETWORK_TYPE_HSPAP:
            		mobileImg.setImageResource(R.drawable.mobilehspaplus); break;
            	case TelephonyManager.NETWORK_TYPE_LTE:
            		mobileImg.setImageResource(R.drawable.mobile4g); break;
            	case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            	default:
            		mobileImg.setImageResource(R.drawable.mobileoff);
            		TX.setText("");
        			RX.setText("");
            }
        else
        {
        	mobileImg.setImageResource(R.drawable.mobileoff);
        	TX.setText("");
			RX.setText("");
        }        
        data=(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        new MobileDataGeneralTask().execute();
        if(data.getLine1Number().equals(""))
        	getActivity().setTitle(getString(R.string.mobile));
        else
        	getActivity().setTitle(data.getLine1Number());
        }
        catch(Exception exc)
        {
      	  mobileImg.setImageResource(R.drawable.mobileoff);
      	  TX.setText("");
		  RX.setText("");
        }
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(mobileData);
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
	
	private void setDataManually()
	{
		AlertDialog.Builder builder=new Builder(getActivity());
		builder.setMessage(getString(R.string.mobile_msg))
		       .setPositiveButton(getString(R.string.ok),new OnClickListener() {
				
		    	   @Override
		    	   public void onClick(DialogInterface dialog, int which)
		    	   {
		    		   Intent intent = new Intent();
		    		   intent.setAction(android.provider.Settings.ACTION_SETTINGS);
					   startActivityForResult(intent,100);
		    		   dialog.dismiss();
		    	   }
		       })
		       .setNegativeButton(getString(R.string.cancel),new OnClickListener() {
				
		    	   @Override
		    	   public void onClick(DialogInterface dialog, int which)
		    	   {
		    		   dialog.dismiss();
		    	   }
			}).show();
	}
	
	@Override
	public void onResume() {
		getActivity().registerReceiver(mobileData,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		super.onResume();
	}
	
	private class MobileDataGeneralTask extends AsyncTask<Void,Void,Void>
	{
		private String ip;
		ArrayList<String> info,value;
		
		public MobileDataGeneralTask()
		{
			ip="";
			info=new ArrayList<String>();
			value=new ArrayList<String>();
		}
		
		@Override
		protected Void doInBackground(Void... params)
		{
		  try
		  {
			boolean done=false;
			for (Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
						 en.hasMoreElements();)
			{
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr=intf.getInetAddresses();
		   						 enumIpAddr.hasMoreElements();)
				{
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()&&
								InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))
						{
									ip=inetAddress.getHostAddress().toString();
									done=true;
									break;
						}
				}
				if(done)
					break;	
			}
            if(data==null)
                 data=(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
			info.add(getString(R.string.ip));
		    value.add(ip);
		    info.add("IMEI");
		    value.add(data.getDeviceId());
		    info.add(getString(R.string.isp));
		    value.add(data.getNetworkOperatorName());
		    publishProgress();
		   }
		   catch(SocketException exc)
		   {
			   
		   }
		   return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
            try
            {
                initCard(info, value);
            }
            catch (NullPointerException exc)
            {
                new MobileDataGeneralTask().execute();
            }
			super.onProgressUpdate(values);
		}
	}
	
    private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
        card=new GeneralInfoCard(getActivity(),info,value,getString(R.string.mobile_info));
        card.init();
        CardView cardView=(CardView)rootView.findViewById(R.id.card_mobile);
        cardView.setCard(card);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode==100)
    		setMobileData();
    	super.onActivityResult(requestCode, resultCode, data);
    }
  }
