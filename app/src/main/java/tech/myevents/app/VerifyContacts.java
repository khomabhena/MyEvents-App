package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
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

public class VerifyContacts extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    VerifyContactsAdapter verifyContactsAdapter;
    static int SCROLL_POS;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_contacts);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        verifyContactsAdapter = new VerifyContactsAdapter(this, R.layout.row_verify_contacts);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (StaticVariables.listVerifyContacts == null) {
            StaticVariables.listVerifyContacts = new ArrayList<>();
            getContacts();
        } else {
            listView.setAdapter(verifyContactsAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Verify Contacts";
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
    protected void onPause() {
        super.onPause();
        SCROLL_POS = listView.getFirstVisiblePosition();
    }

    public void getContacts() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childVerifyContacts)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapUid : dataSnapshot.getChildren()) {
                                GetSetterVerifyContact setter = snapUid.getValue(GetSetterVerifyContact.class);

                                if (setter.isVerificationSent())
                                    continue;

                                count++;
                                verifyContactsAdapter.add(setter);
                            }
                        }
                        if (count != 0) {
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                            listView.setAdapter(verifyContactsAdapter);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private class VerifyContactsAdapter extends ArrayAdapter {

        VerifyContactsAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterVerifyContact object) {
            StaticVariables.listVerifyContacts.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listVerifyContacts.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listVerifyContacts.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_verify_contacts, parent, false);
                holder = new Holder();

                holder.tvPhoneNumber = (TextView) row.findViewById(R.id.tvPhoneNumber);
                holder.tvConfirmationCode = (TextView) row.findViewById(R.id.tvConfirmationCode);
                holder.cardSave = (CardView) row.findViewById(R.id.cardSave);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterVerifyContact setter = (GetSetterVerifyContact) getItem(position);

            holder.tvPhoneNumber.setText("" + setter.getPhoneNumber() + "");
            holder.tvConfirmationCode.setText(setter.getVerificationCode());

            holder.cardSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setter.setVerificationSent(true);
                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childVerifyContacts)
                            .child(setter.getKey())
                            .setValue(setter)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                                }
                            });

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + setter.getPhoneNumber()));
                    startActivity(intent);
                }
            });

            return row;
        }

        private class Holder {
            TextView tvPhoneNumber, tvConfirmationCode;
            CardView cardSave;
        }

    }

}
