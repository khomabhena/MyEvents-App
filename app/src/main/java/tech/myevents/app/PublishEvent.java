package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class PublishEvent extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    static int SCROLL_POS = 0;
    static long LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean PAUSED = false;

    FirebaseAuth auth;
    FirebaseUser firebaseUser = null;

    ChildEventListener listener;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_event);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        StaticVariables.eventsAdapterWaiting = new AdapterEventWaiting(this, R.layout.row_events_waiting);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (StaticVariables.listEventsWaiting == null || ((getCurrentTimestamp() - LAST_LOAD) > (60000 * 20))) {
            if (StaticVariables.listEventsWaiting != null)
                initializeListView();
            else
                StaticVariables.listEventsWaiting = new ArrayList<>();
            loadAllEvents();
        } else
            initializeListView();


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Publish Event";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    private void setStatusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void initializeListView() {
        if (StaticVariables.listEventsWaiting.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(StaticVariables.eventsAdapterWaiting);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
        } else {
            listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadAllEvents() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childEventsWaiting)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (StaticVariables.listEventsWaiting == null)
                            StaticVariables.listEventsWaiting = new ArrayList<>();
                        long count = 0;
                        for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                            count = dataSnapshotChild.getChildrenCount();
                            for (DataSnapshot snapshot : dataSnapshotChild.getChildren()) {
                                GetSetterEvent setter = snapshot.getValue(GetSetterEvent.class);

                                if (getCurrentTimestamp() > setter.getEndTimestamp())
                                    continue;
                                else
                                    count++;

                                if (StaticVariables.listEventsWaiting.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.eventsAdapterWaiting.add(setter);
                                    listView.setAdapter(StaticVariables.eventsAdapterWaiting);
                                } else {
                                    StaticVariables.listEventsWaiting.add(0, setter);
                                    StaticVariables.eventsAdapterWaiting.notifyDataSetChanged();
                                }
                            }
                        }

                        if (count != 0) {
                            NETWORK_AVAILABLE = true;
                            LAST_LOAD = getCurrentTimestamp();
                        }

                        /*if (listener == null)
                            loadEventsListener();*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadEventsListener() {
        listener = FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childEventsWaiting)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (StaticVariables.listEventsWaiting == null)
                                StaticVariables.listEventsWaiting = new ArrayList<>();
                            GetSetterEvent setter = dataSnapshot.getValue(GetSetterEvent.class);

                            StaticVariables.eventsAdapterWaiting.add(setter);
                            StaticVariables.eventsAdapterWaiting.notifyDataSetChanged();
                            listView.setSelection(listView.getAdapter().getCount() - 1);

                        } catch (Exception e) {
                            //
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
    }

    private long getCurrentTimestamp(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return calendar.getTimeInMillis();
    }

}
