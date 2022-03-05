package com.example.myhomie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";
    private Mat mRgba;
    private Mat mGrey;

    private CameraBridgeViewBase mOpenCvCameraView;
    private int mCameraid = 0;
    private ImageView flip_camera;
    private face_Recognition face_Recognition;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG, "OpenCV is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new" + this.getClass());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        //if camera permission is not given it will ask it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

         TextView   fps = (TextView) findViewById(R.id.tvFps);

        mOpenCvCameraView= findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();

        flip_camera = findViewById(R.id.flip_camera);
        flip_camera.setOnClickListener(v -> swapCamera());

        DatabaseReference cua  = database.getReference("Cua");
        cua.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if (value.equals("ON")) {
                    fps.setText("Door: Open");

                }else if(value.equals("OFF")) {
                    fps.setText("Door: Close");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{
            int inputSize=96;
            face_Recognition=new face_Recognition(getAssets(),
                    CameraActivity.this,
                    "model_5.tflite",
                    inputSize);
        }
        catch (IOException e){
            e.printStackTrace();
            // if you get error
            Log.d("CameraActivity", "Model is not loaded");
        }
    }

    private void swapCamera() {

        mCameraid = mCameraid^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraid);
        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV initialization is done");
            Toast.makeText(this, "OpenCV initialization is done", Toast.LENGTH_SHORT).show();
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.d(TAG, "OpenCV is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();;
        }
    }

    public void onCameraViewStarted(int width, int height){
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrey = new Mat(height, width, CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGrey=inputFrame.gray();
        if (mCameraid == 1){
            Core.flip(mRgba,mRgba,1);
            Core.flip(mGrey,mGrey,1);
        }
        mRgba=face_Recognition.recognizeImage(mRgba);
        return mRgba;
    }
}