package com.ruhul.facedetectmlkit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.widget.RelativeLayout;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class RealtimeFaceActivity extends AppCompatActivity {
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_face);

        relativeLayout = findViewById(R.id.Relay);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        previewView = findViewById(R.id.viewFinder);

        //Camera2 api use face detection
        startScan();
    }

    private void startScan() {
        ListenableFuture<ProcessCameraProvider> val = ProcessCameraProvider.getInstance(this);
        val.addListener(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void run() {

                try {

                    ProcessCameraProvider processCameraProvider = val.get();
                    Preview preview = new Preview.Builder().setTargetResolution(new Size(900, 1200)).build();
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

                    ImageAnalysis imageAnalysis1 = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetResolution(new Size(900, 1200)).build();
                    imageAnalysis1.setAnalyzer(ContextCompat.getMainExecutor(RealtimeFaceActivity.this)
                            , new FrameImageAnalyzer(RealtimeFaceActivity.this, relativeLayout, graphicOverlay, findViewById(R.id.textId)));


                    processCameraProvider.unbindAll();

                    Camera camera = processCameraProvider.bindToLifecycle(RealtimeFaceActivity.this, cameraSelector, preview, imageCapture, imageAnalysis1);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(RealtimeFaceActivity.this));

    }
}