package tech.myevents.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import static tech.myevents.app.MainActivity.currentPage;

public class ProfileBusiness extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView tvSize, tvPayment, tvCategory, tvBrandName;
    EditText etBrandName, etEmail, etWebsite, etDescription;
    AutoCompleteTextView autoLocation;
    CardView cardImage, cardSave;
    ImageView ivProfile;
    ProgressBar progressBar, progressBarSave;
    private static String brandname, email, website, description;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String type;
    SharedPreferences.Editor editor;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 27277;
    private static final int REQUEST_WRITE_IMAGE = 1994;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();
        MainActivity.currentPage = 6;

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        galleryPhoto = new GalleryPhoto(this);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, StaticVariables.locations);

        tvSize = (TextView) findViewById(R.id.tvSize);
        etBrandName = (EditText) findViewById(R.id.etVenue);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etWebsite = (EditText) findViewById(R.id.etWebsite);
        etDescription = (EditText) findViewById(R.id.etDescription);
        autoLocation = (AutoCompleteTextView) findViewById(R.id.autoLocation);
        cardImage = (CardView) findViewById(R.id.cardImage);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarSave = (ProgressBar) findViewById(R.id.progressBarSave);
        cardSave = (CardView) findViewById(R.id.cardSave);
        tvPayment = (TextView) findViewById(R.id.tvPayment);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvBrandName = (TextView) findViewById(R.id.tvBrandName);



        autoLocation.setText(prefs.getString(AppInfo.BUSINESS_LOCATION, ""));
        tvPayment.setText(prefs.getString(AppInfo.BUSINESS_ECOCASH_TYPE, ""));
        tvCategory.setText(prefs.getString(AppInfo.BUSINESS_CATEGORY, ""));

        autoLocation.setAdapter(arrayAdapter);
        autoLocation.setOnItemClickListener(this);

        tvSize.setOnClickListener(this);
        cardImage.setOnClickListener(this);
        cardSave.setOnClickListener(this);
        tvPayment.setOnClickListener(this);
        tvCategory.setOnClickListener(this);
        tvBrandName.setOnClickListener(this);


        insertUserProfile();


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Profile Business";
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
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        if (brandname == null)
            brandname = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
        if (email == null)
            email = prefs.getString(AppInfo.BUSINESS_EMAIL, "");
        if (website == null)
            website =  prefs.getString(AppInfo.BUSINESS_WEBSITE, "");
        if (description == null)
            description = prefs.getString(AppInfo.BUSINESS_DESCRIPTION, "");

        etDescription.setText(description);
        etWebsite.setText(website);
        etBrandName.setText(brandname);
        etEmail.setText(email);
        tvSize.setText(prefs.getString(AppInfo.BUSINESS_SIZE, ""));
        tvCategory.setText(prefs.getString(AppInfo.BUSINESS_CATEGORY, ""));

        if (prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false)) {
            etBrandName.setVisibility(View.INVISIBLE);
            tvBrandName.setVisibility(View.VISIBLE);
            tvBrandName.setText(prefs.getString(AppInfo.BUSINESS_BRAND_NAME, ""));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        brandname = etBrandName.getText().toString();
        email = etEmail.getText().toString();
        website = etWebsite.getText().toString();
        description = etDescription.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvSize:
                startActivity(new Intent(ProfileBusiness.this, Size.class));
                break;
            case R.id.cardImage:
                getImageFromStorage();
                break;
            case R.id.cardSave:
                saveBusinessDetails();
                break;
            case R.id.tvPayment:
                startActivity(new Intent(this, PaymentEcocash.class));
                break;
            case R.id.tvCategory:
                startActivity(new Intent(this, BusinessCategory.class));
                break;
            case R.id.tvBrandName:
                Toast.makeText(getApplicationContext(), "Cannot change brand name, account is now verified", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editor = prefs.edit();
        editor.putString(AppInfo.BUSINESS_LOCATION, String.valueOf(parent.getItemAtPosition(position)));
        editor.apply();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }



    private void saveBusinessDetails() {
        progressBarSave.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String brandName;
        if (prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false))
            brandName = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
        else
            brandName = etBrandName.getText().toString();
        final String email = etEmail.getText().toString();
        final String website = etWebsite.getText().toString();
        final String description = etDescription.getText().toString();
        final String size = tvSize.getText().toString();
        final String location = autoLocation.getText().toString();
        String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
        String firebaseToken = prefs.getString(AppInfo.FIREBASE_TOKEN, "");
        String category = prefs.getString(AppInfo.BUSINESS_CATEGORY, "");
        String ecocashType = prefs.getString(AppInfo.BUSINESS_ECOCASH_TYPE,  "");
        String ecocashName = prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, "");
        int ecocashCode = prefs.getInt(AppInfo.BUSINESS_ECOCASH_CODE, 0);
        boolean verified = prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
        int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, 0);

        GetSetterBusiness setter = new GetSetterBusiness(uid, brandName, email, website,
                description, size, location, profileLink, firebaseToken, category,
                ecocashType, ecocashName, ecocashCode, verified, signatureVersion);

        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childBusinesses)
                .child(uid)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            editor = prefs.edit();
                            editor.putString(AppInfo.INDIVIDUAL_LOCATION, location);
                            editor.putString(AppInfo.BUSINESS_BRAND_NAME, brandName);
                            editor.putString(AppInfo.BUSINESS_EMAIL, email);
                            editor.putString(AppInfo.BUSINESS_WEBSITE, website);
                            editor.putString(AppInfo.BUSINESS_SIZE, size);
                            editor.putString(AppInfo.BUSINESS_DESCRIPTION, description);
                            editor.apply();

                            StaticVariables.listEvents = null;
                            StaticVariables.listAds = null;

                            progressBarSave.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Business Details Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBarSave.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Profile Saving Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void insertUserProfile() {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, ""));
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + prefs.getInt(AppInfo.SIG_VER_BUS, 0)))
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
        }
    }




    private void getImageFromStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_IMAGE);
            } else {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        } else {
            startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            handlePosterResult(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //reload my activity with permission granted or use the features that required the permission
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            } else {
                Toast.makeText(getApplicationContext(), "Allow the app to read your storage.", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void handlePosterResult(Intent data) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editor = prefs.edit();
        Uri uri = data.getData();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("profile-business/" + uid + ".jpg");
        final String profileLink = storageRef.child("profile-business/" + uid + ".jpg").toString();
        UploadTask uploadTask = imageRef.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                int version = prefs.getInt(AppInfo.SIG_VER_BUS, 0);
                version += 1;

                editor.putInt(AppInfo.SIG_VER_BUS, version);
                editor.putString(AppInfo.BUSINESS_PROFILE_LINK, profileLink);
                editor.apply();

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileLink);
                    Glide.with(ProfileBusiness.this)
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .signature(new StringSignature("" + version))
                            .crossFade()
                            .into(ivProfile);
                } catch (Exception e) {
                    //
                }
                progressBar.setVisibility(View.GONE);
                saveBusinessDetails();
            }
        });
    }






    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            currentPage = 1;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        currentPage = 1;
        super.onBackPressed();
    }

}
