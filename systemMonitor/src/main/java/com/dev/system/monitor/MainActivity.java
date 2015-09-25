package com.dev.system.monitor;

import java.util.ArrayList;
import uk.me.lewisdeane.lnavigationdrawer.NavigationItem;
import uk.me.lewisdeane.lnavigationdrawer.NavigationListView;
import uk.me.lewisdeane.lnavigationdrawer.NavigationListView.NavigationItemClickListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity
{
	private DrawerLayout drawerLayout;
	private CharSequence drawerTitle;
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private NavigationListView navigationListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		if(Build.VERSION.SDK_INT>=19)
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                		     	 WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
    		     				 WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			SystemBarTintManager tintManager=new SystemBarTintManager(this);
		    tintManager.setStatusBarTintEnabled(true);
		    tintManager.setNavigationBarTintEnabled(true);
		    tintManager.setTintStatusBarColor(Color.parseColor("#318CE7"));
		    tintManager.setTintNavigationBarColor(Color.parseColor("#ff000000"));
		}
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#318CE7")));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#318CE7")));
		setContentView(R.layout.activity_main);
		final boolean firstrun=getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("firstrun",true);
		int runs=getSharedPreferences("PREFERENCE",MODE_PRIVATE).getInt("RUNS",0);
		if(runs%6==0)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("System Monitor Pro is available!");
			builder.setMessage("Check it out!");
			builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dev.system.pro.pro")));
				}
			});
			builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
		}
		runs++;
		getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putInt("RUNS",runs).apply();
		navMenuTitles=getResources().getStringArray(R.array.nav_drawer_items);
		navMenuIcons=getResources().obtainTypedArray(R.array.nav_drawer_icons);
		drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
	    navigationListView=(NavigationListView)findViewById(R.id.list_slidermenu);
	    navigationListView.setBackgroundColor(Color.WHITE);
	    if(Build.VERSION.SDK_INT>=19)
		{
			SystemBarTintManager.SystemBarConfig config = new SystemBarTintManager(this).getConfig();
			navigationListView.setPadding(0,config.getPixelInsetTop(true),config.getPixelInsetRight(),config.getPixelInsetBottom());
		}
		for(int i=0;i<navMenuTitles.length;i++)
			navigationListView.addNavigationItem(navMenuTitles[i],navMenuIcons.getResourceId(i,-1),false);
		navigationListView.setNavigationItemClickListener(new NavigationItemClickListener(){
			@Override
			public void onNavigationItemSelected(String item,ArrayList<NavigationItem> items, int position) {
				Fragment fragment=null;
				switch (position)
				{
				case 0:
					fragment=new HomeFragment();
					break;
				case 1:
					fragment=new WiFiManagement();
					break;
				case 2:
					fragment=new MobileManagement();
					break;
				case 3:
					fragment=new BluetoothManagement();
					break;
				case 4:
					fragment=new GPSManagement();
					break;
				case 5:
					fragment=new StorageManagement();
					break;
                case 6:
					fragment=new CPUManagement();
                    break;
				case 7:
					fragment=new RAMManagement();
					break;
				case 8:
					fragment=new GPUManagement();
					break;
                case 9:
                    fragment=new SensorManagement();
                    break;
				case 10:
					fragment=new AppManagement();
					break;
				case 11:
					fragment=new BatteryManagement();
                    break;
                case 12:
                    fragment=new SystemInfoManagement();
                    break;
				default:
					break;
				}
				if (fragment != null)
				{
					FragmentManager fragmentManager=getFragmentManager();
					fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
					navigationListView.setItemChecked(position,true);
					navigationListView.setSelection(position);
					drawerLayout.closeDrawer(navigationListView);
				}
			}
		});
		navMenuIcons.recycle();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,
            drawerArrow, R.string.drawer_open,
            R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
        drawerLayout.setFocusableInTouchMode(false);
        mDrawerToggle.syncState();
        mDrawerToggle.setAnimateEnabled(true);
        mDrawerToggle.syncState();
		if (savedInstanceState==null)
		{
			Fragment fragment = new HomeFragment();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();
			navigationListView.setItemChecked(0,true);
			navigationListView.setSelection(0);
			drawerLayout.closeDrawer(navigationListView);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent=new Intent(MainActivity.this,AppInfo.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen=drawerLayout.isDrawerOpen(navigationListView);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		getActionBar().setTitle(title);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(navigationListView))
            finish();
        else
            drawerLayout.openDrawer(navigationListView);
    }
}
