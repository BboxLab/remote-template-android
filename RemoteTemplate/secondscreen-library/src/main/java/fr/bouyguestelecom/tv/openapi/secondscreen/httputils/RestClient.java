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
 * Created by fab on 02/09/2014.
 */
public abstract class RestClient {

    protected final String LOG_TAG;

    protected String BASE_URL;

    protected static final String CONTENT_TYPE = "application/json";

    protected Header[] contentTypeHeader = new BasicHeader[] {new BasicHeader("content-type", CONTENT_TYPE)};

    protected AsyncHttpClient client = new AsyncHttpClient();

    protected Context mContext;

    public enum HttpStatus {

        NOT_FOUND (404, "invalid url"),
        NO_CONTENT (204, "no content"),
        UNKNOWN_CODE (-1, "unknown code"),
        AUTHENTICATION_NEEDED (401, "application must be authenticated"),
        AUTHORISATION_NEEDED (403, "application must be authorised");
        private int code;
        private String msg;

        private HttpStatus(int code, String message) {
            this.code = code;
            this.msg = message;
        }

        public int getCode() {
            return code;
        }

        public String toString() {
            return msg;
        }

        public static HttpStatus valueForCode(int code) {
            for (HttpStatus httpStatus : HttpStatus.values()) {
                if (httpStatus.getCode() == code) {
                    return httpStatus;
                }
            }
            return HttpStatus.UNKNOWN_CODE;
        }
    }

    protected RestClient(Context ctx, final String baseURL, final String tag) {
        mContext = ctx;
        BASE_URL = baseURL;
        LOG_TAG = tag;
    }

    /**
     * This method does a GET request on the BboxAPI
     * @param url
     * @param params
     * @param responseHandler
     */
    public void get(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(mContext, BASE_URL+url, contentTypeHeader, params, responseHandler);
    }

    /**
     * This method does a POST request on the BboxAPI. Body must be json.
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
        client.post(mContext, BASE_URL+url, null, httpEntity, CONTENT_TYPE, responseHandler);
    }

    /**
     * This method does a PUT request one the BboxAPI, body must be json.
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
        client.put(mContext, BASE_URL+url, null, httpEntity, CONTENT_TYPE, responseHandler);
    }

    /**
     * This method does a DELETE request one the BboxAPI.
     * @param url
     * @param params
     * @param responseHandler
     */
    public void delete(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.delete(mContext, BASE_URL+url, contentTypeHeader, responseHandler);
    }

    public void setHeader(String headerName, String headerValue)
    {
        client.addHeader(headerName, headerValue);
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }


}
