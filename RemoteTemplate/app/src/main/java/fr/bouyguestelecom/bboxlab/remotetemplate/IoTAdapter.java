package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.iot.IoTScan;

public class IoTAdapter extends ArrayAdapter<IoTScan>                                                   //Generic type's Array view
{
    private Context                 mCtx;                                                           //Current Activity data
    private List<IoTScan>           iotLst;                                                           //Generic Type List
    private LayoutInflater          lyIft;                                                          //XML parser to UI
    private Activity                atv;                                                            //Your current screen, duh !

    public IoTAdapter(Context ctx, List<IoTScan> iot, Activity atv)
    {
        super(ctx, 0, iot);                                                                       //Pre-construction attributes assignation
        mCtx = ctx;
        iotLst = iot;
        this.atv = atv;
        lyIft = (LayoutInflater)this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        //Retrieving the related Context's LayoutInflater for inflating
    }

    @Override
    public View             getView(int pos, View convertView, ViewGroup parent)
    {
        View      view = convertView;

        final IoTScan           iot;                                    //Constant; Assign media from the current Media List's position
        String                  iotFuncStr;
        StringBuilder           strBld;

        iot = iotLst.get(pos);
        strBld = new StringBuilder();

        Bbox bbox = BboxHolder.getInstance().getBbox();

        if (iot != null)
        {
            view = lyIft.inflate(R.layout.element_iot, null);                                     //Inflate the "element_media.xml" fragment in the Channel List's "view"

            final TextView                  iotBrand;  //Constant; Show the "textView" element from "element_media.xml"
            final TextView                  iotName;
            final TextView                  iotMAC;  //Constant; Show the "textView2" element from "element_media.xml"
            final TextView                  iotFunc;

            int                             i;
            int                             y;

            iotBrand = (TextView)view.findViewById(R.id.textView);
            iotName = (TextView)view.findViewById(R.id.textView2);
            iotMAC = (TextView)view.findViewById(R.id.textView3);
            //iotFunc = (TextView)view.findViewById(R.id.textView4);
            i = 0;
            y = iot.getGncDvc().getSmtFct().size() - 1;

            iotBrand.setText(iot.getGncDvc().getMnf());
            iotName.setText(iot.getGncDvc().getName());                                                     //Set "textView"'s content to the channel's name
            iotMAC.setText(iot.getMAC());

            for (String s : iot.getGncDvc().getSmtFct())
            {
                strBld.append(s);
                if (i < y)
                    strBld.append(", ");
            }
            iotFuncStr = strBld.toString();

            //iotFunc.setText(iotFuncStr);
        }
        return (view);
    }
}