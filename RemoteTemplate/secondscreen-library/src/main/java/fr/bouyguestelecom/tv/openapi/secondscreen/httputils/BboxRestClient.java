package fr.bouyguestelecom.tv.openapi.secondscreen.httputils;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * A Http REST client to send request to the BboxAPI. All requests are asynchronous.
 *
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class BboxRestClient {

    private static final String CONTENT_TYPE = "application/json";
    private Header[] contentTypeHeader = new BasicHeader[]{new BasicHeader("content-type", CONTENT_TYPE)};
    private static String BBOX_IP;
    private static String BASE_URL;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private final String LOG_TAG = getClass().toString();
    private Context mContext;

    /**
     * The constructor need the IP of the Bbox and the current {@link android.content.Context Context}
     *
     * @param bboxIp
     * @param context
     */
    public BboxRestClient(String bboxIp, Context context) {
        BBOX_IP = bboxIp;
        BASE_URL = "http://" + BBOX_IP + ":8080/api.bbox.lan/v0/";
        mContext = context;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }

    /**
     * This method does a GET request on the BboxAPI
     *
     * @param url
     * @param params
     * @param responseHandler
     */
    public void get(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(mContext, BASE_URL + url, contentTypeHeader, params, responseHandler);
    }

    /**
     * This method does a POST request on the BboxAPI. Body must be json.
     *
     * @param url
     * @param jsonData
     * @param responseHandler
     */
    public void post(String url, JSONObject jsonData, JsonHttpResponseHandler responseHandler) {
        HttpEntity httpEntity = null;
        if (jsonData != null) {
            try {
                httpEntity = new StringEntity(jsonData.toString());
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        client.post(mContext, BASE_URL + url, contentTypeHeader, httpEntity, CONTENT_TYPE, responseHandler);
    }

    /**
     * This method does a PUT request one the BboxAPI, body must be json.
     *
     * @param url
     * @param jsonData
     * @param responseHandler
     */
    public void put(String url, JSONObject jsonData, JsonHttpResponseHandler responseHandler) {

        HttpEntity httpEntity = null;
        if (jsonData != null) {
            try {
                httpEntity = new StringEntity(jsonData.toString());
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        client.put(mContext, BASE_URL + url, contentTypeHeader, httpEntity, CONTENT_TYPE, responseHandler);
    }

    /**
     * This method does a DELETE request one the BboxAPI.
     *
     * @param url
     * @param params
     * @param responseHandler
     */
    public void delete(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.delete(mContext, BASE_URL + url, contentTypeHeader, responseHandler);
    }

    public String getBboxIP() {
        return BBOX_IP;
    }

    public enum HttpStatus {

        NOT_FOUND(404, "invalid url"),
        NO_CONTENT(204, "no content"),
        UNKNOWN_CODE(-1, "unknown code");

        private int code;
        private String msg;

        private HttpStatus(int code, String message) {
            this.code = code;
            this.msg = message;
        }

        public static HttpStatus valueForCode(int code) {
            for (HttpStatus httpStatus : HttpStatus.values()) {
                if (httpStatus.getCode() == code) {
                    return httpStatus;
                }
            }
            return HttpStatus.UNKNOWN_CODE;
        }

        public int getCode() {
            return code;
        }

        public String toString() {
            return msg;
        }
    }

}




