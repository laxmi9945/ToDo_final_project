package com.app.todo.todoMain.interactor;


import com.app.todo.model.NotesModel;

public interface TrashFragmentInteractorInterface  {
    void getDeleteNote(String uId);

    void deleteNote(NotesModel notesModel, String uId);

    void moveNotefromTrashtoNotes(NotesModel notesModel, String uId);
}
