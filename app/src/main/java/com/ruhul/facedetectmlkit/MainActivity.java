package com.ruhul.facedetectmlkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ruhul.facedetectmlkit.databinding.ActivityMainBinding;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;

import java.io.ByteArrayOutputStream;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 10;
    private AlertDialog alertDialog;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initAlertDialog();
        clickEvent();

        binding.cameraView.setFacing(CameraKit.Constants.FACING_FRONT);

    }

    private void clickEvent() {
        // got to Realtime Face Scan Activity
        binding.realtimeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RealtimeFaceActivity.class));

            }
        });

        binding.choseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UploadImgActivity.class));
            }
        });


        //Capture Image then Face Scan(Static)
        binding.faceDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.cameraView.start();
                binding.cameraView.captureImage();
                binding.graphicOverlay.clear();

                binding.cameraView.addCameraKitListener(new CameraKitEventListener() {
                    @Override
                    public void onEvent(CameraKitEvent cameraKitEvent) {

                    }

                    @Override
                    public void onError(CameraKitError cameraKitError) {

                    }

                    @Override
                    public void onImage(CameraKitImage cameraKitImage) {
                        alertDialog.show();
                        Bitmap bitmap = cameraKitImage.getBitmap();
                        bitmap = Bitmap.createScaledBitmap(bitmap, binding.cameraView.getWidth(), binding.cameraView.getHeight(), false);
                        binding.cameraView.stop();
                        processFaceImg(bitmap);
                    }

                    @Override
                    public void onVideo(CameraKitVideo cameraKitVideo) {
                    }
                });
            }
        });


    }

    private void initAlertDialog() {

        alertDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Detect Face Wait..")
                .setCancelable(false)
                .build();
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("ImgBit", temp);
        return temp;
    }

    private void processFaceImg(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions
                = new FirebaseVisionFaceDetectorOptions.Builder().build();
        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(firebaseVisionFaceDetectorOptions);
        firebaseVisionFaceDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(@NonNull List<FirebaseVisionFace> firebaseVisionFaces) {

                        getFaceResult(firebaseVisionFaces);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count = 0;
        for (FirebaseVisionFace face : firebaseVisionFaces) {
            Rect rect = face.getBoundingBox();
            ReactOverlay reactOverlay = new ReactOverlay(binding.graphicOverlay, rect);
            binding.graphicOverlay.add(reactOverlay);
            count = count + 1;
            if (String.valueOf(face.getTrackingId()) != null) {
                alertDialog.dismiss();
                Toast.makeText(this, "Face Detect Completed", Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar
                        .make(binding.getRoot(), "Face Detect Success: Id " + face.getTrackingId(), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.cameraView.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}