package com.ruhul.facedetectmlkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.media.Image;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.mlkit.vision.common.InputImage;

public class FrameImageAnalyzer implements ImageAnalysis.Analyzer {

    FirebaseVisionFaceDetector detector;
    Context context;
    RelativeLayout relativeLayout;
    GraphicOverlay graphicOverlay;
    TextView textView;

    private int faceId;

    public FrameImageAnalyzer(Context context, RelativeLayout relativeLayout, GraphicOverlay graphicOverlay, TextView textView) {

        this.context = context;
        this.relativeLayout = relativeLayout;
        this.graphicOverlay = graphicOverlay;
        this.textView = textView;
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        Image mediaImage = imageProxy.getImage();
        int rotation = degreesToFirebaseRotation(imageProxy.getImageInfo().getRotationDegrees());
        assert mediaImage != null;
        FirebaseVisionImage image =
                FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

        FirebaseVisionFaceDetectorOptions options
                = new FirebaseVisionFaceDetectorOptions.Builder().setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.2f)
                .enableTracking()
                .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionFaces -> {

                            if (!firebaseVisionFaces.isEmpty()) {

                                graphicOverlay.refreshDrawableState();
                                graphicOverlay.clear();

                                textView.setText("Face detected");


                                for (int i = 0; i < firebaseVisionFaces.size(); i++) {

                                    FirebaseVisionFace firebaseVisionFace = firebaseVisionFaces.get(i);
                                    FirebaseVisionPoint firebaseVisionPoint = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE).getPosition();
                                    FirebaseVisionPoint firebaseVisionPoint1 = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM).getPosition();

                                    double x = Math.pow(firebaseVisionPoint1.getX() - firebaseVisionPoint.getX(), 2);
                                    double y = Math.pow(firebaseVisionPoint1.getY() - firebaseVisionPoint.getY(), 2);


                                    if (firebaseVisionFace.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                        faceId = firebaseVisionFace.getTrackingId();
                                    }


                                    if (Math.sqrt(x + y) < ((0.111) * (firebaseVisionFace.getBoundingBox().width() * 1.5))) {
                                        textView.setText("Looking down: Face Id " + faceId);

                                        Rect rect = firebaseVisionFace.getBoundingBox();
                                        ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);
                                        graphicOverlay.add(reactOverlay);

                                    }

                                    if (firebaseVisionFace.getHeadEulerAngleY() < -20) {
                                        textView.setText("Left cheek: Face Id " + faceId);

                                        Rect rect = firebaseVisionFace.getBoundingBox();
                                        ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);
                                        graphicOverlay.add(reactOverlay);

                                    }

                                    if (firebaseVisionFace.getHeadEulerAngleY() > 20) {
                                        textView.setText("Right cheek Face Id " + faceId);

                                        Rect rect = firebaseVisionFace.getBoundingBox();
                                        ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);
                                        graphicOverlay.add(reactOverlay);

                                    }

                                    if (firebaseVisionFace.getSmilingProbability() > 0.8) {

                                        textView.setText("Smiling Face Id : " + faceId);

                                        Rect rect = firebaseVisionFace.getBoundingBox();
                                        ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);
                                        graphicOverlay.add(reactOverlay);
                                    }

                                    Rect rect = firebaseVisionFace.getBoundingBox();
                                    ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);
                                    graphicOverlay.add(reactOverlay);


                                }

                            } else {

                                graphicOverlay.refreshDrawableState();
                                graphicOverlay.clear();

                            }

                            imageProxy.close();

                        }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imageProxy.close();

                    }

                });


    }

}
