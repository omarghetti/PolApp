package disco.unimib.it.polapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "MainActivity";

    private boolean isCameraOpen;

    SurfaceView cameraView;

    CameraSource cameraSource;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, String.valueOf(isCameraOpen));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView textView = (TextView) findViewById(R.id.textView2);

        final Button scanButton = (Button) findViewById(R.id.scan_button);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);

        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, detector)
                .setAutoFocusEnabled(true)
                .build();


        if(savedInstanceState!=null){
            if(savedInstanceState.getBoolean("cameraopen")==true){
                cameraView.setVisibility(View.VISIBLE);
                scanButton.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                isCameraOpen=true;

            }
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setVisibility(View.GONE);
                cameraView.setVisibility(View.VISIBLE);
                scanButton.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                isCameraOpen=true;
            }
        });

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 50);
                } else {
                    //start your camera
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }

        });


        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Codice rilevato: " + barcodes.valueAt(0).rawValue);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent detected = new Intent(MainActivity.this, DetectedActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("titolo", barcodes.valueAt(0).rawValue);
                            detected.putExtras(bundle);
                            startActivity(detected);
                        }
                    });
                    builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            textView.setVisibility(View.VISIBLE);
                            scanButton.setVisibility(View.VISIBLE);
                            cameraView.setVisibility(View.GONE);
                        }
                    });
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();
                            AlertDialog detected = builder.create();
                            detected.show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("cameraopen", isCameraOpen);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults){
        if(grantResults.length==1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            try {
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
}