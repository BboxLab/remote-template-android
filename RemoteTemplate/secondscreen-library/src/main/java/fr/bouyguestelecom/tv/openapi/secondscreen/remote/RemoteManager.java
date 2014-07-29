package fr.bouyguestelecom.tv.openapi.secondscreen.remote;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import fr.bouyguestelecom.tv.openapi.secondscreen.apiutils.BboxApiUrl;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.CallbackHttpStatus;

/**
 * Use this class to emulate a remote. Also you can send String directly to the TV.
 *
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class RemoteManager {

    private final String LOG_TAG = getClass().toString();

    private BboxRestClient bboxRestClient = null;

    /**
     * The constructor take an initialised {@link fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient BboxRestClient} to be able to dialog with the BboxAPI.
     *
     * @param bboxRestClient Rest client
     */
    public RemoteManager(BboxRestClient bboxRestClient) {
        this.bboxRestClient = bboxRestClient;
    }

    /**
     * Send the given key to the Bbox. you can use the {@link fr.bouyguestelecom.tv.openapi.secondscreen.remote.KeyName KeyName} and {@link fr.bouyguestelecom.tv.openapi.secondscreen.remote.KeyType KeyType} enum with the toString method to get valid params.
     * You get the Http response status code with the callback.
     *
     * @param keyName
     * @param keyType
     * @param callbackHttpStatus
     */
    public void sendKey(String keyName, String keyType, final CallbackHttpStatus callbackHttpStatus) {

        JSONObject body = new JSONObject();
        JSONObject key = new JSONObject();

        try {
            key.put("keyName", keyName);
            key.put("keyType", keyType);
            body.put("key", key);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        bboxRestClient.put(BboxApiUrl.SEND_KEY, body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == 204) {
                    Log.i(LOG_TAG, "Key successfully sent");
                } else {
                    Log.e(LOG_TAG, "Unexpected response while sending key. HTTP code: " + String.valueOf(statusCode) + " Server response: " + response.toString());
                }
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while sending text. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while sending key." + " Exception: " + e.getMessage());
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }
        });
    }

    /**
     * Send the given key to the Bbox. you can use the {@link fr.bouyguestelecom.tv.openapi.secondscreen.remote.KeyName KeyName} and {@link fr.bouyguestelecom.tv.openapi.secondscreen.remote.KeyType KeyType} enum with the toString method to get valid params.
     *
     * @param keyName the key to send
     * @param keyType the type of key to send
     */
    public void sendKey(String keyName, String keyType) {
        sendKey(keyName, keyType, null);
    }


    public void sendKey(String keyName) {
        sendKey(keyName, KeyType.KEY_PRESSED.toString());
    }

    public void sendKey(String keyName, final CallbackHttpStatus callbackHttpStatus) {
        sendKey(keyName, KeyType.KEY_PRESSED.toString(), callbackHttpStatus);
    }

    public void sendKey(KeyName keyName) {
        sendKey(keyName.toString(), KeyType.KEY_PRESSED.toString());
    }

    public void sendKey(KeyName keyName, final CallbackHttpStatus callbackHttpStatus) {
        sendKey(keyName.toString(), KeyType.KEY_PRESSED.toString(), callbackHttpStatus);
    }

    public void sendKey(KeyName keyName, KeyType keyType, final CallbackHttpStatus callbackHttpStatus) {
        sendKey(keyName.toString(), keyType.toString(), callbackHttpStatus);
    }

    /**
     * Send the given String to the Bbox. It's useful to directly fill a text are on TV applications.
     * You get the Http response status code with the callback.
     *
     * @param text
     * @param callbackHttpStatus
     */
    public void sendText(String text, final CallbackHttpStatus callbackHttpStatus) {

        JSONObject body = new JSONObject();
        try {
            body.put("text", text);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        bboxRestClient.post(BboxApiUrl.SEND_TEXT, body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == 204) {
                    Log.i(LOG_TAG, "Text successfully sent");
                } else {
                    Log.e(LOG_TAG, "Unexpected response while sending text. HTTP code: " + String.valueOf(statusCode) + " Server response: " + response.toString());
                }
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while sending text. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while sending key." + " Exception: " + e.getMessage());
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }
        });
    }

    /**
     * Send the given String to the Bbox. It's useful to directly fill a text are on TV applications.
     *
     * @param text text to send
     */
    public void sendText(String text) {
        sendText(text, null);
    }

    /**
     * Request the Bbox for the current volume. Answer is provided in the callback. volume parameter int the callback is set to -1 if an error occur.
     *
     * @param callbackVolume Callback
     */
    public void getVolume(final CallbackVolume callbackVolume) {
        bboxRestClient.get(BboxApiUrl.MANAGE_VOLUME, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (statusCode == 200) {
                    try {
                        callbackVolume.onResult(statusCode, response.getInt("volume"));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callbackVolume.onResult(statusCode, -1);
                    }
                } else {
                    Log.e(LOG_TAG, "Unexpected response while getting volume. HTTP code: " + String.valueOf(statusCode) + " - 204 expected");
                    callbackVolume.onResult(statusCode, -1);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while getting volume. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while getting volume." + " Exception: " + e.getMessage());
                callbackVolume.onResult(statusCode, -1);
            }
        });
    }

    /**
     * Set the current volume of the Bbox to the provided int.
     * You get the Http response status code with the callback.
     *
     * @param volume
     * @param callbackHttpStatus
     */
    public void setVolume(int volume, final CallbackHttpStatus callbackHttpStatus) {
        JSONObject body = new JSONObject();
        try {
            body.put("volume", String.valueOf(volume));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        bboxRestClient.post(BboxApiUrl.MANAGE_VOLUME, body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == 204) {
                    Log.i(LOG_TAG, "Volume successfully sent");
                } else {
                    Log.e(LOG_TAG, "Unexpected response while sending volume. HTTP code: " + String.valueOf(statusCode) + " - 204 expected");
                }
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while sending volume. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while sending volume." + " Exception: " + e.getMessage());
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }
        });
    }

    /**
     * Set the current volume of the Bbox to the provided int.
     *
     * @param volume volume
     */
    public void setVolume(int volume) {
        setVolume(volume, null);
    }

    /**
     * Interface to get current volume.
     */
    public interface CallbackVolume {
        /**
         * current volume in given parameter
         *
         * @param statusCode http status response
         * @param volume     0 <= volume <= 100; -1 if error occur
         */
        public void onResult(int statusCode, int volume);
    }

}
