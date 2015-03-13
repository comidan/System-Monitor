package com.dev.system.monitor;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.view.WindowManager;

public class AppInfo extends PreferenceActivity
{
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
        actionBar.setHomeButtonEnabled(true);
        if(Build.VERSION.SDK_INT>=19)
		{
			SystemBarTintManager.SystemBarConfig config = new SystemBarTintManager(this).getConfig();
			getListView().setPadding(20,config.getPixelInsetTop(true),20,config.getPixelInsetBottom());
		}
		addPreferencesFromResource(R.xml.settings);
		Preference appLink=(Preference)findPreference("app"),
				   gplus=(Preference)findPreference("gplus"),
				   facebook=(Preference)findPreference("facebook"),
                   twitter=(Preference)findPreference("twitter");
		appLink.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				startActivity(new Intent(Intent.ACTION_VIEW,
							      Uri.parse("http://play.google.com/store/apps/details?id="+"com.dev.system.monitor")));
				return false;
			}
		});
		gplus.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW,
					      Uri.parse("https://plus.google.com/u/0/+danielecomi/about")));
				return false;
			}
		});
		facebook.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW,
					      Uri.parse("https://www.facebook.com/daniele.comi.16")));
				return false;
			}
		});

        twitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/TheDeveloper01")));
                return false;
            }
        });
	}
}
