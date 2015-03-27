package com.dev.system.monitor;

import it.gmariotti.cardslib.library.view.CardView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dev.system.monitor.R;
import com.faizmalkani.floatingactionbutton.Fab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GPSManagement extends Fragment
{
	private LocationManager locationManager;
	private View rootView;
	private ImageView locationImg;
	private BroadcastReceiver locationReceiver;
	private GeneralInfoCard card;
	private ArrayList<String> info,value;
	private Activity mainActivity;
	private int card_update=-1;
	private boolean GPS_Status;
	
	@Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		rootView=inflater.inflate(R.layout.fragment_gps,container,false);
		mainActivity=getActivity();
        mainActivity.setTitle("Global Position System");
        Fab mFab=(Fab)rootView.findViewById(R.id.fabbutton);
        mFab.setFabColor(Color.WHITE);
        mFab.setFabDrawable(getResources().getDrawable(android.R.drawable.ic_menu_compass));
        mFab.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				try
				{
					if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						Uri uri = Uri.parse("geo:"+card.getValue().get(card.getInfo().indexOf("Latitude"))+","
												  +card.getValue().get(card.getInfo().indexOf("Longitude"))
												  +"(Your position)");
						Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}
				catch(Exception exc)
				{
					
				}
			}
		});
        mFab.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		        builder.setMessage(getString(R.string.gps_msg)).show();
				return false;
			}
		});
        mFab.showFab();
        locationImg=(ImageView)rootView.findViewById(R.id.signalMobileImg);
        locationManager=(LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
        	locationImg.setImageResource(R.drawable.satellite);
        	GPS_Status=true;
        }
        else
        {
        	locationImg.setImageResource(R.drawable.satelliteoff);
        	GPS_Status=false;
        }
        locate();
        locationReceiver=new BroadcastReceiver()
        {
        	@Override
        	public void onReceive(Context context, Intent intent)
        	{
                locationManager=(LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                	locationImg.setImageResource(R.drawable.satellite);
                	GPS_Status=true;
                }
        	}
        };
        mainActivity.registerReceiver(locationReceiver,new IntentFilter(LocationManager
        																 .PROVIDERS_CHANGED_ACTION));
        locationImg.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v)
			{
				Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
				try
				{
					mainActivity.sendBroadcast(intent);
					if(intent.getBooleanExtra("enabled",false)||!GPS_Status)
						locate();
				}
				catch(SecurityException exc)
				{
					AlertDialog.Builder builder=new Builder(mainActivity);
					builder.setMessage(GPS_Status ? getString(R.string.gps_msg_true) : getString(R.string.gps_msg_false))
					       .setPositiveButton(getString(R.string.ok),new OnClickListener() {
							
					    	   @Override
					    	   public void onClick(DialogInterface dialog, int which)
					    	   {
					    		   startActivityForResult(new Intent(android.provider.Settings
					    				   							.ACTION_LOCATION_SOURCE_SETTINGS),100);
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
				return false;
			}
		});
        return rootView;
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==100)
		{
	        locationManager=(LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);
	        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	        {
	        	locationImg.setImageResource(R.drawable.satellite);
	        	GPS_Status=true;
	        }
	        else
	        {
	        	locationImg.setImageResource(R.drawable.satelliteoff);
	        	GPS_Status=false;
	        }
			locate();
		}
	}
	
	private void locate()
	{
		locationManager=(LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria=new Criteria();
        Location location=locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria,false));
        if(location!=null)
        	new GPSTask().execute(location);
        else
        {
          if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
          {
              if(isAdded()) {
                  info = new ArrayList<String>();
                  value = new ArrayList<String>();
                  info.add(getString(R.string.latitude));
                  value.add(getString(R.string.linking));
                  info.add(getString(R.string.longitude));
                  value.add(getString(R.string.linking));
                  info.add(getString(R.string.altitude));
                  value.add(getString(R.string.linking));
                  info.add(getString(R.string.speed));
                  value.add(getString(R.string.linking));
                  info.add(getString(R.string.precision));
                  value.add(getString(R.string.linking));
                  info.add(getString(R.string.satellites));
                  value.add(getString(R.string.linking));
                  if (card_update != 0)
                      initCard(info, value);
                  card_update = 0;
              }
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2500,1,new LocationListener() {
				
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					
					
				}
				
				@Override
				public void onProviderEnabled(String provider) {
					if(isAdded()) {
                        info = new ArrayList<String>();
                        value = new ArrayList<String>();
                        info.add(getString(R.string.latitude));
                        value.add(getString(R.string.linking));
                        info.add(getString(R.string.longitude));
                        value.add(getString(R.string.linking));
                        info.add(getString(R.string.altitude));
                        value.add(getString(R.string.linking));
                        info.add(getString(R.string.speed));
                        value.add(getString(R.string.linking));
                        info.add(getString(R.string.precision));
                        value.add(getString(R.string.linking));
                        info.add(getString(R.string.satellites));
                        value.add(getString(R.string.linking));
                        if (card_update != 1)
                            initCard(info, value);
                        card_update = 1;
                    }
				}
				
				@Override
				public void onProviderDisabled(String provider) {
                    if(isAdded()) {
                        info = new ArrayList<String>();
                        value = new ArrayList<String>();
                        info.add(getString(R.string.latitude));
                        value.add(getString(R.string.no_link));
                        info.add(getString(R.string.longitude));
                        value.add(getString(R.string.no_link));
                        info.add(getString(R.string.altitude));
                        value.add(getString(R.string.no_link));
                        info.add(getString(R.string.speed));
                        value.add(getString(R.string.no_link));
                        info.add(getString(R.string.precision));
                        value.add(getString(R.string.no_link));
                        info.add(getString(R.string.satellites));
                        value.add(getString(R.string.no_link));
                        if (card_update != 2)
                            initCard(info, value);
                        card_update = 2;
                    }
					
				}
				
				@Override
				public void onLocationChanged(Location location) {
					new GPSTask().execute(location);
				}
			});
          }
          else if(isAdded())
          {
        	 info=new ArrayList<String>();
         	 value=new ArrayList<String>();
             info.add(getString(R.string.latitude));
             value.add(getString(R.string.no_link));
             info.add(getString(R.string.longitude));
             value.add(getString(R.string.no_link));
             info.add(getString(R.string.altitude));
             value.add(getString(R.string.no_link));
             info.add(getString(R.string.speed));
             value.add(getString(R.string.no_link));
             info.add(getString(R.string.precision));
             value.add(getString(R.string.no_link));
             info.add(getString(R.string.satellites));
             value.add(getString(R.string.no_link));
             if(card_update!=3)
 	        	initCard(info,value);
 	        card_update=3;
          }
        }
	}
	
	@Override
	public void onPause() {
		mainActivity.unregisterReceiver(locationReceiver);
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
		mainActivity.registerReceiver(locationReceiver,new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
		super.onResume();
	}
	
	private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
        if(isAdded()) {
            card = new GeneralInfoCard(mainActivity, info, value, getString(R.string.gps));
            card.init();
            CardView cardView = (CardView) rootView.findViewById(R.id.card_mobile);
            cardView.setCard(card);
        }
    }
	
	private class GPSTask extends AsyncTask<Location,Void,Void>
	{
		private ArrayList<String> info,value;
		
		public GPSTask()
		{
			info=new ArrayList<String>();
			value=new ArrayList<String>();
		}
		
		@Override
		protected Void doInBackground(Location... params)
		{
            try
            {
                if(isAdded())
                {
                    info.add(getString(R.string.latitude));
                    value.add(params[0].getLatitude() + "");
                    info.add(getString(R.string.longitude));
                    value.add(params[0].getLongitude() + "");
                    info.add(getString(R.string.altitude));
                    value.add(new DecimalFormat("#.###").format(params[0].getAltitude()) + " m");
                    info.add(getString(R.string.speed));
                    value.add(params[0].getSpeed() * 3.6 + " Km/h");
                    info.add(getString(R.string.precision));
                    value.add(params[0].getAccuracy() + " m");
                    info.add(getString(R.string.satellites));
                    value.add(params[0].getExtras().getInt("satellites") + "");
                }
                else
                {
                    info.add("Latitude");
                    value.add(params[0].getLatitude() + "");
                    info.add("Longitude");
                    value.add(params[0].getLongitude() + "");
                    info.add("Altitude");
                    value.add(new DecimalFormat("#.###").format(params[0].getAltitude()) + " m");
                    info.add("Speed");
                    value.add(params[0].getSpeed() * 3.6 + " Km/h");
                    info.add("Precision");
                    value.add(params[0].getAccuracy() + " m");
                    info.add("Satellites");
                    value.add(params[0].getExtras().getInt("satellites") + "");
                }
            }
            catch(IllegalStateException exc)
            {
                try
                {
                    Intent intent = mainActivity.getIntent();
                    mainActivity.finish();
                    startActivity(intent);
                }
                catch(Exception _exc)
                {
                    //errors due to bad activity life cycle
                }
            }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			initCard(info,value);
		}
	}
}