package fr.bouyguestelecom.tv.openapi.secondscreen.authenticate;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import fr.bouyguestelecom.tv.openapi.secondscreen.apiutils.BboxApiUrl;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.PFSRestClient;

/**
 * This class is the main entry point for all authentication functionality
 * Every single application must register through this process before trying to communicate with the openAPI
 * Created by fab on 01/09/2014.
 */
public class Auth {

    private final String LOG_TAG = getClass().toString();

    private static Auth instance = null;
    private Context mContext = null;
    private String sessionId = null;
    private String token = null;
    private String mbboxIp = null;

    private static final String xSessionId = "x-sessionid";
    private static final String xToken = "x-token";

    /**
     * Retrieve the unique instance of this singleton
     * @return value may be null if createInstance has not been called before
     */
    public static Auth getInstance()
    {
        return instance;
    }

    /**
     * Create the authorization singleton if needed
     * @param ctx : Context of the android application
     * @param bboxIp : bboxIp to be used in the second authentication step
     * @return the singleton instance to be used to authenticate the application
     */
    public static synchronized Auth createInstance(Context ctx, String bboxIp) {
        if(instance == null) {
            instance = new Auth(ctx, bboxIp);
        }
        return instance;
    }

    /**
     * Private constructor, use @see #createInstance(Context ctx, String bboxIp) instead
     * @param ctx
     * @param bboxIp
     */
    private Auth(Context ctx, String bboxIp) {
        mContext = ctx;
        mbboxIp = bboxIp;
    }

    /**
     *
     * @return the sessionId (this session id must be used in every function call
     * between this application and the OpenAPI. To do this the sessionId must be add as an http header
     * in the communication with the OpenAPI
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     *
     * @return the security token provided by the bouyguestelecom external platform. this
     * security token will be processed by the OpenAPI. if this token is "a good one", the openAPI
     * will provide in return a sessionId that must be use in every futher cummincation with
     * the OpenAPI @see{getSessionId}
     */
    public String getSecurityToken() {
        return token;
    }

    /**
     * This is the main entry point, this function will do all the stuff for you in
     * an asynchronous way.
     * appId and secretId make a pair of parameters used by a remote bouyguestelecom platform to decide if you are
     * legitimate to use OpenAPI. This pair (unique by application type) must have been provided to
     * bouyguestelecom prior to any use, if this is not the case the remote platform could not identify you
     * @param appId : the application identifier supplied by you to bouyguestelecom (must be unique per application type)
     * @param secretId : the secretId supplied by you to bouyguestelecom
     * @param callback : an IAuthCallBack object that will be triggered at the end of the authorization process
     */
    public void authenticate(String appId, String secretId, IAuthCallback callback)
    {
        sessionId = null;
        innerAuthenticate(appId, secretId, callback);
    }

    /**
     * This method should be use only in the case of the communication broke with the bbox or if your sessionId is to old. In this case, no
     * need to identify against the remote platform again, just call this method and you should be authorized to communicate with
     * the bbox for another period of time.
     * @param callback : an IAuthCallBack object that will be triggered at the end of the authorization process
     */
    public void connectOnly(final IAuthCallback callback)
    {
        BboxRestClient restClient = new BboxRestClient(mbboxIp, mContext);

        JSONObject body = new JSONObject();

        try {
            body.put("sessionId", token);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        restClient.post(BboxApiUrl.SECURE_CONNECT, body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (statusCode == 401){
                    Log.e(LOG_TAG, "Application is not authorized");
                    callback.onAuthResult(401, "Application is not authorized, please contact bouyguestelecom.");
                }

                if (statusCode == 200 || statusCode == 204) {
                    for (int i = 0; i < headers.length; i++) {
                        if (xSessionId.equals(headers[i].getName())) {
                            Auth.getInstance().sessionId = headers[i].getValue();
                            callback.onAuthResult(statusCode, "Connexion established");
                            break;
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Unexpected response while getting session id. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                    callback.onAuthResult(statusCode, "Unexpected response while getting session id. HTTP code: " + String.valueOf(statusCode) + ". Something went wrong while checking your application's authorisation on the bbox.");
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {

                if (statusCode == 401){
                    Log.e(LOG_TAG, "Application is not authorized");
                    callback.onAuthResult(401, "Application is not authorized, please contact bouyguestelecom.");
                }
                else {
                    Log.e(LOG_TAG, "Unexpected response while getting session id. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                    callback.onAuthResult(statusCode, "Unexpected response while getting session id. HTTP code: " + String.valueOf(statusCode) + ". Something went wrong while checking your application's authorisation on the bbox.");
                }
            }
        });
    }

    public void innerAuthenticate(String appId, String secretId, final IAuthCallback callback) {

        Auth.getInstance().token = null;

        PFSRestClient restClient = new PFSRestClient(mContext);

        JSONObject body = new JSONObject();
        try {
            body.put("appId", String.valueOf(appId));
            body.put("appSecret", String.valueOf(secretId));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        restClient.post("/security/token", body, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (statusCode == 401){
                    Log.e(LOG_TAG, "Application is not authorized, please contact bouyguestelecom.");
                    callback.onAuthResult(401, null);
                }

                if (statusCode == 200 || statusCode == 204) {
                    for (int i = 0; i < headers.length; i++) {
                        if (xToken.equals(headers[i].getName())) {
                            Auth.getInstance().token = headers[i].getValue();
                            Auth.getInstance().connectOnly(callback);
                            break;
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Unexpected response while getting security token. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                    callback.onAuthResult(statusCode, "Unexpected response while getting security token. HTTP code: " + String.valueOf(statusCode) + ". Something went wrong went trying to authenticate your application on the distant PFS. Please make sure your device can access the Internet.");
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {

                if (statusCode == 0)
                {
                    Log.e(LOG_TAG, "Exception occurred while getting security token, please contact bouyguestelecom.");
                    callback.onAuthResult(401, e.getMessage());
                    return;
                }
                if (statusCode == 401){
                    Log.e(LOG_TAG, "Application is not authorized");
                    callback.onAuthResult(401, "Application is not authorized, please contact bouyguestelecom.");
                    return;
                }
                Log.e(LOG_TAG, "Unexpected response while getting security token. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                callback.onAuthResult(statusCode, "Unexpected response while getting security token. HTTP code: " + String.valueOf(statusCode) + ". Something went wrong went trying to authenticate your application on the distant PFS. Please make sure your device can access the Internet.");
            }
        });
    }
}
