package com.cs5520.assignments.numad22su_team24_puddle.chatroom_fragments.dialogs;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.cs5520.assignments.numad22su_team24_puddle.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddNewEventDialog extends DialogFragment {
    private Button exitButton;
    private Toolbar toolbar;
    private String startingTime;
    private String endingTime;
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
    private AppCompatActivity parent;




    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    imageUri = data.getData();
                    Glide.with(getContext()).load(imageUri).into(banner);
                }
            }
    );


    /** the system calls this to get the dialogfragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_event_dialog, container, false);
        exitButton = view.findViewById(R.id.add_event_dialog_exit_button);
        title = view.findViewById(R.id.add_title_edit_text);
        description = view.findViewById(R.id.dialog_description_edit_text);
        banner = view.findViewById(R.id.selected_pud_img);
        startingDateTextView = view.findViewById(R.id.starting_date_text_view);
        endingDateTextView = view.findViewById(R.id.ending_date_text_view);
        toolbar = view.findViewById(R.id.add_event_toolbar);
        parent.setSupportActionBar(toolbar);
        initializeToolbar();
        initializeAllTextViewOnClicks(view);
        exitButton.setOnClickListener(v -> dismiss());
        view.findViewById(R.id.add_banner).setOnClickListener(v -> {
            Intent gallery = new Intent();
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            gallery.setType("image/*");
            startActivityForResult.launch(gallery);
        });
        view.findViewById(R.id.add_event_save_button).setOnClickListener(v->{
            Bundle result = new Bundle();
            // Will inherently not be null since there's an autogenerated time/date
            result.putString("starting_date",startingDate);
            result.putString("ending_date",endingDate);
            result.putString("starting_time",startingTime);
            result.putString("ending_time",endingTime);
            // Title is required
           if (title.getEditText().getText() == null){
                Toast.makeText(getContext(),"Please Enter a title!", Toast.LENGTH_SHORT).show();
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
            } else{
                result.putString("image_uri", "load_default_image");
            }
            getParentFragmentManager().setFragmentResult("event_creation_result", result);
            dismiss();
        });
        return view;
    }

    private void initializeToolbar(){
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setTitle("Add New Event");
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });
    }

    public void uploadToFirebase(Uri uri){
        StorageReference ref = storeRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        ref.putFile(uri).addOnSuccessListener(taskSnapshot
                -> ref.getDownloadUrl().addOnSuccessListener(uri1 -> {
            String imgUrl = uri1.toString();
            imgRef.setValue(imgUrl);
        })).addOnProgressListener(snapshot -> {

        }).addOnFailureListener(e -> {

        });
    }

    public String getFileExtension(Uri muri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(muri));
    }



    private void initializeAllTextViewOnClicks(View view){
        TextView startingTimeView = view.findViewById(R.id.starting_time_text_view);
        TextView endingTimeView = view.findViewById(R.id.ending_time_text_view);
        TextView startingDateView = view.findViewById(R.id.starting_date_text_view);
        TextView endingDateView = view.findViewById(R.id.ending_date_text_view);
        String[] initalTime = DateTimeFormatUtil.formatPresetTime(java.time.LocalTime.now().toString());
        startingDate = java.time.LocalDate.now().toString();
        endingDate = java.time.LocalDate.now().toString();
        startingTime = initalTime[0];
        endingTime = initalTime[1];
        String date = DateTimeFormatUtil.formatEventDate(java.time.LocalDate.now().toString());
        startingTimeView.setText(initalTime[0]);
        endingTimeView.setText(initalTime[1]);
        startingDateView.setText(date);
        endingDateView.setText(date);
        startingDateView.setOnClickListener(v -> showStartingDatePickerDialog());
        endingDateView.setOnClickListener(v -> showEndingDatePickerDialog());
        startingTimeView.setOnClickListener(this::showTimePickerDialog);
        endingTimeView.setOnClickListener(this::showTimePickerDialog);
    }

    /** The system calls this only when creating the layout in a dialog. */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void showStartingDatePickerDialog() {
        EventCalendarStartingDatePickerDialog newFragment = new EventCalendarStartingDatePickerDialog();
        getParentFragmentManager().setFragmentResultListener("starting_date",this,((requestKey, result) -> {
                    String date = result.getString("date");
                    try {
                        if (balanceEndingPickerDates(date)){
                            startingDate = date;
                            endingDate = date;
                            startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                        }
                        else{
                            startingDate = date;
                            startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
        }));
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    public void showEndingDatePickerDialog(){
        EventCalendarEndingDatePickerDialog newFragment = new EventCalendarEndingDatePickerDialog();
        getParentFragmentManager().setFragmentResultListener("ending_date",this,((requestKey, result) -> {
            String date = result.getString("date");
            try {
                // Check if the ending date is before the starting date
                if (balanceStartPickerDates(date)) {
                    // If it is, set them to be the same date
                    startingDate = date;
                    endingDate = date;
                    endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                }
                else{
                    // Otherwise just proceed as normal
                    endingDate = date;
                    endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }));
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        EventTimePickerDialog newFragment = new EventTimePickerDialog();
        newFragment.acceptViews((TextView) v, this);
        newFragment.show(getParentFragmentManager(), "timePicker");
    }

    public boolean balanceStartPickerDates(String date) throws ParseException {
        Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        Date startingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.startingDate);
        if (newDate != null && newDate.before(startingDate)) {
            Log.d("date",date);
            this.startingDate = date;
            startingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
            return true;
        }
        return false;
    }

    public Boolean balanceEndingPickerDates(String date) throws ParseException{
        Date newDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        Date endingDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.endingDate);
        if (newDate != null && newDate.after(endingDate)) {
            Log.d("date",date);
            this.endingDate = date;
            endingDateTextView.setText(DateTimeFormatUtil.formatEventDate(date));
            return true;
        }
        return false;
    }

//    public boolean balanceStartPickerTimes(int hours, int minutes){
//        if (startingDate.equals(endingDate)){
//
//        }
//    }

    public void acceptParent(AppCompatActivity activity){
        parent = activity;
    }
    public void acceptPickerStartTime(String startingTime){
        this.startingTime = startingTime;
    }

    public void acceptPickerEndingTime(String endingTime){
        this.endingTime = endingTime;
    }
}