package com.dev.system.monitor;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.fortysevendeg.swipelistview.SwipeListView;
import java.util.List;

public class PackageAdapter extends BaseAdapter
{
    private List<PackageItem> data;
    private Context context;
    private SwipeListView swipeListView;
    
    public PackageAdapter(final Context context, List<PackageItem> data,SwipeListView swipeListView) {
        this.context=context;
        this.data=data;
        this.swipeListView=swipeListView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public PackageItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PackageItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.package_row, parent, false);
            holder = new ViewHolder();
            holder.ivImage=(ImageView)convertView.findViewById(R.id.example_row_iv_image);
            holder.tvTitle=(TextView)convertView.findViewById(R.id.example_row_tv_title);
            holder.tvDescription=(TextView)convertView.findViewById(R.id.example_row_tv_description);
            holder.ramUsage=(TextView)convertView.findViewById(R.id.textView1);
            holder.bAction1=(Button)convertView.findViewById(R.id.example_row_b_action_1);
            holder.bAction2=(Button)convertView.findViewById(R.id.example_row_b_action_2);
            holder.bAction3=(Button)convertView.findViewById(R.id.example_row_b_action_3);
            convertView.setTag(holder);
        }
        else 
            holder = (ViewHolder) convertView.getTag();
        ((SwipeListView)parent).recycle(convertView, position);
        holder.ivImage.setImageDrawable(item.getIcon());
        holder.tvTitle.setText(item.getName());
        holder.tvDescription.setText(item.getPackageName());
        holder.ramUsage.setText(item.getMemoryInKByte()/1024+" MB");
        holder.bAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	ActivityManager activityManager=(ActivityManager)context.getSystemService("activity");
                activityManager.killBackgroundProcesses(item.getPackageName());
                data.remove(item);
                notifyDataSetChanged();
               	swipeListView.closeOpenedItems();
                Toast.makeText(context,item.getName()+" closed",Toast.LENGTH_LONG).show();
            }
        });

        holder.bAction2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + item.getPackageName()));
                context.startActivity(intent);
            }
        });

        holder.bAction3.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(14)
            public void onClick(View v) {
                Uri packageUri = Uri.parse("package:" + item.getPackageName());
                Intent uninstallIntent;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                } else {
                    uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                }
                context.startActivity(uninstallIntent);
                data.remove(item);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    static class ViewHolder {  //per viewholder
        ImageView ivImage;
        TextView tvTitle;
        TextView tvDescription;
        TextView ramUsage;
        TextView cpuUsage;
        Button bAction1;
        Button bAction2;
        Button bAction3;
    }
}

