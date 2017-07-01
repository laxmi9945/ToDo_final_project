package com.app.todo.notesadd.view;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.baseclass.BaseActivity;
import com.app.todo.notesadd.presenter.NotesAddPresenter;
import com.app.todo.notesadd.presenter.NotesAddPresenterInterface;
import com.app.todo.todoMain.view.notificationManager.ScheduleClient;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class NotesAddActivity extends BaseActivity implements NotesAddActivityInterface,
        View.OnClickListener, ColorPickerDialogListener {
    private static final String TAG = "NetworkStateReceiver";
    private static final int DIALOG_ID = 0;
    LinearLayout linearLayout;
    NotesAddPresenterInterface presenter;
    String color_pick = "0";
    Toolbar toolbar;
    AppCompatImageView backIcon, reminderIcon, saveIcon, colorpickIcon,notepinIcon;
    ProgressDialog progressDialog;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    private AppCompatTextView timeTextView, dateTextView, reminderTextView,commaSeparator,lastComma,reminderTimetextView;
    private AppCompatEditText titleEdittext, contentEdittext;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private Date date;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    LinearLayout timeLayout;
    private ScheduleClient scheduleClient;
    int hour,minute,years,month,day;
    boolean isPinned=false;
    boolean isReminder=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_notesadd);
        //setTitle(getString(R.string.notes_add));
        firebaseAuth = FirebaseAuth.getInstance();

        initView();

        date = new Date();
        CharSequence sequence = DateFormat.format(getString(R.string.date_time), date.getTime());
        CharSequence sequence2 = DateFormat.format(getString(R.string.time), date.getTime());
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();
        dateTextView.setText(sequence);
        timeTextView.setText(sequence2);
        sharedPreferences = this.getSharedPreferences(Constants.keys, Context.MODE_PRIVATE);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                day=dayOfMonth;
                years=year;
                month=monthOfYear;
                updateLabel();
                setreminderTime();
                Log.i(TAG, "onDateSet: " + year + "    " + dayOfMonth);


            }
        };
        datePickerDialog = new DatePickerDialog(this, onDateSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    private void setreminderTime() {

        Calendar mcurrentTime = Calendar.getInstance();
         hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
         minute = mcurrentTime.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour=selectedHour;
                minute=selectedMinute;
                reminderTimetextView.setText( selectedHour + ":" + selectedMinute);
                isReminder=true;
                Toast.makeText(NotesAddActivity.this, getString(R.string.reminder_time_set) +reminderTimetextView.getText().toString(), Toast.LENGTH_SHORT).show();
                lastComma.setVisibility(View.VISIBLE);
            }
        }, hour, minute, true);//24hr time
       // scheduleClient.setAlarmForNotification(myCalendar);
        timePickerDialog.show();
    }

    @Override
    public void initView() {
        progressDialog = new ProgressDialog(this);
        presenter = new NotesAddPresenter(this, this);
        backIcon = (AppCompatImageView) findViewById(R.id.back_icon);
        saveIcon = (AppCompatImageView) findViewById(R.id.save_icon);
        reminderIcon = (AppCompatImageView) findViewById(R.id.reminder_icon);
        colorpickIcon = (AppCompatImageView) findViewById(R.id.color_pick_icon);
        linearLayout = (LinearLayout) findViewById(R.id.root_layout);
        myCalendar = Calendar.getInstance();
        timeLayout= (LinearLayout) findViewById(R.id.time_layout);
        dateTextView = (AppCompatTextView) findViewById(R.id.recenttime_textView);
        commaSeparator= (AppCompatTextView) findViewById(R.id.comma);
        lastComma= (AppCompatTextView) findViewById(R.id.reminderTime_comma);
        timeTextView = (AppCompatTextView) findViewById(R.id.time_textView);
        titleEdittext = (AppCompatEditText) findViewById(R.id.title_editText);
        reminderTextView = (AppCompatTextView) findViewById(R.id.reminder_textView);
        reminderTimetextView= (AppCompatTextView) findViewById(R.id.reminderTime_textView);
        contentEdittext = (AppCompatEditText) findViewById(R.id.content_editText);
        toolbar = (Toolbar) findViewById(R.id.notes_add_toolbar);
        notepinIcon= (AppCompatImageView) findViewById(R.id.note_push_pin_icon);
      // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setClicklistener();

    }

    @Override
    public void setClicklistener() {
        dateTextView.setOnClickListener(this);
        titleEdittext.setOnClickListener(this);
        contentEdittext.setOnClickListener(this);
        backIcon.setOnClickListener(this);
        saveIcon.setOnClickListener(this);
        reminderIcon.setOnClickListener(this);
        colorpickIcon.setOnClickListener(this);
        notepinIcon.setOnClickListener(this);

    }

    private void updateLabel() {
        String myFormat = getString(R.string.month_year_format); //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        reminderTextView.setText(sdf.format(myCalendar.getTime()));
        // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service
        Toast.makeText(this, getString(R.string.reminder_date_set) + reminderTextView.getText().toString(),
                Toast.LENGTH_SHORT).show();
        //scheduleClient.setAlarmForNotification(myCalendar);
        commaSeparator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_icon:
                //saveNotes();
                finish();
                //overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
                break;
            case R.id.save_icon:
                saveNotes();
                //startActivity(new Intent(this, NotesFragment.class));
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
                if(isPinned) {
                    isPinned = false;
                    Toast.makeText(this, "Note unpinned", Toast.LENGTH_SHORT).show();
                    notepinIcon.setImageResource(R.drawable.note_push_pin);
                }else {
                    isPinned=true;
                    Toast.makeText(this, "Note pinned", Toast.LENGTH_SHORT).show();
                    notepinIcon.setImageResource(R.drawable.unpin);
                }


                break;

        }
    }

    private void saveNotes() {

        //NotesModel notesModel=new NotesModel();
        //DataBaseUtility dataBaseUtility=new DataBaseUtility(this);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.currentTimeKey, timeTextView.getText().toString());
        bundle.putString(Constants.titleKey, titleEdittext.getText().toString());
        bundle.putString(Constants.descriptionKey, contentEdittext.getText().toString());
        bundle.putString(Constants.currentDateKey, dateTextView.getText().toString());
        bundle.putString(Constants.reminderDate, reminderTextView.getText().toString());
        bundle.putString(Constants.reminderTime,reminderTimetextView.getText().toString());
        bundle.putString(Constants.colorKey, color_pick);
        bundle.putBoolean(Constants.pinned,isPinned );
        presenter.addNoteToFirebase(bundle);
       // dataBaseUtility.addNotes(notesModel);
        Calendar calendar=Calendar.getInstance();
        calendar.set(years,month,day,hour,minute);
     //if(reminderTextView!=null && reminderTimetextView!=null) {
        if(isReminder) {
            scheduleClient.setAlarmForNotification(calendar, bundle);
        }
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
    public void noteAddSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noteAddFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        switch (dialogId) {
            case DIALOG_ID:

                color_pick = String.valueOf(color);
                linearLayout.setBackgroundColor(color);

                // We got result from the dialog that is shown when clicking on the icon in the action bar.
                /*Toast.makeText(this, "Selected Color: #" + Integer.toHexString(color),
                        Toast.LENGTH_SHORT).show();
*/
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onBackPressed() {
        saveNotes();
        super.onBackPressed();
    }
    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
    }

}