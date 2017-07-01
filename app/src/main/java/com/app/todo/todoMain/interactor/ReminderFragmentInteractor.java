package com.app.todo.todoMain.interactor;

import android.content.Context;

import com.app.todo.R;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.presenter.ReminderFragmentPresenterInterface;
import com.app.todo.utils.CommonChecker;
import com.app.todo.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class ReminderFragmentInteractor implements ReminderFragmentInteractorInterface {
    Context context;
    ReminderFragmentPresenterInterface presenter;

    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabaseReference;

    public ReminderFragmentInteractor(Context context, ReminderFragmentPresenterInterface presenter) {
        this.context = context;
        this.presenter = presenter;
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.userdata);
    }

    @Override
    public void getReminderNotes(String uId) {
        presenter.showDialog(context.getString(R.string.loading));
        if(CommonChecker.isNetworkConnected(context)){
            mDatabaseReference.child(uId).addValueEventListener(new ValueEventListener() {
                //String uId = firebaseAuth.getCurrentUser().getUid();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<ArrayList<NotesModel>> arrayListGenericTypeIndicator = new
                            GenericTypeIndicator<ArrayList<NotesModel>>() {
                    };
                    ArrayList<NotesModel> notesModel = new ArrayList<>();

                    for (DataSnapshot post : dataSnapshot.getChildren()) {

                        ArrayList<NotesModel> notesModel_ArrayList;
                        notesModel_ArrayList = post.getValue(arrayListGenericTypeIndicator);
                        notesModel.addAll(notesModel_ArrayList);

                    }
                    notesModel.removeAll(Collections.singleton(null));

                    presenter.gettingReminderSuccess(notesModel);
                    presenter.hideDialog();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    presenter.gettingReminderFailure(context.getString(R.string.archive_failure));
                    presenter.hideDialog();
                }
            });
        }else {
            presenter.gettingReminderFailure(context.getString(R.string.fail));
        }
        presenter.hideDialog();
    }

    @Override
    public void deleteReminderNotes(NotesModel notesModel, String uId) {
        mDatabaseReference.child(uId).child(notesModel.getDate())
                .child(String.valueOf(notesModel.getId())).child("deleted")
                .setValue(true);
    }
}
