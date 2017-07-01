package com.app.todo.todoMain.view.fragment;

import com.app.todo.model.NotesModel;

import java.util.List;

/**
 * Created by bridgeit on 10/5/17.
 */

public interface ReminderFragmentInterface  {
    void showDialog(String message);
    void hideDialog();
    void gettingReminderSuccess(List<NotesModel> notesModelList);

    void gettingReminderFailure(String message);
}
