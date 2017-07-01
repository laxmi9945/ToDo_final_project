package com.app.todo.todoMain.interactor;

import android.content.Context;
import android.util.Log;

import com.app.todo.R;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.presenter.NotesFragmentPresenter;
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
import java.util.List;

/**
 * Created by bridgeit on 13/5/17.
 */

public class NotesFragmentInteractor implements NotesFragmentInteractorInterface {

    Context context;
    NotesFragmentPresenter presenter;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    public NotesFragmentInteractor(Context context, NotesFragmentPresenter presenter) {
        this.context=context;
        this.presenter=presenter;
        firebaseAuth= FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.userData));
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
       /// databaseReference.keepSynced(true);
    }

    @Override
    public void getNoteList(final String uId) {
        presenter.showDialog(context.getString(R.string.fetching_data));
        String userId = firebaseAuth.getCurrentUser().getUid();
        //if (CommonChecker.isNetworkConnected(context)) {
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   /* Map<String, Object> objectMap = (HashMap<String, Object>)
                            dataSnapshot.getValue();*/
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
                    Log.i("check", "onDataChange:  "+notesModel.size());

                    presenter.getNoteListSuccess(notesModel);
                    presenter.hideDialog();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    presenter.hideDialog();

                }
            });

        /*}else {
            presenter.getNoteListFailure(context.getString(R.string.no_internet));
            presenter.hideDialog();
        }*/
    }

    @Override
    public void getDelete(NotesModel notesModel, String uId) {
        databaseReference.child(uId)
                .child(notesModel.getDate()).child(String.valueOf(notesModel.getId()))
                .setValue(notesModel);
    }

    @Override
    public void getArchive(NotesModel notesModel, String uId) {
        databaseReference.child(uId).child(notesModel.getDate())
                .child(String.valueOf(notesModel.getId()))
                .setValue(notesModel);
    }

    @Override
    public void setNoteArchive(NotesModel notesModel, String uId) {
        databaseReference.child(uId).child(notesModel.getDate()).child(String
                .valueOf(notesModel.getId())).setValue(notesModel);
    }


    @Override
    public void updateSerialIdNote(NotesModel datamodel, List<NotesModel> allNotes, String uId) {
        databaseReference.child(uId).child(datamodel.getDate())
                .child(String.valueOf(datamodel.getId())).child(Constants.notesSerialId)
                .setValue(allNotes.indexOf(datamodel));
    }


}
