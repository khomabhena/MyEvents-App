package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

class AdapterMessage extends ArrayAdapter {

    AdapterMessage(Context context, int resource) {
        super(context, resource);
    }

    SharedPreferences prefs;
    DBOperations dbOperations;
    SQLiteDatabase db;

    public void add(GetSetterInbox object) {
        StaticVariables.listMessages.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listMessages.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_messages, parent, false);
            holder = new Holder();

            holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
            holder.tvMessage = (TextView) row.findViewById(R.id.tvMessage);
            holder.tvTimestamp = (TextView) row.findViewById(R.id.tvTimestamp);
            holder.constRow = (ConstraintLayout) row.findViewById(R.id.constRow);
            holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
            holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
            holder.tvType = row.findViewById(R.id.tvType);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        final GetSetterInbox setter = (GetSetterInbox) getItem(position);
        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        SharedPreferences prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        try {
            StorageReference storageReference;
            if (setter.getSendTo().equals(uidU)) {
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getLinkSender());
            } else {
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getLinkReceiver());
            }
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + setter.getSignatureVersion()))
                    .crossFade()
                    .into(holder.ivProfile);
        } catch (Exception e) {
            holder.ivProfile.setImageResource(0);
        }

        if ((setter.getSendTo()).equals(uid))
            holder.tvUsername.setText(setter.getSender());
        else
            holder.tvUsername.setText(setter.getReceiver());

        if ((setter.getMessage()).length() > 36)
            holder.tvMessage.setText(setter.getMessage().substring(0, 36).replace("\n", " ") + "..." + "");
        else
            holder.tvMessage.setText(setter.getMessage().replace("\n", " "));

        if (setter.getSendTo().equals(uid)) {
            //get senderVerified
            if (setter.isSenderVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);
        } else {
            // get receiverVerified
            if (setter.isReceiverVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);
        }

        holder.tvTimestamp.setText(getDate(setter.getTimestamp()));
        if (setter.getChatRoom().contains("myevents")) {
            String cus = "Customer Care";
            holder.tvType.setText(cus);
            holder.ivVerified.setVisibility(View.VISIBLE);

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (!email.equals(StaticVariables.adminEmail)) {
                holder.tvUsername.setText(cus);
            }

        } else if (setter.getChatRoom().contains("ad"))
            holder.tvType.setText("Advert");
        else if (setter.getChatRoom().contains("friend"))
            holder.tvType.setText("Friend");
        else if (setter.getChatRoom().contains("bus"))
            holder.tvType.setText("Business");
        else if (setter.getChatRoom().contains("event"))
            holder.tvType.setText("Event");

        holder.constRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Inbox.class);
                intent.putExtra("from", StaticVariables.message);
                intent.putExtra("position", position);
                getContext().startActivity(intent);
            }
        });

        return row;

    }

    private static class Holder {
        ImageView ivProfile, ivVerified;
        TextView tvUsername, tvMessage, tvTimestamp, tvType;
        ConstraintLayout constRow;
    }


    String getDate(long timeReceived) {
        String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getCurrentTimestamp());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Calendar calendarR = Calendar.getInstance();
        calendarR.setTimeInMillis(timeReceived);
        calendarR.set(Calendar.HOUR_OF_DAY, 0);
        calendarR.set(Calendar.MINUTE, 0);

        long midnight = calendar.getTimeInMillis();
        long midnightReceiver = calendarR.getTimeInMillis();

        if (midnight == midnightReceiver) {
            calendar.setTimeInMillis(timeReceived);
            return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
        } else {
            calendar.setTimeInMillis(timeReceived);
            return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE)) + ", " +
                    calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                    getYear(calendar.get(Calendar.YEAR));
        }
        //

    }

    private String getYear(int year) {
        String year2 = String.valueOf(year);
        return year2.substring(2, 4);
    }

    public String getTheValue(int value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
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
