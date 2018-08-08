package com.technohack.barcodereaderdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.barcodeId)
    SurfaceView mSurfaceView;
    @BindView(R.id.textViewId)
    TextView mTextView;

    final int CameraRequestCode = 100;

    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CameraRequestCode:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {

                        cameraSource.start(mSurfaceView.getHolder());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //to define the barcode and the type of the barcode in this case we have taken the
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)                   //it will allow to read all type of barcodes
                .build();


        //set the size of the image and sync the camera with barcode detector
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(600, 480)
                .build();


        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    //ask for permission to use the Camera
                    //it only works after the marshmallow version

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},CameraRequestCode);

                    return;
                }

                try {

                    //start the camera so that it can read the QR-code
                    cameraSource.start(holder);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                //stop the camera
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> qrCodes=detections.getDetectedItems();

                if(qrCodes.size() !=0)
                {
                    Log.d("Barcode:","Detected Succesfully!!!");

                    mTextView.post(new Runnable() {
                        @Override
                        public void run() {

                            //when barcode read by the barcode reader then it will automatically vibrate the mobile for a second

                            Vibrator vibrator= (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

                            vibrator.vibrate(1000);

                            mTextView.setText(qrCodes.valueAt(0).displayValue);

                        }
                    });
                }
            }
        });



    }
}
