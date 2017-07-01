package com.app.todo.todoMain.interactor;


import com.app.todo.model.NotesModel;

public interface ArchiveFragmentInteractorInterface  {
    void getArchiveNote(String uId);

    void getDelete(NotesModel notesModel, String uId);

    void setUnArchive(NotesModel notesModel, String uId);
}
