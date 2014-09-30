package fr.bouyguestelecom.tv.openapi.secondscreen.apiutils;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class BboxApiUrl {

    public static final String USER_INTERFACE = "userinterface";
    public static final String REMOTE_CONTROLLER = "remotecontroller";
    public static final String SEND_KEY = USER_INTERFACE + "/" + REMOTE_CONTROLLER + "/key";
    public static final String SEND_TEXT = USER_INTERFACE + "/" + REMOTE_CONTROLLER + "/text";
    public static final String VOLUME = "volume";
    public static final String MANAGE_VOLUME = USER_INTERFACE + "/" + VOLUME;
    public static final String APPLICATIONS = "applications";
    public static final String APPLICATIONS_RUN = APPLICATIONS + "/run";
    public static final String APPLICATIONS_REGISTER = APPLICATIONS + "/register";
    public static final String SECURE_AUTH = "security/token";
    public static final String SECURE_CONNECT = "security/sessionId";
    public static final String NOTIFICATION = "notification";
    public static final String PACKAGE_NAME = "packageName";
    public static final String APP_NAME = "appName";
    public static final String LOGO_URL = "logoUrl";
    public static final String APP_ID = "appId";
    public static final String APP_STATE = "appState";
}
