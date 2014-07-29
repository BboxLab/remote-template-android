package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by PCHERIER on 16/04/2014.
 */
public class NotificationsAdapter extends ArrayAdapter<String> {

    private List<String> notifications;
    private Context mContext;
    private Activity activity;
    private LayoutInflater vi;

    public NotificationsAdapter(Context context, List<String> objects, Activity activity) {
        super(context, 0, objects);
        mContext = context;
        this.notifications = objects;
        this.activity = activity;
        vi = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = vi.inflate(R.layout.element_notification, null);

        String notification = notifications.get(position);

        TextView text = (TextView) convertView.findViewById(R.id.textView);

        text.setText(notification);

        return convertView;
    }
}
