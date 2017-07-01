package com.app.todo.notesadd.presenter;

import android.os.Bundle;

import com.app.todo.model.NotesModel;


public interface NotesAddPresenterInterface {

    void addNoteToFirebase(Bundle bundle);
    void addNoteToLocalDB(NotesModel model);
    void showDialog(String message);
    void hideDialog();

    void noteAddSuccess(String message);
    void noteAddFailure(String message);

}