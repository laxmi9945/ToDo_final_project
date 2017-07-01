package com.app.todo.todoMain.view.fragment;

import com.app.todo.model.NotesModel;

import java.util.List;

public interface ArchiveFragmentInterface{
    void showDialog(String message);
    void hideDialog();

    void archiveSuccess(List<NotesModel> modelList);
    void archiveFailure(String message);
}
