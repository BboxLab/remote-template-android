package fr.bouyguestelecom.tv.openapi.secondscreen.httputils;

import android.content.Context;

public class PFSRestClient extends RestClient {

    private final static String TAG = "PFSRestClient";

    private final static String URL = "https://dev.bouyguestelecom.fr";

    public PFSRestClient(Context context) {
        super(context, URL, TAG);
    }
}




