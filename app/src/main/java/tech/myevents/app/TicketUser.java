package tech.myevents.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TicketUser extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;

    SharedPreferences prefsApp;
    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> ticketKeys;

    static boolean NETWORK_AVAILABLE = false;

    TicketsAdapter ticketsAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tickets);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        final String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ticketKeys = dbOperations.getTicketKeys(db, localUid);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        ticketsAdapter = new TicketsAdapter(this, R.layout.row_tickets_user);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childNotificationTicket)
                .child(localUid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (!dataSnapshot.exists())
                            return;
                        final GetSetterNotifications setterNotifications = dataSnapshot.getValue(GetSetterNotifications.class);
                        if (setterNotifications.isSeen())
                            return;
                        else {
                            setterNotifications.setSeen(true);
                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationTicket)
                                    .child(localUid) //User uid
                                    .child(setterNotifications.getKey()) //Notification Key
                                    .removeValue();
                        }
                        if (setterNotifications.getType().equals(StaticVariables.ticket)) {
                            String eventKey = setterNotifications.getPostKey();
                            String ticketKey =setterNotifications.getBroadcasterUid();
                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childTickets)
                                    .child(eventKey)
                                    .child(localUid)
                                    .child(ticketKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists())
                                                return;
                                            GetSetterTicket setterTicket = dataSnapshot.getValue(GetSetterTicket.class);
                                            if (setterTicket.isValid())
                                                Toast.makeText(getApplicationContext(), "Signing revoked", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getApplicationContext(), "Ticket signed", Toast.LENGTH_SHORT).show();

                                            new InsertTicket(TicketUser.this).execute(setterTicket);

                                            for (int x = 0; x < StaticVariables.listTickets.size(); x++) {
                                                GetSetterTicket setterT = (GetSetterTicket) StaticVariables.listTickets.get(x);
                                                if (setterT.getTicketKey().equals(setterTicket.getTicketKey())) {
                                                    setterT.setValid(setterTicket.isValid());
                                                    ticketsAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "User Tickets";
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
        StaticVariables.listTickets = new ArrayList<>();
        new TicketBackTask().execute();
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

    private class TicketBackTask extends AsyncTask<Void, GetSetterTicket, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getTicketsUser(db, FirebaseAuth.getInstance().getCurrentUser().getUid(), localUid);
            int count = cursor.getCount();

            String eventKey, uid, broadcasterUid, ticketNumber,
                    venue, location, name, category,
                    merchantName, ticketKey, mtsCode;
            int  ticketPrice, id, num;
            boolean valid;
            long endTimestamp, startTimestamp;

            while (cursor.moveToNext()) {
                String numz = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.NUM));
                if (numz == null)
                    num = 0;
                else
                    num = Integer.parseInt(numz);
                id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Tickets.ID)));
                eventKey = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.EVENT_KEY));
                uid = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.UID));
                broadcasterUid = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.BROADCASTER_UID));
                ticketNumber = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.TICKET_NUMBER));
                venue = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.VENUE));
                location = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.LOCATION));
                name = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.NAME));
                category = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.CATEGORY));
                merchantName = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.MARCHANT_NAME));
                ticketKey = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.TICKET_KEY));
                ticketPrice = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Tickets.PRICE)));
                endTimestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Tickets.END_TIMESTAMP)));
                startTimestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Tickets.START_TIMESTAMP)));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.VALID));
                mtsCode = cursor.getString(cursor.getColumnIndex(DBContract.Tickets.MTS_CODE));

                if (veri.equals("yes"))
                    valid = true;
                else
                    valid = false;

                GetSetterTicket setter = new GetSetterTicket(num, id, eventKey, ticketKey, uid, broadcasterUid,
                        ticketNumber, venue, location, name, category, merchantName,
                        ticketPrice, valid, endTimestamp, startTimestamp, mtsCode);
                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterTicket... values) {
            ticketsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(ticketsAdapter);
            } else {
                listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            loadTickets();
        }

    }

    private void loadTickets() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapKey : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapUid: snapKey.getChildren()) {
                                if (uid.equals(snapUid.getKey())) {
                                    for (DataSnapshot snapUser: snapUid.getChildren()) {
                                        GetSetterTicket setter = snapUser.getValue(GetSetterTicket.class);
                                        /*if (getCurrentTimestamp() > (setter.getEndTimestamp() + (86400000 * 2)))
                                            continue;*/

                                        if (!ticketKeys.contains(setter.getTicketKey()))
                                            if (StaticVariables.listTickets.size() == 0) {
                                                tvEmpty.setVisibility(View.GONE);
                                                progressBar.setVisibility(View.GONE);
                                                listView.setVisibility(View.VISIBLE);

                                                ticketsAdapter.add(setter);
                                                listView.setAdapter(ticketsAdapter);
                                            } else {
                                                StaticVariables.listTickets.add(0, setter);
                                                ticketsAdapter.notifyDataSetChanged();
                                            }

                                        new InsertTicket(TicketUser.this).execute(setter);
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    private class InsertTicket extends AsyncTask<GetSetterTicket, Void, Void> {

        Context context;

        public InsertTicket(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterTicket... params) {
            GetSetterTicket setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //makeAdImpression(setterEvent);
            ContentValues values = new ContentValues();
            values.put(DBContract.Tickets.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
            values.put(DBContract.Tickets.NUM, "" + setter.getNum());
            values.put(DBContract.Tickets.UID, setter.getUid());
            values.put(DBContract.Tickets.BROADCASTER_UID, setter.getBroadcasterUid());
            values.put(DBContract.Tickets.EVENT_KEY, setter.getEventKey());
            values.put(DBContract.Tickets.NAME, setter.getName());
            values.put(DBContract.Tickets.VENUE, setter.getVenue());
            values.put(DBContract.Tickets.LOCATION, setter.getLocation());
            values.put(DBContract.Tickets.TICKET_NUMBER, setter.getTicketNumber());
            values.put(DBContract.Tickets.PRICE, "" + setter.getTicketPrice());
            values.put(DBContract.Tickets.START_TIMESTAMP, "" + setter.getStartTimestamp());
            values.put(DBContract.Tickets.CATEGORY, setter.getCategory());
            values.put(DBContract.Tickets.END_TIMESTAMP, setter.getEndTimestamp());
            values.put(DBContract.Tickets.MARCHANT_NAME, setter.getMerchantName());
            values.put(DBContract.Tickets.TICKET_KEY, setter.getTicketKey());
            values.put(DBContract.Tickets.MTS_CODE, setter.getMtsCode());
            values.put(DBContract.Tickets.VALID, setter.isValid() ? "yes":"no");

            if (!ticketKeys.contains(setter.getTicketKey()))
                db.insert(DBContract.Tickets.TABLE_NAME, null, values);
            else
                db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);

            ticketKeys = dbOperations.getTicketKeys(db, localUid);

            return null;
        }

    }






    private class TicketsAdapter extends ArrayAdapter {

        TicketsAdapter(Context context, int resource) {
            super(context, resource);
        }

        SharedPreferences prefs;

        public void add(GetSetterTicket object) {
            StaticVariables.listTickets.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listTickets.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listTickets.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_tickets_user, parent, false);
                holder = new Holder();

                holder.tvPrice = (TextView) row.findViewById(R.id.tvPriceZ);
                holder.tvMerchantName = (TextView) row.findViewById(R.id.tvMerchantNameZ);
                holder.tvName = (TextView) row.findViewById(R.id.tvNameZ);
                holder.tvLocation = (TextView) row.findViewById(R.id.tvLocationZ);
                holder.tvVenue = (TextView) row.findViewById(R.id.tvVenueZ);
                holder.tvMonth = (TextView) row.findViewById(R.id.tvMonthZ);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTimeZ);
                holder.tvCategory = (TextView) row.findViewById(R.id.tvCategoryZ);
                holder.cardSend = (CardView) row.findViewById(R.id.cardSend);
                holder.tvTicketCode = (TextView) row.findViewById(R.id.tvTicketCodeZ);
                holder.tvSend = (TextView) row.findViewById(R.id.tvSend);
                holder.tvTicketZ = row.findViewById(R.id.tvTicketZ);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
            final GetSetterTicket setterTicket = (GetSetterTicket) getItem(position);

            holder.tvCategory.setText(setterTicket.getCategory());
            holder.tvMonth.setText(getDate(setterTicket.getStartTimestamp(), "month"));
            holder.tvTime.setText(getDate(setterTicket.getStartTimestamp(), "time"));
            holder.tvName.setText(setterTicket.getName());
            holder.tvLocation.setText(setterTicket.getLocation());
            holder.tvVenue.setText(setterTicket.getVenue());
            holder.tvMerchantName.setText(setterTicket.getMerchantName().equals(StaticVariables.free) ? "..........." : setterTicket.getMerchantName());
            String price = setterTicket.getMerchantName().equals(StaticVariables.free) ? "0" : "" + setterTicket.getTicketPrice();
            holder.tvPrice.setText("$" + price);
            holder.tvTicketCode.setText(setterTicket.getTicketNumber());
            holder.tvTicketZ.setText(setterTicket.getMerchantName().equals(StaticVariables.free) ? "Free ticket" : "Purchased via ecocash");

            if (!setterTicket.isValid()) {
                holder.tvSend.setText("Ticket signed" + "" + "");
                holder.cardSend.setCardBackgroundColor(getContext().getResources().getColor(R.color.ticketSigned));
            } else {
                holder.tvSend.setText("Send to a friend" + "" + "");
                holder.cardSend.setCardBackgroundColor(getContext().getResources().getColor(R.color.mtsColor));
            }
            holder.tvTicketCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", setterTicket.getTicketNumber());
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });
            holder.cardSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (setterTicket.isValid()) {
                        //Toast.makeText(getContext(), "Send to friend", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), TicketSend.class);
                        intent.putExtra("position", position);
                        getContext().startActivity(intent);
                    } else
                        Toast.makeText(getContext(), "Ticket signed", Toast.LENGTH_SHORT).show();

                }
            });


            return row;

        }

        String getDate(long timeReceived, String type) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeReceived);


            if (type.equals("time")) {
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
            } else {
                return "" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                        getYear(calendar.get(Calendar.YEAR));
            }
        }

        private String getTheValue(int value){
            String theValue = String.valueOf(value);
            if (theValue.length() == 1){
                return "0"+theValue;
            } else {
                return theValue;
            }
        }

        private String getYear(int year) {
            String year2 = String.valueOf(year);
            return year2.substring(2, 4);
        }

        private class Holder {
            CardView cardSend;
            TextView tvPrice, tvMerchantName, tvName, tvLocation,
                    tvVenue, tvMonth, tvTime, tvCategory, tvTicketCode, tvSend, tvTicketZ;
        }

    }

}
