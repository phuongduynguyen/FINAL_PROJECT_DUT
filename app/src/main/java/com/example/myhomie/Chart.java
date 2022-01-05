package com.example.myhomie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.view.WindowManager;
import android.content.Intent;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.Legend;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

import android.widget.AbsSeekBar;
import static com.example.myhomie.DataLocalManager.getDataFirebase;
import static com.example.myhomie.DataLocalManager.getIntData_Set;
import static com.example.myhomie.DataLocalManager.getIntData_Update;
import static com.example.myhomie.DataLocalManager.setDataFirebase;
import static com.example.myhomie.DataLocalManager.setIntData_Set;
import static com.example.myhomie.DataLocalManager.setIntData_Update;
import static com.facebook.login.widget.ProfilePictureView.TAG;

public class Chart extends AppCompatActivity {

    private TextView mConnectionState;
    private double startTime = 0.0;
    private ArrayList<Entry> valuesTemperature = new ArrayList<>();
    private ArrayList<Entry> valuesPressure = new ArrayList<>();
    private ArrayList<Entry> valuesAltitude = new ArrayList<>();
    private ArrayList<Entry> valuesCo2 = new ArrayList<>();
    static int updatePeriod = 1000;
    SeekBar seekBarUpdate, seekBarDataSet;
    private TextView textViewUpdate, textViewDataSet;
    private LineChart chartTemperature, chartPressure, chartAltitude, chartCo2;
    private SwitchCompat aSwitchRun;
    static int maximumDataSet = 20;
    private String receiveBuffer = "";
    DatabaseReference mData;
    private float apsuat = 0;
    private float nhietdo = 0;
    private float doam = 0;
    private float co2 = 0;
    static boolean state = false;
    boolean state_run = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bmp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        anhxa();
        mData         = FirebaseDatabase.getInstance().getReference();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializeCharts();
        setData();

        if(state){
        textViewDataSet.setText("Data set: " + DataLocalManager.getIntData_Set());
        seekBarDataSet.setProgress(DataLocalManager.getIntData_Set());
        textViewUpdate.setText("Update period: " + DataLocalManager.getIntData_Update() + " s");
        seekBarUpdate.setProgress( DataLocalManager.getIntData_Update());
        updatePeriod    =   DataLocalManager.getIntData_Update() * 1000;
        maximumDataSet  =   DataLocalManager.getIntData_Set() ;
        }

        seekBarUpdate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePeriod = (progress + 1) * 1000;
                DataLocalManager.setIntData_Update(progress+1);
                if(progress != 0)
                    textViewUpdate.setText("Update period: " + (progress + 1) + " seconds");
                else
                    textViewUpdate.setText("Update period: 1 s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarDataSet.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maximumDataSet = progress + 20;
                DataLocalManager.setIntData_Set( (progress + 20));
                state = true;
                textViewDataSet.setText("Data set: " + (progress + 20));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        aSwitchRun.setOnClickListener(v -> {
            state_run =! state_run;
            mData.child("Status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Toast.makeText(Chart.this, "Connected to firebase", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(Chart.this, "Cant connected to firebase", Toast.LENGTH_SHORT).show();
                }
            });

            if(state_run){
                mConnectionState.setText("ON");
                mData.child("ApSuat").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        double currentTime;
                        try {
                            apsuat = Float.parseFloat(value);
                        }catch (Exception e){
                            apsuat = 0;
                        }

                        if (apsuat!=0){
                            if(startTime == 0.0)
                            {
                                startTime = Calendar.getInstance().getTimeInMillis();
                                currentTime = startTime;
                            }else
                            {
                                currentTime = Calendar.getInstance().getTimeInMillis();
                            }

                            double time = (currentTime - startTime) / 1000.0;

                            valuesPressure.add(new Entry((float)time, apsuat));

                            while(valuesPressure.size() > maximumDataSet)
                                valuesPressure.remove(0);

                            updateCharts();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Chart.this, "Fall get Pressure data from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
                mData.child("DoAm").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);

                        double currentTime;
                        try {
                            doam = Float.parseFloat(value);
                        }catch (Exception e){
                            doam = 0;
                        }

                        if (doam!=0) {
                            if (startTime == 0.0) {
                                startTime = Calendar.getInstance().getTimeInMillis();
                                currentTime = startTime;
                            } else {
                                currentTime = Calendar.getInstance().getTimeInMillis();
                            }

                            double time = (currentTime - startTime) / 1000.0;

                            valuesAltitude.add(new Entry((float) time, doam));

                            while (valuesAltitude.size() > maximumDataSet)
                                valuesAltitude.remove(0);

                            updateCharts();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Chart.this, "Fall get Humid data from Firebase", Toast.LENGTH_SHORT).show();

                    }
                });
                mData.child("NhietDo").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        double currentTime;
                        try {
                            nhietdo = Float.parseFloat(value);
                        }catch (Exception e){
                            nhietdo = 0;
                        }

                        if (nhietdo!=0) {
                            if (startTime == 0.0) {
                                startTime = Calendar.getInstance().getTimeInMillis();
                                currentTime = startTime;
                            } else {
                                currentTime = Calendar.getInstance().getTimeInMillis();
                            }

                            double time = (currentTime - startTime) / 1000.0;

                            valuesTemperature.add(new Entry((float) time, nhietdo));

                            while (valuesTemperature.size() > maximumDataSet)
                                valuesTemperature.remove(0);

                            updateCharts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Chart.this, "Fall get Temperature data from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
                mData.child("Co2").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        double currentTime;
                        try {
                            co2 = Float.parseFloat(value);
                        }catch (Exception e){
                            co2 = 0;
                        }

                        if (co2!=0) {
                            if (startTime == 0.0) {
                                startTime = Calendar.getInstance().getTimeInMillis();
                                currentTime = startTime;
                            } else {
                                currentTime = Calendar.getInstance().getTimeInMillis();
                            }

                            double time = (currentTime - startTime) / 1000.0;

                            valuesCo2.add(new Entry((float) time, co2));

                            while (valuesCo2.size() > maximumDataSet)
                                valuesCo2.remove(0);
                            updateCharts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(Chart.this, "Fall get Co2 data from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                mConnectionState.setText("OFF");
                clearUI();
            }

        });



    }

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putFloat("apsuat", apsuat);
        outState.putFloat("nhietdo", nhietdo);
        outState.putFloat("doam", doam);
        outState.putInt("datatset",maximumDataSet);
        outState.putInt("period",updatePeriod);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle outState) {
        super.onRestoreInstanceState(outState);
        seekBarUpdate.setProgress(outState.getInt("period"));
        seekBarDataSet.setProgress(outState.getInt("datatset"));
        textViewUpdate.setText("Update period: " + (outState.getInt("period") + 1) + " seconds");
    }

    private void anhxa() {
        mConnectionState    = findViewById(R.id.connection_state);
        aSwitchRun          = findViewById(R.id.switchRun);
        textViewUpdate      = findViewById(R.id.textViewUpdate);
        textViewDataSet     = findViewById(R.id.textViewDataSet);
        seekBarUpdate       = findViewById(R.id.seekBarUpdate);
        seekBarDataSet      = findViewById(R.id.seekBarDataSet);

    }

    private void initializeCharts()
    {
        chartTemperature = findViewById(R.id.chartTemperature);
        chartTemperature.setDrawGridBackground(true);

        // no description text
        chartTemperature.getDescription().setEnabled(false);

        // enable touch gestures
        chartTemperature.setTouchEnabled(false);

        // enable scaling and dragging
        chartTemperature.setDragEnabled(true);
        chartTemperature.setScaleEnabled(true);
        chartTemperature.setScaleY(1.0f);

        // if disabled, scaling can be done on x- and y-axis separately
        chartTemperature.setPinchZoom(false);

        chartTemperature.getAxisLeft().setDrawGridLines(true);
        chartTemperature.getAxisRight().setEnabled(false);
        chartTemperature.getXAxis().setDrawGridLines(true);
        chartTemperature.getXAxis().setDrawAxisLine(false);
        chartTemperature.animateY(2000);
        chartTemperature.animateX(2000);



        chartPressure = findViewById(R.id.chartPressure);
        chartPressure.setDrawGridBackground(true);

        // no description text
        chartPressure.getDescription().setEnabled(false);

        // enable touch gestures
        chartPressure.setTouchEnabled(false);

        // enable scaling and dragging
        chartPressure.setDragEnabled(true);
        chartPressure.setScaleEnabled(true);
        chartPressure.setScaleY(1.0f);

        // if disabled, scaling can be done on x- and y-axis separately
        chartPressure.setPinchZoom(false);

        chartPressure.getAxisLeft().setDrawGridLines(true);
        chartPressure.getAxisRight().setEnabled(false);
        chartPressure.getXAxis().setDrawGridLines(true);
        chartPressure.getXAxis().setDrawAxisLine(false);



        chartAltitude = findViewById(R.id.chartAltitude);
        chartAltitude.setDrawGridBackground(true);

        // no description text
        chartAltitude.getDescription().setEnabled(false);

        // enable touch gestures
        chartAltitude.setTouchEnabled(false);

        // enable scaling and dragging
        chartAltitude.setDragEnabled(true);
        chartAltitude.setScaleEnabled(true);
        chartAltitude.setScaleY(1.0f);

        // if disabled, scaling can be done on x- and y-axis separately
        chartAltitude.setPinchZoom(false);
        chartAltitude.getAxisLeft().setDrawGridLines(true);
        chartAltitude.getAxisRight().setEnabled(false);
        chartAltitude.getXAxis().setDrawGridLines(true);
        chartAltitude.getXAxis().setDrawAxisLine(false);



        // init co2 chart

        chartCo2 = findViewById(R.id.charCo2);
        chartCo2.setDrawGridBackground(true);

        // no description text
        chartCo2.getDescription().setEnabled(false);

        // enable touch gestures
        chartCo2.setTouchEnabled(false);

        // enable scaling and dragging
        chartCo2.setDragEnabled(true);
        chartCo2.setScaleEnabled(true);
        chartCo2.setScaleY(1.0f);

        // if disabled, scaling can be done on x- and y-axis separately
        chartCo2.setPinchZoom(false);
        chartCo2.getAxisLeft().setDrawGridLines(true);
        chartCo2.getAxisRight().setEnabled(false);
        chartCo2.getXAxis().setDrawGridLines(true);
        chartCo2.getXAxis().setDrawAxisLine(false);
    }

    private void updateCharts()
    {
        chartTemperature.resetTracking();
        chartPressure.resetTracking();
        chartAltitude.resetTracking();
        chartCo2.resetTracking();
        setData();
        // redraw
        chartTemperature.invalidate();
        chartPressure.invalidate();
        chartAltitude.invalidate();
        chartCo2.invalidate();
    }

    private void setData() {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(valuesTemperature, "Temperature (°C)");

        set1.setColor(Color.RED);
        set1.setLineWidth(1.0f);
        set1.setDrawValues(true);
        set1.setDrawCircles(true);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);

        // set data
        chartTemperature.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chartTemperature.getLegend();
        l.setEnabled(true);


        // create a dataset and give it a type
        set1 = new LineDataSet(valuesPressure, "Pressure (Pa)");

        set1.setColor(Color.GREEN);
        set1.setLineWidth(1.0f);
        set1.setDrawValues(true);
        set1.setDrawCircles(true);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        data = new LineData(set1);

        // set data
        chartPressure.setData(data);

        // get the legend (only possible after setting data)
        l = chartPressure.getLegend();
        l.setEnabled(true);



        // create a dataset and give it a type
        set1 = new LineDataSet(valuesAltitude, "Humid (%)");
        set1.setColor(Color.BLUE);
        set1.setLineWidth(1.0f);
        set1.setDrawValues(true);
        set1.setDrawCircles(true);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        data = new LineData(set1);

        // set data
        chartAltitude.setData(data);

        // get the legend (only possible after setting data)
        l = chartAltitude.getLegend();
        l.setEnabled(true);


        // create a dataset and give it a type
        set1 = new LineDataSet(valuesCo2, "Co2 (ppm)");
        set1.setColor(Color.CYAN);
        set1.setLineWidth(1.0f);
        set1.setDrawValues(true);
        set1.setDrawCircles(true);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        data = new LineData(set1);

        // set data
        chartCo2.setData(data);

        // get the legend (only possible after setting data)
        l = chartCo2.getLegend();
        l.setEnabled(true);

    }

    private void clearUI() {
        receiveBuffer="";
        valuesTemperature.clear();
        valuesPressure.clear();
        valuesAltitude.clear();
        valuesCo2.clear();
        startTime = 0.0;
        updateCharts();
    }

    private void messageHandler() {
        if (receiveBuffer != null) {
            double currentTime;
            float temperature = -999.0f, pressure = -999.0f, altitude = -999.0f;

            try
            {
                temperature = (float) 16.5;
                pressure = (float) 11011;
                altitude = 100101;
            }catch (Exception e)
            {
                temperature = -999.0f;
                pressure = -999.0f;
                altitude = -999.0f;
            }

            if(temperature != -999.0 || pressure != -999.0 || altitude != -999.0)
            {
                if(startTime == 0.0)
                {
                    startTime = Calendar.getInstance().getTimeInMillis();
                    currentTime = startTime;
                }else
                {
                    currentTime = Calendar.getInstance().getTimeInMillis();
                }

                double time = (currentTime - startTime) / 1000.0;

                valuesTemperature.add(new Entry((float)time, temperature));
                //valuesPressure.add(new Entry((float)time, pressure));
                valuesAltitude.add(new Entry((float)time, altitude));

                while(valuesTemperature.size() > maximumDataSet)
                    valuesTemperature.remove(0);

                while(valuesPressure.size() > maximumDataSet)
                    valuesPressure.remove(0);

                while(valuesAltitude.size() > maximumDataSet)
                    valuesAltitude.remove(0);
                updateCharts();
            }

        }
    }
}