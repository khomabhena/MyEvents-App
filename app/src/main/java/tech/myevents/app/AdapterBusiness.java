package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

class AdapterBusiness extends ArrayAdapter {

    //List list = new ArrayList();
    private SharedPreferences prefsList;
    private SharedPreferences.Editor editor;
    private SQLiteDatabase db;

    private SharedPreferences prefs;
    SharedPreferences prefsApp;
    String accType, location;

    AdapterBusiness(Context context, int resource) {
        super(context, resource);
    }

    public void add(GetSetterBusiness object) {
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + localUid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (accType.equals(StaticVariables.individual))
            location = prefs.getString(AppInfo.INDIVIDUAL_LOCATION,  StaticVariables.locations[2]);
        else
            location = prefs.getString(AppInfo.BUSINESS_LOCATION, StaticVariables.locations[2]);

        if (location.trim().equals(object.getLocation().trim())) {
            if (StaticVariables.listBusinesses.size() == 0)
                StaticVariables.listBusinesses.add(object);
            else
                StaticVariables.listBusinesses.add(0, object);
        } else
            StaticVariables.listBusinesses.add(object);

        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listBusinesses.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listBusinesses.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_business, parent, false);
            holder = new Holder();

            holder.ivLogo = (ImageView) row.findViewById(R.id.ivLogo);
            holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
            holder.tvEmail = (TextView) row.findViewById(R.id.tvEmail);
            holder.tvWebsite = (TextView) row.findViewById(R.id.tvWebsite);
            holder.tvLocation = (TextView) row.findViewById(R.id.tvLocation);
            holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
            holder.cardRow = (CardView) row.findViewById(R.id.cardRow);
            holder.ivMessage =  (ImageView) row.findViewById(R.id.ivMessage);
            holder.llComments = (LinearLayout) row.findViewById(R.id.llComments);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefsList = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        final GetSetterBusiness setter = (GetSetterBusiness) getItem(position);
        DBOperations dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + setter.getSignatureVersion()))
                    .crossFade()
                    .into(holder.ivLogo);
        } catch (Exception e) {
            //
        }

        if (setter.isVerified())
            holder.ivVerified.setVisibility(View.VISIBLE);
        else
            holder.ivVerified.setVisibility(View.GONE);

        holder.tvBrandName.setText(setter.getBrandName());
        holder.tvEmail.setText(setter.getEmail());
        holder.tvWebsite.setText(setter.getWebsite());
        holder.tvLocation.setText(setter.getLocation());
        //holder.tvImpression.setText(getImpression(String.valueOf(adsGetSetter.getImpressions())));

        holder.cardRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BusinessDetails.class);
                intent.putExtra("from", StaticVariables.business);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Inbox.class);
                intent.putExtra("from", StaticVariables.business);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });
        holder.llComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Comments.class);
                intent.putExtra("from", StaticVariables.business);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });

        return row;
    }

    private static class Holder {
        ImageView ivLogo, ivVerified, ivMessage;
        TextView tvBrandName, tvEmail, tvWebsite, tvLocation;
        CardView cardRow;
        LinearLayout llComments;
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