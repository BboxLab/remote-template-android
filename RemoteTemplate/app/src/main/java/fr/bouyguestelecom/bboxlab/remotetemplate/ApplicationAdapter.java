package fr.bouyguestelecom.bboxlab.remotetemplate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.bouyguestelecom.tv.openapi.secondscreen.application.Application;
import fr.bouyguestelecom.tv.openapi.secondscreen.application.ApplicationsManager;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.httputils.CallbackHttpStatus;


/**
 * Created by PCHERIER on 07/04/2014.
 */
public class ApplicationAdapter extends ArrayAdapter<Application> {

    private Context mContext;
    private List<Application> applications;
    private LayoutInflater vi;
    private Activity activity;


    public ApplicationAdapter(Context context, List<Application> apps, Activity activity) {
        super(context, 0, apps);
        mContext = context;
        applications = apps;
        this.activity = activity;
        vi = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        final Application app = applications.get(position);

        if (app != null) {
            view = vi.inflate(R.layout.element_app, null);

            final Button start = (Button) view.findViewById(R.id.button);
            final Button stop = (Button) view.findViewById(R.id.button2);
            final TextView appName = (TextView) view.findViewById(R.id.textView);
            final TextView appState = (TextView) view.findViewById(R.id.textView2);

            switch (app.getAppState()) {
                case BACKGROUND:
                    appState.setTextColor(Color.GRAY);
                    break;
                case FOREGROUND:
                    appState.setTextColor(Color.parseColor("#228B22"));
                    break;
                case STOPPED:
                    appState.setTextColor(Color.RED);
                    break;
                case UNKNOWN_STATE:
                    appState.setTextColor(Color.MAGENTA);
                    break;
            }

            appName.setText(app.getAppName());
            appState.setText(app.getAppState().toString());

            Bbox bbox = BboxHolder.getInstance().getBbox();

            if (bbox != null) {
                final ApplicationsManager applicationsManager = bbox.getApplicationsManager();

                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        applicationsManager.startApplication(app, new CallbackHttpStatus() {
                            @Override
                            public void onResult(int i) {
                                applicationsManager.getApplications(new ApplicationsManager.CallbackApplications() {
                                    @Override
                                    public void onResult(int status, List<Application> applicationsList) {
                                        applications = applicationsList;
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                    }
                });

                stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        applicationsManager.stopApplication(app, new CallbackHttpStatus() {
                            @Override
                            public void onResult(int i) {
                                applicationsManager.getApplications(new ApplicationsManager.CallbackApplications() {
                                    @Override
                                    public void onResult(int status, List<Application> applicationsList) {
                                        applications = applicationsList;
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
        return view;
    }
}
