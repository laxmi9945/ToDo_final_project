package com.app.todo.todoMain.presenter;

import android.content.Context;

import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.interactor.ArchiveFragmentInteractor;
import com.app.todo.todoMain.interactor.ArchiveFragmentInteractorInterface;
import com.app.todo.todoMain.view.fragment.ArchiveFragmentInterface;

import java.util.List;


public class ArchiveFragmentPresenter implements ArchiveFragmentPresenterInterface {
    Context context;
    ArchiveFragmentInterface viewInterface;
    ArchiveFragmentInteractorInterface interactor;
    public ArchiveFragmentPresenter(Context context, ArchiveFragmentInterface viewInterface){
        this.context=context;
        this.viewInterface=viewInterface;
        interactor=new ArchiveFragmentInteractor(context,this);
    }

    @Override
    public void showDialog(String message) {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void getArchiveNote(String uId) {
        interactor.getArchiveNote(uId);
    }

    @Override
    public void noteArchiveSuccess(List<NotesModel> notesModelList) {
            viewInterface.archiveSuccess(notesModelList);
    }

    @Override
    public void noteArchiveFailure(String message) {

        viewInterface.archiveFailure(message );

    }

    @Override
    public void getDelete(NotesModel notesModel, String uId) {
        interactor.getDelete(notesModel,uId);
    }

    @Override
    public void setUnArchive(NotesModel notesModel, String uId) {
        interactor.setUnArchive(notesModel,uId);
    }
}
