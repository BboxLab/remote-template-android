package fr.bouyguestelecom.tv.openapi.secondscreen.bbox;

import android.content.Context;

import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient;
import fr.bouyguestelecom.tv.openapi.secondscreen.remote.RemoteManager;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class Bbox {

    public String ip;
    public String macAddress;
    public ApplicationsManager applicationsManager;
    public RemoteManager remoteManager;
    private BboxRestClient bboxRestClient;
    private Context mContex;

    public Bbox(String ip, Context context) {
        this.mContex = context;
        this.ip = ip;
        this.macAddress = WOLPowerManager.getMacFromArpCache(ip);
        bboxRestClient = new BboxRestClient(ip, mContex);
        applicationsManager = new ApplicationsManager(bboxRestClient);
        remoteManager = new RemoteManager(bboxRestClient);
    }

    public BboxRestClient getBboxRestClient() {
        return bboxRestClient;
    }

    public RemoteManager getRemoteManager() {
        return remoteManager;
    }

    public String getIp() {
        return ip;
    }

    public ApplicationsManager getApplicationsManager() {
        return applicationsManager;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
