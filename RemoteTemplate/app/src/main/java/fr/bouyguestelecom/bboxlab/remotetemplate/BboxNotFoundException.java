package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by tristan on 09/11/15.
 */
public class BboxNotFoundException extends Exception {

    public BboxNotFoundException() {
        super("No bbox found! Check if a Miami bbox is present in the same network and if the service BboxApi is running.");
    }

    public void toast (Context context) {
        Toast toast = Toast.makeText(context, "No Bbox found. Please make sure the box is on the same network with the service BboxApi running.", Toast.LENGTH_LONG);
        toast.show();
    }
}
