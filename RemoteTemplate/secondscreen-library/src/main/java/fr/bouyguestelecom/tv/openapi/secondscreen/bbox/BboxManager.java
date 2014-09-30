package fr.bouyguestelecom.tv.openapi.secondscreen.bbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class BboxManager {

    private final static String LOG_TAG = "BboxManager";
    private final static String SERVICE_NAME = "Bboxapi";
    private final static String SERVICE_TYPE = "_http._tcp.";
    private final static String SERVICE_TYPE_LOCAL = "_http._tcp.local";
    private WifiManager.MulticastLock multicastLock = null;
    private WifiManager wifiManager = null;
    private JmDNS jmDNS;
    private ServiceListener serviceListener;
    private Context context;
    private CallbackBboxFound callbackBboxFound;

    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;

    public void startLookingForBbox(Context context, CallbackBboxFound callbackBboxFound) {

        Log.i("BboxManager", "Start looking for Bbox");

        this.callbackBboxFound = callbackBboxFound;

        this.context = context;
        if (multicastLock == null) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("LibBboxOpenAPI");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
        }

        if (Build.VERSION.SDK_INT >= 16) {
            initializeDiscoveryListener();
        } else {
            JmDNSThread jmDNSThread = new JmDNSThread(callbackBboxFound);
            jmDNSThread.execute();
        }

    }

    public void stopLookingForBbox() {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        } else {
            jmDNS.removeServiceListener(SERVICE_TYPE_LOCAL, serviceListener);
            try {
                jmDNS.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    public interface CallbackBboxFound {
        public void onResult(Bbox bbox);
    }

    private class JmDNSThread extends AsyncTask<Void, Void, Void> {

        CallbackBboxFound callbackBboxFound;

        public JmDNSThread(CallbackBboxFound callbackBboxFound) {
            this.callbackBboxFound = callbackBboxFound;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                jmDNS = JmDNS.create();
                serviceListener = new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        Log.i(LOG_TAG, "Service found: " + event.getName());
                        if (event.getName().startsWith(SERVICE_NAME)) {
                            jmDNS.requestServiceInfo(event.getType(), event.getName(), true);
                        }
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent event) {

                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        String bboxIP = event.getInfo().getInet4Addresses()[0].getHostAddress();
                        Log.i(LOG_TAG, "Bbox found on " + bboxIP);
                        callbackBboxFound.onResult(new Bbox(bboxIP, context));

                    }
                };

                jmDNS.addServiceListener(SERVICE_TYPE, serviceListener);

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }
    }

    @TargetApi(16)
    public void initializeDiscoveryListener() {

        this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        this.mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.d(LOG_TAG, "resolve fail");
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                String ip = nsdServiceInfo.getHost().toString().split("/")[1];
                Log.d(LOG_TAG, "Bbox found on " + ip);
                callbackBboxFound.onResult(new Bbox(ip, context));

            }
        };

        this.mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Log.d(LOG_TAG, "Service discovery started");
            }

            @Override
            public void onDiscoveryStopped(String s) {

            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(LOG_TAG, "Service discovery success" + service);
                if (service.getServiceName().equals(SERVICE_NAME)) {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {

            }
        };

        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

}