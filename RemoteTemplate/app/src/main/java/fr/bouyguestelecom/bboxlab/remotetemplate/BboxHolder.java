package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.BboxManager;

/**
 * Bbox should never be stored locally to avoid
 */
public class BboxHolder {
    private static final String TAG = BboxHolder.class.getCanonicalName();
    private static String APP_ID = "122-101";
    private static String APP_SECRET = "7EFC9FA674F0450C8A929C243F49FDDE";

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
    public Bbox getBbox() {
        if (mBbox == null) {
            //throw new RuntimeException("No bbox found! Check if a Miami bbox is present in the same network and if the service BboxApi is running.");
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
