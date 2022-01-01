package com.example.myhomie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity3 extends AppCompatActivity {

    private static final int NOTIFICATION_ID = 1;
    private static final int JOB_ID = 123;
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver broadcastReceiver;

    TextView tvName, tvCountry, tvTemp, tvHumid, tvStatus, tvWind, tvDay, tvCloudsmall;
    ImageView imgIcon;
    String City = "";
    RelativeLayout relativeLayout;

    static {
        if (OpenCVLoader.initDebug()){
            Log.d("MainActivity: ", "OpenCV loaded");
        }
        else {
            Log.d("MainActivity: ", "OpenCV not loaded");
        }
    }

    private static final int NOTIFY_ID = 2020;

    ImageView   infor, sensor, weather, cctv;

    LinearLayout textsplash, texthome, menus;
    Animation    frombottom;
    FirebaseAuth mAth;
    DatabaseReference mData;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        final DatabaseReference alert = database.getReference("Alert");
        final long[] pattern = {2000, 1000};

        mAth          = FirebaseAuth.getInstance();
        mData         = FirebaseDatabase.getInstance().getReference();
        frombottom    = AnimationUtils.loadAnimation(this, R.anim.frombottom);


        tvName      = findViewById(R.id.tvName);
        tvCountry   = findViewById(R.id.tvCountry);
        tvTemp      = findViewById(R.id.tvTemperature);
        tvHumid     = findViewById(R.id.tvHumidity);
        tvStatus    = findViewById(R.id.tvStatus);
        tvWind      = findViewById(R.id.tvSpeed);
        tvDay       = findViewById(R.id.tvDay);
        tvCloudsmall= findViewById(R.id.tvCloud);
        imgIcon     = findViewById(R.id.imageIcon);


        infor   = findViewById(R.id.menuInfor);
        sensor  = findViewById(R.id.sensor);
        weather = findViewById(R.id.imgWeather);
        cctv    = findViewById(R.id.cctv);

        texthome        = findViewById(R.id.textHome);
        textsplash      = findViewById(R.id.textSplash);
        menus           = findViewById(R.id.menus);
        relativeLayout  = findViewById(R.id.nextday);


        textsplash.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(300);
        texthome.startAnimation(frombottom);

        broadcastReceiver = new BroadcastReceiver();

        relativeLayout.setOnClickListener(v -> {
            String city = "Danang";
            Intent intent = new Intent(MainActivity3.this, Nextday.class);
            intent.putExtra("name", city);
            startActivity(intent);

        });
        infor.setOnClickListener(v -> showmenu2());

        sensor.setOnClickListener(v -> {
            Showmenu();
        });

        weather.setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.mqtt_esp32cam_viewer_full_version");
            if (intent != null) {
                startActivity(intent);//null pointer check in case package name was not found
            }else{
                Toast.makeText(MainActivity3.this, "There is no package", Toast.LENGTH_SHORT).show();
            }

        });

        cctv.setOnClickListener(v -> startActivity(new Intent(MainActivity3.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, 50);
        }

        registerForContextMenu(sensor);


        myStartService();
        alert.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);

                if (value.equals("ON")) {
                    Song song = new Song(R.raw.iphone);
                    sendNotification();
                    vibrator.vibrate(pattern,0);
                    Toast.makeText(MainActivity3.this,"You have Wanning",Toast.LENGTH_SHORT).show();
                    startMusic(song);
                }else {
                    vibrator.cancel();
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        GetCurrentWeatherData("Danang");

    }

    private void openChangePassDialog(int gravity){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog);

        Window window = dialog.getWindow();
        if (window == null)
        {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(Gravity.BOTTOM == gravity)
        {
            dialog.setCancelable(true);
        } else {
            dialog.setCancelable(true);
        }

        mData  = FirebaseDatabase.getInstance().getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference pass = database.getReference("Pass");

        TextView tvPass = dialog.findViewById(R.id.doorPass);
        EditText editText = dialog.findViewById(R.id.edtChangePass);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        pass.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                tvPass.setText("Door Password: " + value);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String password = editText.getText().toString();
            mData.child("Pass").setValue(password);
        });

        dialog.show();

        
    }

    private void startMusic(Song song) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), song.getResource());
        }
        mediaPlayer.start();
    }

    private void myStartService() {
        Song song = new Song(R.raw.iphone);
        Intent intent = new Intent(this, MyService.class);
        Bundle bundle= new Bundle();
        bundle.putSerializable("song", song );
        intent.putExtras(bundle);
        startService(intent);
    }

    private void myStopService() {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }


    private void sendNotification() {


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.fire);

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, BrowserView.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(getNotificationId(), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, com.example.myhomie.Notification.CHANNEL_ID)
                .setContentTitle("FIRE Warning!!")
                .setContentText("Abnormally high concentration of CO detected ")
                .setSmallIcon(R.drawable.notificationn)
                .setLargeIcon(bitmap)
                .setShowWhen(true)
                .setContentIntent(resultPendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(getNotificationId(), notification );
        }

    }

    private int getNotificationId () {
        return (int) new Date().getTime();
    }


    private void Showmenu(){
        PopupMenu popupMenu = new PopupMenu(this, sensor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_device, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.phongKhach:
                    Intent intent = new Intent(MainActivity3.this, Custom_Device.class);
                    startActivity(intent);
                    break;
                case R.id.phongBep:
                    Intent intent4 = new Intent(MainActivity3.this, Login_Activity.class);
                    startActivity(intent4);
                    break;
                case R.id.monitor:
                    Intent intent2 = new Intent(MainActivity3.this, MainActivity4.class);
                    startActivity(intent2);
                    break;
                case R.id.weather:
                    Intent intent1 = new Intent(MainActivity3.this, Weather.class);
                    startActivity(intent1);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showmenu2(){
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        PopupMenu popupMenu = new PopupMenu(this, infor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_demo, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.menuLogout:
                    myStopService();
                    mAth.signOut();
                    googleSignInClient.signOut();
                    LoginManager.getInstance().logOut();
                    Intent intent = new Intent(MainActivity3.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.menuChangePass:
                    openChangePassDialog(Gravity.CENTER);
                    break;
            }
            return false;
        });
        popupMenu.show();


    }
    public void GetCurrentWeatherData(String data){
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity3.this);
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+data+"&units=metric&appid=3ca18de60aae5c95c830000d31962c54";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String day = jsonObject.getString("dt");
                        String name = jsonObject.getString("name");
                        String name1 = "Turan";

                        if (name.equals(name1))
                        {
                            tvName.setText("Danang");

                        }else {
                            tvName.setText(name);

                        }


                        long l = Long.valueOf(day);
                        Date date = new Date(l*1000L);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm:ss");
                        String Day = simpleDateFormat.format(date);

                        tvDay.setText(Day);

                        JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
                        JSONObject jsonObjectWeather = jsonArrayWeather.getJSONObject(0);
                        String status = jsonObjectWeather.getString("main");
                        String icon = jsonObjectWeather.getString("icon");

                        Picasso.with(MainActivity3.this).load("http://openweathermap.org/img/wn/"+icon+"@2x.png").into(imgIcon);
                        tvStatus.setText(status);

                        JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                        String nhietdo = jsonObjectMain.getString("temp");
                        String doam = jsonObjectMain.getString("humidity");
                        Double a = Double.valueOf(nhietdo);
                        String Nhietdo = String.valueOf(a.intValue());
                        tvTemp.setText(Nhietdo+"°C");
                        tvHumid.setText(doam+"%");

                        JSONObject jsonObjectWind = jsonObject.getJSONObject("wind");
                        String gio = jsonObjectWind.getString("speed");
                        tvWind.setText(gio+"m/s");

                        JSONObject jsonObjecClouds = jsonObject.getJSONObject("clouds");
                        String may = jsonObjecClouds.getString("all");
                        tvCloudsmall.setText(may+"%");

                        JSONObject jsonObjectSys = jsonObject.getJSONObject("sys");
                        String  country = jsonObjectSys.getString("country");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {

                });

        requestQueue.add(stringRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
