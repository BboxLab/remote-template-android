package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.Context;
//import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.media.Media;
//import fr.bouyguestelecom.tv.openapi.secondscreen.media.MediaManager;
//import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.CallbackHttpStatus;

public class MediaAdapter extends ArrayAdapter<Media>                                               //Generic type's Array view
{
    private Context                 mCtx;                                                           //Current Activity data
    private List<Media>             mediaLst;                                                       //Generic Type List
    private LayoutInflater          lyIft;                                                          //XML parser to UI
    private Activity                atv;                                                            //Your current screen, duh !

    public MediaAdapter(Context ctx, List<Media> media, Activity atv)
    {
        super(ctx, 0, media);                                                                       //Pre-construction attributes assignation
        mCtx = ctx;
        mediaLst = media;
        this.atv = atv;
        lyIft = (LayoutInflater)this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        //Retrieving the related Context's LayoutInflater for inflating
    }

    @Override
    public View             getView(int pos, View convertView, ViewGroup parent)
    {
        View                                view = convertView;

        final Media                         media = mediaLst.get(pos);                              //Constant; Assign media from the current Media List's position

        if (media != null)
        {
            view = lyIft.inflate(R.layout.element_media, null);                                     //Inflate the "element_media.xml" fragment in the Channel List's "view"

            final TextView                  mediaName = (TextView)view.findViewById(R.id.textView); //Constant; Show the "textView" element from "element_media.xml"
            final TextView                  mediaPos = (TextView)view.findViewById(R.id.textView2); //Constant; Show the "textView2" element from "element_media.xml"

            mediaName.setText(media.getChan());                                                     //Set "textView"'s content to the channel's name

            switch (media.getStatus())                                                              //Channel color setter
            {
                case "play":
                    mediaName.setTextColor(Color.parseColor("#228B22"));
                    break;
                case "stop":
                    mediaName.setTextColor(Color.RED);
                    break;
            }
            mediaPos.setText(media.getPos());                                                       //Set "textView2"'s content to the channel's name
        }
        return (view);
    }
}