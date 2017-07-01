package com.app.todo.todoMain.view.fragment;


import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.localdatabase.DataBaseUtility;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.view.activity.TodoMainActivity;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

import static com.facebook.FacebookSdk.getApplicationContext;

public class NoteseditFragment extends Fragment implements NoteseditFragmentInterface {
    private static final String TAG = "NoteseditFragment";
    AppCompatEditText titleEditText, contentEdtitext;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    NotesModel notesModel;
    String uId;
    int id;
    Date date;
    String str3, str4;
    CharSequence sequence, sequence2;
    AppCompatImageButton backButton;
    AppCompatTextView reminderTextView;
    private Calendar myCalendar;
    Context mContext;
    private DatePickerDialog.OnDateSetListener datePicker;
    Toolbar toolbar;
    String  color_pick;
    LinearLayout linearLayout;
    public NoteseditFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TodoMainActivity) getActivity()).showOrHideFab(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Fabric.with(getActivity(), new Crashlytics());
        View view = inflater.inflate(R.layout.fragment_edit_notes, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("Notes edit");

        mContext = getActivity();
        reminderTextView = (AppCompatTextView) view.findViewById(R.id.reminderEdit_textView);
        //trash_toolbar= (Toolbar) view.findViewById(R.id.edit_toolbar);
        //for crate home button
        //AppCompatActivity activity = (AppCompatActivity) getActivity();
        ///activity.setSupportActionBar(trash_toolbar);
        //getActivity().findViewById(R.id.edit_toolbar);
        // ((TodoMainActivity) mContext).initMenuBar();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference().child(Constants.userdata);
        linearLayout= (LinearLayout) view.findViewById(R.id.update_layout);
       // backButton = (AppCompatImageButton) view.findViewById(R.id.back_button);
        titleEditText = (AppCompatEditText) view.findViewById(R.id.edit_title);
        contentEdtitext = (AppCompatEditText) view.findViewById(R.id.edit_content);
        uId = firebaseAuth.getCurrentUser().getUid();
        date = new Date();
        sequence = DateFormat.format(getString(R.string.date_time), date.getTime());
        sequence2 = DateFormat.format(getString(R.string.time), date.getTime());

        myCalendar = Calendar.getInstance();
        datePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

       /* dateTextView = (AppCompatTextView)view.findViewById(R.id.recenttime_textView);
        timeTextView = (AppCompatTextView)view.findViewById(R.id.time_textView);*/

//        backButton.setOnClickListener(this);
        Bundle bundle = getArguments();

        if (bundle != null) {
            String notes_title = getArguments().getString("title");
            String notes_content = getArguments().getString("content");
            str3 = getArguments().getString("date");
            str4 = getArguments().getString("time");
            String reminder_date=getArguments().getString("reminder");
            String color_layout=getArguments().getString("color");
            if (bundle.containsKey("id"))
                id = (bundle.getInt("id"));
            //String str5=getArguments().getString("id");

            titleEditText.setText(notes_title);
            contentEdtitext.setText(notes_content);

//            reminderTextView.setText(reminder_date);
            if(color_layout!=null) {
                linearLayout.setBackgroundColor(Integer.parseInt(color_layout));
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.notes_update, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                DataBaseUtility dataBaseUtility = new DataBaseUtility(getActivity());
                notesModel = new NotesModel();
                notesModel.setTitle(titleEditText.getText().toString());
                notesModel.setContent(contentEdtitext.getText().toString());
                notesModel.setDate(str3);
                notesModel.setTime(str4);
                notesModel.setId(id);
                //notesModel.setReminderDate(reminderTextView.getText().toString());
                notesModel.setColor(color_pick);
                dataBaseUtility.updateNote(notesModel);
                try {
                    databaseReference
                            .child(uId).child(str3).child(String.valueOf(notesModel.getId()))
                            .setValue(notesModel);
                } catch (Exception e) {
                    Log.i(TAG, "onClick: " + e);
                }
                getActivity().setTitle("Notes");
                getActivity().getFragmentManager().popBackStackImmediate();
                return super.onOptionsItemSelected(item);

            case R.id.action_reminder:
                new DatePickerDialog(getActivity(), datePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.action_edit_color_picker:
//                int color_pick = ((TodoMainActivity)getActivity()).setFragmentColor();
//                linearLayout.setBackgroundColor(color_pick);
                ColorPickerDialog.newBuilder().
                        setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(true)
                        .setDialogId(0).setColor(Color.BLACK).
                        setShowAlphaSlider(true).
                        show(getActivity());
               break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_button:
                getActivity().setTitle("Notes");
                getActivity().getFragmentManager().popBackStackImmediate();

                break;

        }

    }

    private void updateLabel() {

        String myFormat = "MMMM dd, yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        reminderTextView.setText(sdf.format(myCalendar.getTime()));
        Calendar current = Calendar.getInstance();
        if ((myCalendar.compareTo(current) <= 0)) {

            //The set Date/Time already passed

            new DatePickerDialog(getActivity(), datePicker, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.invalid_date),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.reminder_date_set)
                    + reminderTextView.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showDialog(String message) {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
      /*  color_pick = String.valueOf(color);
        linearLayout.setBackgroundColor(color);*/
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    /*@Override
    public void showDialog(String message) {

    }

    @Override
    public void hideDialog() {

    }*/

    /*@Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
       *//* switch (dialogId) {
            case DIALOG_ID:*//*
                color_pick= String.valueOf(color);
                linearLayout.setBackgroundColor(color);
              *//*  break;
        }*//*
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }*/

}
