package tech.myevents.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import java.util.Calendar;

public class MomentShare extends AppCompatActivity implements View.OnClickListener {

    ImageView ivMoment, ivSelectImage;
    ProgressBar progressBar;
    EditText etMessage;
    View viewSend;
    CardView cardImage;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;
    AlertDialog.Builder builder;
    String accType;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 27277;
    private static final int REQUEST_WRITE_IMAGE = 1994;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_moment);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
        galleryPhoto = new GalleryPhoto(this);

        ivMoment = (ImageView) findViewById(R.id.ivMoment);
        ivSelectImage = (ImageView) findViewById(R.id.ivSelectImage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etMessage = (EditText) findViewById(R.id.etMessage);
        viewSend = findViewById(R.id.viewSend);
        cardImage = (CardView) findViewById(R.id.cardImage);

        ivSelectImage.setOnClickListener(this);
        viewSend.setOnClickListener(this);
        insertMomentImage();

        if (!prefs.getString(AppInfo.MOMENT_LINK, "").equals(""))
            cardImage.setVisibility(View.VISIBLE);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Moment Share";
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!prefs.getBoolean(AppInfo.CONTACT_VERIFIED, false) && prefs.getInt(AppInfo.CONTACT_NUMBER, 0) == 0) {
            builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Verify contact number:");
            builder.setMessage("You have to verify your contact number before sharing moments!!!.");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MomentShare.this, ContactNumber.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSelectImage:
                getImageFromStorage();
                break;
            case R.id.viewSend:
                shareMoment();
                break;
        }
    }

    private void shareMoment() {
        if (etMessage.getText().toString().trim().equals("") && prefs.getString(AppInfo.MOMENT_LINK, "").equals(""))
            return;
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = FirebaseDatabase.getInstance().getReference().child(StaticVariables.childMoments).child(uid).push().getKey();
        String type = StaticVariables.moment;
        String username = accType.equals(StaticVariables.individual) ?
                prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
        String story = etMessage.getText().toString();
        String link = prefs.getString(AppInfo.MOMENT_LINK, "");
        String profileLink = accType.equals(StaticVariables.individual) ?
                prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
        long timestamp = getCurrentTimestamp();
        final int contactNumber = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        int sigVer = accType.equals(StaticVariables.individual) ?
                prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0);

        final GetSetterMoment setter = new GetSetterMoment(key, type, username, story, link, timestamp,
                profileLink, contactNumber, sigVer);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childMoments)
                .child(uid)
                .child(key)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            etMessage.setText("");
                            ivMoment.setImageResource(0);
                            editor = prefs.edit();
                            editor.putString(AppInfo.MOMENT_LINK, "");
                            editor.apply();
                            cardImage.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Moment shared", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void insertMomentImage() {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(prefs.getString(AppInfo.MOMENT_LINK, ""));
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivMoment);
        } catch (Exception e) {
            //
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
            cardImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features that required the permission
                    startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
                } else {
                    Toast.makeText(getApplicationContext(), "Allowed access to your storage.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void handlePosterResult(Intent data) {
        editor = prefs.edit();
        Uri uri = data.getData();
        final String previousImage = prefs.getString(AppInfo.MOMENT_LINK, "");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("broadcast-pics/"+ uri.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "Image UploadFailed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-accType, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                editor.putString(AppInfo.MOMENT_LINK, downloadUrl.toString());
                editor.apply();

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl.toString());
                    Glide.with(MomentShare.this)
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .crossFade()
                            .into(ivMoment);

                    if (!previousImage.equals(""))
                        FirebaseStorage.getInstance().getReferenceFromUrl(previousImage).delete();
                } catch (Exception e) {
                    //
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public long getCurrentTimestamp(){
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
