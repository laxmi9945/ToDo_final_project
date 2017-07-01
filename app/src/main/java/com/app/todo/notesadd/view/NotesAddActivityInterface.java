package com.app.todo.notesadd.view;

public interface NotesAddActivityInterface  {

    void showDialog(String message);
    void hideDialog();

    void noteAddSuccess(String message);
    void noteAddFailure(String message);

}