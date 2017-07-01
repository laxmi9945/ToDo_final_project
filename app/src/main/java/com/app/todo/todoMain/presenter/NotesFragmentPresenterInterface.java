package com.app.todo.todoMain.presenter;

import com.app.todo.model.NotesModel;

import java.util.List;

/**
 * Created by bridgeit on 13/5/17.
 */

public interface NotesFragmentPresenterInterface {
    void showDialog(String message);
    void hideDialog();
    void getNoteList(String uId);
    void getNoteListSuccess(List<NotesModel> modelList);
    void getNoteListFailure(String message);

    void getDelete(NotesModel notesModel, String uId);

    void getArchive(NotesModel notesModel, String uId);

    void setNoteArchive(NotesModel notesModel, String uId);

    void updateSerialIdNote(NotesModel datamodel, List<NotesModel> allNotes, String uId);
}
