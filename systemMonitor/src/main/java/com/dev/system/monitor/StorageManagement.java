package com.dev.system.monitor;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import it.gmariotti.cardslib.library.view.CardView;

public class StorageManagement extends Fragment
{
    private View rootView;
    private Activity mainActivity;
    private GeneralInfoCard card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_storage,container,false);
        mainActivity=getActivity();
        mainActivity.setTitle(getString(R.string.storage));
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long bytesAvailable = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
        long kbAvailable = bytesAvailable / 1024;
        long bytesTotal=(long)stat.getBlockSize() *(long)stat.getBlockCount();
        long kbTotal=bytesTotal / 1024;
        ArrayList<String> info=new ArrayList<>(),values=new ArrayList<>();
        info.add(getString(R.string.total_mem));
        values.add(getMostSuitableSizeUnit(kbTotal));
        info.add(getString(R.string.available_mem));
        values.add(getMostSuitableSizeUnit(kbAvailable));
        ((TextView)rootView.findViewById(R.id.stat_main_storage)).setText((new DecimalFormat("#.##").format(100-((kbTotal-kbAvailable)/(double)kbTotal)*100)+" %"));
        initCard(info, values,getString(R.string.internal_storage), R.id.card_main_storage);
        PieGraph pieGraph=(PieGraph)rootView.findViewById(R.id.graph_main_storage);
        PieSlice slice=new PieSlice(),_slice=new PieSlice();
        slice.setColor(Color.parseColor("#99CC00"));
        slice.setValue(kbAvailable);
        _slice.setColor(Color.parseColor("#FFBB33"));
        _slice.setValue(kbTotal-kbAvailable);
        pieGraph.addSlice(slice);
        pieGraph.addSlice(_slice);
        pieGraph.setInnerCircleRatio(150);
        for(PieSlice s : pieGraph.getSlices())
            s.setGoalValue(s.getValue());
        pieGraph.setDuration(1000);
        pieGraph.setInterpolator(new AccelerateDecelerateInterpolator());
        pieGraph.animateToGoalValues();
        String sdPath;
        if((sdPath=getExternalStoragePath())!=null)
        {
            stat = new StatFs(new File(sdPath).getPath());
            bytesAvailable = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
            kbAvailable = bytesAvailable / 1024;
            bytesTotal=(long)stat.getBlockSize() *(long)stat.getBlockCount();
            kbTotal=bytesTotal/ 1024;
            info.clear();
            values.clear();
            info.add(getString(R.string.total_mem));
            values.add(getMostSuitableSizeUnit(kbTotal));
            info.add(getString(R.string.available_mem));
            values.add(getMostSuitableSizeUnit(kbAvailable));
            ((TextView)rootView.findViewById(R.id.stat_extra_storage)).setText((new DecimalFormat("#.##").format(100-((kbTotal-kbAvailable)/(double)kbTotal)*100)+" %"));
            initCard(info,values,getString(R.string.external_storage),R.id.card_extra_storage);
            pieGraph=(PieGraph)rootView.findViewById(R.id.graph_extra_storage);
            slice=new PieSlice();
            _slice=new PieSlice();
            slice.setColor(Color.parseColor("#99CC00"));
            slice.setValue(kbAvailable);
            _slice.setColor(Color.parseColor("#FFBB33"));
            _slice.setValue(kbTotal-kbAvailable);
            pieGraph.addSlice(slice);
            pieGraph.addSlice(_slice);
            pieGraph.setInnerCircleRatio(150);
            for(PieSlice s : pieGraph.getSlices())
                s.setGoalValue(s.getValue());
            pieGraph.setDuration(1000);
            pieGraph.setInterpolator(new AccelerateDecelerateInterpolator());
            pieGraph.animateToGoalValues();
        }
        else
        {
            ((PieGraph)rootView.findViewById(R.id.graph_extra_storage)).setVisibility(View.GONE);
            ((CardView)rootView.findViewById(R.id.card_extra_storage)).setVisibility(View.GONE);
        }
        return rootView;
    }

    private void initCard(ArrayList<String> info,ArrayList<String> value,String title,int id)
    {
        card=new GeneralInfoCard(mainActivity,info,value,title);
        card.init();
        CardView cardView = (CardView) rootView.findViewById(id);
        cardView.setCard(card);
    }

    private String getExternalStoragePath()
    {
        final String[] paths={"/emmc",
                              "/mnt/sdcard/external_sd",
                              "/storage/extSdCard",
                              "/mnt/extSdCard/",
                              "/storage/extSdCard/",
                              "/mnt/external_sd",
                              "/sdcard/sd",
                              "/mnt/sdcard/bpemmctest",
                              "/mnt/sdcard/_ExternalSD",
                              "/mnt/sdcard-ext",
                              "/mnt/Removable/MicroSD",
                              "/Removable/MicroSD",
                              "/mnt/external1",
                              "/mnt/extSdCard",
                              "/mnt/extsd",
                              "/storage/sdcard1",
                              "/storage/extsdcard",
                              "/storage/sdcard0/external_sdcard",
                              "/mnt/extsdcard",
                              "/mnt/media_rw/sdcard1",
                              "/removable/microsd",
                              "/mnt/emmc",
                              "/storage/external_SD",
                              "/storage/ext_sd",
                              "/storage/removable/sdcard1",
                              "/data/sdext",
                              "/data/sdext2",
                              "/data/sdext3",
                              "/data/sdext4"};
        for(int i=0;i<paths.length;i++)
            if(new File(paths[i]).exists())
                return paths[i];
        return null;
    }

    private String getMostSuitableSizeUnit(double valueInKB)
    {
        double temp;
        if((temp=valueInKB/(1024*1024))>0)
            return new DecimalFormat("#.##").format(temp)+" GB";
        else if((temp=valueInKB/1024)>0)
            return new DecimalFormat("#.##").format(temp)+" MB";
        else
            return new DecimalFormat("#.##").format(valueInKB)+" KB";
    }
}
