package tech.myevents.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

class AdapterEventWaiting extends ArrayAdapter {

    AdapterEventWaiting(Context context, int resource) {
        super(context, resource);
    }

    SharedPreferences prefs, prefsApp;
    String accType;

    public void add(GetSetterEvent object) {
        StaticVariables.listEventsWaiting.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listEventsWaiting.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listEventsWaiting.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_events, parent, false);
            holder = new Holder();

            holder.ivBitmap = (ImageView) row.findViewById(R.id.ivBitmap);
            holder.tvName = (TextView) row.findViewById(R.id.tvName);
            holder.tvVenue = (TextView) row.findViewById(R.id.tvVenue);
            holder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            holder.cardRow = (CardView) row.findViewById(R.id.cardRow);
            holder.tvTime = (TextView) row.findViewById(R.id.tvTime);
            holder.ivShare = (ImageView) row.findViewById(R.id.ivShare);
            holder.ivMessage = (ImageView) row.findViewById(R.id.ivMessage);
            holder.llComments = (LinearLayout) row.findViewById(R.id.llComments);
            holder.tvTimeReceived = (TextView) row.findViewById(R.id.tvTimeReceived);
            holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
            holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
            holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
            holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
            holder.cardImage = (CardView) row.findViewById(R.id.cardImage);
            holder.tvExclusive = row.findViewById(R.id.tvExclusive);

            row.setTag(holder);
        } else {
           holder = (Holder) row.getTag();
        }

        final GetSetterEvent setter = (GetSetterEvent) getItem(position);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getBrandLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + setter.getSignatureVersion()))
                    .crossFade()
                    .into(holder.ivProfile);
        } catch (Exception e) { }

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(holder.ivBitmap);
        } catch (Exception e) { }

        if (setter.isVerified())
            holder.ivVerified.setVisibility(View.VISIBLE);
        else
            holder.ivVerified.setVisibility(View.GONE);

        holder.tvBrandName.setText(setter.getBrandName());
        holder.tvTimeReceived.setText(calculateTimeReceived(setter.getBroadcastTime()));
        holder.tvName.setText(setter.getName());
        holder.tvVenue.setText(setter.getVenue());
        holder.tvDate.setText(setter.getStartDate());
        holder.tvTime.setText(setter.getStartTime());

        if (setter.getEventStatus().equals(StaticVariables.eventExclusive))
            holder.tvExclusive.setVisibility(View.VISIBLE);
        else
            holder.tvExclusive.setVisibility(View.GONE);

        holder.cardRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EventDetails.class);
                intent.putExtra("from", StaticVariables.eventWaiting);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });

        holder.cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BusinessDetails.class);
                intent.putExtra("from", StaticVariables.eventWaiting);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Inbox.class);
                intent.putExtra("from", StaticVariables.eventWaiting);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip;
                if (!setter.getMxeCode().equals(""))
                    clip = ClipData.newPlainText("label", "Your event MXE code is: " + setter.getMxeCode());
                else
                    clip = ClipData.newPlainText("label", "Your event has been published, thank you for using our service.");

                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();

                GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
                if (setterEvent.getEventStatus().equals(StaticVariables.eventExclusive))
                    publishEventExclusive(setterEvent);
                else
                    publishEvent(setterEvent);
            }
        });
        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", "Your event has been published, thank you for using our service.");
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();

                GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
                if (setterEvent.getEventStatus().equals(StaticVariables.eventExclusive))
                    publishEventExclusive(setterEvent);
                else
                    publishEvent(setterEvent);
            }
        });

        return row;

    }

    private void publishEvent(final GetSetterEvent setter) {
        setter.setEventStatus(StaticVariables.newBroad);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();
        ref
                .child(StaticVariables.childEvents)
                .child(setter.getBroadcasterUid())
                .child(setter.getEventKey())
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ref.child(StaticVariables.childEventsWaiting)
                                    .child(setter.getBroadcasterUid())
                                    .child(setter.getEventKey())
                                    .removeValue();

                            sendNotificationToUsers(setter);
                            Toast.makeText(getContext(), "Event Published", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Publish Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendNotificationToUsers(final GetSetterEvent setterEvent) {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childUsers)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterUser setter = snap.getValue(GetSetterUser.class);

                            if (!setter.getInterestCode().contains(setterEvent.getInterestCode()))
                                continue;

                            String key = FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(setter.getUid())
                                    .push()
                                    .getKey();

                            GetSetterNotifications setterNotifications = new
                                    GetSetterNotifications(StaticVariables.event, key, setterEvent.getBroadcasterUid(),
                                    setterEvent.getEventKey(), false);

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
                                    GetSetterNotifications(StaticVariables.event, key, setterEvent.getBroadcasterUid(),
                                    setterEvent.getEventKey(), false);

                            for (int x = 0; x < StaticVariables.interestCodes.length; x++)
                                if (setterBusiness.getCategory().equals(StaticVariables.categories[x]))
                                    businessInterestCode = StaticVariables.interestCodes[x];

                            if (businessInterestCode.equals(setterEvent.getInterestCode()))
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

    private void publishEventExclusive(final GetSetterEvent setter) {
        setter.setEventStatus(StaticVariables.eventExclusive);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();
        ref
                .child(StaticVariables.childEvents)
                .child(setter.getBroadcasterUid())
                .child(setter.getEventKey())
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ref.child(StaticVariables.childEventsWaiting)
                                    .child(setter.getBroadcasterUid())
                                    .child(setter.getEventKey())
                                    .removeValue();

                            Toast.makeText(getContext(), "Event Published", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Publish Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static class Holder {
        ImageView ivBitmap, ivShare, ivMessage, ivProfile, ivVerified, ivSeen, ivSeenComments;
        TextView tvName, tvVenue, tvDate, tvTime, tvTimeReceived, tvBrandName, tvExclusive;
        CardView cardRow, cardImage;
        LinearLayout llComments;
        ProgressBar progressBar;
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

    private String getImpression(String impression) {
        char[] aud = impression.toCharArray();
        int len = impression.length();
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < aud.length; x++) {
            if (x == len - 9) {
                if (len != 9) {
                    builder.append(",");
                }
            }
            if (x == len - 6) {
                if (len != 6) {
                    builder.append(",");
                }
            }
            if (x == len - 3) {
                if (len != 3) {
                    builder.append(",");
                }
            }
            builder.append(aud[x]);
        }

        return String.valueOf(builder);
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
