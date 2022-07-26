package com.cs5520.assignments.numad22su_team24_puddle.chatroom_fragments.dialogs;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.cs5520.assignments.numad22su_team24_puddle.MapActivity;
import com.cs5520.assignments.numad22su_team24_puddle.PuddleListActivity;
import com.cs5520.assignments.numad22su_team24_puddle.R;
import com.cs5520.assignments.numad22su_team24_puddle.SelectLocation;
import com.cs5520.assignments.numad22su_team24_puddle.SettingsActivity;
import com.cs5520.assignments.numad22su_team24_puddle.Utils.FirebaseDB;
import com.cs5520.assignments.numad22su_team24_puddle.Utils.Util;
import com.cs5520.assignments.numad22su_team24_puddle.chatroom_fragments.MessageNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


public class AddNewEventDialog extends AppCompatActivity {
    private Toolbar toolbar;
    private int[] startingTime;
    private int[] endingTime;
    private String startingDate;
    private String endingDate;
    private TextInputLayout title;
    private TextInputLayout description;
    private Uri imageUri;
    private ImageView banner;
    private StorageReference storeRef;
    private DatabaseReference imgRef;
    private TextView startingDateTextView;
    private TextView endingDateTextView;
    private TextView startingTimeTextView;
    private TextView endingTimeTextView;
    private TextView addLocationTextView;
    private String selectedLocation;
    private MessageNotification notification;
    private ValueEventListener valueEventListener;
    private DatabaseReference userRef;
    private ArrayList<ValueEventListener> valueEventListeners = new ArrayList<>();
    private ArrayList<DatabaseReference> references = new ArrayList<>();


    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    imageUri = data.getData();
                    Glide.with(this).load(imageUri).into(banner);
                    uploadToFirebase(imageUri);
                }
            }
    );


    ActivityResultLauncher<Intent> startMapActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    Intent intent = result.getData();
                    if (intent.getStringExtra("selectedLocation") != null) {
                        selectedLocation = intent.getStringExtra("selectedLocation");
                        addLocationTextView.setText(selectedLocation);

                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** the system calls this to get the dialogfragment's layout, regardless
         of whether it's being displayed as a dialog or an embedded fragment. */
        setContentView(R.layout.add_event_dialog);
        title = findViewById(R.id.add_title_edit_text);
        description = findViewById(R.id.dialog_description_edit_text);
        banner = findViewById(R.id.selected_pud_img);
        startingDateTextView = findViewById(R.id.starting_date_text_view);
        endingDateTextView = findViewById(R.id.ending_date_text_view);
        startingTimeTextView = findViewById(R.id.starting_time_text_view);
        endingTimeTextView = findViewById(R.id.ending_time_text_view);
        toolbar = findViewById(R.id.add_event_toolbar);
        addLocationTextView = findViewById(R.id.add_location_text_view);
        addLocationTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectLocation.class);
            startMapActivityForResult.launch(intent);
        });
        setSupportActionBar(toolbar);
        initializeToolbar();
        uploadImageToFb();
        initializeAllTextViewOnClicks(savedInstanceState);
        findViewById(R.id.add_banner).setOnClickListener(v -> {
            Intent gallery = new Intent();
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            gallery.setType("image/*");
            startActivityForResult.launch(gallery);
        });
        notification = new MessageNotification(this);
        if (!Util.listener.isRegistered()){
            Util.listener.registerListener(notification);
        }
        findViewById(R.id.add_event_save_button).setOnClickListener(v -> {
            Intent intent_result = new Intent();
            Bundle result = new Bundle();
            // Will inherently not be null since there's an autogenerated time/date
            result.putString("starting_date", startingDate);
            result.putString("ending_date", endingDate);
            result.putString("starting_time", DateTimeFormatUtil.formatEventTime(startingTime[0],startingTime[1]));
            result.putString("ending_time", DateTimeFormatUtil.formatEventTime(endingTime[0],endingTime[1]));
            if (selectedLocation == null) {
                Toast.makeText(this, "Please enter a location!", Toast.LENGTH_SHORT).show();
                return;
            }
            result.putString("selected_location", selectedLocation);
            // Title is required
            if (title.getEditText().getText() == null || title.getEditText().getText().toString().equals("")) {
                Toast.makeText(this, "Please enter a title!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                result.putString("title", title.getEditText().getText().toString());
            }
            //
            if (description.getEditText().getText() != null) {
                result.putString("description", description.getEditText().getText().toString());
            }
            if (imageUri != null) {
                result.putString("image_uri", imageUri.toString());
            } else {
                result.putString("image_uri", "load_default_image");
            }
            intent_result.putExtras(result);
            setResult(RESULT_OK, intent_result);
            finish();
        });
    }

    private void initializeToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setTitle("Add New Event");
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    public void uploadImageToFb() {
        imgRef = FirebaseDB.getDataReference("images");
        imgRef.setValue("url");

        storeRef = FirebaseDB.storageRef;
    }

    public void uploadToFirebase(Uri uri) {
        StorageReference ref = storeRef.child(System.currentTimeMillis() + "." +
                getFileExtension(uri));
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("here",uri.toString());
                                imageUri = uri;
                            }
                        }).start();
                    }

        });
    }
    });
    }

    public String getFileExtension(Uri muri) {
        ContentResolver cr = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }

    private void initializeAllTextViewOnClicks(Bundle savedInstanceState) {
        TextView startingTimeView = findViewById(R.id.starting_time_text_view);
        TextView endingTimeView = findViewById(R.id.ending_time_text_view);
        TextView startingDateView = findViewById(R.id.starting_date_text_view);
        TextView endingDateView = findViewById(R.id.ending_date_text_view);
        if (savedInstanceState != null) {
            int startingHours = savedInstanceState.getInt("starting_hours");
            int startingMins =  savedInstanceState.getInt("starting_mins");
            int endingHours = savedInstanceState.getInt("ending_hours");
            int endingMins =  savedInstanceState.getInt("ending_mins");
            startingTime = new int[]{startingHours,startingMins};
            endingTime = new int[]{endingHours,endingMins};
            startingDate = savedInstanceState.getString("starting_date");
            endingDate = savedInstanceState.getString("ending_date");
            startingDateView.setText(DateTimeFormatUtil.formatEventDate(startingDate));
            endingDateView.setText(DateTimeFormatUtil.formatEventDate(endingDate));
            if (savedInstanceState.getString("image_uri") != null) {
                imageUri = Uri.parse(savedInstanceState.getString("image_uri"));
                Glide.with(this).load(imageUri).into(banner);
            }
        } else{
            int[][] initalTime = DateTimeFormatUtil.formatPresetTime(java.time.LocalTime.now().toString());
            startingTime = initalTime[0];
            endingTime = initalTime[1];
            startingDate = java.time.LocalDate.now().toString();
            endingDate = java.time.LocalDate.now().toString();
            String date = DateTimeFormatUtil.formatEventDate(java.time.LocalDate.now().toString());
            startingDateView.setText(date);
            endingDateView.setText(date);
        }
        startingTimeView.setText(DateTimeFormatUtil.formatEventTime(startingTime[0],startingTime[1]));
        endingTimeView.setText(DateTimeFormatUtil.formatEventTime(endingTime[0],endingTime[1]));
        startingDateView.setOnClickListener(v -> showStartingDatePickerDialog());
        endingDateView.setOnClickListener(v -> showEndingDatePickerDialog());
        startingTimeView.setOnClickListener(v-> showStartingTimePickerDialog());
        endingTimeView.setOnClickListener(v -> showEndingTimePickerDialog());
    }

    public void showStartingDatePickerDialog() {
        EventCalendarStartingDatePickerDialog newFragment = new EventCalendarStartingDatePickerDialog();
        getSupportFragmentManager().setFragmentResultListener("starting_date", this, ((requestKey, result) -> {
            String date = result.getString("date");
            try {
                if (balanceEndingPickerDates(date)) {
                    startingDate = date;
                    endingDate = date;
                    startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                } else {
                    startingDate = date;
                    startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }));
        newFragment.show(getSupportFragmentManager(), "startingDatePicker");
    }

    public void showEndingDatePickerDialog() {
        EventCalendarEndingDatePickerDialog newFragment = new EventCalendarEndingDatePickerDialog();
        getSupportFragmentManager().setFragmentResultListener("ending_date", this, ((requestKey, result) -> {
            String date = result.getString("date");
            try {
                // Check if the ending date is before the starting date
                if (balanceStartPickerDates(date)) {
                    // If it is, set them to be the same date
                    startingDate = date;
                    endingDate = date;
                    endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                } else {
                    // Otherwise just proceed as normal
                    endingDate = date;
                    endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }));
        newFragment.show(getSupportFragmentManager(), "endingDatePicker");
    }

    public void showStartingTimePickerDialog() {
        EventStartingTimePickerDialog newFragment = new EventStartingTimePickerDialog();
        getSupportFragmentManager().setFragmentResultListener("starting_time", this, ((requestKey, result) ->{
                    int hours = result.getInt("starting_hour");
                    int minutes = result.getInt("starting_minute");
                    try {
                        balanceStartingPickerTimes(hours,minutes);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    startingTime = new int[]{hours,minutes};
                            startingTimeTextView.setText(DateTimeFormatUtil.formatEventTime(startingTime[0],startingTime[1]));
                        }));
        newFragment.show(getSupportFragmentManager(), "startingTimePicker");
    }


    public void showEndingTimePickerDialog() {
        EventEndingTimePickerDialog newFragment = new EventEndingTimePickerDialog();
        getSupportFragmentManager().setFragmentResultListener("ending_time", this, ((requestKey, result) ->{
            int hours = result.getInt("ending_hour");
            int minutes = result.getInt("ending_minute");
            endingTime = new int[]{hours,minutes};
            endingTimeTextView.setText(DateTimeFormatUtil.formatEventTime(endingTime[0],endingTime[1]));
        }));
        newFragment.show(getSupportFragmentManager(), "endingTimePicker");
    }

    public void balanceStartingPickerTimes(int hours, int minutes) throws ParseException {
        Date endingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.endingDate);
        Date startingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.startingDate);
        Log.d("here",endingDate.toString());
        Log.d("here",startingDate.toString());
        // If the dates are the same
        if (Objects.requireNonNull(endingDate).equals(startingDate)) {
            if (hours >= endingTime[0]) {
                endingTime[0] = hours + 1 % 25;
                endingTime[1] = minutes;
            }
        }
        endingTimeTextView.setText(DateTimeFormatUtil.formatEventTime(endingTime[0],endingTime[1]));
    }

    public boolean balanceStartPickerDates(String date) throws ParseException {
        Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        Date startingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.startingDate);
        if (newDate != null && newDate.before(startingDate)) {
            Log.d("date", date);
            this.startingDate = date;
            startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
            return true;
        }
        return false;
    }

    public Boolean balanceEndingPickerDates(String date) throws ParseException {
        Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        Date endingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.endingDate);
        if (newDate != null && newDate.after(endingDate)) {
            Log.d("date", date);
            this.endingDate = date;
            endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
            return true;
        }
        return false;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("starting_hours",startingTime[0]);
        outState.putInt("starting_mins",startingTime[1]);
        outState.putInt("ending_hours",endingTime[0]);
        outState.putInt("ending_mins",endingTime[1]);
        outState.putString("starting_date",startingDate);
        outState.putString("ending_date",endingDate);
        outState.putString("selected_location", selectedLocation);
        if (imageUri != null) {
            outState.putString("image_uri", imageUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    public void navigateHome(MenuItem item) {
        Intent intent = new Intent(this, PuddleListActivity.class);
        startActivity(intent);
    }


    public void navigateToMap(MenuItem item) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    public void navigateToSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}