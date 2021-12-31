package com.example.myhomie.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myhomie.Chart;
import com.example.myhomie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class HumidFragment extends Fragment {
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_humid, container, false);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("DoAm");
        final TextView mHumidityTextView = root.findViewById(R.id.humidityTextView);
        final ProgressBar mHumidityProgressBar = root.findViewById(R.id.humidityProgressBar);

        final RelativeLayout mMenu = root.findViewById(R.id.linearLayout);

        mMenu.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Chart.class);
            startActivity(intent);
        });
        DatabaseReference myRef_1 = database.getReference("IAQ");
        final TextView mIAQ = root.findViewById(R.id.sub_titleTextView);
        myRef_1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String value = snapshot.getValue().toString();
                int iaq = (int) Float.parseFloat(value);
                if (iaq > 0 && iaq <= 50)
                {
                    mIAQ.setText("IAQ: Good" );
                }else if(iaq > 51 && iaq <= 100){
                    mIAQ.setText("IAQ: Average" );
                }else if(iaq > 101 && iaq <= 150) {
                    mIAQ.setText("IAQ: Little Bad");
                }else if(iaq > 151 && iaq <= 200){
                    mIAQ.setText("IAQ: Bad" );
                }else if(iaq > 201 && iaq <= 300){
                    mIAQ.setText("IAQ: Worse" );
                }else if(iaq > 301 && iaq <= 500){
                    mIAQ.setText("IAQ: Very Bad" );
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                value = value.replaceAll(" ", "");
                if (value.equals("nan"))
                {
                    mHumidityTextView.setText("100%");
                    mHumidityProgressBar.setProgress(100);
                }else {
                    mHumidityTextView.setText(value+" %");
                    assert value != null;
                    int temp = (int) Float.parseFloat(value);
                    mHumidityProgressBar.setProgress(temp);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });


        return root;
    }
}
