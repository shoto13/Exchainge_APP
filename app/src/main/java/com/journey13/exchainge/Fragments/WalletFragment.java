package com.journey13.exchainge.Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journey13.exchainge.Model.User;
import com.journey13.exchainge.Model.Wallet;
import com.journey13.exchainge.MyListAdapter;
import com.journey13.exchainge.R;
import com.journey13.exchainge.settingsChangeTagline;
import com.journey13.exchainge.settingsChangeUsername;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalletFragment extends Fragment {

    private Button updateBalanceButton;
    private TextView monthlyFundsDollar, monthlyFundsEXCH, balanceDollar, balanceEXCH;

    FirebaseUser fuser;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);

        updateBalanceButton = rootView.findViewById(R.id.getBalanceButton);
        monthlyFundsDollar = rootView.findViewById(R.id.monthlyFundsDollar);
        monthlyFundsEXCH = rootView.findViewById(R.id.monthlyFundsEXCH);
        balanceDollar = rootView.findViewById(R.id.balanceDollar);
        balanceEXCH = rootView.findViewById(R.id.balanceEXCH);

        updateBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO update hard coded values used for testing
                monthlyFundsDollar.setText("$2.77");
                // monthlyFundsEXCH.setText("247.88 EXCH");
                balanceDollar.setText("$12.88");
                // balanceEXCH.setText("1120 EXCH");
            }
        });

        //Firebase setup
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Wallets").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wallet wallet = dataSnapshot.getValue(Wallet.class);
                assert wallet != null;
                String balString = wallet.getwBalance().toString() + " ExCH";
                String monthlyFString = wallet.getwMonthlyEarnings().toString() + " ExCH";
                balanceEXCH.setText(balString);
                monthlyFundsEXCH.setText(monthlyFString);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return rootView;
    }


}