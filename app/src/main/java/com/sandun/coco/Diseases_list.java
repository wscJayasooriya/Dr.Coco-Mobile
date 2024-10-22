package com.sandun.coco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandun.coco.model.DiseaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Diseases_list extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<DiseaseData> dataList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    ImageView hamburgerIcon, homeICon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Diseases_view", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diseases_list);

        // Initialize UI components
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycleView);
        hamburgerIcon = findViewById(R.id.hamburgerIcon);

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_role", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "");

        // Check if the role is "user"
        if (role.equals("user")) {
            // Hide the FAB icon
            fab.setVisibility(View.GONE);
        }

        // Set up RecyclerView layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(Diseases_list.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Show progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Diseases_list.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize data list and adapter
        dataList = new ArrayList<>();
        DiseaseAdapter adapter = new DiseaseAdapter(Diseases_list.this, dataList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Diseases");

        // Retrieve data from Firebase and update RecyclerView
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DiseaseData dataClass = itemSnapshot.getValue(DiseaseData.class);
                    Objects.requireNonNull(dataClass).setKey(itemSnapshot.getKey());
                    dataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        // Set up onClick listeners
//        homeICon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Retrieve user role from SharedPreferences
//                SharedPreferences sharedPreferences = getSharedPreferences("user_role", MODE_PRIVATE);
//                String role = sharedPreferences.getString("role", "");
//
//                // Determine the activity to redirect based on the user's role
//                Class<?> targetActivity = role.equals("admin") ? Admin_Home.class : Home.class;
//
//                // Start the corresponding activity
//                startActivity(new Intent(Diseases_list.this, targetActivity));
//                finish(); // Close the current activity
//            }
//        });

        hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Diseases_list.this, Navigation.class);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Diseases_list.this, Diseases_Add.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Retrieve user role from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_role", MODE_PRIVATE);
            String role = sharedPreferences.getString("role", "");

            // Determine the activity to redirect based on the user's role
            Class<?> targetActivity = role.equals("admin") ? Admin_Home.class : Home.class;

            // Start the corresponding activity
            startActivity(new Intent(Diseases_list.this, targetActivity));
            finish(); // Close current activity
            return true; // Consume the event
        }
        return super.onKeyUp(keyCode, event);
    }
}
