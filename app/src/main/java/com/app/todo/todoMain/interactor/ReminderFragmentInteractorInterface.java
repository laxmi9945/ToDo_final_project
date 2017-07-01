package com.app.todo.todoMain.interactor;


import com.app.todo.model.NotesModel;

public interface ReminderFragmentInteractorInterface {

    void getReminderNotes(String uId);

    void deleteReminderNotes(NotesModel notesModel, String uId);
}
