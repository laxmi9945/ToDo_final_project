package com.app.todo.notesadd.interactor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.app.todo.R;
import com.app.todo.model.NotesModel;
import com.app.todo.notesadd.presenter.NotesAddPresenterInterface;
import com.app.todo.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class NotesAddInteractor implements NotesAddInteractorInterface {

    Context context;
    NotesAddPresenterInterface presenter;

    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabaseReference;
    private String TAG = "tag";
    SharedPreferences sharedPreferences;

    public NotesAddInteractor(Context context, NotesAddPresenterInterface presenter) {
        this.context = context;
        this.presenter = presenter;

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        /*FirebaseDatabase.getInstance().setPersistenceEnabled(true);*/
    }

    @Override
    public void addNoteToFirebase(final NotesModel model) {

        presenter.showDialog(context.getString(R.string.note_add_loading_message));

        final String userId;
        userId = firebaseAuth.getCurrentUser().getUid();

        try {
            mDatabaseReference.child(context.getString(R.string.userData))
                    .addValueEventListener(new ValueEventListener() {
                        NotesModel notesModel = model;

                        final GenericTypeIndicator<ArrayList<NotesModel>> typeIndicator = new
                                GenericTypeIndicator<ArrayList<NotesModel>>() {
                        };

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int index = 0;
                            ArrayList<NotesModel> notesModel_ArrayList = new ArrayList<>();
                            //notesModel_ArrayList = dataSnapshot.getValue(typeIndicator);
                            try {
                                if (dataSnapshot.hasChild(userId)) {
                                    notesModel_ArrayList.addAll(dataSnapshot.child(userId)
                                            .child(model.getDate())
                                            .getValue(typeIndicator));
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "onDataChange: " + e);
                            }


                            index = notesModel_ArrayList.size();
                            if (notesModel != null) {
                                //int index = (int) dataSnapshot.child(String.valueOf(model.getId())).getChildrenCount();
                                //if (index != 0) {
                                    model.setId(index);
                                    mDatabaseReference.child(context.getString(R.string.userData))
                                            .child(userId)
                                            .child(model.getDate()).child(String.valueOf(model.getId()))
                                            .setValue(model);
                                    model.setId(index);
                                    int serialId=index;
                                    model.setNoteSerialId(serialId);
                               /* SharedPreferences.Editor editor = getSharedPreferences(Constants.note_serial, MODE_PRIVATE).edit();
                                editor.putString("name", "Elena");
                                editor.putInt("idName", 12);
                                editor.commit();*/

                               /* } else {
                                    model.setId(index);
                                    mDatabaseReference.child(context.getString(R.string.userData))
                                            .child(userId)
                                            .child(model.getDate()).child(String.valueOf(model.getId()))
                                            .setValue(model);
                                }*/
                                notesModel = null;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            presenter.noteAddSuccess(context.getString(R.string.saved));
        } catch (Exception e) {
            presenter.noteAddFailure(e.getMessage());
        }
        presenter.hideDialog();

    }
}