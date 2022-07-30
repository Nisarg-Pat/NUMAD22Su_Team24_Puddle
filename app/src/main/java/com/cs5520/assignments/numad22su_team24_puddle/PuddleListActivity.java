package com.cs5520.assignments.numad22su_team24_puddle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs5520.assignments.numad22su_team24_puddle.Adapter.MyPuddlesAdapter;
import com.cs5520.assignments.numad22su_team24_puddle.Adapter.PuddleListAdapter;
import com.cs5520.assignments.numad22su_team24_puddle.Utils.FirebaseDB;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PuddleListActivity extends AppCompatActivity implements View.OnClickListener {

    // Widgets
    RecyclerView puddleListRecyclerView;
    PuddleListAdapter puddleListAdapter;
    ShapeableImageView profileIcon;
    ImageView createIcon;
    ImageView navigationIcon;
    ImageView filterIcon;
    Button nearMeBtn, myPuddlesBtn;

    // Firebase
    FirebaseUser current_user;
    DatabaseReference userRef;

    private HashMap<String, String> userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puddle_list);
        userDetails = new HashMap<>();

        profileIcon = findViewById(R.id.puddle_list_header_profile_icon);
        profileIcon.setOnClickListener(this);
        nearMeBtn = findViewById(R.id.header_near_me_btn);
        nearMeBtn.setOnClickListener(this);
        myPuddlesBtn = findViewById(R.id.header_my_puddles_btn);
        myPuddlesBtn.setOnClickListener(this);
        createIcon = findViewById(R.id.header_create_puddle_icon);
        createIcon.setOnClickListener(this);
        filterIcon = findViewById(R.id.header_filter_icon);
        filterIcon.setOnClickListener(this);
        navigationIcon = findViewById(R.id.header_navigation_icon);
        navigationIcon.setOnClickListener(this);

        // Api Calls
        fetchCurrentUserData();

        // Initializing Widgets
        puddleListRecyclerView = findViewById(R.id.puddle_list_rv);

        updateRecyclerView(myPuddlesBtn);
    }

    private List<List<Puddle>> getPuddleList() {
        List<List<Puddle>> puddlesList = new ArrayList<>();
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.puddle);
        for (Category category : Category.values()) {
            List<Puddle> puddleArray = new ArrayList<>();
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddleArray.add(new Puddle("Number " + (category.id + 1), "Number " + (category.id + 1), bitmap));
            puddlesList.add(puddleArray);
        }
        return puddlesList;
    }

    public void fetchCurrentUserData() {
        current_user = FirebaseDB.getCurrentUser();

        userRef = FirebaseDB.getDataReference(getString(R.string.users)).child(current_user.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    userDetails.put(snap.getKey(), snap.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onClick(View view) {
        if (view.equals(profileIcon)) {

        } else if (view.equals(nearMeBtn) || view.equals(myPuddlesBtn)) {
            updateRecyclerView(view);
        } else if (view.equals(createIcon)) {

        } else if (view.equals(navigationIcon)) {

        } else if (view.equals(filterIcon)) {

        }
    }

    private void updateRecyclerView(View view) {
        // Initializing RecyclerView
        if (view.equals(nearMeBtn)) {
            puddleListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            puddleListRecyclerView.setAdapter(new PuddleListAdapter(this, getPuddleList()));
        } else {
            puddleListRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            puddleListRecyclerView.setAdapter(new MyPuddlesAdapter(this));
        }

    }
}