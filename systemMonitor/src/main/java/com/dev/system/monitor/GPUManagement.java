package com.dev.system.monitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by daniele on 17/08/2015.
 */
public class GPUManagement extends Fragment
{
    private View rootView;
    private Activity mainActivity;
    private GeneralInfoCard card;
    private HashMap<String,String> gl10,gl20;
    private ArrayList<String> info=new ArrayList(),values=new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_gpu, container, false);
        mainActivity=getActivity();
        mainActivity.setTitle(getString(R.string.gpu));
        DisplayMetrics display = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        Display var7 = ((WindowManager)mainActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DecimalFormat var5 = new DecimalFormat("#.##");
        double var3 = (double)((float)display.widthPixels / display.xdpi);
        double var1 = (double)((float)display.heightPixels / display.ydpi);
        var1 = Math.sqrt(var3 * var3 + var1 * var1);
        info.add("Resolution");
        values.add(display.heightPixels+" x "+display.widthPixels);
        info.add("Screen size");
        values.add(var5.format(var1) + "\" - " + var5.format(2.54D * var1) + " cm");
        info.add("Pixel density");
        values.add((int)display.xdpi + " ppi");
        info.add("Dots density");
        values.add(display.densityDpi + " dpi");
        info.add("Refresh rate");
        values.add(var5.format((double)var7.getRefreshRate()) + " Hz");
        initCard(info,values);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final GPU gpu=new GPU(mainActivity);
        gpu.loadOpenGLGles10Info(new GPU.OnCompleteCallback<GPU.OpenGLGles10Info>() {

            @Override
            public void onComplete(final GPU.OpenGLGles10Info info) {
                gl10 = info.getSummary();
            }
        });
        gpu.loadOpenGLGles20Info(new GPU.OnCompleteCallback<GPU.OpenGLGles20Info>() {

            @Override
            public void onComplete(final GPU.OpenGLGles20Info info) {
                gl20 = info.getSummary();
                printResults();
            }
        });
    }

    private void printResults()
    {
        HashMap<String,String> realValues;
        if(gl20!=null&&!gl20.get("VENDOR").equals(""))
            realValues=gl20;
        else
            realValues=gl10;
        info.add("Renderer");
        values.add(realValues.get("RENDERER"));
        info.add("Vendor");
        values.add(realValues.get("VENDOR"));
        info.add("Version");
        values.add(realValues.get("VERSION").substring(0,13)); //OpenGL ES X.X
        initCard(info,values);
    }

    private void initCard(ArrayList<String> info,ArrayList<String> value)
    {
        card=new GeneralInfoCard(mainActivity,info,value,"GPU Information");
        card.init();
        CardView cardView = (CardView) rootView.findViewById(R.id.card_gpu);
        cardView.setCard(card);
    }
}
