package com.sandun.coco;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandun.coco.util.AlertUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPassword extends AppCompatActivity {

    EditText emailEditText;
    ImageButton resetLinkButton;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.login_email);
        resetLinkButton = findViewById(R.id.imageButton);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        resetLinkButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                // Handle empty email
                AlertUtil.showAlert(ForgotPassword.this,"Warning", "Please enter your email",SweetAlertDialog.WARNING_TYPE);
            } else {
                checkEmailInDatabase(email);
            }
        });
    }

    private void checkEmailInDatabase(final String email) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean emailFound = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userEmail = snapshot.child("useremail").getValue(String.class);
                    if (email.equals(userEmail)) {
                        emailFound = true;
                        // Proceed with password reset
                        sendPasswordResetEmail(email);
                        break;
                    }
                }
                if (!emailFound) {
                    Log.d("ForgotPassword", "Email not found in database or has no data");
                    AlertUtil.showAlert(ForgotPassword.this, "Warning", "Your Email is not found!",SweetAlertDialog.WARNING_TYPE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ForgotPassword", "Error getting data", databaseError.toException());
                AlertUtil.showAlert(ForgotPassword.this, "Error", "Failed to check email!",SweetAlertDialog.ERROR_TYPE);
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Inform the user that the password reset email was sent
                        AlertUtil.showAlert(ForgotPassword.this,"Success", "Password reset link sent! Check your mailbox",SweetAlertDialog.SUCCESS_TYPE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(ForgotPassword.this, MainActivity.class));
                                finish();
                            }
                        }, 3000);
                    } else {
                        // Inform the user if sending the reset email failed
                        AlertUtil.showAlert(ForgotPassword.this,"Error", "Failed to send reset email!", SweetAlertDialog.ERROR_TYPE);
                    }
                });
    }
}