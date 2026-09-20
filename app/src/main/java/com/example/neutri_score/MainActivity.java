package com.example.neutri_score;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView btnHeaderUser;
    private TextView tvScanStatusMsg;
    private TextView btnSimulateScan;
    private TextView btnUploadLabel;
    private EditText etIngredientProductName;
    private EditText etRawIngredients;
    private TextView btnAnalyzeIngredients;
    private TextView btnClearIngredients;

    private EditText etSearchProductName;
    private TextView btnSearchProductByName;

    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── ActivityResultLaunchers ───────────────────────────────────────────────

    private final ActivityResultLauncher<Intent> barcodeScanLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String barcodeValue = result.getData()
                                    .getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_VALUE);
                            onBarcodeScanned(barcodeValue);
                        } else {
                            tvScanStatusMsg.setText("READY TO SCAN");
                        }
                    });

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            onImagePicked(uri);
                        } else {
                            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupScannerControls();
        setupProductSearchControls();
        setupIngredientDecoder();

        BottomNavHelper.setupBottomNav(this, "SCANNER");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bgExecutor.shutdown();
    }

    private void initViews() {
        btnHeaderUser        = findViewById(R.id.btnHeaderUser);
        tvScanStatusMsg      = findViewById(R.id.tvScanStatusMsg);
        btnSimulateScan      = findViewById(R.id.btnSimulateScan);
        btnUploadLabel       = findViewById(R.id.btnUploadLabel);
        etSearchProductName  = findViewById(R.id.etSearchProductName);
        btnSearchProductByName = findViewById(R.id.btnSearchProductByName);
        etIngredientProductName = findViewById(R.id.etIngredientProductName);
        etRawIngredients     = findViewById(R.id.etRawIngredients);
        btnAnalyzeIngredients = findViewById(R.id.btnAnalyzeIngredients);
        btnClearIngredients  = findViewById(R.id.btnClearIngredients);

        NeoBrutalistUtils.applyTactileFeedback(btnHeaderUser);
        NeoBrutalistUtils.applyTactileFeedback(btnSimulateScan);
        NeoBrutalistUtils.applyTactileFeedback(btnUploadLabel);
        if (btnSearchProductByName != null) {
            NeoBrutalistUtils.applyTactileFeedback(btnSearchProductByName);
        }
        NeoBrutalistUtils.applyTactileFeedback(btnAnalyzeIngredients);
        NeoBrutalistUtils.applyTactileFeedback(btnClearIngredients);

        btnHeaderUser.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void launchAnalysis(ProductModel product) {
        Intent intent = new Intent(this, ProductAnalysisActivity.class);
        intent.putExtra("PRODUCT_MODEL", product);
        startActivity(intent);
    }

    private void setupScannerControls() {
        btnSimulateScan.setOnClickListener(v -> {
            tvScanStatusMsg.setText("OPENING CAMERA...");
            barcodeScanLauncher.launch(new Intent(this, BarcodeScannerActivity.class));
        });

        btnUploadLabel.setOnClickListener(v -> {
            startActivity(new Intent(this, LabelOcrScannerActivity.class));
        });
    }

    private void showProductNotFoundDialog(String barcode) {
        new AlertDialog.Builder(this)
                .setTitle("Product Unlisted (" + barcode + ")")
                .setMessage("Barcode \"" + barcode + "\" is not registered in online databases.\n\n"
                        + "Tap 'SCAN PACKAGE LABEL (OCR)' to point your camera at the back label for live instant pack analysis!")
                .setPositiveButton("SCAN PACKAGE LABEL (OCR)", (d, w) -> {
                    startActivity(new Intent(this, LabelOcrScannerActivity.class));
                })
                .setNeutralButton("Search By Name", (d, w) -> {
                    if (etSearchProductName != null) {
                        etSearchProductName.requestFocus();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Barcode → API lookup
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called after camera returns a barcode/QR code value.
     * Checks local presets first (instant), then hits Open Food Facts API.
     */
    private void onBarcodeScanned(String barcodeValue) {
        tvScanStatusMsg.setText("DECODING...");

        // Fast path: local preset barcodes
        String localKey = localKeyForBarcode(barcodeValue);
        if (localKey != null) {
            tvScanStatusMsg.setText("READY TO SCAN");
            ProductModel product = ProductRepository.getInstance().getProduct(localKey);
            ProductRepository.getInstance().addScanLog(product);
            launchAnalysis(product);
            return;
        }

        // Slow path: fetch from Open Food Facts API
        fetchProductFromApi(barcodeValue);
    }

    /**
     * Runs an async API fetch with a loading dialog.
     * Shows "Not Found" if the barcode isn't in Open Food Facts.
     */
    private void fetchProductFromApi(String barcode) {
        // Show loading dialog with multi-API indicator
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null))
                .setMessage("Searching Open Food Facts & UPC databases…\nBarcode: " + barcode)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        btnSimulateScan.setEnabled(false);

        bgExecutor.execute(() -> {
            ProductModel product = OpenFoodFactsService.fetchProduct(barcode);

            mainHandler.post(() -> {
                loadingDialog.dismiss();
                btnSimulateScan.setEnabled(true);
                tvScanStatusMsg.setText("READY TO SCAN");

                if (product != null) {
                    // Found — log and open analysis
                    ProductRepository.getInstance().addScanLog(product);
                    launchAnalysis(product);
                } else {
                    // Not found in any API — prompt for ingredient input
                    showProductNotFoundDialog(barcode);
                }
            });
        });
    }

    private void setupProductSearchControls() {
        if (btnSearchProductByName == null || etSearchProductName == null) return;

        btnSearchProductByName.setOnClickListener(v -> {
            String query = etSearchProductName.getText().toString().trim();
            if (TextUtils.isEmpty(query)) {
                Toast.makeText(this, "Enter a product or brand name (e.g. Bingo Mad Angles)", Toast.LENGTH_SHORT).show();
                return;
            }

            performLiveSearch(query);
        });
    }

    private void performLiveSearch(String query) {
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null))
                .setMessage("Searching FMCG food databases for:\n\"" + query + "”…")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        bgExecutor.execute(() -> {
            ProductModel product = OpenFoodFactsService.fetchProductByQuery(query);

            mainHandler.post(() -> {
                loadingDialog.dismiss();
                if (product != null) {
                    ProductRepository.getInstance().addScanLog(product);
                    launchAnalysis(product);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("No Match Found")
                            .setMessage("No database entry was found matching \"" + query + "\".\n\n"
                                    + "You can paste the ingredient list from the back of the package into the DECODE INGREDIENTS box below for an instant FSSAI health score!")
                            .setPositiveButton("Paste Ingredients", (d, w) -> {
                                etRawIngredients.requestFocus();
                            })
                            .setNegativeButton("Close", null)
                            .show();
                }
            });
        });
    }

    /** Maps a small set of known Indian product barcodes to local preset keys. */
    private String localKeyForBarcode(String barcode) {
        if (barcode == null) return null;
        String clean = barcode.trim();
        switch (clean) {
            case "8901110510398": return "lays";
            case "8901725130441": return "kurkure";
            case "8901058000842": return "maggi";
            case "3948764032301":
            case "03948764032301":
            case "5449000000996":
            case "5449000014528": return "sprite";
            default:             return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image picker (Upload Label)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Image picked from gallery.
     * AWS Rekognition OCR will extract ingredients from the image in a future sprint.
     * For now, prompts user to paste ingredients manually.
     */
    private void onImagePicked(Uri imageUri) {
        new AlertDialog.Builder(this)
                .setTitle("Label Image Received")
                .setMessage("Image selected successfully!\n\n"
                        + "OCR ingredient extraction via AWS Rekognition is coming soon.\n\n"
                        + "For now, please manually paste the ingredient list in the "
                        + "DECODE INGREDIENTS box below.")
                .setPositiveButton("Got it", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ingredient Decoder
    // ─────────────────────────────────────────────────────────────────────────

    private void setupIngredientDecoder() {
        btnAnalyzeIngredients.setOnClickListener(v -> {
            String input = etRawIngredients.getText().toString().trim();
            String customName = etIngredientProductName != null ? etIngredientProductName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "Please paste ingredients text first!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProductModel decodedProduct = ProductRepository.getInstance().analyzeRawIngredients(customName, input);
            launchAnalysis(decodedProduct);
        });

        btnClearIngredients.setOnClickListener(v -> {
            if (etIngredientProductName != null) etIngredientProductName.setText("");
            etRawIngredients.setText("");
            Toast.makeText(this, "Input Cleared", Toast.LENGTH_SHORT).show();
        });
    }
}