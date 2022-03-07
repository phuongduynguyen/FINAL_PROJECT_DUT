package com.example.myhomie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

public class Reset_Activity extends AppCompatActivity {

    String[] items = {"Device", "Monitor", "Door", "Camera"};
    AutoCompleteTextView autoComplete;
    ArrayAdapter<String> adapterItem;
    String item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        autoComplete = findViewById(R.id.auto_complete);
        adapterItem = new ArrayAdapter<String>(this, R.layout.list_item, items);
        autoComplete.setAdapter(adapterItem);
        autoComplete.setOnItemClickListener((parent, view, position, id) -> { item = parent.getItemAtPosition(position).toString();
          switch (item) {
              case "Device":
                  Toast.makeText(this, "Device", Toast.LENGTH_SHORT).show();
              case "Monitor":
                  Toast.makeText(this, "Monitor", Toast.LENGTH_SHORT).show();
              case "Door":
                  Toast.makeText(this, "Door", Toast.LENGTH_SHORT).show();
              case "Camera":
                  Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show();
          }
        });
    }
}