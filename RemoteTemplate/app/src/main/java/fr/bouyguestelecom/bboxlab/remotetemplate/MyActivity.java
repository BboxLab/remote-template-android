package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;
import com.example.google.tv.anymotelibrary.client.AnymoteSender;
import com.google.anymote.Key.Code;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.bouyguestelecom.tv.openapi.secondscreen.application.Application;
import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.Auth;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.BboxManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationType;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.WebSocket;

public class MyActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ClientListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * This manages discovering, pairing and connecting to Google TV devices on
     * network.
     */

    private AnymoteClientService mAnymoteClientService;
    private ProgressBar progressBar;
    private Context mContext;

    /**
     * The proxy used to send events to the server using Anymote Protocol
     */
    public AnymoteSender anymoteSender;
    public ServiceConnection mConnection;

    private static final String LOG_TAG = "MainActivity";
    private static List<String> notificationsList = new ArrayList<String>();
    private static String IP_PREFERENCE = "bboxIP";
    private static String DEFAULT_IP = "10.1.0.50";
    public static Activity mainActivity;
    public static Context mContex;
    private BboxManager bboxManager;
    private Bbox currentBbox;
    IAuthCallback authenticationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_my_activity);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mContex = getApplicationContext();

        // Callback used by after security check
        // Status different than 2xx means that something wrong happened
        // Check the reason to know exactly why there is an error (problem with tokens means problem during connection with distant platform
        // authentication while problem with sessionId means problem with bbox connectivity check)
        authenticationCallback = new IAuthCallback() {
            @Override
            public void onAuthResult(int statusCode, String reason) {

                //Put stuff here that need authentication process to be done
                if(statusCode > 299 || statusCode < 200)
                {
                    // Something wrong happen during authentication process
                    displayWarningConnectivityMessage(reason);
                    return;
                }

                // We have to get our AppID in order to initiate a websocket connection.
                currentBbox.getApplicationsManager().getMyAppId("Remote_Controller", new ApplicationsManager.CallbackAppId() {
                    @Override
                    public void onResult(int statusCode, String appId) {

                        // Now we have our AppID, we can therefor instantiate a NotificationManager with the WebSocket implementation.
                        final NotificationManager notification = WebSocket.getInstance(appId, currentBbox);

                        // Before the NotificationManager start listening we are going to subscribe to Message.
                        // We provide a callback, because we want to start listening to notifications after we subscribe to Message.
                        notification.subscribe(NotificationType.MESSAGE, new NotificationManager.CallbackSubscribed() {
                            @Override
                            public void onResult(int statusCode) {

                                // We can check if the subscription is a success with the http return code.
                                Log.d(LOG_TAG, "status subscribe:" + statusCode);

                                // We also subscribe to Applications, but we do not provide a callback this time. We don't want to wait for the return.
                                notification.subscribe(NotificationType.APPLICATION, null);

                                // We add a AllNotificationsListener to Log all the notifications we receive.
                                notification.addAllNotificationsListener(new NotificationManager.Listener() {
                                    @Override
                                    public void onNotification(JSONObject jsonObject) {
                                        // We here add the received notification to a list, to be able to print it in our UI.
                                        notificationsList.add(jsonObject.toString());
                                        Log.d(LOG_TAG, jsonObject.toString());
                                    }
                                });

                                // Here we add a MessageListener. Inside we will only receive Message notifications.
                                // Message notification will still appear in the AllNotificationListener.
                                notification.addMessageListener(new NotificationManager.Listener() {
                                    @Override
                                    public void onNotification(JSONObject jsonObject) {
                                        Log.d(LOG_TAG, jsonObject.toString());
                                    }
                                });

                                // Same here with a ApplicationListener.
                                notification.addApplicationListener(new NotificationManager.Listener() {
                                    @Override
                                    public void onNotification(JSONObject jsonObject) {
                                        Log.d(LOG_TAG, jsonObject.toString());
                                    }
                                });

                                // Once we have set our listeners, we can start listening for notifications.
                                notification.listen(new NotificationManager.CallbackConnected() {
                                    @Override
                                    public void onConnect() {
                                        Log.i(LOG_TAG, "WebSockets connected");

                                        // As soon as we are connected with the NotificationManager, we send a message to ourself.
                                        notification.sendMessage(notification.getChannelId(), "hello myself");
                                    }
                                });
                            }
                        });
                    }
                });

                initAnymoteConnection();

            }
        };

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        bboxSearch();

        // Try to find a Bbox and authenticate
        return;
    }

    private void displayWarningConnectivityMessage(String textContent) {
        int popupWidth = 600;
        int popupHeight = 500;

        // Inflate the vocon_popup.xml
        LinearLayout viewGroup = (LinearLayout) MyActivity.mainActivity.findViewById(R.id.warningConnectivity);
        LayoutInflater layoutInflater = (LayoutInflater) MyActivity.mainActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.warning_connectivity, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(MyActivity.mainActivity); //final popup si bug
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        assert layout != null;

        TextView textView = (TextView) layout.findViewById(R.id.textViewWarningConnectivity);

        textView.setText(textContent);
        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
        // Getting a reference to Close button, and close the popup when clicked.

        Button close = (Button) layout.findViewById(R.id.buttonWarningConnectivity);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.dismiss();
            }

        });
    }

    public void bboxSearch() {

        bboxManager = new BboxManager();
        bboxManager.startLookingForBbox(getApplicationContext(), new BboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final Bbox bboxFound) {

                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();

                // We save our Bbox.
                currentBbox = bboxFound;

                // We store the IP of the Bbox in the applications preferences.
                SharedPreferences preference = getPreferences(0);
                SharedPreferences.Editor editor = preference.edit();
                editor.putString(IP_PREFERENCE, bboxFound.getIp());
                editor.commit();

                /* security will be used in the next release
                // BBox Ip should be know by this line so we can try to authenticate with Bytel platform and share authentication token with Bbox
                // result can be check in the IAuthCallback callback
                AuthentTask authentProcess = new AuthentTask();

                authentProcess.doInBackground(authenticationCallback);
                */

                // When security will be available, do this stuff in the security's callback
                initAnymoteConnection();

            }
        });

    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1, mContex))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my_activity2, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Context mContext;
        private Bbox bbox;

        public PlaceholderFragment(Context mContext) {
            this.mContext = mContext;
        }

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, Context mContext) {
            PlaceholderFragment fragment = new PlaceholderFragment(mContext);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final int section = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;

            if (section == 1) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                initRemote(rootView);
            } else if (section == 2) {
                rootView = inflater.inflate(R.layout.fragment_apps, container, false);
                initAppController(rootView);
            } else if (section == 3 || section == 10) {
                rootView = inflater.inflate(R.layout.fragment_settings, container, false);
                initSetting(rootView);
            } else if (section == 4) {
                rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
                initNotifications(rootView);
            } else {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                initRemote(rootView);
            }

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MyActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));

        }

        // Settings
        private void initSetting(View rootView) {

            final SharedPreferences preference = getActivity().getPreferences(0);

            Button save = (Button) rootView.findViewById(R.id.button);

            final EditText editText = (EditText) rootView.findViewById(R.id.editText);

            editText.setText(preference.getString("bboxIP", "*.*.*.*"));

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SharedPreferences.Editor editor = preference.edit();
                    editor.putString("bboxIP", editText.getText().toString());
                    editor.commit();

                    /* security will be used in the nex release
                    // BBox Ip should be know by this line so we can try to authenticate with Bytel platform and share authentication token with Bbox
                    // result can be check in the IAuthCallback callback
                    AuthentTask authentProcess = new AuthentTask();

                    authentProcess.doInBackground(((MyActivity) getActivity()).authenticationCallback);
                    */

                        // When security will be available, do this stuff in the security's callback

                    //Destroy the connection before trying to establish a new one
                    if(((MyActivity) getActivity()).anymoteSender != null)
                    {
                        ((MyActivity) getActivity()).anymoteSender.disconnect();
                    }
                    ((MyActivity) getActivity()).initAnymoteConnection();

                    }
                });
        }

        // App controller
        private void initAppController(final View rootView) {

            mContext = getActivity().getApplicationContext();

            SharedPreferences preference = getActivity().getPreferences(0);
            bbox = new Bbox(preference.getString(IP_PREFERENCE, DEFAULT_IP));

            ApplicationsManager applicationsManager = bbox.getApplicationsManager();

            // We are getting all the installed applications on the Bbox, and show them in a listView.
            applicationsManager.getApplications(new ApplicationsManager.CallbackApplications() {
                @Override
                public void onResult(int status, List<Application> applications) {
                    ApplicationAdapter applicationAdapter = new ApplicationAdapter(mContext, applications, getActivity());
                    ListView listView = (ListView) rootView.findViewById(R.id.listView);
                    listView.setAdapter(applicationAdapter);
                }
            });
        }

        // Notifications
        private void initNotifications(final View rootView) {
            mContext = getActivity().getApplicationContext();

            // We are showing all our received notification in a ListView.
            NotificationsAdapter notificationsAdapter = new NotificationsAdapter(mContext, notificationsList, getActivity());
            ListView listView = (ListView) rootView.findViewById(R.id.listView);
            listView.setAdapter(notificationsAdapter);
        }

        private void initRemote(View rootView) {

            final Map<Integer, Code> buttons = new HashMap<Integer, Code>();
            buttons.put(R.id.button0, Code.KEYCODE_0);
            buttons.put(R.id.button1, Code.KEYCODE_1);
            buttons.put(R.id.button2, Code.KEYCODE_2);
            buttons.put(R.id.button3, Code.KEYCODE_3);
            buttons.put(R.id.button4, Code.KEYCODE_4);
            buttons.put(R.id.button5, Code.KEYCODE_5);
            buttons.put(R.id.button6, Code.KEYCODE_6);
            buttons.put(R.id.button7, Code.KEYCODE_7);
            buttons.put(R.id.button8, Code.KEYCODE_8);
            buttons.put(R.id.button9, Code.KEYCODE_9);
            buttons.put(R.id.buttonBack, Code.KEYCODE_BACK);
            buttons.put(R.id.buttonExit, Code.KEYCODE_BACK);
            buttons.put(R.id.buttonUp, Code.KEYCODE_DPAD_UP);
            buttons.put(R.id.buttonDown, Code.KEYCODE_DPAD_DOWN);
            buttons.put(R.id.buttonLeft, Code.KEYCODE_DPAD_LEFT);
            buttons.put(R.id.buttonRight, Code.KEYCODE_DPAD_RIGHT);
            buttons.put(R.id.buttonOk, Code.KEYCODE_ENTER);
            buttons.put(R.id.buttonHome, Code.KEYCODE_HOME);

            SharedPreferences preference = getActivity().getPreferences(0);

            for (final Integer id : buttons.keySet()) {

                final Button button = (Button) rootView.findViewById(id);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyActivity activity = (MyActivity) getActivity();
                        if (activity.anymoteSender != null) {
                            activity.anymoteSender.sendKeyPress(buttons.get(id));
                        }
                    }
                });
            }

        }
    }

    private void initAnymoteConnection() {
        mConnection = new ServiceConnection() {
            /*
             * ServiceConnection listener methods.
             */
            public void onServiceConnected(ComponentName name, IBinder service) {
                mAnymoteClientService = ((AnymoteClientService.AnymoteClientServiceBinder) service).getService();
                mAnymoteClientService.attachClientListener(MyActivity.this);
            }

            public void onServiceDisconnected(ComponentName name) {
                mAnymoteClientService.detachClientListener(MyActivity.this);
                mAnymoteClientService = null;
            }
        };

        Intent intent = new Intent(MyActivity.this, AnymoteClientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onConnected(AnymoteSender anymoteSender) {
        this.anymoteSender = anymoteSender;
    }

    @Override
    public void onDisconnected() {
        this.anymoteSender = null;
    }

    @Override
    public void onConnectionFailed() {
        this.anymoteSender = null;
    }
}
