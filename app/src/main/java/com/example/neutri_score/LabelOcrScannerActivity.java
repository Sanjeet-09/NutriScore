package com.example.neutri_score;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LabelOcrScannerActivity extends AppCompatActivity {

    private static final String TAG = "LabelOcrScanner";
    private static final int PERMISSION_CAMERA_REQUEST = 2001;

    private PreviewView ocrPreviewView;
    private TextView tvOcrStatusBadge;
    private TextView tvOcrLiveSnippet;
    private TextView btnBackOcr;
    private TextView btnToggleFlashOcr;
    private TextView btnCaptureLabelOcr;

    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;
    private Camera camera;
    private boolean isFlashOn = false;
    private String accumulatedOcrText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_label_ocr_scanner);

        initViews();
        cameraExecutor = Executors.newSingleThreadExecutor();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (checkCameraPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST
            );
        }
    }

    private void initViews() {
        ocrPreviewView      = findViewById(R.id.ocrPreviewView);
        tvOcrStatusBadge    = findViewById(R.id.tvOcrStatusBadge);
        tvOcrLiveSnippet    = findViewById(R.id.tvOcrLiveSnippet);
        btnBackOcr          = findViewById(R.id.btnBackOcr);
        btnToggleFlashOcr   = findViewById(R.id.btnToggleFlashOcr);
        btnCaptureLabelOcr  = findViewById(R.id.btnCaptureLabelOcr);

        NeoBrutalistUtils.applyTactileFeedback(btnBackOcr);
        NeoBrutalistUtils.applyTactileFeedback(btnToggleFlashOcr);
        NeoBrutalistUtils.applyTactileFeedback(btnCaptureLabelOcr);

        btnBackOcr.setOnClickListener(v -> finish());

        btnToggleFlashOcr.setOnClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isFlashOn = !isFlashOn;
                camera.getCameraControl().enableTorch(isFlashOn);
                btnToggleFlashOcr.setText(isFlashOn ? "FLASH ON" : "FLASH OFF");
            }
        });

        btnCaptureLabelOcr.setOnClickListener(v -> processFinalOcrResult());
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required for package label OCR scanner", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(ocrPreviewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e(TAG, "Camera initialization error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String text = visionText.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        accumulatedOcrText = text.trim();
                        runOnUiThread(() -> updateOcrUiSnippet(accumulatedOcrText));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "OCR recognition error: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void updateOcrUiSnippet(String text) {
        String ingredientsOnly = ProductRepository.getInstance().cleanOcrTextToIngredientsOnly(text);
        if (ingredientsOnly.isEmpty()) ingredientsOnly = text;

        String cleanSnippet = ingredientsOnly.replaceAll("\\s+", " ").trim();
        if (cleanSnippet.length() > 120) {
            cleanSnippet = cleanSnippet.substring(0, 120) + "…";
        }
        tvOcrLiveSnippet.setText("INGREDIENTS DETECTED:\n" + cleanSnippet);
        tvOcrStatusBadge.setText("INGREDIENT LABEL DETECTED");
    }

    private void processFinalOcrResult() {
        if (accumulatedOcrText == null || accumulatedOcrText.trim().isEmpty()) {
            Toast.makeText(this, "No text detected yet! Hold camera steady over the package label.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductModel model = ProductRepository.getInstance().analyzeRawIngredients(accumulatedOcrText);

        Intent intent = new Intent(this, ProductAnalysisActivity.class);
        intent.putExtra("PRODUCT_MODEL", model);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}
