package com.sandun.coco;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandun.coco.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Prediction extends AppCompatActivity {

    ImageView DiseaseView, homeICon;
    TextView result, textView4, textTreatment, resultTreatment;
    ImageButton upload_btn, camera_btn;
    int imageSize = 224;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        result = findViewById(R.id.result);
        textView4 = findViewById(R.id.textView4);
        textTreatment = findViewById(R.id.textTreatment);
        resultTreatment = findViewById(R.id.resultTreatment);
        DiseaseView = findViewById(R.id.DiseaseView);
        camera_btn = findViewById(R.id.camera_btn);
        upload_btn = findViewById(R.id.upload_btn);
        homeICon = findViewById(R.id.homeICon);

        result.setVisibility(View.GONE);
        textView4.setVisibility(View.GONE);
        textTreatment.setVisibility(View.GONE);
        resultTreatment.setVisibility(View.GONE);

        databaseReference = FirebaseDatabase.getInstance().getReference("Diseases");

        homeICon.setOnClickListener(v -> {
            Intent intent = new Intent(Prediction.this, Home.class);
            startActivity(intent);
        });

        camera_btn.setOnClickListener(view -> {
            // Launch camera if we have permission
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 1);
            } else {
                //Request camera permission if we don't have it.
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    public void uploadImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 2);  // Using request code 2 for gallery
    }

    public void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val >> 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {
                    "Caterpillars",
                    "Drying of Leaflets",
                    "Flaccidity",
                    "Leaflets",
                    "Yellowing",
            };
            String predictedDisease = classes[maxPos];
            result.setText(predictedDisease);
            // Fetch treatment information from Firebase
            fetchDiseaseTreatment(predictedDisease);
            showPredictionResults();
            model.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void fetchDiseaseTreatment(String diseaseName) {
        Log.d("fetchDiseaseTreatment", "Fetching treatment for disease: " + diseaseName);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean diseaseFound = false;
                for (DataSnapshot diseaseSnapshot : dataSnapshot.getChildren()) {
                    String name = diseaseSnapshot.child("diseaseName").getValue(String.class);
                    if (name != null && name.equals(diseaseName)) {
                        String treatment = diseaseSnapshot.child("diseaseTreatement").getValue(String.class);
                        if (treatment != null) {
                            resultTreatment.setText(treatment);
                            diseaseFound = true;
                        } else {
                            resultTreatment.setText("No treatment information available.");
                        }
                        break;
                    }
                }
                if (!diseaseFound) {
                    Log.d("fetchDiseaseTreatment", "Disease not found: " + diseaseName);
                    resultTreatment.setText("No treatment information available.");
                }
                textTreatment.setVisibility(View.VISIBLE);
                resultTreatment.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("fetchDiseaseTreatment", "Database error", databaseError.toException());
                resultTreatment.setText("No treatment information available.");
            }
        });
    }



    private void showPredictionResults() {
        textView4.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);
        textTreatment.setVisibility(View.VISIBLE);
        resultTreatment.setVisibility(View.VISIBLE);
    }

    public void classifyImageWithLoading(Bitmap image) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Processing...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Delay for 2 seconds, then classify image and hide the loading dialog
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            classifyImage(image);
            loadingDialog.dismiss(); // Hide the loading dialog after classification
        }, 2000); // 2 seconds delay
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            DiseaseView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImageWithLoading(image);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                DiseaseView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImageWithLoading(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Redirect to Users_list activity
            startActivity(new Intent(Prediction.this, Home.class));
            finish(); // Close current activity
            return true; // Consume the event
        }
        return super.onKeyUp(keyCode, event);
    }

}