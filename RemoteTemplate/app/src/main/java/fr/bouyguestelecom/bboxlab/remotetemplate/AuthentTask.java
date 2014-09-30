package fr.bouyguestelecom.bboxlab.remotetemplate;

/**
 * Created by fab on 18/09/2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.Auth;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;

public class AuthentTask extends AsyncTask<IAuthCallback, Integer, Void> {
    private static final String TAG = AuthentTask.class.getName();

    //Should be replaced by your specific value
    private final static String appId = "remoteTemplateId";

    //Should be replaced by your specific value
    private final static String appSecret = "MySecretToken";

    @Override
    protected Void doInBackground(IAuthCallback... params) {

        final SharedPreferences preference = MyActivity.mainActivity.getPreferences(Context.MODE_PRIVATE);

        final String bboxSavedIp = preference.getString("bboxIP", null);
        Auth auth = Auth.createInstance(
                MyActivity.mContex, bboxSavedIp);
        auth.authenticate(appId, appSecret, params[0]);

        return null;
    }
}