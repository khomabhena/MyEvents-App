package tech.myevents.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

class AdapterInbox extends ArrayAdapter {

    GetSetterInbox setter;

    AdapterInbox(Context context, int resource) {
        super(context, resource);
    }

    public void add(GetSetterInbox object) {
        StaticVariables.listInbox.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return StaticVariables.listInbox.size();
    }

    @Override
    public Object getItem(int position) {
        return StaticVariables.listInbox.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Holder holder;
        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_inbox, parent, false);
            holder = new Holder();

            holder.tvSend = (TextView) row.findViewById(R.id.tvSend);
            holder.tvReceive = (TextView) row.findViewById(R.id.tvReceive);
            holder.tvTimeS = (TextView) row.findViewById(R.id.tvTimeS);
            holder.tvTimeR = (TextView) row.findViewById(R.id.tvTimeR);
            holder.llSend = (LinearLayout) row.findViewById(R.id.llSend);
            holder.llReceive = (LinearLayout) row.findViewById(R.id.llReceive);

            row.setTag(holder);
        } else {
           holder = (Holder) row.getTag();
        }

        setter = (GetSetterInbox) getItem(position);

        if (!setter.getSendTo().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            //sent
            holder.llReceive.setVisibility(View.GONE);
            holder.llSend.setVisibility(View.VISIBLE);
            holder.tvSend.setText(setter.getMessage());
            holder.tvTimeS.setText(getDate(setter.getTimestamp()));
        } else {
            //received
            holder.llSend.setVisibility(View.GONE);
            holder.llReceive.setVisibility(View.VISIBLE);
            holder.tvReceive.setText(setter.getMessage());
            holder.tvTimeR.setText(getDate(setter.getTimestamp()));
        }

        holder.tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSetterInbox setterZ = (GetSetterInbox) StaticVariables.listInbox.get(position);
                if (setterZ.getKey().length() < 3)
                    customerCare(position);
            }
        });
        holder.tvReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSetterInbox setterZ = (GetSetterInbox) StaticVariables.listInbox.get(position);
                if (setterZ.getKey().length() < 3)
                    customerCare(position);
            }
        });
        holder.tvSend.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                GetSetterInbox setterZ = (GetSetterInbox) StaticVariables.listInbox.get(position);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", setterZ.getMessage());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        holder.tvReceive.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                GetSetterInbox setterZ = (GetSetterInbox) StaticVariables.listInbox.get(position);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", setterZ.getMessage());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return row;

    }

    private void customerCare(int position) {
        if (setter.getKey().length() > 3)
            return;
        SharedPreferences prefApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefApp.edit();
        switch (position) {
            case 2:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), Profile.class));
                break;
            case 3:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.business);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), ProfileBusiness.class));
                break;
            case 4:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.business);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), BroadcastEvent.class));
                break;
            case 5:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.business);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), BroadcastAd.class));
                break;
            case 6:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), ShareApp.class));
                break;
            case 7:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), Business.class));
                break;
            case 8:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), MomentShare.class));
                break;
            case 9:
                editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                editor.apply();
                getContext().startActivity(new Intent(getContext(), Friends.class));
                break;
        }
    }

    private static class Holder {
        TextView tvSend, tvReceive, tvTimeS, tvTimeR;
        LinearLayout llSend, llReceive;
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
