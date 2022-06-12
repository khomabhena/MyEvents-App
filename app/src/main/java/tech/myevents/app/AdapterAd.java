package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

class AdapterAd extends ArrayAdapter {

    //List list = new ArrayList();
    private SharedPreferences prefs;
    SharedPreferences prefsApp;
    String accType, location;
    private SharedPreferences.Editor editor;
    private SQLiteDatabase db;

    AdapterAd(Context context, int resource) {
        super(context, resource);
    }

    public void add(GetSetterAd object) {
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + localUid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (accType.equals(StaticVariables.individual))
            location = prefs.getString(AppInfo.INDIVIDUAL_LOCATION, StaticVariables.locations[2]);
        else
            location = prefs.getString(AppInfo.BUSINESS_LOCATION, StaticVariables.locations[2]);

        if (location.trim().equals(object.getAdLocation().trim())) {
            if (StaticVariables.listAds.size() == 0)
                StaticVariables.listAds.add(object);
            else
                StaticVariables.listAds.add(0, object);
        } else
            StaticVariables.listAds.add(object);

        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listAds.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listAds.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_ads, parent, false);
            holder = new Holder();

            holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
            holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
            holder.tvTitle = (TextView) row.findViewById(R.id.tvTitle);
            holder.tvBroadcastTime = (TextView) row.findViewById(R.id.tvBroadcastTime);
            holder.cardRow = (CardView) row.findViewById(R.id.cardRow);
            holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
            holder.ivBitmap = (ImageView) row.findViewById(R.id.ivBitmap);
            holder.tvCategory = (TextView) row.findViewById(R.id.tvCategory);
            holder.cardImage = (CardView) row.findViewById(R.id.cardImage);
            holder.ivShare = (ImageView) row.findViewById(R.id.ivShare);
            holder.ivMessage = (ImageView) row.findViewById(R.id.ivMessage);
            holder.ivSeenComments = (ImageView) row.findViewById(R.id.ivSeenComments);
            holder.ivSeen = (ImageView) row.findViewById(R.id.ivSeen);
            holder.llComments = (LinearLayout) row.findViewById(R.id.llComments);
            holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
            holder.ivDelete = row.findViewById(R.id.ivDelete);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
        final GetSetterAd setter = (GetSetterAd) getItem(position);
        DBOperations dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getBrandLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + setter.getSignatureVersion()))
                    .crossFade()
                    .into(holder.ivProfile);
        } catch (Exception e) {
            //
        }
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(holder.ivBitmap);
        } catch (Exception e) {
            //
        }

        if (setter.isVerified())
            holder.ivVerified.setVisibility(View.VISIBLE);
        else
            holder.ivVerified.setVisibility(View.GONE);

        holder.tvBrandName.setText(setter.getBrandName());
        holder.tvTitle.setText(setter.getTitle());
        holder.tvCategory.setText(getCategory(setter.getInterestCode()));
        holder.tvBroadcastTime.setText(calculateTimeReceived(setter.getBroadcastTime()));

        holder.cardRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AdDetails.class);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BusinessDetails.class);
                intent.putExtra("from", StaticVariables.ad);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Inbox.class);
                intent.putExtra("from", StaticVariables.ad);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Comments.class);
                intent.putExtra("from", StaticVariables.ad);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Via @myevents_app\n\n" +
                        "Advert Name:  " + setter.getTitle().trim() + "\n" +
                        "By. :  " + setter.getBrandName().trim() + "\n\n" +
                        "Category:  " + getCategory(setter.getInterestCode()) + "\n" +
                        "#MyEventsApp";

                PopupMenu popupPay = new PopupMenu(getContext(), holder.ivShare);
                popupPay.getMenuInflater().inflate(R.menu.menu_share, popupPay.getMenu());
                final String finalMessage = message;
                popupPay.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Share on WhatsApp":
                                shareOnWhatsApp(setter);
                                break;
                            case "Share on Twitter":
                                shareOnTwitter(finalMessage);
                                break;
                            case "Share as a moment":
                                shareMoment(holder, setter);
                                break;
                        }

                        return true;
                    }
                });
                popupPay.show();
            }
        });

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email.equals(StaticVariables.adminEmail)) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GetSetterAd setterEvent = (GetSetterAd) StaticVariables.listAds.get(position);
                    deleteEvent(setterEvent);
                }
            });
        }
        if (email.equals("colwanymab@gmail.com")) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GetSetterAd setterEvent = (GetSetterAd) StaticVariables.listAds.get(position);
                    deleteEvent(setterEvent);
                }
            });
        }

        return row;
    }

    private static class Holder {
        ImageView ivProfile, ivVerified, ivBitmap, ivShare, ivMessage, ivSeenComments, ivSeen, ivDelete;
        TextView tvBrandName, tvTitle, tvBroadcastTime, tvCategory;
        CardView cardRow, cardImage;
        LinearLayout llComments;
        ProgressBar progressBar;
    }

    private void deleteEvent(final GetSetterAd setter) {
        setter.setStatus(StaticVariables.deleted);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();
        ref
                .child(StaticVariables.childAds)
                .child(setter.getBroadcasterUid())
                .child(setter.getAdKey())
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            sendNotificationToUsers(setter);
                            Toast.makeText(getContext(), "Ad Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Publish Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendNotificationToUsers(final GetSetterAd setterAd) {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childUsers)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterUser setter = snap.getValue(GetSetterUser.class);

                            String key = FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(setter.getUid())
                                    .push()
                                    .getKey();

                            GetSetterNotifications setterNotifications = new
                                    GetSetterNotifications(StaticVariables.ad, key, setterAd.getBroadcasterUid(),
                                    setterAd.getAdKey(), false);

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(setter.getUid())
                                    .child(key)
                                    .setValue(setterNotifications);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterBusiness setterBusiness = snap.getValue(GetSetterBusiness.class);

                            String key = FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(setterBusiness.getUid())
                                    .push()
                                    .getKey();

                            GetSetterNotifications setterNotifications = new
                                    GetSetterNotifications(StaticVariables.ad, key, setterAd.getBroadcasterUid(),
                                    setterAd.getAdKey(), false);

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(setterBusiness.getUid())
                                    .child(key)
                                    .setValue(setterNotifications);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void shareMoment(final Holder holder, GetSetterAd setterZ) {
        if (!prefs.getBoolean(AppInfo.CONTACT_VERIFIED, false) || accType.equals(StaticVariables.business)) {
            if (prefsApp.getBoolean(AppInfo.CONTACT_VERIFIED, false))
                Toast.makeText(getContext(), "Verify your contact", Toast.LENGTH_SHORT).show();
            if (accType.equals(StaticVariables.business))
                Toast.makeText(getContext(), "Switch to individual acc", Toast.LENGTH_SHORT).show();
            return;
        }

        holder.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = FirebaseDatabase.getInstance().getReference().child(StaticVariables.childMoments).child(uid).push().getKey();
        String type = StaticVariables.ad;
        String username = accType.equals(StaticVariables.individual) ?
                prefs.getString(AppInfo.INDIVIDUAL_USERNAME, ""): prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
        String story = "";
        String link = setterZ.getProfileLink();
        String profileLink = accType.equals(StaticVariables.individual) ?
                prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
        long timestamp = getCurrentTimestamp();
        int contactNumber = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        int signatureVersion = prefs.getInt(AppInfo.SIG_VER_INDI, 0);

        GetSetterMoment setter = new GetSetterMoment(key, type, username, story, link, timestamp,
                profileLink, contactNumber, signatureVersion);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childMoments)
                .child(uid)
                .child(key)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            holder.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Moment shared", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void shareOnWhatsApp(GetSetterAd setter) {
        String message = "_Shared via MyEvents App_\n\n" +
                "Advert title:\n*" + setter.getTitle().trim() + "*\n\n" +
                "Advertised by:\n*" + setter.getBrandName().trim() + "*\n\n" +
                "Details:\n*" + setter.getDetails().trim() + "*\n\n";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.whatsapp");
        getContext().startActivity(intent);
    }

    private void shareOnTwitter(String finalMessage) {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, finalMessage);
        tweetIntent.setType("text/plain");

        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo: resolveInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            getContext().startActivity(tweetIntent);
        } else {
            Intent i = new Intent();
            i.putExtra(Intent.EXTRA_TEXT, finalMessage);
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://twitter.com/intent/tweet?text=" + urlEncode(finalMessage)));
            getContext().startActivity(i);
            Toast.makeText(getContext(), "Twitter app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCategory(String interestCode) {
        for (int x = 0; x < StaticVariables.interestCodes.length; x++) {
            if (StaticVariables.interestCodes[x].equals(interestCode))
                return StaticVariables.categories[x];
        }
        return "";
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String getTheValue(long value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
        }
    }

    private String calculateTimeReceived(long timeReceived) {
        long currentTimestamp = getCurrentTimestamp();
        long min = 60000;
        long hour = 3600000;
        long day = 86400000;

        long timeBetween = currentTimestamp - timeReceived;
        String attach;
        if (timeBetween > day) {
            if (timeBetween > (day * 2)) {
                attach = "d";
            } else {
                attach = "d";
            }
            long result = timeBetween / day;

            return String.valueOf(result) + attach;
        } else if (timeBetween > hour) {
            if (timeBetween > (hour * 2)) {
                attach = "hrs";
            } else {
                attach = "hr";
            }

            long result = timeBetween / hour;
            return String.valueOf(result) + attach;
        } else if (timeBetween > min){

            long result = timeBetween / min;
            return String.valueOf(result) + "min";
        } else {
            long result = timeBetween / 1000;
            return String.valueOf(result) + "sec";
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

}