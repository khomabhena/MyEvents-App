package tech.myevents.app;


import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAd extends Fragment implements View.OnClickListener {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;

    static int SCROLL_POS = 0;
    static long LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> adKey;
    FirebaseAuth auth;
    FirebaseUser firebaseUser = null;

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();

    public FragmentAd() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ad, container, false);

        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        auth = FirebaseAuth.getInstance();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adKey = dbOperations.getAdKeys(db, localUid);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        StaticVariables.adsAdapter = new AdapterAd(getContext(), R.layout.row_ads);

        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        if (StaticVariables.listAds != null)
            initializeListView();
        else
            new AdsBackTask().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(12548);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvEmpty:
                LAST_LOAD = 0;
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SCROLL_POS = listView.getFirstVisiblePosition();
    }



    private void initializeListView() {
        if (StaticVariables.listAds.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(StaticVariables.adsAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
            updateValues();
        } else {
            //listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }
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




    private class AdsBackTask extends AsyncTask<Void, GetSetterAd, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getAds(db, localUid);
            int count = cursor.getCount();

            if (count != 0)
                StaticVariables.listAds = new ArrayList<>();

            int impressions, sigVer;
            String adKey, broadcasterUid, brandName, brandLink, title, details, profileLink;
            String locationCode, interestCode, status, id;
            long duration, broadcastTime, updateTime;
            boolean verified;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Ad.ID));
                impressions = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Ad.IMPRESSIONS)));
                duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Ad.DURATION)));
                if (getCurrentTimestamp() > duration) {
                    count--;
                    continue;
                }
                broadcastTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Ad.BROADCAST_TIME)));
                adKey = cursor.getString(cursor.getColumnIndex(DBContract.Ad.KEY));
                broadcasterUid = cursor.getString(cursor.getColumnIndex(DBContract.Ad.BROADCASTER_UID));
                brandName = cursor.getString(cursor.getColumnIndex(DBContract.Ad.BRAND_NAME));
                brandLink = cursor.getString(cursor.getColumnIndex(DBContract.Ad.BRAND_LINK));
                title = cursor.getString(cursor.getColumnIndex(DBContract.Ad.TITLE));
                details = cursor.getString(cursor.getColumnIndex(DBContract.Ad.DETAILS));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Ad.PROFILE_LINK));
                locationCode = cursor.getString(cursor.getColumnIndex(DBContract.Ad.LOCATION_CODE));
                interestCode = cursor.getString(cursor.getColumnIndex(DBContract.Ad.INTEREST_CODE));
                status = cursor.getString(cursor.getColumnIndex(DBContract.Ad.STATUS));
                updateTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Ad.UPDATE_TIME)));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Ad.VERIFIED));
                sigVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Ad.SIGNATURE_VERSION)));

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterAd setter = new GetSetterAd(broadcasterUid, adKey, impressions, 0,
                        0, brandName, brandLink, title, details, profileLink, locationCode, interestCode,
                        status, duration, broadcastTime, 0, 0, "", null, locationCode,
                        updateTime, verified, sigVer);

                setterUpdate = new GetSetterUpdate("business", id, broadcasterUid, "", "");
                listUpdate.add(setterUpdate);

                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterAd... values) {
            StaticVariables.adsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(StaticVariables.adsAdapter);
                LAST_LOAD = getCurrentTimestamp();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(SCROLL_POS);
                    }
                });
                updateValues();
            } else {
                //progressBar.setVisibility(View.GONE);
                //listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            addFirebaseListener();
        }
    }

    private void updateValues() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterBusiness setterBusiness = snap.getValue(GetSetterBusiness.class);

                            for (int x =0; x < listUpdate.size(); x++) {
                                GetSetterUpdate setterUpdate = (GetSetterUpdate) listUpdate.get(x);
                                if (setterUpdate.getUid().equals(setterBusiness.getUid())) {

                                    for (int y = 0; y < StaticVariables.listAds.size(); y++) {
                                        GetSetterAd setterAd = (GetSetterAd) StaticVariables.listAds.get(y);
                                        if (setterAd.getBroadcasterUid().equals(setterUpdate.getUid())) {
                                            setterAd.setBrandName(setterBusiness.getBrandName());
                                            setterAd.setBrandLink(setterBusiness.getProfileLink());
                                            setterAd.setVerified(setterBusiness.isVerified());
                                            setterAd.setSignatureVersion(setterBusiness.getSignatureVersion());
                                            StaticVariables.adsAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(DBContract.Ad.BRAND_NAME, setterBusiness.getBrandName());
                                    values.put(DBContract.Ad.BRAND_LINK, setterBusiness.getProfileLink());
                                    values.put(DBContract.Ad.VERIFIED, setterBusiness.isVerified() ? "yes": "no");
                                    values.put(DBContract.Ad.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                    db.update(DBContract.Ad.TABLE_NAME, values,
                                            DBContract.Ad.ID + "='" + setterUpdate.getDbId() + "'",
                                            null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addFirebaseListener() {
        final String userInterest = prefs.getString(AppInfo.INTEREST_CODE, "1a1b1c1d1e1f1g1h1i1j1k1l1m1n1o1p1q");
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childAds)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            for (DataSnapshot snap: snapshot.getChildren()) {
                                GetSetterAd setter = snap.getValue(GetSetterAd.class);
                                if (setter.getStatus().equals(StaticVariables.deleted)) {
                                    new DeleteAd(getContext()).execute(setter);
                                    continue;
                                }

                                if (!userInterest.contains(setter.getInterestCode()))
                                    continue;

                                if (adKey.contains(setter.getAdKey()))
                                    continue;

                                if (getCurrentTimestamp() > setter.getDuration())
                                    continue;

                                if (StaticVariables.listAds == null)
                                    StaticVariables.listAds = new ArrayList<>();

                                if (StaticVariables.listAds.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.adsAdapter.add(setter);
                                    listView.setAdapter(StaticVariables.adsAdapter);
                                } else {
                                    StaticVariables.listAds.add(0, setter);
                                    StaticVariables.adsAdapter.notifyDataSetChanged();
                                }

                                new InsertAds(getContext()).execute(setter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class InsertAds extends AsyncTask<GetSetterAd, Void, Void> {

        Context context;

        public InsertAds(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterAd... params) {
            GetSetterAd setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            if (!adKey.contains(setter.getAdKey())) {
                ContentValues values = new ContentValues();
                values.put(DBContract.Ad.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                values.put(DBContract.Ad.IMPRESSIONS, String.valueOf(setter.getImpressions()));
                values.put(DBContract.Ad.DURATION, String.valueOf(setter.getDuration()));
                values.put(DBContract.Ad.KEY, setter.getAdKey());
                values.put(DBContract.Ad.BROADCASTER_UID, setter.getBroadcasterUid());
                values.put(DBContract.Ad.BRAND_NAME, setter.getBrandName());
                values.put(DBContract.Ad.BRAND_LINK, setter.getBrandLink());
                values.put(DBContract.Ad.TITLE, setter.getTitle());
                values.put(DBContract.Ad.DETAILS, setter.getDetails());
                values.put(DBContract.Ad.PROFILE_LINK, setter.getProfileLink());
                values.put(DBContract.Ad.LOCATION_CODE, setter.getAdLocation());
                values.put(DBContract.Ad.INTEREST_CODE, setter.getInterestCode());
                values.put(DBContract.Ad.STATUS, setter.getStatus());
                values.put(DBContract.Ad.VERIFIED, setter.isVerified() ? "yes" : "no");
                values.put(DBContract.Ad.BROADCAST_TIME, "" + setter.getBroadcastTime());

                values.put(DBContract.Ad.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));
                values.put(DBContract.Ad.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                db.insert(DBContract.Ad.TABLE_NAME, null, values);
                addImpression(setter.getAdKey());
            }
            return null;
        }

    }

    private class DeleteAd extends AsyncTask<GetSetterAd, Void, Void> {

        Context context;

        public DeleteAd(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterAd... params) {
            GetSetterAd setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            ContentValues values = new ContentValues();
            values.put(DBContract.Ad.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
            values.put(DBContract.Ad.IMPRESSIONS, String.valueOf(setter.getImpressions()));
            values.put(DBContract.Ad.DURATION, String.valueOf(setter.getDuration()));
            values.put(DBContract.Ad.KEY, setter.getAdKey());
            values.put(DBContract.Ad.BROADCASTER_UID, setter.getBroadcasterUid());
            values.put(DBContract.Ad.BRAND_NAME, setter.getBrandName());
            values.put(DBContract.Ad.BRAND_LINK, setter.getBrandLink());
            values.put(DBContract.Ad.TITLE, setter.getTitle());
            values.put(DBContract.Ad.DETAILS, setter.getDetails());
            values.put(DBContract.Ad.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Ad.LOCATION_CODE, setter.getAdLocation());
            values.put(DBContract.Ad.INTEREST_CODE, setter.getInterestCode());
            values.put(DBContract.Ad.STATUS, setter.getStatus());
            values.put(DBContract.Ad.VERIFIED, setter.isVerified() ? "yes" : "no");
            values.put(DBContract.Ad.BROADCAST_TIME, "" + setter.getBroadcastTime());

            values.put(DBContract.Ad.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));
            values.put(DBContract.Ad.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

            db.update(DBContract.Ad.TABLE_NAME, values,
                    DBContract.Ad.KEY + "='" + setter.getAdKey() + "'",
                    null);

            return null;
        }

    }

    private void addImpression(String adKey) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        String username = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "..."));
        String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, ""));
        String location = prefs.getString(AppInfo.BUSINESS_LOCATION, prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "No location"));
        int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, prefs.getInt(AppInfo.SIG_VER_INDI, 0));

        GetSetterImpression setter = new GetSetterImpression(type, uid, adKey, username, profileLink,
                location, false, getCurrentTimestamp(), signatureVersion);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(adKey)
                .child(uid)
                .setValue(setter);
    }

}
