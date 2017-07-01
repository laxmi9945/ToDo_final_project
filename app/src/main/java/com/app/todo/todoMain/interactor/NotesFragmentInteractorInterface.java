package com.app.todo.todoMain.interactor;

import com.app.todo.model.NotesModel;

import java.util.List;

/**
 * Created by bridgeit on 13/5/17.
 */

public interface NotesFragmentInteractorInterface {
    void getNoteList(String uId);

    void getDelete(NotesModel notesModel, String uId);

    void getArchive(NotesModel notesModel, String uId);

    void setNoteArchive(NotesModel notesModel, String uId);
    void updateSerialIdNote(NotesModel datamodel, List<NotesModel> allNotes, String uId);
}
