package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Impressions extends AppCompatActivity {

    ListView listView;
    ProgressBar progressBar;
    TextView tvEmpty;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> impressionKeys;

    private static int MESSAGE_SCROLL_POS = 0;
    private static long MESSAGE_LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean DB_LISTENING = false;

    static ChildEventListener listener;
    String postKey;
    ImpressionsAdapter impressionsAdapter;
    Toolbar toolbar;
    View  viewToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressions);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewToolbar = findViewById(R.id.viewToolbar);
        setSupportActionBar(toolbar);
        statusBarColor();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        postKey = getIntent().getExtras().getString("key");
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        impressionKeys = dbOperations.getImpressionKeys(db, postKey, localUid);

        impressionsAdapter = new ImpressionsAdapter(this, R.layout.row_impressions);

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Impressions";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);
    }

    private void statusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            viewToolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            viewToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticVariables.listImpressions = new ArrayList<>();
        new BackTask().execute();
    }

    public void finishActivity(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }




    private class BackTask extends AsyncTask<Void, GetSetterImpression, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getImpressions(db, postKey, localUid);
            int count = cursor.getCount();
            if (count != 0)
                StaticVariables.listImpressions = new ArrayList<>();

            String uid, key, username, profileLink, location, type;
            boolean verified;
            long timestamp;
            int sigVer;

            while (cursor.moveToNext()) {
                uid = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.UID));
                username = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.USERNAME));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.PROFILE_LINK));
                location = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.LOCATION));
                timestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Impressions.TIMESTAMP)));
                key = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.KEY));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.VERIFIED));
                sigVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Impressions.SIGNATURE_VERSION)));
                type = cursor.getString(cursor.getColumnIndex(DBContract.Impressions.TYPE));

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterImpression setter = new GetSetterImpression(type, uid, key, username,
                        profileLink, location, verified, timestamp, sigVer);

                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterImpression... values) {
            impressionsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(impressionsAdapter);
                listView.setSelection(listView.getAdapter().getCount() - 1);
            } else {
                listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            //addFirebaseListener();
        }

    }

    private void addFirebaseListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(postKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                            GetSetterImpression setter = snapshot.getValue(GetSetterImpression.class);

                            if (StaticVariables.listImpressions == null)
                                StaticVariables.listImpressions = new ArrayList<>();

                            if (!impressionKeys.contains(setter.getUid()))
                                if (StaticVariables.listImpressions.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    impressionsAdapter.add(setter);
                                    listView.setAdapter(impressionsAdapter);
                                } else {
                                    StaticVariables.listImpressions.add(0, setter);
                                    impressionsAdapter.notifyDataSetChanged();
                                }

                            new InsertImpressions(Impressions.this).execute(setter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class InsertImpressions extends AsyncTask<GetSetterImpression, Void, Void> {

        Context context;

        public InsertImpressions(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterImpression... params) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterImpression setter = params[0];

            ContentValues values = new ContentValues();
            values.put(DBContract.Impressions.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
            values.put(DBContract.Impressions.UID, setter.getUid());
            values.put(DBContract.Impressions.KEY, setter.getKey());
            values.put(DBContract.Impressions.USERNAME, setter.getUsername());
            values.put(DBContract.Impressions.LOCATION, setter.getLocation());
            values.put(DBContract.Impressions.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Impressions.TIMESTAMP, "" + setter.getTimestamp());
            values.put(DBContract.Impressions.VERIFIED, setter.isVerified() ? "yes" : "no");
            values.put(DBContract.Impressions.SIGNATURE_VERSION, "" + setter.getSignatureVersion());
            values.put(DBContract.Impressions.TYPE, "" + setter.getType());

            if (!impressionKeys.contains(setter.getUid()))
                db.insert(DBContract.Impressions.TABLE_NAME, null, values);
            else
                db.update(DBContract.Impressions.TABLE_NAME, values, DBContract.Impressions.UID + "='" + setter.getUid() + "'", null);

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            impressionKeys = dbOperations.getImpressionKeys(db, postKey, localUid);

            return null;
        }

    }




    private class ImpressionsAdapter extends ArrayAdapter {

        ImpressionsAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterImpression object) {
            StaticVariables.listImpressions.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listImpressions.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listImpressions.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_impressions, parent, false);
                holder = new Holder();

                holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
                holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
                holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
                holder.tvLocation = (TextView) row.findViewById(R.id.tvLocation);
                holder.tvDate = (TextView) row.findViewById(R.id.tvDate);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterImpression setter = (GetSetterImpression) getItem(position);

            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .crossFade()
                        .into(holder.ivProfile);
            } catch (Exception e) {
                //
            }

            holder.tvUsername.setText(setter.getUsername());
            holder.tvLocation.setText(setter.getLocation());
            holder.tvDate.setText(getDate(setter.getTimestamp()));
            if (setter.isVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);

            return row;

        }

        private class Holder {
            ImageView ivProfile, ivVerified;
            TextView tvUsername, tvLocation, tvDate;
        }

        String getDate(long timeReceived) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeReceived);

            calendar.setTimeInMillis(timeReceived);
            return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE)) + ", " +
                    calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) + 1] + "-" +
                    getYear(calendar.get(Calendar.YEAR));
        }

        private String getYear(int year) {
            String year2 = String.valueOf(year);
            return year2.substring(2, 4);
        }

        public String getTheValue(int value) {
            String theValue = String.valueOf(value);
            if (theValue.length() == 1){
                return "0"+theValue;
            } else {
                return theValue;
            }
        }

    }

}
