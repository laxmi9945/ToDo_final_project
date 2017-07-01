package com.app.todo.notesedit.view;

public interface NotesEditActivityInterface {
    void showDialog(String message);
    void hideDialog();
    void noteEditSuccess(String message);
    void noteEditFailure(String message);
}
