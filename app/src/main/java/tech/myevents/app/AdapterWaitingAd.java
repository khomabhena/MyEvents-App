package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.Calendar;

class AdapterWaitingAd extends ArrayAdapter {

    //List list = new ArrayList();
    private SharedPreferences prefsList;
    private SharedPreferences.Editor editor;
    private SQLiteDatabase db;

    AdapterWaitingAd(Context context, int resource) {
        super(context, resource);
    }

    public void add(GetSetterAd object) {
        StaticVariables.listAdsWaiting.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listAdsWaiting.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listAdsWaiting.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_ads_waiting, parent, false);
            holder = new Holder();

            holder.ivLogo = (ImageView) row.findViewById(R.id.ivLogo);
            holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
            holder.tvTitle = (TextView) row.findViewById(R.id.tvTitle);
            holder.tvBroadcastTime = (TextView) row.findViewById(R.id.tvBroadcastTime);
            holder.cardAdRow = (CardView) row.findViewById(R.id.cardAdRow);
            holder.tvLocation = (TextView) row.findViewById(R.id.tvLocation);
            holder.ivBroadcast = (ImageView) row.findViewById(R.id.ivBroadcast);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefsList = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        final GetSetterAd setter = (GetSetterAd) getItem(position);
        DBOperations dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .placeholder(R.drawable.sq6)
                    .crossFade()
                    .into(holder.ivLogo);
        } catch (Exception e) {
            //
        }

        holder.tvBrandName.setText(setter.getBrandName());
        holder.tvTitle.setText(setter.getTitle());
        holder.tvLocation.setText(setter.getAdLocation());
        holder.tvBroadcastTime.setText(calculateTimeReceived(setter.getBroadcastTime()));

        holder.ivBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishAd(setter);
            }
        });

        return row;
    }

    private static class Holder {
        ImageView ivLogo, ivBroadcast;
        TextView tvBrandName, tvTitle, tvBroadcastTime, tvLocation;
        CardView cardAdRow;
    }

    private void publishAd(final GetSetterAd setter) {
        setter.setStatus(StaticVariables.newBroad);

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
                            ref.child(StaticVariables.childAdsWaiting)
                                    .child(setter.getBroadcasterUid())
                                    .child(setter.getAdKey())
                                    .removeValue();

                            sendNotificationToUsers(setter);
                            Toast.makeText(getContext(), "Ad Published", Toast.LENGTH_SHORT).show();
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

                            if (!setter.getInterestCode().contains(setterAd.getInterestCode()))
                                continue;

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
                        String businessInterestCode = null;
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

                            for (int x = 0; x < StaticVariables.interestCodes.length; x++)
                                if (setterBusiness.getCategory().equals(StaticVariables.categories[x]))
                                    businessInterestCode = StaticVariables.interestCodes[x];

                            if (businessInterestCode.equals(setterAd.getInterestCode()))
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

    String calculateNPTime(String endTimestamp) {
        long currentTimestamp = getCurrentTimestamp();
        long eventEndTimestamp = Long.parseLong(endTimestamp);
        long sec = 1000;
        long min = 60000;
        long hour = 3600000;
        long day = 86400000;

        long timeBetween = eventEndTimestamp - currentTimestamp;
        long days = timeBetween / day;
        long daysMod = timeBetween % day;
        long hours = timeBetween / hour;
        long hoursMod = timeBetween % hour;
        long minutes = hoursMod / min;

        StringBuilder builder = new StringBuilder();
        builder.append(getTheValue(hours)).append("hrs ").append(getTheValue(minutes)).append("min");

        return String.valueOf(builder);
    }

    private String getTheValue(long value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
        }
    }

    String calculateTimeReceived(long timeReceived) {
        long currentTimestamp = getCurrentTimestamp();
        long min = 60000;
        long hour = 3600000;
        long day = 86400000;

        long timeBetween = currentTimestamp - timeReceived;
        String attach;
        if (timeBetween > day) {
            if (timeBetween > (day * 2)) {
                attach = " days ago";
            } else {
                attach = " day ago";
            }
            long result = timeBetween / day;

            return String.valueOf(result) + attach;
        } else if (timeBetween > hour) {
            if (timeBetween > (hour * 2)) {
                attach = " hours ago";
            } else {
                attach = " hour ago";
            }

            long result = timeBetween / hour;
            return String.valueOf(result) + attach;
        } else if (timeBetween > min){

            long result = timeBetween / min;
            return String.valueOf(result) + " min ago";
        } else {
            long result = timeBetween / 1000;
            return String.valueOf(result) + " sec ago";
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