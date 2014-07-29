package fr.bouyguestelecom.tv.openapi.secondscreen.notification;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.bouyguestelecom.tv.openapi.secondscreen.apiutils.BboxApiUrl;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class WebSocket implements NotificationManager {

    private static String LOG_TAG = "WebSocket";

    private static String WEBSOCKET_PREFIX = "ws://";
    private static String WEBSOCKET_PORT = "9090";
    private static String WEBSOCKET_ADDRESS;

    private static String RESOURCE_ID_KEY = "resourceId";
    private static String ERROR_KEY = "error";
    private static String BODY_KEY = "body";
    private static String DESTINATION_KEY = "destination";
    private static String APP_ID_KEY = "appId";
    private static String RESOURCES_KEY = "resources";
    private static String SOURCE_KEY = "source";
    private static WebSocket instance = null;
    private WebSocketClient webSocketClient;
    private CallbackConnected callbackConnected;
    private String appId;
    private String channelId;
    private BboxRestClient bboxRestClient;
    private List<Listener> allNotificationsListeners = new ArrayList<Listener>();
    private List<Listener> applicationsListeners = new ArrayList<Listener>();
    private List<Listener> messagesListeners = new ArrayList<Listener>();
    private List<Listener> mediaListeners = new ArrayList<Listener>();
    private List<Listener> userInputListeners = new ArrayList<Listener>();
    private List<Listener> errorListener = new ArrayList<Listener>();
    private Map<String, List<Listener>> listenerMap = new HashMap<String, List<Listener>>();
    private HashSet<String> notificationsSubscribed = new HashSet<String>();

    private WebSocket(final String appId, BboxRestClient bboxRestClient) {
        assert (appId == null) : "You must provide a valid appId when initializing WebSocket";
        assert (bboxRestClient == null) : "You must provide a valid BboxRestClient when initializing WebSocket";

        this.appId = appId;
        this.bboxRestClient = bboxRestClient;
        listenerMap.put(NotificationType.APPLICATIONS.toString(), applicationsListeners);
        listenerMap.put(NotificationType.MESSAGE.toString(), messagesListeners);
        listenerMap.put(NotificationType.MEDIA.toString(), mediaListeners);
        listenerMap.put(NotificationType.USER_INPUT.toString(), userInputListeners);

        WEBSOCKET_ADDRESS = WEBSOCKET_PREFIX + bboxRestClient.getBboxIP() + ":" + WEBSOCKET_PORT;
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        webSocketClient = new WebSocketClient(URI.create(WEBSOCKET_ADDRESS)) {
            @Override
            public void onOpen(ServerHandshake handShakeData) {
                webSocketClient.send(appId);
                if (callbackConnected != null) {
                    callbackConnected.onConnect();
                }
            }

            @Override
            public void onMessage(String message) {
                try {
                    JSONObject msg = new JSONObject(message);
                    JSONObject ret = new JSONObject();

                    for (Listener listener : allNotificationsListeners) {
                        listener.onNotification(msg);
                    }

                    if (msg.has(ERROR_KEY)) {
                        for (Listener listener : errorListener) {
                            listener.onNotification(msg);
                        }
                        Log.e(LOG_TAG, msg.toString());
                    } else if (listenerMap.containsKey(msg.getString(RESOURCE_ID_KEY))) {
                        for (Listener listener : listenerMap.get(msg.getString(RESOURCE_ID_KEY))) {
                            listener.onNotification(msg.getJSONObject(BODY_KEY));
                        }
                    } else {
                        JSONObject error = new JSONObject();
                        try {
                            error.put("msg", "invalid resourceId: " + msg.getString(RESOURCE_ID_KEY));
                            error.put("status", "0");
                            ret.put(ERROR_KEY, msg);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }

                        for (Listener listener : errorListener) {
                            listener.onNotification(error);
                        }

                        Log.e(LOG_TAG, "invalid resourceId: " + msg.getString(RESOURCE_ID_KEY));

                    }

                } catch (JSONException e) {

                    JSONObject error = new JSONObject();
                    JSONObject msg = new JSONObject();

                    try {
                        msg.put("msg", e.getMessage());
                        msg.put("status", "0");
                        error.put(ERROR_KEY, msg);
                    } catch (JSONException e1) {
                        Log.e(LOG_TAG, e1.getMessage());
                    }

                    for (Listener listener : errorListener) {
                        listener.onNotification(error);
                    }
                    Log.e(LOG_TAG, e.getMessage() + " Message: " + message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e(LOG_TAG, "socket closed");
            }

            @Override
            public void onError(Exception ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }
        };
    }

    private WebSocket(String appId, Bbox bbox) {
        this(appId, bbox == null ? null : bbox.getBboxRestClient());
    }

    public static WebSocket getInstance(final String appId, BboxRestClient bboxRestClient) {
        if (instance == null) {
            instance = new WebSocket(appId, bboxRestClient);
        }
        return instance;
    }

    public static WebSocket getInstance(final String appId, Bbox bbox) {
        if (instance == null) {
            instance = new WebSocket(appId, bbox);
        }
        return instance;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public void subscribe(NotificationType notificationType, CallbackSubscribed callbackSubscribed) {
        notificationsSubscribed.add(notificationType.toString());
        updateSubscribe(callbackSubscribed);
    }

    @Override
    public void subscribe(NotificationType notificationType, String additionalParam, CallbackSubscribed callbackSubscribed) {
        notificationsSubscribed.add(notificationType.toString() + "/" + additionalParam);
        updateSubscribe(callbackSubscribed);
    }

    @Override
    public void unSubscribe(NotificationType notificationType, CallbackSubscribed callbackSubscribed) {
        notificationsSubscribed.remove(notificationType.toString());
        updateSubscribe(callbackSubscribed);
    }

    @Override
    public void unSubscribe(NotificationType notificationType, String additionalParam, CallbackSubscribed callbackSubscribed) {
        notificationsSubscribed.remove(notificationType.toString() + "/" + additionalParam);
        updateSubscribe(callbackSubscribed);
    }

    @Override
    public void unSubscribeToAll() {
        notificationsSubscribed.clear();
        bboxRestClient.delete(BboxApiUrl.NOTIFICATION + "/" + channelId, null, new JsonHttpResponseHandler() {

        });
    }

    @Override
    public void subscribe(NotificationType[] notificationTypes, CallbackSubscribed callbackSubscribed) {
        for (int i = 0; i < notificationTypes.length; i++) {
            notificationsSubscribed.add(notificationTypes[i].toString());
        }
        updateSubscribe(callbackSubscribed);
    }

    @Override
    public void subscribe(String[] notificationTypes, CallbackSubscribed callbackSubscribed) {
        for (int i = 0; i < notificationTypes.length; i++) {
            notificationsSubscribed.add(notificationTypes[i]);
        }
        updateSubscribe(callbackSubscribed);
    }

    public void updateSubscribe(final CallbackSubscribed callbackSubscribed) {
        JSONObject toSend = new JSONObject();
        JSONArray toSubscribe = new JSONArray();
        try {
            for (String notification : notificationsSubscribed) {
                JSONObject resource = new JSONObject();
                resource.put(RESOURCE_ID_KEY, notification);
                toSubscribe.put(resource);
            }
            toSend.put(APP_ID_KEY, appId);
            toSend.put(RESOURCES_KEY, toSubscribe);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bboxRestClient.post(BboxApiUrl.NOTIFICATION, toSend, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        String[] tab = header.getValue().split("/");
                        int idx = tab.length;
                        channelId = tab[idx - 1];
                        Log.e(LOG_TAG, "channelId: " + channelId);
                    }
                }
                if (callbackSubscribed != null) {
                    callbackSubscribed.onResult(statusCode);
                }
            }
        });
    }

    @Override
    public void sendMessage(String channelID, String msg) {

        JSONObject toSend = new JSONObject();

        try {
            toSend.put(DESTINATION_KEY, channelID);
            toSend.put(SOURCE_KEY, appId);
            toSend.put(BODY_KEY, msg);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        webSocketClient.send(toSend.toString());
    }

    @Override
    public void sendMessage(String channelID, JSONObject msg) {
        sendMessage(channelID, msg.toString());
    }

    @Override
    public void sendRoomMessage(String room, String msg) {
        sendMessage(NotificationType.MESSAGE + "/" + room, msg);
    }

    @Override
    public void sendRoomMessage(String room, JSONObject msg) {
        sendMessage(room, msg.toString());
    }

    @Override
    public void addApplicationListener(Listener listener) {
        applicationsListeners.add(listener);
    }

    @Override
    public void addMediaListener(Listener listener) {
        mediaListeners.add(listener);
    }

    @Override
    public void addMessageListener(Listener listener) {
        messagesListeners.add(listener);
    }

    @Override
    public void addUserInputListener(Listener listener) {
        userInputListeners.add(listener);
    }

    @Override
    public void addErrorListener(Listener listener) {
        errorListener.add(listener);
    }

    @Override
    public void addAllNotificationsListener(Listener listener) {
        allNotificationsListeners.add(listener);
    }

    @Override
    public void removeApplicationListener(Listener listener) {
        applicationsListeners.remove(listener);
    }

    @Override
    public void removeMediaListener(Listener listener) {
        mediaListeners.remove(listener);
    }

    @Override
    public void removeMessageListener(Listener listener) {
        messagesListeners.remove(listener);
    }

    @Override
    public void removeUserInputListener(Listener listener) {
        userInputListeners.remove(listener);
    }

    @Override
    public void removeErrorListener(Listener listener) {
        errorListener.remove(listener);
    }

    @Override
    public void removeAllNotificationsListener(Listener listener) {
        allNotificationsListeners.remove(listener);
    }

    @Override
    public void listen(CallbackConnected callbackConnected) {
        assert (webSocketClient == null) : "You must init WebSocket with the init() method before trying to listen";
        this.callbackConnected = callbackConnected;
        webSocketClient.connect();
    }

    @Override
    public void close() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
