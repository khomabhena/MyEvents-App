package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static tech.myevents.app.MainActivity.currentPage;

public class Business extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;

    SharedPreferences prefsApp;
    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> businessKeys;
    Toolbar toolbar;
    View viewToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businesses);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewToolbar = findViewById(R.id.viewToolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        StaticVariables.businessesAdapter = new AdapterBusiness(this, R.layout.row_business);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        businessKeys = dbOperations.getBusinessKeys(db);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Business";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticVariables.listBusinesses = new ArrayList<>();
        new BusinessBackTask().execute();
    }

    public void finishActivity(View view) {
        finish();
    }

    //ToDO update business and friend accounts every time respective accounts are opened

    private void setStatusBarColor() {
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



    private class BusinessBackTask extends AsyncTask<Void, GetSetterBusiness, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Cursor cursor;
            cursor = dbOperations.getBusinesses(db);
            int count = cursor.getCount();

            String uid, brandName, email, website, description,
                    location, profileLink, category;
            boolean verified;
            int signVer;

            while (cursor.moveToNext()) {
                uid = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.UID));
                brandName = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.BRAND_NAME));
                email = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.EMAIL));
                website = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.WEBSITE));
                description = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.DESCRIPTION));
                location = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.LOCATION));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.PROFILE_LINK));
                category = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.CATEGORY));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Businesses.VERIFIED));
                signVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Businesses.SIGNATURE_VERSION)));

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterBusiness setter = new GetSetterBusiness(uid, brandName, email, website, description,
                        "size", location, profileLink, "firebatoken", category + "check",
                        "ecocash", "econame", 0, verified, signVer);
                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterBusiness... values) {
            StaticVariables.businessesAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(StaticVariables.businessesAdapter);
            } else {
                listView.setVisibility(View.GONE);
            }
            getAllBusinessesFromFirebase();
        }
    }

    public void getAllBusinessesFromFirebase() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;

                        for (DataSnapshot snapUid : dataSnapshot.getChildren()) {
                            GetSetterBusiness setter = snapUid.getValue(GetSetterBusiness.class);

                            if (StaticVariables.listBusinesses == null)
                                StaticVariables.listBusinesses = new ArrayList<>();

                            if (!businessKeys.contains(setter.getUid()))
                                if (StaticVariables.listBusinesses.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.businessesAdapter.add(setter);
                                    listView.setAdapter(StaticVariables.businessesAdapter);
                                } else {
                                    StaticVariables.listBusinesses.add(0, setter);
                                    StaticVariables.businessesAdapter.notifyDataSetChanged();
                                }

                            new InsertBusiness(Business.this).execute(setter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private class InsertBusiness extends AsyncTask<GetSetterBusiness, Void, Void> {

        Context context;

        public InsertBusiness(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterBusiness... params) {
            GetSetterBusiness setter = params[0];
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            ContentValues values = new ContentValues();
            values.put(DBContract.Businesses.LOCAL_UID, localUid + type);
            values.put(DBContract.Businesses.UID, setter.getUid());
            values.put(DBContract.Businesses.KEY, setter.getUid());
            values.put(DBContract.Businesses.BRAND_NAME, setter.getBrandName());
            values.put(DBContract.Businesses.LOCATION, setter.getLocation());
            values.put(DBContract.Businesses.WEBSITE, setter.getWebsite());
            values.put(DBContract.Businesses.EMAIL, setter.getEmail());
            values.put(DBContract.Businesses.DESCRIPTION, setter.getDescription());
            values.put(DBContract.Businesses.CATEGORY, setter.getCategory());
            values.put(DBContract.Businesses.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Businesses.VERIFIED, setter.isVerified() ? "yes": "no");
            values.put(DBContract.Businesses.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

            if (!businessKeys.contains(setter.getUid()))
                db.insert(DBContract.Businesses.TABLE_NAME, null, values);
            else
                db.update(DBContract.Businesses.TABLE_NAME, values, DBContract.Businesses.UID + "='" + setter.getUid() + "'", null);

            businessKeys = dbOperations.getBusinessKeys(db);

            return null;
        }

    }

}
