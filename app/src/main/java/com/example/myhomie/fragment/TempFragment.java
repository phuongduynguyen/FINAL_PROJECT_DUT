package com.example.myhomie.fragment;

import android.content.Intent;
import android.os.Bundle;
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

public class TempFragment extends Fragment {

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_temp, container, false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("NhietDo");
        DatabaseReference myRef_1 = database.getReference("IAQ");

        final TextView mTemperatureTextView = root.findViewById(R.id.temperatureTextView);
        final ProgressBar mTemperatureProgressBar = root.findViewById(R.id.temperatureProgressBar);
        final RelativeLayout mMenu = root.findViewById(R.id.linearLayout);
        final TextView mIAQ = root.findViewById(R.id.sub_titleTextView);

        mMenu.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Chart.class);
            startActivity(intent);
        });

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
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                value = value.replaceAll(" ", "");
                if (value.equals("nan"))
                {
                    mTemperatureTextView.setText("40°C");
                    mTemperatureProgressBar.setProgress(40);
                }else {
                    mTemperatureTextView.setText(value+" °C");
                    assert value != null;
                    int temp = (int) Float.parseFloat(value);
                    mTemperatureProgressBar.setProgress(temp);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        return root;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
