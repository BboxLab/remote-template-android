package fr.bouyguestelecom.tv.openapi.secondscreen.application;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.openapi.secondscreen.apiutils.BboxApiUrl;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.CallbackHttpStatus;

/**
 * Applications manager. Provide methods to get and manage applications installed on the Bbox
 *
 * @author Pierre-Etienne Cherière PCHERIER@bouyguestelecom.fr
 */
public class ApplicationsManager {

    private final String LOG_TAG = getClass().toString();

    public BboxRestClient bboxRestClient;

    /**
     * Need a {@link fr.bouyguestelecom.tv.openapi.secondscreen.httputils.BboxRestClient BboxRestClient} to be abble to make http calls to the Bbox.
     *
     * @param bboxRestClient
     */
    public ApplicationsManager(BboxRestClient bboxRestClient) {
        this.bboxRestClient = bboxRestClient;
    }

    /**
     * Get the list of all installed applications in the provided callback. Return null if an error occur.
     *
     * @param callbackApplications
     */
    public void getApplications(final CallbackApplications callbackApplications) {
        bboxRestClient.get(BboxApiUrl.APPLICATIONS, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                if (statusCode == 200) {
                    List<Application> applications = new ArrayList<Application>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject app = response.getJSONObject(i);
                            applications.add(new Application(app.getString(BboxApiUrl.PACKAGE_NAME), app.getString(BboxApiUrl.APP_NAME), app.getString(BboxApiUrl.LOGO_URL), ApplicationState.valueForState(app.getString(BboxApiUrl.APP_STATE)), app.getString(BboxApiUrl.APP_ID)));
                        }
                        callbackApplications.onResult(statusCode, applications);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callbackApplications.onResult(statusCode, null);
                    }
                } else {
                    Log.e(LOG_TAG, "Unexpected response while getting applications. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                    callbackApplications.onResult(statusCode, null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while getting volume. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while getting volume." + " Exception: " + e.getMessage());
                callbackApplications.onResult(statusCode, null);
            }
        });
    }

    /**
     * Get the requested Application.
     *
     * @param packageName         The packageName of the requested application.
     * @param callbackApplication
     */
    public void getApplication(final String packageName, final CallbackApplication callbackApplication) {
        bboxRestClient.get(BboxApiUrl.APPLICATIONS, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                if (statusCode == 200) {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject app = response.getJSONObject(i);
                            if (app.getString("packageName").equals(packageName)) {
                                callbackApplication.onResult(statusCode, new Application(app.getString(BboxApiUrl.PACKAGE_NAME), app.getString(BboxApiUrl.APP_NAME), app.getString(BboxApiUrl.LOGO_URL), ApplicationState.valueForState(app.getString(BboxApiUrl.APP_STATE)), app.getString(BboxApiUrl.APP_ID)));
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callbackApplication.onResult(statusCode, null);
                    }
                } else {
                    Log.e(LOG_TAG, "Unexpected response while getting application. HTTP code: " + String.valueOf(statusCode) + " - 200 expected");
                    callbackApplication.onResult(statusCode, null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                if (statusCode > 0)
                    Log.e(LOG_TAG, "Error while getting volume. HTTP code: " + String.valueOf(statusCode) + " - Server response: " + errorResponse.toString());
                else
                    Log.e(LOG_TAG, "Error while getting volume." + " Exception: " + e.getMessage());
                callbackApplication.onResult(statusCode, null);
            }
        });
    }

    /**
     * Start the provided {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} on the Bbox
     *
     * @param app
     * @param callbackHttpStatus
     */
    public void startApplication(final Application app, final CallbackHttpStatus callbackHttpStatus) {
        bboxRestClient.post(BboxApiUrl.APPLICATIONS + "/" + app.getPackageName(), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode == 204) {
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].getName().equals("location")) {
                            String[] tab = headers[i].getValue().split("/");
                            int idx = tab.length;
                            String appId = tab[idx - 1];
                            app.setAppId(appId);
                            break;
                        }
                    }
                }
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e(LOG_TAG, e.getMessage());
                if (callbackHttpStatus != null) {
                    callbackHttpStatus.onResult(statusCode);
                }
            }
        });
    }

    public void startApplication(Application app) {
        startApplication(app, null);
    }

    /**
     * Stop the provided {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} on the Bbox
     *
     * @param app
     * @param callbackHttpStatus
     */
    public void stopApplication(final Application app, final CallbackHttpStatus callbackHttpStatus) {
        getApplication(app.getPackageName(), new CallbackApplication() {
            @Override
            public void onResult(int statusCode, Application application) {
                if (application != null)
                    bboxRestClient.delete(BboxApiUrl.APPLICATIONS_RUN + "/" + application.getAppId(), null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (callbackHttpStatus != null) {
                                callbackHttpStatus.onResult(statusCode);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                            Log.e(LOG_TAG, e.getMessage());
                            if (callbackHttpStatus != null) {
                                callbackHttpStatus.onResult(statusCode);
                            }
                        }
                    });
            }
        });
    }

    public void stopApplication(Application app) {
        stopApplication(app, null);
    }

    /**
     * Change the state of the {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} on the Bbox.
     * This also update the {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} provided to the new status.
     *
     * @param app                the app
     * @param applicationState   the new state wanted
     * @param callbackHttpStatus
     */
    public void changeApplicationState(final Application app, final ApplicationState applicationState, final CallbackHttpStatus callbackHttpStatus) {
        getApplication(app.getPackageName(), new CallbackApplication() {
            @Override
            public void onResult(int statusCode, final Application app) {
                if (app != null) {

                    JSONObject body = new JSONObject();
                    try {
                        body.put(BboxApiUrl.APP_STATE, applicationState.toString());
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    bboxRestClient.post(BboxApiUrl.APPLICATIONS_RUN + "/" + app.getAppId(), body, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (statusCode == 204)
                                app.setAppState(applicationState);
                            callbackHttpStatus.onResult(statusCode);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                            Log.e(LOG_TAG, e.getMessage());
                            callbackHttpStatus.onResult(statusCode);
                        }
                    });
                } else {
                    callbackHttpStatus.onResult(-1);
                }
            }
        });
    }

    /**
     * Get the state of the {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} on the Bbox.
     * This also update the {@link fr.bouyguestelecom.tv.openapi.secondscreen.application.Application Application} provided to the new status.
     *
     * @param appToGet         the app
     * @param callbackAppState
     */
    public void getAppState(final Application appToGet, final CallbackAppState callbackAppState) {

        getApplication(appToGet.getPackageName(), new CallbackApplication() {
            @Override
            public void onResult(int statusCode, Application app) {
                if (app == null) {
                    callbackAppState.onResult(null);
                } else {
                    appToGet.setAppState(app.getAppState());
                    callbackAppState.onResult(app.getAppState());
                }
            }
        });
    }

    public void getMyAppId(String appName, final CallbackAppId callbackAppId) {
        JSONObject body = new JSONObject();

        try {
            body.put("appName", appName);
            bboxRestClient.post(BboxApiUrl.APPLICATIONS_REGISTER, body, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (statusCode == 204) {
                        for (Header header : headers) {
                            if (header.getName().equals("Location")) {
                                String[] tab = header.getValue().split("/");
                                int idx = tab.length;
                                String appId = tab[idx - 1];

                                callbackAppId.onResult(statusCode, appId);
                            }
                        }
                    } else {
                        callbackAppId.onResult(statusCode, null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    callbackAppId.onResult(statusCode, null);
                }
            });
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public interface CallbackApplications {
        /**
         * @param statusCode Http status code of the request.
         * @param apps       null if an error occurred
         */
        public void onResult(int statusCode, List<Application> apps);
    }

    public interface CallbackApplication {
        /**
         * @param statusCode Http status code of the request.
         * @param app        null if an error occurred
         */
        public void onResult(int statusCode, Application app);
    }

    public interface CallbackAppState {
        public void onResult(ApplicationState applicationState);
    }

    public interface CallbackAppId {
        public void onResult(int statusCode, String appId);
    }
}
