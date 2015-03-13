package com.dev.system.monitor;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;

public class GeneralInfoCard extends CardWithList
{
	private ArrayList<String> info,value;
    private ArrayList<TextView> infoTextViews;
	private String title;
	private Context context;
	
    public GeneralInfoCard(Context context,ArrayList<String> info,ArrayList<String> value,String title)
    {
        super(context);
        this.info=info;
        this.value=value;
        this.title=title;
        this.context=context;
        infoTextViews=new ArrayList<>();
    }

    @Override
    protected CardHeader initCardHeader()
    {
        CardHeader header=new CardHeader(getContext(), R.layout.card_general_info_inner_header);
        header.setTitle(title);
        return header;
    }

    @Override
    protected void initCard()
    {

    }

    @Override
    protected List<ListObject> initChildren()
    {
        List<ListObject> mObjects=new ArrayList<ListObject>();
        for(int i=0;i<info.size();i++)
        {
        	InfoObject infoObject=new InfoObject(this);
        	infoObject.info=info.get(i);
        	infoObject.value=value.get(i);
        	mObjects.add(infoObject);
        }
        return mObjects;
    }

    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent)
    {
        final TextView infoText=(TextView)convertView.findViewById(R.id.card_info_title),
        			   valueText =(TextView)convertView.findViewById(R.id.card_info_value);
        InfoObject infoObject=(InfoObject)object;
        infoText.setText(infoObject.info);
        valueText.setText(infoObject.value);
        infoTextViews.add(valueText);
        convertView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				ClipboardManager clipboard=(ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(valueText.getText().toString());
				Toast.makeText(context,"Copied to clipboard",Toast.LENGTH_SHORT).show();
				return false;
			}
		});
        return  convertView;
    }

    @Override
    public int getChildLayoutId()
    {
        return R.layout.card_general_info_inner_main;
    }

    ArrayList<String> getInfo()
    {
    	return info;
    }
    
    ArrayList<String> getValue()
    {
    	return value;
    }

    ArrayList<TextView> getTextViews(){return infoTextViews;}
    
    public class InfoObject extends DefaultListObject
    {
        public String info;
        public String value;

        public InfoObject(Card parentCard)
        {
            super(parentCard);
            init();
        }

        private void init()
        {
        	
        }
    }
}
