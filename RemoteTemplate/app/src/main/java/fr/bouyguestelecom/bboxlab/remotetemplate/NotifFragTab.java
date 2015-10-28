package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by epham on 09/09/15.
 */

public abstract class NotifFragTab
    extends Fragment
{
    @Override
    public void         onCreate(Bundle svdItcSte)
    {
        super.onCreate(svdItcSte);
    }

    /*@Override
    public View         onCreateView(LayoutInflater ift, ViewGroup ctn, Bundle svdItcSte)
    {
        NotificationsAdapter ntfAdp = new NotificationsAdapter(getActivity()
                .getApplicationContext()
                , getArguments(), getActivity());    // We are showing all our received notification in a ListView.
        //ListView listView = (ListView)rootView.findViewById(R.id.listView);

        ListView listView = (ListView)rootView.findViewById(R.id.);

        listView.setAdapter(notificationsAdapter);
    }*/
}