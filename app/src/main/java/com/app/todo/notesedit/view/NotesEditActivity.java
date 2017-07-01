package com.app.todo.notesedit.view;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.baseclass.BaseActivity;
import com.app.todo.localdatabase.DataBaseUtility;
import com.app.todo.model.NotesModel;
import com.app.todo.notesedit.presenter.NotesEditActivityPresenter;
import com.app.todo.notesedit.presenter.NotesEditActivityPresenterInterface;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class NotesEditActivity extends BaseActivity implements NotesEditActivityInterface,View.OnClickListener,
        ColorPickerDialogListener {
    private static final int DIALOG_ID = 0;
    AppCompatEditText titleEditText, contentEdtitext;
    AppCompatTextView reminderDatetextView,reminderTimetextView,lastComma,commaSeparator;
    AppCompatImageView backIcon, reminderIcon, saveIcon, colorpickIcon,notepinIcon;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    NotesModel notesModel;
    String str3, str4;
    int id;
    String color_pick;
    String uId;
    FirebaseAuth firebaseAuth;
    DatePickerDialog datePickerDialog;
    String getColor;
    LinearLayout linearLayout;
    String reminderDate;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private Calendar myCalendar;
    TimePickerDialog timePickerDialog;
    ProgressDialog progressDialog;
    NotesEditActivityPresenterInterface presenter;
    boolean isunPinned=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_notesadd);

        initView();
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
                setreminderTime();
            }
        };
        datePickerDialog = new DatePickerDialog(this, onDateSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String notes_title = bundle.getString(Constants.notes_titile);
            String notes_content = bundle.getString(Constants.notes_content);
            str3 = bundle.getString(Constants.notes_date);
            str4 = bundle.getString(Constants.notes_time);
            String reminder_date = bundle.getString(Constants.reminderDate);
            String reminder_time=bundle.getString(Constants.reminderTime);
            getColor = bundle.getString(Constants.colorKey);
            isunPinned=bundle.getBoolean(Constants.pinned);
            if (bundle.containsKey(Constants.id))
                id = (bundle.getInt(Constants.id));
            //String str5=getArguments().getString("id");
            titleEditText.setText(notes_title);
            contentEdtitext.setText(notes_content);
            if (getColor != null) {
                linearLayout.setBackgroundColor(Integer.parseInt(getColor));
            }
            if(isunPinned){
                notepinIcon.setImageResource(R.drawable.unpin);
            }

            //reminderDatetextView.setText(reminder_date);
        }
    }

    private void setreminderTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                reminderTimetextView.setText( selectedHour + ":" + selectedMinute);
                Toast.makeText(NotesEditActivity.this, getString(R.string.reminder_time_set) +reminderTimetextView.getText().toString(), Toast.LENGTH_SHORT).show();
                lastComma.setVisibility(View.VISIBLE);
            }
        }, hour, minute, true);//24hr time
        timePickerDialog.show();
    }

    @Override
    public void initView() {
        presenter = new NotesEditActivityPresenter(this, this);
        progressDialog = new ProgressDialog(this);
        myCalendar = Calendar.getInstance();
        linearLayout = (LinearLayout) findViewById(R.id.root_layout);
        firebaseAuth = FirebaseAuth.getInstance();
        uId = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(Constants.userdata);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        titleEditText = (AppCompatEditText) findViewById(R.id.title_editText);
        contentEdtitext = (AppCompatEditText) findViewById(R.id.content_editText);
        reminderDatetextView = (AppCompatTextView) findViewById(R.id.reminder_textView);
        reminderTimetextView= (AppCompatTextView) findViewById(R.id.reminderTime_textView);
        lastComma= (AppCompatTextView) findViewById(R.id.reminderTime_comma);
        commaSeparator= (AppCompatTextView) findViewById(R.id.comma);
        backIcon = (AppCompatImageView) findViewById(R.id.back_icon);
        saveIcon = (AppCompatImageView) findViewById(R.id.save_icon);
        reminderIcon = (AppCompatImageView) findViewById(R.id.reminder_icon);
        colorpickIcon = (AppCompatImageView) findViewById(R.id.color_pick_icon);
        notepinIcon= (AppCompatImageView) findViewById(R.id.note_push_pin_icon);
        setClicklistener();
    }

    @Override
    public void setClicklistener() {
        backIcon.setOnClickListener(this);
        saveIcon.setOnClickListener(this);
        reminderIcon.setOnClickListener(this);
        colorpickIcon.setOnClickListener(this);
        notepinIcon.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        saveNotes();
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_icon:
                //saveNotes();
                onBackPressed();
                supportFinishAfterTransition();
                //overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
                break;
            case R.id.save_icon:
                saveNotes();
                finish();
                break;
            case R.id.reminder_icon:
                datePickerDialog.show();
                break;
            case R.id.color_pick_icon:
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(DIALOG_ID)
                        .setColor(Color.BLACK)
                        .setShowAlphaSlider(true)
                        .show(this);
                break;
            case R.id.note_push_pin_icon:
                if(isunPinned) {
                    isunPinned = false;
                    Toast.makeText(this, "Note unpinned", Toast.LENGTH_SHORT).show();
                    notepinIcon.setImageResource(R.drawable.note_push_pin);

                }else {
                    isunPinned=true;
                    Toast.makeText(this, "Note pinned", Toast.LENGTH_SHORT).show();
                    notepinIcon.setImageResource(R.drawable.unpin);
                }


                break;

        }
    }

    private void saveNotes() {
        DataBaseUtility dataBaseUtility = new DataBaseUtility(this);
        notesModel = new NotesModel();
        notesModel.setTitle(titleEditText.getText().toString());
        notesModel.setContent(contentEdtitext.getText().toString());
        notesModel.setDate(str3);
        notesModel.setTime(str4);
        notesModel.setId(id);
        notesModel.setReminderDate(reminderDatetextView.getText().toString());
        notesModel.setReminderTime(reminderTimetextView.getText().toString());
        //notesModel.setIsPinned();
        notesModel.setColor(getColor);
        dataBaseUtility.updateNote(notesModel);
        try {
            databaseReference
                    .child(uId).child(str3).child(String.valueOf(notesModel.getId()))
                    .setValue(notesModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("Notes");
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        switch (dialogId) {
            case DIALOG_ID:
                getColor = String.valueOf(color);
                linearLayout.setBackgroundColor(color);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void updateLabel() {
        String myFormat = getString(R.string.month_year_format); //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        reminderDatetextView.setText(sdf.format(myCalendar.getTime()));
        Toast.makeText(this, getString(R.string.reminder_date_set) + reminderDatetextView.getText().toString(),
                Toast.LENGTH_SHORT).show();
        commaSeparator.setVisibility(View.VISIBLE);
        }

    @Override
    public void showDialog(String message) {
        if (!isFinishing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    @Override
    public void hideDialog() {
        if (!isFinishing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void noteEditSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noteEditFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

