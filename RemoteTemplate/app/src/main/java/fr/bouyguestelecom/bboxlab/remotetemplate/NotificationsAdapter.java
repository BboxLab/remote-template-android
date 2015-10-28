package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

public class NotificationsAdapter
        extends ArrayAdapter<String>
{
    private List<String>                            ntfAls;
    private Context                                 na_ctx;
    private Activity                                na_atv;
    private LayoutInflater                          lyIft;

    public NotificationsAdapter(Context ctx, List<String> obj, Activity atv)
    {
        super(ctx, 0, obj);
        na_ctx = ctx;
        this.ntfAls = obj;
        this.na_atv = atv;
        lyIft = (LayoutInflater)this.na_ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View             getView(int pos, View cvtView, ViewGroup prt)
    {
        cvtView = lyIft.inflate(R.layout.element_notification, null);
        String              ntf = ntfAls.get(pos);
        TextView            txt = (TextView)cvtView.findViewById(R.id.textView);

        txt.setText(ntf);

        return (cvtView);
    }
}

/*public class NotificationsAdapter
        extends ArrayAdapter<String>
{
    private List<HashMap<String, String>>           ntfAls;
    private Context                                 na_ctx;
    private Activity                                na_atv;
    private LayoutInflater                          lyIft;
    private int                                     i;
    private int                                     y;

    public NotificationsAdapter(Context ctx, List<HashMap<String,String>> lst, Activity atv)
    {
        super(ctx, 0);
        na_ctx = ctx;
        this.ntfAls = lst;
        this.na_atv = atv;
        lyIft = (LayoutInflater)this.na_ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.i = 0;
        this.y = 0;
    }

    @Override
    public View             getView(int position, View convertView, ViewGroup parent)
    {
        HashMap<String, String>             hsm = ntfAls.get(i);
        for (HashMap.Entry<String, String> mapEntry : hsm.entrySet())
        {
            //TextView                        ntfKey = new TextView(na_ctx);
            TextView                        ntf = (TextView)convertView.findViewById(R.id.textView);
            ntf.setText(mapEntry.getKey() + ": " + mapEntry.getValue() + "\r\n");
            //layout.addView(tv);
        }
        i++;
        convertView = lyIft.inflate(R.layout.element_notification, null);



        //String              notification = notifications.get(position);
        //TextView            text = (TextView)convertView.findViewById(R.id.textView);

        //text.setText(notification);

        return (convertView);
    }
}*/