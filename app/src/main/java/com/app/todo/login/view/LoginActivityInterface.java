package com.app.todo.login.view;

import android.view.View;

import com.app.todo.model.UserInfoModel;



public interface LoginActivityInterface extends View.OnClickListener {
    void loginSuccess(UserInfoModel userInfoModel, String uid);
    void loginFailure(String message);
    void showProgressDialog(String message);
    void hideProgressDialog();
    void showError(int errorType);
}
