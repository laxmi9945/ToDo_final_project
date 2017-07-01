package com.app.todo.todoMain.view.fragment;

import com.app.todo.model.NotesModel;

import java.util.List;


public interface NotesFragmentInterface   {
    void showDialog(String message);

    void hideDialog();

    void getNotesListSuccess(List<NotesModel> modelList);

    void getNotesListFailure(String message);
}
