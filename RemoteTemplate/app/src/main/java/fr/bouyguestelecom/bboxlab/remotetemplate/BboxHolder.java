package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.BboxManager;

/**
 * Bbox should never be stored locally to avoid
 */
public class BboxHolder {
    private static final String TAG = BboxHolder.class.getCanonicalName();
    private static String APP_ID = "";
    private static String APP_SECRET = "";

    public static BboxHolder mInstance = new BboxHolder();
    private Bbox mBbox;
    private BboxManager bboxManager = new BboxManager();

    /**
     * Singleton: private constructor. Instance must be retrieved with getInstance method
     */
    private BboxHolder(){
    }

    public BboxManager getBboxManager()
    {
        return bboxManager;
    }

    public void bboxSearch(final Context context, final IAuthCallback callback) {

        Log.i("BboxManager", "Start looking for Bbox");
        bboxManager.startLookingForBbox(context, new BboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final Bbox bboxFound) {

                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();

                // We save our Bbox.
                mBbox = bboxFound;

                Log.i(TAG, "Bbox found: " + mBbox.getIp());

                mBbox.authenticate(APP_ID, APP_SECRET, callback);
            }
        });

    }
    /**
     * set the current bbox
     *
     * @param ip      bbox ip
     */
    public void setCustomBbox(String ip) {
        mBbox = new Bbox(ip);
    }

    /**
     *
     * Do authentication.
     *
     * @param appId     application id.
     * @param appSecret application secret.
     * @param callback  callback when done.
     */
    public void authenticate(String appId, String appSecret, IAuthCallback callback) {
        if (mBbox != null) {
            mBbox.authenticate(appId, appSecret, callback);
        }
    }

    /**
     * Return the current bbox. null if not correctly initialized !
     *
     * @return the bbox.
     */
    public Bbox getBbox() throws BboxNotFoundException {
        if (mBbox == null) {
            throw new BboxNotFoundException();
        }
        return mBbox;
    }

    public static BboxHolder getInstance() {
        return mInstance;
    }

    public interface IBboxSearchCallback {
        public void onBboxFound();
    }
}
