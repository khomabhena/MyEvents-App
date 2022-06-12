package tech.myevents.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class TicketSign extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    AutoCompleteTextView autoSearchTicket;
    CardView cardSearchNumber, cardGenerate;
    View viewBlock;
    CardView cardTicketZ, cardSign;
    TextView tvCategoryZ, tvTicketZ, tvTimeZ, tvMonthZ, tvVenueZ,
            tvLocationZ, tvNameZ, tvPriceZ, tvSign, tvMTSCode, tvTicketCodeZ;

    DBOperations dbOperations;
    SharedPreferences prefsApp;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    SQLiteDatabase db;
    List<String> ticketKeys;

    static boolean NETWORK_AVAILABLE = false;

    TicketsAdapter ticketsAdapter;
    String[] arrayTicketNumbers;
    List<String> listTicketNumbers;
    List<String> listTicketKeys;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_tickets);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ticketKeys = dbOperations.getTicketKeys(db, localUid);
        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        ticketsAdapter = new TicketsAdapter(this, R.layout.row_tickets_sign);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        autoSearchTicket = (AutoCompleteTextView) findViewById(R.id.autoSearchTicket);
        cardSearchNumber = (CardView) findViewById(R.id.cardSearchNumber);
        viewBlock = findViewById(R.id.viewBlock);

        cardTicketZ = (CardView) findViewById(R.id.cardTicketZ);
        cardSign = (CardView) findViewById(R.id.cardSign);
        cardGenerate = (CardView) findViewById(R.id.cardGenerate);

        tvCategoryZ = (TextView) findViewById(R.id.tvCategoryZ);
        tvTicketZ = (TextView) findViewById(R.id.tvTicketZ);
        tvTimeZ = (TextView) findViewById(R.id.tvTimeZ);
        tvMonthZ = (TextView) findViewById(R.id.tvMonthZ);
        tvVenueZ = (TextView) findViewById(R.id.tvVenueZ);
        tvLocationZ = (TextView) findViewById(R.id.tvLocationZ);
        tvNameZ = (TextView) findViewById(R.id.tvNameZ);
        tvPriceZ = (TextView) findViewById(R.id.tvPriceZ);
        tvSign = (TextView) findViewById(R.id.tvSign);
        tvMTSCode = (TextView) findViewById(R.id.tvMTSCode);
        tvTicketCodeZ = (TextView) findViewById(R.id.tvTicketCodeZ);

        if (prefs.getLong(AppInfo.MTS_ENDTIMESTAMP, 0) < getCurrentTimestamp()) {
            editor = prefs.edit();
            editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, false);
            editor.putLong(AppInfo.MTS_ENDTIMESTAMP, 0);
            editor.putString(AppInfo.MTS_SIGNING_UID, "");
            editor.apply();
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Ticket Sign";
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
        listTicketNumbers = new ArrayList<>();
        new TicketBackTask().execute();
    }



    private class TicketBackTask extends AsyncTask<Void, GetSetterTicket, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getTicketsSigner(db, FirebaseAuth.getInstance().getCurrentUser().getUid(), localUid);
            int count = cursor.getCount();

            String eventKey, uid, broadcasterUid, ticketNumber,
                    venue, location, name, category,
                    merchantName, ticketKey, mtsCode;
            int  ticketPrice, id, num;
            boolean valid;
            long endTimestamp, startTimestamp;
            listTicketKeys = new ArrayList<>();

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
                listTicketKeys.add(ticketKey);
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
        String mtsUid = prefs.getString(AppInfo.MTS_SIGNING_UID, "");
        final String uid;
        long mtsEndTimestamp = prefs.getLong(AppInfo.MTS_ENDTIMESTAMP, 0);
        if (mtsEndTimestamp < getCurrentTimestamp())
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else
            uid = !mtsUid.equals("") ? mtsUid : FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapKey : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapUid: snapKey.getChildren()) {
                                for (DataSnapshot snapUser: snapUid.getChildren()) {
                                    GetSetterTicket setter = snapUser.getValue(GetSetterTicket.class);
                                    if (setter.getBroadcasterUid().equals(uid)) {
                                        /*if (getCurrentTimestamp() > (setter.getEndTimestamp() + (86400000 * 2)))
                                            continue;*/

                                        listTicketNumbers.add(setter.getTicketNumber());

                                        if (!listTicketKeys.contains(setter.getTicketKey()))
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

                                        new InsertTicket(TicketSign.this).execute(setter);
                                    }
                                }
                            }
                        }
                        initializeTicketSearch(listTicketNumbers);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
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

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
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
            values.put(DBContract.Tickets.VALID, setter.isValid() ? "yes" : "no");

            if (ticketKeys.contains(setter.getTicketKey()))
                db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
            else
                db.insert(DBContract.Tickets.TABLE_NAME, null, values);

            /*ticketKeys = dbOperations.getTicketKeys(db, localUid);
            initializeTicketSearch(listTicketNumbers);*/

            return null;
        }

    }



    private void initializeTicketSearch(List<String> listTicketNumbers) {
        final GetSetterTicket[] setterTickets = new GetSetterTicket[1];
        arrayTicketNumbers = listTicketNumbers.toArray(new String[listTicketNumbers.size()]);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayTicketNumbers);
        autoSearchTicket.setAdapter(arrayAdapter);
        autoSearchTicket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int x = 0; x < arrayTicketNumbers.length; x++) {
                    if (arrayTicketNumbers[x].equals(String.valueOf(parent.getItemAtPosition(position)))) {
                        setterTickets[0] = (GetSetterTicket) StaticVariables.listTickets.get(x);
                        break;
                    }
                }

                if (String.valueOf(parent.getItemAtPosition(position)) != null) {
                    if (String.valueOf(parent.getItemAtPosition(position)).length() > 4) {

                        if (!setterTickets[0].isValid()) {
                            tvSign.setText("Revoke signing" + "" + "");
                            cardSign.setCardBackgroundColor(getResources().getColor(R.color.ticketSigned));
                        } else {
                            tvSign.setText("Sign ticket" + "" + "");
                            cardSign.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        }

                        cardTicketZ.setVisibility(View.VISIBLE);
                        cardSign.setVisibility(View.VISIBLE);
                        viewBlock.setVisibility(View.VISIBLE);
                        tvCategoryZ.setText(setterTickets[0].getCategory());
                        tvMonthZ.setText(getDate(setterTickets[0].getStartTimestamp(), "month"));
                        tvTimeZ.setText(getDate(setterTickets[0].getStartTimestamp(), "time"));
                        tvNameZ.setText(setterTickets[0].getName());
                        tvLocationZ.setText(setterTickets[0].getLocation());
                        tvVenueZ.setText(setterTickets[0].getVenue());
                        tvTicketCodeZ.setText(setterTickets[0].getTicketNumber());
                        tvPriceZ.setText("$" + setterTickets[0].getTicketPrice() + "");
                        tvTicketZ.setText(setterTickets[0].getCategory());

                        cardSign.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (setterTickets[0].isValid()) {
                                    signTicket(setterTickets[0]);
                                } else {
                                    unSignTicket(setterTickets[0]);
                                }
                                cardTicketZ.setVisibility(View.GONE);
                                cardSign.setVisibility(View.GONE);
                                viewBlock.setVisibility(View.GONE);
                            }
                        });

                    }
                }
            }
        });
    }

    private void signTicket(final GetSetterTicket setter) {
        setter.setValid(false);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childTickets)
                .child(setter.getEventKey())
                .child(setter.getUid())
                .child(setter.getTicketKey())
                .setValue(setter).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    tvSign.setText("Revoke signing" + "" + "");
                    cardSign.setCardBackgroundColor(getResources().getColor(R.color.ticketSigned));

                    for (int x = 0; x < StaticVariables.listTickets.size(); x++) {
                        GetSetterTicket setterTicket = (GetSetterTicket) StaticVariables.listTickets.get(x);
                        if (setterTicket.getTicketKey().equals(setter.getTicketKey())) {
                            setterTicket.setValid(false);
                            if (ticketsAdapter != null)
                                ticketsAdapter.notifyDataSetChanged();
                        }
                    }

                    String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                    ContentValues values = new ContentValues();
                    values.put(DBContract.Tickets.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                    values.put(DBContract.Tickets.VALID,  "no");
                    db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
                    Toast.makeText(getApplicationContext(), "Ticket Signed", Toast.LENGTH_SHORT).show();

                    String key = FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childNotificationTicket)
                            .child(setter.getUid())
                            .push()
                            .getKey();

                    GetSetterNotifications setterNotifications = new
                            GetSetterNotifications(StaticVariables.ticket, key, setter.getTicketKey(),
                            setter.getEventKey(), false);

                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childNotificationTicket)
                            .child(setter.getUid())
                            .child(key)
                            .setValue(setterNotifications);
                } else {
                    setter.setValid(true);
                    Toast.makeText(getApplicationContext(), "Failed to Sign Ticket", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void unSignTicket(final GetSetterTicket setter) {
        setter.setValid(true);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childTickets)
                .child(setter.getEventKey())
                .child(setter.getUid())
                .child(setter.getTicketKey())
                .setValue(setter).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    tvSign.setText("Sign ticket" + "" + "");
                    cardSign.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));

                    for (int x = 0; x < StaticVariables.listTickets.size(); x++) {
                        GetSetterTicket setterTicket = (GetSetterTicket) StaticVariables.listTickets.get(x);
                        if (setterTicket.getTicketKey().equals(setter.getTicketKey())) {
                            setterTicket.setValid(true);
                            if (ticketsAdapter != null)
                                ticketsAdapter.notifyDataSetChanged();
                        }
                    }

                    String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                    ContentValues values = new ContentValues();
                    values.put(DBContract.Tickets.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                    values.put(DBContract.Tickets.VALID,  "yes");
                    db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
                    Toast.makeText(getApplicationContext(), "Signing revoked", Toast.LENGTH_SHORT).show();

                    String key = FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childNotificationTicket)
                            .child(setter.getUid())
                            .push()
                            .getKey();

                    GetSetterNotifications setterNotifications = new
                            GetSetterNotifications(StaticVariables.ticket, key, setter.getTicketKey(),
                            setter.getEventKey(), false);

                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childNotificationTicket)
                            .child(setter.getUid())
                            .child(key)
                            .setValue(setterNotifications);

                } else {
                    setter.setValid(false);
                    Toast.makeText(getApplicationContext(), "Failed to revoke ticket signing", Toast.LENGTH_LONG).show();
                }
            }
        });
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






    private class TicketsAdapter extends ArrayAdapter {

        TicketsAdapter(Context context, int resource) {
            super(context, resource);
        }

        SharedPreferences prefs;
        GetSetterTicket setterTicket;
        Holder holder;

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
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_tickets_sign, parent, false);
                holder = new Holder();

                holder.tvPrice = (TextView) row.findViewById(R.id.tvPriceZ);
                holder.tvMerchantName = (TextView) row.findViewById(R.id.tvTicketCodeZ);
                holder.tvName = (TextView) row.findViewById(R.id.tvNameZ);
                holder.tvLocation = (TextView) row.findViewById(R.id.tvLocationZ);
                holder.tvVenue = (TextView) row.findViewById(R.id.tvVenueZ);
                holder.tvMonth = (TextView) row.findViewById(R.id.tvMonthZ);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTimeZ);
                holder.tvTicket = (TextView) row.findViewById(R.id.tvTicketZ);
                holder.tvCategory = (TextView) row.findViewById(R.id.tvCategoryZ);
                holder.cardSign = (CardView) row.findViewById(R.id.cardSign);
                holder.tvSign = (TextView) row.findViewById(R.id.tvSign);
                holder.cardGenerate = (CardView) row.findViewById(R.id.cardGenerate);
                holder.tvMTSCode = (TextView) row.findViewById(R.id.tvMTSCode);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
            setterTicket = (GetSetterTicket) getItem(position);

            holder.tvCategory.setText(setterTicket.getCategory());
            holder.tvMonth.setText(getDate(setterTicket.getStartTimestamp(), "month"));
            holder.tvTime.setText(getDate(setterTicket.getStartTimestamp(), "time"));
            holder.tvName.setText(setterTicket.getName());
            holder.tvLocation.setText(setterTicket.getLocation());
            holder.tvVenue.setText(setterTicket.getVenue());
            holder.tvMerchantName.setText(setterTicket.getTicketNumber());
            String price = !setterTicket.getMerchantName().equals(StaticVariables.free) ? "" + setterTicket.getTicketPrice() : "0";
            holder.tvPrice.setText("$" + price);
            holder.tvTicket.setText(setterTicket.getMerchantName().equals(StaticVariables.free) ? "Free ticket" : "Purchased via ecocash");

            if (!setterTicket.isValid()) {
                holder.tvSign.setText("Revoke signing" + "" + "");
                holder.cardSign.setCardBackgroundColor(getContext().getResources().getColor(R.color.ticketSigned));
            } else {
                holder.tvSign.setText("Sign ticket" + "" + "");
                holder.cardSign.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
            }
            if (setterTicket.getMtsCode().equals(""))
                holder.tvMTSCode.setText("Generate MTS Code" + "" + "");
            else
                holder.tvMTSCode.setText(setterTicket.getMtsCode());

            holder.cardGenerate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setterTicket.getMtsCode().equals(""))
                        generateMtsCode(position);
                    else
                        Toast.makeText(getContext(), "Code already generated", Toast.LENGTH_SHORT).show();
                }
            });
            holder.cardSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GetSetterTicket setterZ = (GetSetterTicket) StaticVariables.listTickets.get(position);
                    if (setterZ.isValid())
                        signTicket(setterZ, holder, position);
                    else
                        unSignTicket(setterZ, holder, position);
                }
            });
            holder.cardGenerate.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", holder.tvMTSCode.getText().toString());
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            return row;

        }

        private void generateMtsCode(int position) {
            final GetSetterTicket setterTicket = (GetSetterTicket) StaticVariables.listTickets.get(position);
            final String mtsCode = getPromotionCode();
            setterTicket.setMtsCode(mtsCode);
            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childTickets)
                    .child(setterTicket.getEventKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                for (DataSnapshot snapshot: dataSnapshot1.getChildren()) {
                                    final GetSetterTicket setter = snapshot.getValue(GetSetterTicket.class);
                                    setter.setMtsCode(mtsCode);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childTickets)
                                            .child(setter.getEventKey())
                                            .child(setter.getUid())
                                            .child(setter.getTicketKey())
                                            .setValue(setter)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Code generated", Toast.LENGTH_SHORT).show();

                                                        ContentValues values = new ContentValues();
                                                        values.put(DBContract.Tickets.MTS_CODE, mtsCode);

                                                        for (int x = 0; x < StaticVariables.listTickets.size(); x++) {
                                                            GetSetterTicket ticketSet = (GetSetterTicket) StaticVariables.listTickets.get(x);
                                                            if (ticketSet.getEventKey().equals(setter.getEventKey())) {
                                                                ticketSet.setMtsCode(mtsCode);
                                                                ticketsAdapter.notifyDataSetChanged();

                                                                db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        private void signTicket(final GetSetterTicket setter, final Holder holder, final int position) {
            setter.setValid(false);

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childTickets)
                    .child(setter.getEventKey())
                    .child(setter.getUid())
                    .child(setter.getTicketKey())
                    .setValue(setter).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //holder.tvSign.setText("Revoke signing" + "" + "");
                        //holder.cardSign.setCardBackgroundColor(getContext().getResources().getColor(R.color.ticketSigned));

                        GetSetterTicket setterTicket = (GetSetterTicket) StaticVariables.listTickets.get(position);
                        setterTicket.setValid(false);
                        ticketsAdapter.notifyDataSetChanged();

                        String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                        ContentValues values = new ContentValues();
                        values.put(DBContract.Tickets.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                        values.put(DBContract.Tickets.VALID, "no");
                        db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
                        Toast.makeText(getContext(), "Ticket signed", Toast.LENGTH_SHORT).show();

                        String key = FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationTicket)
                                .child(setter.getUid())
                                .push()
                                .getKey();

                        GetSetterNotifications setterNotifications = new
                                GetSetterNotifications(StaticVariables.ticket, key, setter.getTicketKey(),
                                setter.getEventKey(), false);

                        FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationTicket)
                                .child(setter.getUid())
                                .child(key)
                                .setValue(setterNotifications);

                    } else {
                        setter.setValid(true);
                        Toast.makeText(getContext(), "Failed to Sign Ticket", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private void unSignTicket(final GetSetterTicket setter, final Holder holder, final int position) {
            setter.setValid(true);

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childTickets)
                    .child(setter.getEventKey())
                    .child(setter.getUid())
                    .child(setter.getTicketKey())
                    .setValue(setter).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //holder.tvSign.setText("Sign ticket" + "" + "");
                        //holder.cardSign.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));

                        GetSetterTicket setterTicket = (GetSetterTicket) StaticVariables.listTickets.get(position);
                        setterTicket.setValid(true);
                        ticketsAdapter.notifyDataSetChanged();

                        String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                        ContentValues values = new ContentValues();
                        values.put(DBContract.Tickets.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                        values.put(DBContract.Tickets.VALID,  "yes");
                        db.update(DBContract.Tickets.TABLE_NAME, values, DBContract.Tickets.TICKET_KEY + "='" + setter.getTicketKey() + "'", null);
                        Toast.makeText(getContext(), "Signing revoked", Toast.LENGTH_SHORT).show();

                        String key = FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationTicket)
                                .child(setter.getUid())
                                .push()
                                .getKey();

                        GetSetterNotifications setterNotifications = new
                                GetSetterNotifications(StaticVariables.ticket, key, setter.getTicketKey(),
                                setter.getEventKey(), false);

                        FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationTicket)
                                .child(setter.getUid())
                                .child(key)
                                .setValue(setterNotifications);

                    } else {
                        setter.setValid(false);
                        Toast.makeText(getContext(), "Failed to revoke ticket signing", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private class Holder {
            CardView cardSign, cardGenerate;
            TextView tvPrice, tvMerchantName, tvName, tvLocation,
                    tvVenue, tvMonth, tvTime, tvTicket, tvCategory, tvMTSCode, tvSign;
        }


        private String getPromotionCode() {
            String[] alpha = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

            Random random = new Random();
            String a = alpha[checkNum(random.nextInt(26))];
            String b = alpha[checkNum(random.nextInt(26))];
            String bx = alpha[checkNum(random.nextInt(26))];
            int c = random.nextInt(9);
            int d = random.nextInt(9);
            int e = random.nextInt(9);
            int f = random.nextInt(9);
            String bz = alpha[checkNum(random.nextInt(26))];
            String g = alpha[checkNum(random.nextInt(26))];
            String h = alpha[checkNum(random.nextInt(26))];

            return a + b + c + d + bx + bz + e + f + g + h;
        }

        private int checkNum(int a) {
            if (a == 0 || a == 27) {
                if (a == 0) {
                    a += 1;
                }
                if (a == 27) {
                    a -= 1;
                }
            }

            return a;
        }

    }

}
