package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;
import com.example.google.tv.anymotelibrary.client.AnymoteSender;
import com.google.anymote.Key.Code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.application.Application;
import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.media.Media;
import fr.bouyguestelecom.tv.openapi.secondscreen.media.MediaManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.NotificationType;
import fr.bouyguestelecom.tv.openapi.secondscreen.notification.WebSocket;

public class MyActivity
        extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ClientListener               //Allows this activity to have a slidable sidebar with sub-fragments activities callback, and a callback for Anymote services
{
    public static Activity                                      mainActivity;
    private CharSequence                                        mTitle;                             //Stores the last screen title. To be used in {@link #restoreActionBar()}.
    public IAuthCallback                                        authenticationCallback;             //Authentication callback
    private NavigationDrawerFragment                            mNavigationDrawerFragment;          //The slidable's sidebar fragment
    private AnymoteClientService                                mAnymoteClientService;              //Anymote client class, this manages discovering, pairing and connecting to Google TV devices on network.
    public AnymoteSender                                        anymoteSender;                      //A proxy class that sends messages to the Anymote server using Anymote protocol.
    public ServiceConnection                                    mConnection;                        //Interface for monitoring a service state
    private static List<String> notificationsList =             new ArrayList<String>();                 //Notifications' ArrayList
    private static final String LOG_TAG =                       MyActivity.class.getCanonicalName();    //For logging purposes, fetch the class' canonical name
    private ProgressBar                                         progressBar;

    @Override
    protected void          onCreate(Bundle savedInstanceState)                                     //Initializing the main activity
    {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_my_activity);                                              //Set the chosen xml activity on the top of activities' stack
        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);                                          //Allow the slidable bar to interact with fragments of the main activity
        mTitle = getTitle();
        authenticationCallback = new IAuthCallback()                                                //Authentication to
        {
            @Override
            public void             onAuthResult(int code, String  msg)                             //Authentication callback
            {
                Log.d(LOG_TAG, "onAuthResult msg=" + msg + " code=" + code);                        //Logs this class' onAuthResult

                if (code > 299 || code < 200)                                                       //Status different than 2xx means that something wrong happened
                {                                                                                   //Put stuff here that need authentication process to be done
                    displayWarningConnectivityMessage(msg);                                         //Something wrong happen during authentication process
                    return;
                }                                                                                   //Check the reason to know exactly why there is an error (problem with tokens means problem during connection with distant platform
                                                                                                    //Authentication while problem with sessionId means problem with bbox connectivity check)
                final Bbox          bbox = BboxHolder.getInstance().getBbox();                      //Starts almost everything, creates a BboxHolder that holds a Bbox

                if (bbox != null)
                {
                    bbox.getApplicationsManager().getMyAppId("Remote_Controller",
                            new ApplicationsManager.CallbackAppId()                                 //We have to get our AppID in order to initiate a websocket connection.
                            {
                                @Override
                                public void             onResult(int statusCode, String appId)
                                {                                                                   //Now we have our AppID, we can therefor instantiate a NotificationManager with the WebSocket implementation.
                                    final NotificationManager notification = WebSocket.getInstance(appId, bbox);    //Websocket start point

                                    notification.subscribe(NotificationType.MESSAGE,                // Before the NotificationManager start listening we are going to subscribe to Message.
                                            new NotificationManager.CallbackSubscribed()            // We provide a callback, because we want to start listening to notifications after we subscribe to Message.
                                            {
                                                @Override
                                                public void             onResult(int statusCode)
                                                {
                                                    Log.d(LOG_TAG, "status subscribe:" + statusCode);   // We can check if the subscription is a success with the http return code.
                                                    notification.subscribe(NotificationType.APPLICATION, null);     // We also subscribe to Applications and Media, but we do not provide a callback this time. We don't want to wait for the return.
                                                    notification.subscribe(NotificationType.MEDIA, null);
                                                    notification.addAllNotificationsListener(new NotificationManager.Listener()     // We add a AllNotificationsListener to Log all the notifications we receive.
                                                    {
                                                        @Override
                                                        public void onNotification(JSONObject jsonObject)
                                                        {
                                                            notificationsList.add(jsonObject.toString());   // We here add the received notification to a list, to be able to print it in our UI.
                                                            Log.d(LOG_TAG, jsonObject.toString());
                                                        }
                                                    });
                                                    notification.addMessageListener(new NotificationManager.Listener()      // Here we add a MessageListener. Inside we will only receive Message notifications.
                                                    {                                                                       // Message notification will still appear in the AllNotificationListener.
                                                        @Override
                                                        public void onNotification(JSONObject jsonObject)
                                                        {
                                                            Log.d(LOG_TAG, jsonObject.toString());
                                                        }
                                                    });
                                                    notification.addApplicationListener(new NotificationManager.Listener()      // Same here with a ApplicationListener.
                                                    {
                                                        @Override
                                                        public void onNotification(JSONObject jsonObject)
                                                        {
                                                            Log.d(LOG_TAG, jsonObject.toString());
                                                        }
                                                    });
                                                    notification.listen(new NotificationManager.CallbackConnected()     // Once we have set our listeners, we can start listening for notifications.
                                                    {
                                                        @Override
                                                        public void             onConnect()
                                                        {
                                                            Log.i(LOG_TAG, "WebSockets connected");
                                                            notification.sendMessage(notification.getChannelId(), "hello myself");      // As soon as we are connected with the NotificationManager, we send a message to ourself.
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }
                initAnymoteConnection();                                                            //Pairing to a Bbox through anymote protocol
            }
        };
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));                                    // Set up the drawer.
        BboxHolder.getInstance().bboxSearch(this, authenticationCallback);                          //Search the first bbox on the network, then authenticate this bbox on Bouygues' Telecom Cloud
    }

    private void            displayWarningConnectivityMessage(String textContent)                   //Display a popup if an authentication failed
    {
        int                         popupWidth = 600;
        int                         popupHeight = 500;
        LinearLayout                viewGroup = (LinearLayout)MyActivity.mainActivity
                                                .findViewById(R.id.warningConnectivity);            // Inflate the vocon_popup.xml
        LayoutInflater              layoutInflater = (LayoutInflater)MyActivity.mainActivity
                                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View                        layout = layoutInflater.inflate(R.layout
                                                .warning_connectivity, viewGroup);
        final PopupWindow           popup = new PopupWindow(MyActivity.mainActivity);               // Creating the PopupWindow if bug

        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        assert layout != null;

        TextView                    textView = (TextView)layout.findViewById(R.id
                                                .textViewWarningConnectivity);

        textView.setText(textContent);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);                                         // Displaying the popup at the specified location, + offsets.

        Button                      close = (Button)layout.findViewById(R.id
                                            .buttonWarningConnectivity);                            // Getting a reference to Close button, and close the popup when clicked.

        close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void             onClick(View v)
            {
                popup.dismiss();
            }
        });
    }

    @Override
    public void             onNavigationDrawerItemSelected(int position)                            //Set the slidable sidebar sub-fragments
    {

        FragmentManager             fragmentManager = getSupportFragmentManager();                  // update the main content by replacing fragments

        fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment
                .newInstance(position + 1, getApplicationContext())).commit();
    }

    public void             onSectionAttached(int number)
    {
        switch (number)
        {
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
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            /*case 6:
                mTitle = getString(R.string.title_section6);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                break;
            case 8:
                mTitle = getString(R.string.title_section8);
                break;*/
        }
    }

    public void             restoreActionBar()                                                      //Sets action bar elements
    {
        ActionBar           actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean          onCreateOptionsMenu(Menu menu)
    {
        if (!mNavigationDrawerFragment.isDrawerOpen())
        {
            getMenuInflater().inflate(R.menu.my_activity2, menu);                                   // Only show items in the action bar relevant to this screen if the drawer is not showing. Otherwise, let the drawer decide what to show in the action bar.
            restoreActionBar();
            return (true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean          onOptionsItemSelected(MenuItem item)                                    // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml
    {
        int                 id = item.getItemId();

        if (id == R.id.action_settings)
            return (true);
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment
            extends Fragment                                                                        //Concerns every sub-fragments of the slisdable sidebar's fragment
    {
        private static final String ARG_SECTION_NUMBER = "section_number";              //The fragment argument representing the section number for this fragment

        public PlaceholderFragment()
        {

        }

        public static PlaceholderFragment newInstance(int sectionNumber, Context mContext)          //Returns a new instance of this fragment for the given section number.
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return (fragment);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container
                , Bundle savedInstanceState)
        {
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
            } else if (section == 5)                                                                  //If the "Channel List" tab is chosen
            {
                rootView = inflater.inflate(R.layout.fragment_chanlist, container, false);
                initChanList(rootView);                                                             //Starts the whole tab fragment_chanlist.xml
            }
            else {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                initRemote(rootView);
            }
            return (rootView);
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            ((MyActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        private void initSetting(View rootView)                                          //Concerns slidable sidebar's "Settings" sub-fragment
        {
            final SharedPreferences preference = getActivity().getPreferences(0);
            Button save = (Button) rootView.findViewById(R.id.button);
            final EditText editText = (EditText) rootView.findViewById(R.id.editText);
            final TextView crtIP = (TextView) rootView.findViewById(R.id.crtIP);
            final TextView showCrtIP = (TextView) rootView.findViewById(R.id.showCrtIP);

            editText.setText(preference.getString("bboxIP", "*.*.*.*"));
            showCrtIP.setText(preference.getString("*.*.*.*", BboxHolder.getInstance().getBbox().getIp()));
            save.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
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

                    if (((MyActivity) getActivity()).anymoteSender != null)                          //Destroy any Anymote connections before establishing a new one
                        ((MyActivity) getActivity()).anymoteSender.disconnect();
                    ((MyActivity) getActivity()).initAnymoteConnection();
                }
            });
        }

        private void initAppController(final View rootView)                              // App controller
        {
            ApplicationsManager applicationsManager = BboxHolder.getInstance().getBbox()
                    .getApplicationsManager();

            applicationsManager.getApplications(new ApplicationsManager.CallbackApplications()      // We are getting all the installed applications on the Bbox, and show them in a listView.
            {
                @Override
                public void onResult(int status, List<Application> applications)
                {
                    ApplicationAdapter applicationAdapter = new ApplicationAdapter(getActivity()
                            .getApplicationContext()
                            , applications, getActivity());
                    ListView listView = (ListView) rootView
                            .findViewById(R.id.listView);

                    listView.setAdapter(applicationAdapter);
                }
            });
        }

        private void initNotifications(final View rootView)                              // Notifications
        {
            NotificationsAdapter notificationsAdapter = new NotificationsAdapter(getActivity()
                    .getApplicationContext()
                    , notificationsList, getActivity());    // We are showing all our received notification in a ListView.
            ListView listView = (ListView) rootView.findViewById(R.id.listView);

            listView.setAdapter(notificationsAdapter);

        }

        private void initRemote(View rootView)
        {
            final Map<Integer, KeyHit[]> buttons = new HashMap<Integer, KeyHit[]>();

            buttons.put(R.id.button0, KeyHit.shiftedHit(Code.KEYCODE_0));
            buttons.put(R.id.button1, KeyHit.shiftedHit(Code.KEYCODE_1));
            buttons.put(R.id.button2, KeyHit.shiftedHit(Code.KEYCODE_2));
            buttons.put(R.id.button3, KeyHit.shiftedHit(Code.KEYCODE_3));
            buttons.put(R.id.button4, KeyHit.shiftedHit(Code.KEYCODE_4));
            buttons.put(R.id.button5, KeyHit.shiftedHit(Code.KEYCODE_5));
            buttons.put(R.id.button6, KeyHit.shiftedHit(Code.KEYCODE_6));
            buttons.put(R.id.button7, KeyHit.shiftedHit(Code.KEYCODE_7));
            buttons.put(R.id.button8, KeyHit.shiftedHit(Code.KEYCODE_8));
            buttons.put(R.id.button9, KeyHit.shiftedHit(Code.KEYCODE_9));
            buttons.put(R.id.buttonBack, KeyHit.hit(Code.KEYCODE_BACK));
            buttons.put(R.id.buttonExit, KeyHit.hit(Code.KEYCODE_BACK));
            buttons.put(R.id.buttonUp, KeyHit.hit(Code.KEYCODE_DPAD_UP));
            buttons.put(R.id.buttonDown, KeyHit.hit(Code.KEYCODE_DPAD_DOWN));
            buttons.put(R.id.buttonLeft, KeyHit.hit(Code.KEYCODE_DPAD_LEFT));
            buttons.put(R.id.buttonRight, KeyHit.hit(Code.KEYCODE_DPAD_RIGHT));
            buttons.put(R.id.buttonOk, KeyHit.hit(Code.KEYCODE_ENTER));
            buttons.put(R.id.buttonHome, KeyHit.hit(Code.KEYCODE_HOME));
            for (final Integer id : buttons.keySet()) {
                final Button button = (Button) rootView.findViewById(id);

                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        MyActivity activity = (MyActivity) getActivity();

                        if (activity.anymoteSender != null) {
                            KeyHit[] hits=buttons.get(id);
                            for (int i = 0; i < hits.length; i++) {
                                KeyHit hit=hits[i];
                                activity.anymoteSender.sendKey(hit.getCode(), hit.getAction());
                            }
                        }
                    }
                });
            }
        }

        private void initChanList(final View rootView)                                   //Starts Channel List view, taking place on main view
        {
            MediaManager mediaMng = BboxHolder.getInstance().getBbox().getMediaManager();   //Preparing fetching sequence; Locking target Bbox, Checking setted Bbox presence, Using the setted Bbox's Media fetching sequence

            mediaMng.getChannels(new MediaManager.CallbackChannels()                                //Asynchronous REST data fetching through callback
            {                                                                                       //Anonymous channels' fetching function startpoint
                @Override
                public void onResult(int status, final List<Media> chans)                     //On receiving JSON data, fill the Channel List's fragment with
                {
                    MediaAdapter mediaAdp = new MediaAdapter(getActivity()
                            .getApplicationContext(), chans, getActivity());    //Initialize the Channel List's main fragment
                    ListView lstView = (ListView) rootView.findViewById(R.id.listView);  //Create the ListView and fill it with the corresponding xml fragment; This is where getView is used through override
                    final ToggleButton tglBtn = (ToggleButton) rootView.findViewById(R.id.toggleButton);

                    tglBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(CompoundButton btnView, boolean isChk)
                        {
                            String lstStr = String.valueOf(chans.size());

                            if (isChk) {
                                tglBtn.setTextOn("Channels: " + lstStr);
                            } else {
                                tglBtn.setTextOff("Channels: " + lstStr);
                            }
                        }
                    });

                    lstView.setAdapter(mediaAdp);                                                   //Fill lstView with every "element_media.xml"'s sub-fragments
                }
            });
        }
    }

    private void            initAnymoteConnection()
    {
        mConnection = new ServiceConnection()                                                       //ServiceConnection listener methods.
        {
            public void             onServiceConnected(ComponentName name, IBinder service)
            {
                mAnymoteClientService = ((AnymoteClientService.AnymoteClientServiceBinder) service).getService();
                mAnymoteClientService.attachClientListener(MyActivity.this);
            }

            public void             onServiceDisconnected(ComponentName name)
            {
                mAnymoteClientService.detachClientListener(MyActivity.this);
                mAnymoteClientService = null;
            }
        };
        Intent          intent = new Intent(MyActivity.this, AnymoteClientService.class);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void             onConnected(AnymoteSender anymoteSender)
    {
        this.anymoteSender = anymoteSender;
    }

    @Override
    public void             onDisconnected()
    {
        this.anymoteSender = null;
    }

    @Override
    public void             onConnectionFailed()
    {
        this.anymoteSender = null;
    }
}