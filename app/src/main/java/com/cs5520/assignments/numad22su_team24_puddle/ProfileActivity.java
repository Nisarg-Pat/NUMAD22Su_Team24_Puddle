package com.cs5520.assignments.numad22su_team24_puddle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.cs5520.assignments.numad22su_team24_puddle.Utils.FirebaseDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    ShapeableImageView profileIcon;
    TextInputEditText displayET, usernameET, bioET, phoneNumberET;
    Button saveBtn;
    Uri imageUri;

    DatabaseReference imgRef;
    StorageReference storeRef;

    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    imageUri = data.getData();
                    Log.d("here",imageUri.toString());
                    profileIcon.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgRef = FirebaseDB.getDataReference("images");
        storeRef = FirebaseDB.storageRef;

        profileIcon = findViewById(R.id.profile_user_icon);
        displayET = findViewById(R.id.profile_display_name_et);
        usernameET = findViewById(R.id.profile_username_et);
        bioET = findViewById(R.id.profile_description_et);
        phoneNumberET = findViewById(R.id.profile_phone_number_et);
        saveBtn = findViewById(R.id.profile_save_btn);

        saveBtn.setOnClickListener(v -> saveBtnClick());
        profileIcon.setOnClickListener(v -> setProfileImage());

        fillDetails();

    }

    private void setProfileImage() {
            Intent gallery = new Intent();
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            gallery.setType("image/*");
            startActivityForResult.launch(gallery);
    }

    private void fillDetails() {
        displayET.setText(FirebaseDB.currentUser.getDisplay_name());
        usernameET.setText(FirebaseDB.currentUser.getUsername());
        bioET.setText(FirebaseDB.currentUser.getBio());
        phoneNumberET.setText(FirebaseDB.currentUser.getPhone_number());
    }

    private void saveBtnClick() {
        DatabaseReference ref = FirebaseDB.getDataReference("Users").child(FirebaseDB.currentUser.getId());

        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("username", usernameET.getText().toString());
        hashMap.put("id", FirebaseDB.currentUser.getId());
        hashMap.put("username", FirebaseDB.currentUser.getUsername());
        hashMap.put("password", FirebaseDB.currentUser.getPassword());
        hashMap.put("email", FirebaseDB.currentUser.getEmail());
        hashMap.put("display_name", displayET.getText().toString());
        hashMap.put("bio", bioET.getText().toString());
        hashMap.put("phone_number", phoneNumberET.getText().toString());
        hashMap.put("profile_icon", "");

        ref.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseDB.currentUser.setDisplay_name(displayET.getText().toString());
                    FirebaseDB.currentUser.setBio(bioET.getText().toString());
                    FirebaseDB.currentUser.setPhone_number(phoneNumberET.getText().toString());
                    FirebaseDB.currentUser.setProfile_icon("");
                    Intent intent = new Intent(ProfileActivity.this, PuddleListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Error registering user!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void uploadToFirebase(Uri uri) {
        StorageReference ref = storeRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imgUrl = uri.toString();
                        imgRef.setValue(imgUrl);
                    }
                });
                Toast.makeText(ProfileActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public String getFileExtension(Uri muri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile_menu_logout) {
            FirebaseDB.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}