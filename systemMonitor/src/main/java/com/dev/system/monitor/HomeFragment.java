package com.dev.system.monitor;

import com.dev.system.monitor.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment
{
	public HomeFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		getActivity().setTitle("System Monitor");
		return inflater.inflate(R.layout.changelog_list,container,false);
    }
}
