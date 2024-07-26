package com.sandun.coco;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sandun.coco.R;
import com.sandun.coco.util.AlertUtil;
import com.sandun.coco.util.PasswordUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Register extends AppCompatActivity {

    EditText userName, userPhone, userEmail, userPassword, userRePassword;
    ImageView register_btn;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);
        userRePassword = findViewById(R.id.userRePassword);
        register_btn = findViewById(R.id.register_btn);

        TextView loginLink = findViewById(R.id.login_page);
        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
        });

        register_btn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        String rePassword = userRePassword.getText().toString().trim();
        String username = userName.getText().toString().trim();
        String userContact = userPhone.getText().toString().trim();

        if (!password.equalsIgnoreCase(rePassword)) {
            showAlert("Warning", "Passwords do not match");
        } else if (TextUtils.isEmpty(username)) {
            showAlert("Warning", "Enter Your Username");
        } else if (TextUtils.isEmpty(userContact)) {
            showAlert("Warning", "Enter Your Contact Number");
        } else if (!isValidSriLankanPhone(userContact)) {
            showAlert("Warning", "Enter a valid Contact Number");
        } else if (TextUtils.isEmpty(email) || !isValidEmail(email)) {
            showAlert("Warning", "Enter a valid Email");
        } else if (TextUtils.isEmpty(password)) {
            showAlert("Warning", "Enter Your Password");
        } else if (!PasswordUtil.isValidPassword(password)) {
            showAlert("Warning", "Password must contain at least 8 characters, including uppercase, lowercase, numbers, and special characters!");
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            FirebaseUser registeredUser = firebaseAuth.getCurrentUser();
                            if (registeredUser != null) {
                                String userId = registeredUser.getUid();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                                Map<String, String> userMap = new HashMap<>();
                                userMap.put("role", "user");
                                userMap.put("useremail", email);
                                userMap.put("username", username);
                                userMap.put("userphone", userContact);
                                String hashPassword = PasswordUtil.hashPassword(password);
                                userMap.put("userPassword", hashPassword);
                                String defaultUserImage = "https://firebasestorage.googleapis.com/v0/b/coco-6ce23.appspot.com/o/Users%2Favatar.png?alt=media&token=bc2629c1-e4a0-45e6-b37c-2ba09764f91a";
                                userMap.put("userImage", defaultUserImage);

                                databaseReference.setValue(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Intent intent = new Intent(Register.this, MainActivity.class);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            AlertUtil.showSweetAlertDialog(Register.this, "Warning", "Error adding user details to Database: " + e.getMessage(), SweetAlertDialog.WARNING_TYPE);
                                            Log.e("DatabaseError", "Error adding user details to Database: " + e.getMessage(), e);
                                        });
                            }
                        } else {
                            handleRegistrationFailure(task.getException());
                        }
                    });
        }

    }

    private void showAlert(String title, String message) {
        AlertUtil.showSweetAlertDialog(Register.this, title, message, SweetAlertDialog.WARNING_TYPE);
    }

    public boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    public boolean isValidSriLankanPhone(String phone) {
        String regex = "^07[0-9]{8}$";
        return phone.matches(regex);
    }

    private void handleRegistrationFailure(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            showAlert("Warning", "Email already exists!");
        } else {
            showAlert("Error", "Registration Failed: " + exception.getMessage());
        }
    }
}
