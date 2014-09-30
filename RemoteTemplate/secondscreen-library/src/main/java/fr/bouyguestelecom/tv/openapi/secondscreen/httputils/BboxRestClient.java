package fr.bouyguestelecom.tv.openapi.secondscreen.httputils;

import android.content.Context;

/**
 * A Http REST client to send request to the BboxAPI. All requests are asynchronous.
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class BboxRestClient extends RestClient {

    private static final String TAG = "BboxRestClient";

    private String BBOX_IP;
    private String URL;
    private String idSessionHeader = null;

    /**
     * The constructor need the IP of the Bbox and the current {@link android.content.Context Context}
     * @param bboxIp
     * @param context
     */
    public BboxRestClient(String bboxIp, Context context) {
        super(context, "http://" + bboxIp + ":8080/api.bbox.lan/v0/", TAG);
        BBOX_IP = bboxIp;
        URL = "http://" + BBOX_IP + ":8080/api.bbox.lan/v0/";
    }

    public String getBboxIP() {
        return BBOX_IP;
    }

    public String getIdSession()
    {
        return idSessionHeader;
    }

    public void setIdSession(String headerName, String headerValue)
    {
        idSessionHeader = headerValue;
        super.setHeader(headerName, headerValue);
    }
}




