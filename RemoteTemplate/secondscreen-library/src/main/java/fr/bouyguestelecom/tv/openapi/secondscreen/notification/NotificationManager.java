package fr.bouyguestelecom.tv.openapi.secondscreen.notification;

import org.json.JSONObject;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public interface NotificationManager {

    public void subscribe(NotificationType notificationType, CallbackSubscribed callbackSubscribed);

    public void subscribe(NotificationType notificationType, String additionalParam, CallbackSubscribed callbackSubscribed);

    public void subscribe(NotificationType[] notificationTypes, CallbackSubscribed callbackSubscribed);

    public void subscribe(String[] notificationTypes, CallbackSubscribed callbackSubscribed);

    public void unSubscribe(NotificationType notificationType, CallbackSubscribed callbackSubscribed);

    public void unSubscribe(NotificationType notificationType, String additionalParam, CallbackSubscribed callbackSubscribed);

    public void unSubscribeToAll();

    public void sendMessage(String channelID, String msg);

    public void sendMessage(String channelID, JSONObject msg);

    public void sendRoomMessage(String room, String msg);

    public void sendRoomMessage(String room, JSONObject msg);

    public void addApplicationListener(Listener listener);

    public void addMediaListener(Listener listener);

    public void addMessageListener(Listener listener);

    public void addUserInputListener(Listener listener);

    public void addErrorListener(Listener listener);

    public void addAllNotificationsListener(Listener listener);

    public void removeApplicationListener(Listener listener);

    public void removeMediaListener(Listener listener);

    public void removeMessageListener(Listener listener);

    public void removeUserInputListener(Listener listener);

    public void removeErrorListener(Listener listener);

    public void removeAllNotificationsListener(Listener listener);

    public void listen(CallbackConnected callbackConnected);

    public void close();

    public String getAppId();

    public String getChannelId();


    public interface Listener {
        public void onNotification(JSONObject msg);
    }

    public interface CallbackConnected {
        public void onConnect();
    }

    public interface CallbackSubscribed {
        public void onResult(int statusCode);
    }

}
