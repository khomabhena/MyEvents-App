package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
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

public class VerifyBusiness extends AppCompatActivity {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    VerifyBusinessAdapter verifyBusinessAdapter;
    static int SCROLL_POS;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_business);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        verifyBusinessAdapter = new VerifyBusinessAdapter(this, R.layout.row_business_verify);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (StaticVariables.listVerifyBusiness== null) {
            StaticVariables.listVerifyBusiness = new ArrayList<>();
            getContacts();
        } else {
            listView.setAdapter(verifyBusinessAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
        }


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Verify Business";
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
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snapUid : dataSnapshot.getChildren()) {
                            GetSetterBusiness setter = snapUid.getValue(GetSetterBusiness.class);

                            if (StaticVariables.listVerifyBusiness == null)
                                StaticVariables.listVerifyBusiness = new ArrayList<>();

                            if (!setter.isVerified())
                                if (StaticVariables.listVerifyBusiness.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    verifyBusinessAdapter.add(setter);
                                    listView.setAdapter(verifyBusinessAdapter);
                                } else {
                                    StaticVariables.listVerifyBusiness.add(0, setter);
                                    verifyBusinessAdapter.notifyDataSetChanged();
                                }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private class VerifyBusinessAdapter extends ArrayAdapter {

        VerifyBusinessAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterBusiness object) {
            StaticVariables.listVerifyBusiness.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listVerifyBusiness.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listVerifyBusiness.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_business_verify, parent, false);
                holder = new Holder();

                holder.tvEmail = (TextView) row.findViewById(R.id.tvEmail);
                holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
                holder.tvLocation = (TextView) row.findViewById(R.id.tvLocation);
                holder.tvWebsite = (TextView) row.findViewById(R.id.tvWebsite);
                holder.cardVerify = (CardView) row.findViewById(R.id.cardVerify);
                holder.ivLogo =  (ImageView) row.findViewById(R.id.ivLogo);
                holder.ivVerified =  (ImageView) row.findViewById(R.id.ivVerified);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterBusiness setter = (GetSetterBusiness) getItem(position);

            holder.tvBrandName.setText("" + setter.getBrandName() + "");
            holder.tvEmail.setText(setter.getEmail());
            holder.tvLocation.setText(setter.getLocation());
            holder.tvWebsite.setText(setter.getWebsite());

            holder.cardVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setter.setVerified(true);
                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childBusinesses)
                            .child(setter.getUid())
                            .setValue(setter)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(getContext(), "Verified", Toast.LENGTH_SHORT).show();
                                    GetSetterVerified setterVerified = new GetSetterVerified(setter.getUid(), true);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childVerifiedBusinesses)
                                            .child(setter.getUid())
                                            .setValue(setterVerified)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(getContext(), "Verified for settings", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            });

            return row;
        }

        private class Holder {
            ImageView ivLogo, ivVerified;
            TextView tvBrandName, tvEmail, tvLocation, tvWebsite;
            CardView cardVerify;
        }

    }

}
