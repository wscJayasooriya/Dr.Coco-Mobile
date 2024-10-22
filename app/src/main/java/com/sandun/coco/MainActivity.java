package com.sandun.coco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sandun.coco.util.AlertUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    EditText login_email, login_password;

    ImageButton login_btn;

    TextView forgotPassword;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_btn);
        forgotPassword = findViewById(R.id.forgotPassword);
        firebaseAuth = FirebaseAuth.getInstance();

        TextView registerLink = findViewById(R.id.reigterLink);

        registerLink.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });

        forgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(intent);
        });

        login_btn.setOnClickListener(view -> {
            String email = login_email.getText().toString().trim();
            String password = login_password.getText().toString().trim();


            if (TextUtils.isEmpty(email)) {
                AlertUtil.showSweetAlertDialog(MainActivity.this, "Warning", "Enter Your Email", SweetAlertDialog.WARNING_TYPE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                AlertUtil.showSweetAlertDialog(MainActivity.this, "Warning", "Enter Your Password", SweetAlertDialog.WARNING_TYPE);
                return;
            }


            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            checkUserRole(currentUser.getUid());
                            SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                            pDialog.setTitleText("Login...");
                            pDialog.setCancelable(true);
                            pDialog.show();
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                // User does not exist
                                AlertUtil.showSweetAlertDialog(MainActivity.this, "Error", "User does not exist!", SweetAlertDialog.ERROR_TYPE);
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                // Incorrect password
                                AlertUtil.showSweetAlertDialog(MainActivity.this, "Error", "Incorrect email or password!", SweetAlertDialog.ERROR_TYPE);
                            } else {
                                // Other error
                                AlertUtil.showSweetAlertDialog(MainActivity.this, "Error", "Login Failed: " + exception.getMessage(), SweetAlertDialog.ERROR_TYPE);
                            }
                        }
                    });

        });
    }

    private void checkUserRole(String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);

                    if ("admin".equals(role)) {
                        startActivity(new Intent(MainActivity.this, Admin_Home.class));
                    } else if ("user".equals(role)) {
                        Intent homeIntent = new Intent(MainActivity.this, Home.class);
                        homeIntent.putExtra("userRole", role); // Passing the role to the next activity
                        startActivity(homeIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid role!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}