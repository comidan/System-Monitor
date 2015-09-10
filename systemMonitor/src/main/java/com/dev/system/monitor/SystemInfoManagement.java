package com.dev.system.monitor;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import it.gmariotti.cardslib.library.view.CardView;

public class SystemInfoManagement extends Fragment
{
    private View rootView;
    private Activity mainActivity;
    private GeneralInfoCard card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_system_info,container,false);
        mainActivity=getActivity();
        mainActivity.setTitle(getString(R.string.system));
        ArrayList<String> info=new ArrayList<>(),value=new ArrayList<>();
        info.add(getString(R.string.name));
        value.add(getVersionName());
        info.add("Version");
        value.add(Build.VERSION.RELEASE);
        info.add("API");
        value.add(Build.VERSION.SDK);
        info.add("Kernel");
        value.add(System.getProperty("os.version"));
        info.add("Build Number");
        value.add(getBuildNumber());
        info.add("Device");
        value.add(Build.DEVICE);
        info.add(getString(R.string.model));
        value.add(Build.MODEL);
        info.add(getString(R.string.product));
        value.add(Build.PRODUCT);
        initCard(info,value);
        return rootView;
    }

    private String getVersionName()
    {
        switch(Build.VERSION.SDK_INT)
        {
            case 14:
            case 15: return "Ice Cream Sandwich";
            case 16:
            case 17:
            case 18:return "Jelly Bean";
            case 19:return "KitKat";
            case 20:return "KitKat Watch";
            case 21:
            case 22:return "Lollipop";
            case 23:return "Marshmallow";
            default:return "Android";
        }
    }

    private String getBuildNumber()
    {
        String fingerPrint=Build.FINGERPRINT;
        char temp='/';
        for(int i=0,slashes=0;i<fingerPrint.length();i++)
        {
            if (fingerPrint.charAt(i)=='\\'||fingerPrint.charAt(i)=='/')
            {
                slashes++;
                temp=fingerPrint.charAt(i);
            }
            if(slashes==3)
                try
                {
                    return fingerPrint.substring(i + 1, fingerPrint.substring(i + 1).indexOf(temp) + i);
                }
                catch(StringIndexOutOfBoundsException exc)
                {
                    return fingerPrint;                                     //fixed a magic crash
                }
        }
        return getVersionName();
    }

    private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
        card=new GeneralInfoCard(mainActivity,info,value,"Android");
        card.init();
        CardView cardView = (CardView) rootView.findViewById(R.id.card_system_info);
        cardView.setCard(card);
    }
}
