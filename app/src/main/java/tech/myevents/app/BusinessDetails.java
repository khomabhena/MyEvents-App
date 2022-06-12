package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class BusinessDetails extends AppCompatActivity {

    ImageView ivLogo, ivVerified;
    TextView tvAccount, tvLocation, tvCategory, tvEmail, tvWebsite, tvDescription;

    SharedPreferences prefsApp;
    DBOperations dbOperations;
    SQLiteDatabase db;

    int position;
    String from, businessUid;
    private List listBusinesses;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarColor();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert collapsingToolbarLayout != null;

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        listBusinesses = new ArrayList<>();

        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivVerified = (ImageView) findViewById(R.id.ivVerified);
        tvAccount = (TextView) findViewById(R.id.tvAccount);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvWebsite = (TextView) findViewById(R.id.tvWebsite);
        tvDescription = (TextView) findViewById(R.id.tvDescription);

        position = getIntent().getExtras().getInt("position");
        from = getIntent().getExtras().getString("from");


        if (from.equals(StaticVariables.ad)) {

            GetSetterAd setterAd = (GetSetterAd) StaticVariables.listAds.get(position);
            businessUid = setterAd.getBroadcasterUid();
            new BusinessBackTask().execute();
        } else if (from.equals(StaticVariables.event)) {

            GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEvents.get(position);
            businessUid = setterEvent.getBroadcasterUid();
            new BusinessBackTask().execute();
        } else if (from.equals(StaticVariables.playing)) {

            GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listPlaying.get(position);
            businessUid = setterEvent.getBroadcasterUid();
            new BusinessBackTask().execute();
        } else if (from.equals(StaticVariables.eventExclusive)) {

            GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEventsExclusive.get(position);
            businessUid = setterEvent.getBroadcasterUid();
            new BusinessBackTask().execute();
        } else if (from.equals(StaticVariables.eventWaiting)) {

            GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
            businessUid = setterEvent.getBroadcasterUid();
            new BusinessBackTask().execute();
        } else {

            final GetSetterBusiness setterBusiness = (GetSetterBusiness) StaticVariables.listBusinesses.get(position);
            businessUid = setterBusiness.getUid();
            insertBusinessValues(setterBusiness);
        }


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Business Details";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);

    }

    private void setStatusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
    }

    private void insertBusinessValues(final GetSetterBusiness setterBusiness) {
        insertUserProfile(setterBusiness);
        collapsingToolbarLayout.setTitle(setterBusiness.getBrandName());
        tvCategory.setText(setterBusiness.getCategory());
        tvEmail.setText(setterBusiness.getEmail());
        tvWebsite.setText(setterBusiness.getWebsite());
        tvLocation.setText(setterBusiness.getLocation());
        tvDescription.setText(setterBusiness.getDescription());

        if (setterBusiness.isVerified()) {
            ivVerified.setImageResource(R.drawable.ic_verified_teal);
            tvAccount.setText("Verified Business Account");
        }
        tvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setterBusiness.isVerified())
                    Toast.makeText(getApplicationContext(), "This is a verified business account", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "This business account is not verified!!", Toast.LENGTH_LONG).show();
            }
        });
        tvWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://" + setterBusiness.getWebsite().replace("www", "")));
                startActivity(intent);
            }
        });
        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("email"));
                String[] mail = {setterBusiness.getEmail()};
                intent.putExtra(Intent.EXTRA_EMAIL, mail);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hello " + setterBusiness.getBrandName());
                intent.setType("message/rfc822");
                Intent chooser = Intent.createChooser(intent, "Launch Email");
                startActivity(chooser);
            }
        });
    }

    private void insertUserProfile(GetSetterBusiness setterBusiness) {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setterBusiness.getProfileLink());
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivLogo);
        } catch (Exception e) {
            ivLogo.setImageResource(0);
        }
    }





    private class BusinessBackTask extends AsyncTask<Void, GetSetterBusiness, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Cursor cursor;
            cursor = dbOperations.getBusiness(db, businessUid);
            int count = cursor.getCount();

            String uid, brandName, email, website, description,
                    location, profileLink, category;
            boolean verified;
            int sigVer;

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
                sigVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Businesses.SIGNATURE_VERSION)));

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterBusiness setter = new GetSetterBusiness(uid, brandName, email, website,
                        description, "", location, profileLink, "", category, "", "", 0, verified, sigVer);
                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterBusiness... values) {
            listBusinesses.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                GetSetterBusiness setter = (GetSetterBusiness) listBusinesses.get(0);
                insertBusinessValues(setter);
                getAllBusinessFromFirebase(false);
            } else
                getAllBusinessFromFirebase(true);
        }
    }

    private void getAllBusinessFromFirebase(final boolean b) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childBusinesses)
                .child(businessUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        GetSetterBusiness setter = dataSnapshot.getValue(GetSetterBusiness.class);
                        if (b)
                            insertBusinessValues(setter);

                        new InsertBusiness(BusinessDetails.this).execute(setter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
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
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
            int count = dbOperations.getBusiness(db, businessUid).getCount();

            ContentValues values = new ContentValues();
            values.put(DBContract.Businesses.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
            values.put(DBContract.Businesses.UID, setter.getUid());
            values.put(DBContract.Businesses.KEY, setter.getUid());
            values.put(DBContract.Businesses.BRAND_NAME, setter.getBrandName());
            values.put(DBContract.Businesses.LOCATION, setter.getLocation());
            values.put(DBContract.Businesses.WEBSITE, setter.getWebsite());
            values.put(DBContract.Businesses.EMAIL, setter.getEmail());
            values.put(DBContract.Businesses.DESCRIPTION, setter.getDescription());
            values.put(DBContract.Businesses.CATEGORY, setter.getCategory());
            values.put(DBContract.Businesses.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Businesses.VERIFIED, setter.isVerified() ? "yes" : "no");
            values.put(DBContract.Businesses.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

            if (count == 0)
                db.insert(DBContract.Businesses.TABLE_NAME, null, values);
            else
                db.update(DBContract.Businesses.TABLE_NAME, values, DBContract.Businesses.UID + "='" + businessUid + "'", null);

            return null;
        }

    }

}
