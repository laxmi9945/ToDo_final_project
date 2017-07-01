package com.app.todo.todoMain.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.app.todo.R;
import com.app.todo.login.view.LoginActivity;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity  {

    AppCompatTextView textView;
    Animation animation;
    FirebaseAuth firebaseAuth;
    AppCompatImageView splashImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, TodoMainActivity.class));
        }
        splashImageView= (AppCompatImageView) findViewById(R.id.splashimage);
        textView = (AppCompatTextView) findViewById(R.id.appCompatTextView);
        fadeOutAndHideImage(splashImageView);
        /*animation = new TranslateAnimation(450, 0, 450, 0);
        animation.setDuration(Constants.Splash_textView_animation_time);
        animation.setRepeatMode(Animation.RESTART);
        textView.startAnimation(animation);
        final ImageView imageView = (ImageView) findViewById(R.id.splashimage);
        animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        final Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);
        imageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.startAnimation(anim);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }, Constants.SplashScreen_TimeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });*/


    }
    private void fadeOutAndHideImage(final AppCompatImageView img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(4000);
        // img.setVisibility(View.GONE);
        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }, Constants.SplashScreen_TimeOut);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }

}
