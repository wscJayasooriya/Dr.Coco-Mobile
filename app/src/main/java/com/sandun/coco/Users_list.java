package com.sandun.coco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.sandun.coco.R;
import com.sandun.coco.model.UserData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Users_list extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView UserRecycleView;
    List<UserData> userdataList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;

    ImageView hamburgerIcon,homeICon;

    UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        UserRecycleView = findViewById(R.id.UserrecycleView);
        hamburgerIcon = findViewById(R.id.hamburgerIcon);
        homeICon = findViewById(R.id.homeIcon);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(Users_list.this, 1);
        UserRecycleView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(Users_list.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        userdataList = new ArrayList<>();

        UsersAdapter adapter = new UsersAdapter(Users_list.this, userdataList);
        UserRecycleView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userdataList.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    UserData dataClass = itemSnapshot.getValue(UserData.class);
                    dataClass.setKey(itemSnapshot.getKey());
                    userdataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                dialog.dismiss();
            }
        });

//        homeICon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Retrieve user role from SharedPreferences
//                SharedPreferences sharedPreferences = getSharedPreferences("user_role", MODE_PRIVATE);
//                String role = sharedPreferences.getString("role", "");
//
//                // Determine the activity to redirect based on the user's role
//                Class<?> targetActivity = (role != null && role.equals("admin")) ? Admin_Home.class : Home.class;
//
//                // Start the corresponding activity
//                startActivity(new Intent(Users_list.this, targetActivity));
//                finish(); // Close the current activity
//            }
//        });

//        hamburgerIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Users_list.this, Navigation.class);
//                startActivity(intent);
//            }
//        });


    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Retrieve user role from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_role", MODE_PRIVATE);
            String role = sharedPreferences.getString("role", "");

            // Determine the activity to redirect based on the user's role
            Class<?> targetActivity = (role != null && role.equals("admin")) ? Admin_Home.class : MainActivity.class;

            // Start the corresponding activity
            startActivity(new Intent(Users_list.this, targetActivity));
            finish(); // Close current activity
            return true; // Consume the event
        }
        return super.onKeyUp(keyCode, event);
    }
}