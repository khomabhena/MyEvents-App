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

public class PublishAd extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    static int SCROLL_POS = 0;
    static long LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;

    FirebaseAuth auth;
    FirebaseUser firebaseUser = null;

    static boolean DB_LISTENING = false;
    ChildEventListener listener;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_ad);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);

        StaticVariables.adsAdapterWaiting = new AdapterWaitingAd(this, R.layout.row_ads_waiting);

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        if (StaticVariables.listAdsWaiting == null || ((getCurrentTimestamp() - LAST_LOAD) > (60000 * 20))) {
            if (StaticVariables.listAdsWaiting != null)
                initializeListView();
            else
                StaticVariables.listAdsWaiting = new ArrayList<>();
            loadAllAds();
        } else
            initializeListView();


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Publish Ad";
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
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        listView.setAdapter(StaticVariables.adsAdapterWaiting);
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(SCROLL_POS);
            }
        });
    }

    private void loadAllAds() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childAdsWaiting)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (StaticVariables.listAdsWaiting == null)
                            StaticVariables.listAdsWaiting = new ArrayList<>();
                        long count = 0;
                        for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                            count = dataSnapshotChild.getChildrenCount();
                            for (DataSnapshot snapshot : dataSnapshotChild.getChildren()) {
                                GetSetterAd setter = snapshot.getValue(GetSetterAd.class);

                                if (getCurrentTimestamp() > setter.getDuration())
                                    continue;
                                else
                                    count++;

                                if (StaticVariables.listAdsWaiting.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.adsAdapterWaiting.add(setter);
                                    listView.setAdapter(StaticVariables.adsAdapterWaiting);
                                } else {
                                    StaticVariables.listAdsWaiting.add(0, setter);
                                    StaticVariables.adsAdapterWaiting.notifyDataSetChanged();
                                }

                            }
                        }

                        if (count != 0) {
                            NETWORK_AVAILABLE = true;
                            LAST_LOAD = getCurrentTimestamp();
                        }
                        /*if (listener == null)
                            loadAdsListener();*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadAdsListener() {
        listener = FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childAdsWaiting)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (StaticVariables.listAdsWaiting == null)
                                StaticVariables.listAdsWaiting = new ArrayList<>();

                            GetSetterAd setter = dataSnapshot.getValue(GetSetterAd.class);

                            StaticVariables.adsAdapterWaiting.add(setter);
                            StaticVariables.adsAdapterWaiting.notifyDataSetChanged();
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
