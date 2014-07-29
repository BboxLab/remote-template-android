package fr.bouyguestelecom.tv.openapi.secondscreen.application;

import org.json.JSONObject;

import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationManager;

/**
 * Representation of an application installed on the Bbox
 *
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class Application {

    private String packageName;
    private String appName;
    private String logoUrl;
    private String appId;
    private ApplicationState appState;
    private NotificationManager.Listener appListener;
    private ChangeListener changeListener;

    public Application(String packageName, String appName, String logoUrl, ApplicationState appState, String appId) {
        this.packageName = packageName;
        this.appName = appName;
        this.logoUrl = logoUrl;
        this.appState = appState;
        this.appId = appId;
        initAppListener();
    }

    public Application(String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
        initAppListener();
    }

    public Application(String packageName) {
        this.packageName = packageName;
    }

    private void initAppListener() {
        appListener = new NotificationManager.Listener() {
            @Override
            public void onNotification(JSONObject msg) {

            }
        };
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public NotificationManager.Listener getAppListener() {
        return appListener;
    }

    public String getPackageName() {
        return packageName;
    }

    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    private void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    private void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public ApplicationState getAppState() {
        return appState;
    }

    public void setAppState(ApplicationState appState) {
        this.appState = appState;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public interface ChangeListener {
        public void onChange();
    }
}
