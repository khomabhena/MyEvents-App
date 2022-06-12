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

public class Profile extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    EditText etUsername, etWhatAppNumber;
    AutoCompleteTextView autoLocation;
    TextView tvInterest;
    CardView cardImage, cardSave;
    ImageView ivProfile;
    ProgressBar progressBar, progressBarSave;
    private static String username, phoneNumber;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 27277;
    private static final int REQUEST_WRITE_IMAGE = 1994;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.currentPage = 5;
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        galleryPhoto = new GalleryPhoto(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, StaticVariables.locations);

        etUsername = (EditText) findViewById(R.id.etVenue);
        autoLocation = (AutoCompleteTextView) findViewById(R.id.autoLocation);
        tvInterest = (TextView) findViewById(R.id.tvInterest);
        cardImage = (CardView) findViewById(R.id.cardImage);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etWhatAppNumber = (EditText) findViewById(R.id.etWhatAppNumber);
        progressBarSave = (ProgressBar) findViewById(R.id.progressBarSave);
        cardSave = (CardView) findViewById(R.id.cardSave);

        autoLocation.setAdapter(arrayAdapter);

        tvInterest.setOnClickListener(this);
        autoLocation.setOnItemClickListener(this);
        cardImage.setOnClickListener(this);
        cardSave.setOnClickListener(this);

        insertUserProfile();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Profile";
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
        if (username == null)
            username = prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "");
        if (phoneNumber == null)
            phoneNumber = String.valueOf(prefs.getInt(AppInfo.WHATSAPP_PHONE_NUMBER, 0));

        etUsername.setText(username);
        autoLocation.setText(prefs.getString(AppInfo.INDIVIDUAL_LOCATION, ""));
        if (!phoneNumber.equals("0"))
            etWhatAppNumber.setText(phoneNumber);
        showSelectedInterests(prefs.getString(AppInfo.INTEREST_CODE, ""), "Select your interests");
    }

    @Override
    protected void onPause() {
        super.onPause();
        username = etUsername.getText().toString();
        phoneNumber = etWhatAppNumber.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvInterest:
                startActivity(new Intent(Profile.this, Interests.class));
                break;
            case R.id.cardImage:
                getImageFromStorage();
                break;
            case R.id.cardSave:
                saveUserDetails();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editor = prefs.edit();
        editor.putString(AppInfo.INDIVIDUAL_LOCATION, String.valueOf(parent.getItemAtPosition(position)));
        editor.apply();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    private void saveUserDetails() {
        progressBarSave.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String username = etUsername.getText().toString();
        final String location = autoLocation.getText().toString();
        final int whatsAppNumber;
        if (etWhatAppNumber.getText().toString().trim().equals(""))
            whatsAppNumber = 0;
        else
            whatsAppNumber = Integer.parseInt(etWhatAppNumber.getText().toString());
        String interestCode = prefs.getString(AppInfo.INTEREST_CODE, "");
        String profileLink = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");
        String firebaseToken = prefs.getString(AppInfo.FIREBASE_TOKEN, "");
        int contactNumber = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        int signatureVersion = prefs.getInt(AppInfo.SIG_VER_INDI, 0);

        GetSetterUser setter = new GetSetterUser(uid, username, location, interestCode,
                profileLink, firebaseToken, whatsAppNumber, contactNumber, signatureVersion);

        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childUsers)
                .child(uid)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            editor = prefs.edit();
                            editor.putString(AppInfo.INDIVIDUAL_LOCATION, location);
                            editor.putString(AppInfo.INDIVIDUAL_USERNAME, username);
                            editor.putInt(AppInfo.WHATSAPP_PHONE_NUMBER, whatsAppNumber);
                            editor.apply();

                            StaticVariables.listEvents = null;
                            StaticVariables.listAds = null;


                            progressBarSave.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Profile Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBarSave.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Profile Saving Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void insertUserProfile() {
        String url = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + prefs.getInt(AppInfo.SIG_VER_INDI, 0)))
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
        }
    }




    private void showSelectedInterests(String interestCode, String text) {
        for (int x = 0; x < StaticVariables.interestCodes.length; x++) {
            if (interestCode.contains(StaticVariables.interestCodes[x]))
                text +=  "\n\n" + StaticVariables.categories[x];
        }
        tvInterest.setText(text);
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
        StorageReference imageRef = storageRef.child("profile-individual/" + uid + ".jpg");
        final String  profileLink = storageRef.child("profile-individual/" + uid + ".jpg").toString();
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
                int version = prefs.getInt(AppInfo.SIG_VER_INDI, 0);
                version += 1;

                editor.putInt(AppInfo.SIG_VER_INDI, version);
                editor.putString(AppInfo.INDIVIDUAL_PROFILE_LINK, profileLink);
                editor.apply();

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileLink);
                    Glide.with(Profile.this)
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .signature(new StringSignature("" + version))
                            .crossFade()
                            .into(ivProfile).notify();
                } catch (Exception e) {
                    //
                }
                progressBar.setVisibility(View.GONE);
                saveUserDetails();
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
