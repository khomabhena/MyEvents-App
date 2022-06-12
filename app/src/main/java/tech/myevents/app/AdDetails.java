package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class AdDetails extends AppCompatActivity implements View.OnClickListener {

    ImageView ivProfile;
    TextView tvBrandName, tvLocation, tvImpressions,tvDetails;
    View viewImpression;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> impressionKeys;

    private static int position;
    GetSetterAd setterAd;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarColor();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert collapsingToolbarLayout != null;

        try { position = getIntent().getExtras().getInt("position"); } catch (Exception e) { }
        setterAd = (GetSetterAd) StaticVariables.listAds.get(position);

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        impressionKeys = dbOperations.getImpressionKeys(db, setterAd.getAdKey(), localUid);

        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvBrandName = (TextView) findViewById(R.id.tvBrandName);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvImpressions = (TextView) findViewById(R.id.tvImpressions);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        viewImpression = findViewById(R.id.viewImpression);

        viewImpression.setOnClickListener(this);
        tvBrandName.setText(setterAd.getBrandName());
        tvLocation.setText(setterAd.getAdLocation());
        tvImpressions.setText("" + "" + setterAd.getImpressions());
        tvDetails.setText(setterAd.getDetails());
        tvImpressions.setText("" + dbOperations.getImpressionCount(db, setterAd.getAdKey()) + "");

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setterAd.getProfileLink());
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            //
        }
        addFirebaseListener();
        collapsingToolbarLayout.setTitle(setterAd.getTitle());

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Ad Details";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);

    }

    private void setStatusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewImpression:
                Intent intent = new Intent(this, Impressions.class);
                intent.putExtra("key", setterAd.getAdKey());
                startActivity(intent);
                break;
        }
    }

    private void addFirebaseListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(setterAd.getAdKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;

                        tvImpressions.setText("" + dataSnapshot.getChildrenCount() + "");

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                            GetSetterImpression setter = snapshot.getValue(GetSetterImpression.class);

                            if (impressionKeys.contains(setter.getUid()))
                                return;

                            new InsertImpressions(AdDetails.this).execute(setter);
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

            if (!impressionKeys.contains(setter.getUid())) {
                //makeAdImpression(setterEvent);
                ContentValues values = new ContentValues();
                values.put(DBContract.Impressions.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                values.put(DBContract.Impressions.UID, uid);
                values.put(DBContract.Impressions.KEY, setter.getKey());
                values.put(DBContract.Impressions.USERNAME, setter.getUsername());
                values.put(DBContract.Impressions.LOCATION, setter.getLocation());
                values.put(DBContract.Impressions.PROFILE_LINK, setter.getProfileLink());
                values.put(DBContract.Impressions.TIMESTAMP, "" + setter.getTimestamp());
                values.put(DBContract.Impressions.VERIFIED, setter.isVerified() ? "yes":"no");

                db.insert(DBContract.Impressions.TABLE_NAME, null, values);
            }

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            impressionKeys = dbOperations.getImpressionKeys(db, setterAd.getAdKey(), localUid);

            return null;
        }

    }

}
