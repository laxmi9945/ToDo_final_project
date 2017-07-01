package com.app.todo.todoMain.view.activity;


import android.animation.LayoutTransition;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.adapter.RecyclerAdapter;
import com.app.todo.baseclass.BaseActivity;
import com.app.todo.localdatabase.DataBaseUtility;
import com.app.todo.login.view.LoginActivity;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.view.fragment.AboutFragment;
import com.app.todo.todoMain.view.fragment.ArchiveFragment;
import com.app.todo.todoMain.view.fragment.NotesFragment;
import com.app.todo.todoMain.view.fragment.NoteseditFragmentInterface;
import com.app.todo.todoMain.view.fragment.OnSearchTextChange;
import com.app.todo.todoMain.view.fragment.ReminderFragment;
import com.app.todo.todoMain.view.fragment.ShareFragment;
import com.app.todo.todoMain.view.fragment.TrashFragment;
import com.app.todo.utils.Constants;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;

//import com.bumptech.glide.Glide;

public class TodoMainActivity extends BaseActivity implements TodoMainActivityInterface, SearchView.OnQueryTextListener, View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, NoteseditFragmentInterface {
    static final String TAG = "NetworkStateReceiver";
    private final int PICK_IMAGE_CAMERA = 100, PICK_IMAGE_GALLERY = 200, CROP_IMAGE = 1;
    RecyclerView recyclerView;
    boolean isList = false;
    RecyclerAdapter recyclerAdapter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    CardView cardView;
    DrawerLayout drawer;
    Toolbar toolbar,delete_ToolBar;
    NavigationView navigationView;
    Menu menu;
    DataBaseUtility dataBaseUtility;
    AppCompatTextView titleTextView;
    AppCompatTextView dateTextview;
    AppCompatTextView contentTextview;
    AppCompatTextView nav_header_Name;
    AppCompatTextView nav_header_Email;
    CircleImageView circleImageView;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    List<NotesModel> allNotes;
    FloatingActionButton floatingActionButton;
    ProgressDialog progressDialog;
    String fb_first_name;
    String fb_last_name;
    String fb_email;
    String imageUrl;
    String google_first_name;
    String google_email;
    String google_imageUrl;
    String uId;
    GoogleSignInOptions googleSignInOptions;
    GoogleApiClient googleApiClient;
    File file;
    Uri uri;
    Intent CamIntent, GalIntent, CropIntent;
    OnSearchTextChange searchTagListener;
    NotesFragment notesFragment;
    PendingIntent pendingIntent;
    private StorageReference storageReference;
    Uri downloadUrl2;
    FirebaseUser firebaseUser;
    Bitmap bitmap;
    ArchiveFragment archiveFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG,FirebaseDatabase.getInstance().toString());
        }catch (Exception e){
            Log.w(TAG,"SetPresistenceEnabled:Fail"+FirebaseDatabase.getInstance().toString());
            e.printStackTrace();
        }
        setContentView(R.layout.activity_drawerlayout);
        initView();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_from_left)
                .replace(R.id.frameLayout_container, new NotesFragment(), NotesFragment.TAG)
                .commit();

        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.keys, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("isList", false)) {
            isList = false;
        } else {
            isList = true;
        }

        //setActionBar(trash_toolbar);
        if (sharedPreferences.getBoolean(Constants.key_fb_login, false)) {
            isFbLogin();
        } else if (sharedPreferences.getBoolean(Constants.key_google_login, false)) {
            isGoogleLogin();

        } else {

            String uEmail = firebaseUser.getEmail();
            String uName=uEmail.substring(0, uEmail.lastIndexOf("@"));
            String uName2=uName.substring(0,1).toUpperCase() + uName.substring(1).toLowerCase();
            nav_header_Name.setText(uName2);
            nav_header_Email.setText(uEmail);
            circleImageView.setOnClickListener(this);
        }
        setTitle(getString(R.string.notes));
        dataBaseUtility = new DataBaseUtility(this);
        toolbar.setVisibility(View.VISIBLE);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        floatingActionButton.setVisibility(View.VISIBLE);

    }

    boolean isFbLogin() {

        //Getting data from SharedPreference
        fb_first_name = sharedPreferences.getString(Constants.fb_name_key, "");
        fb_last_name = sharedPreferences.getString(Constants.fb_lastname_key, "");
        fb_email = sharedPreferences.getString(Constants.fb_email_key, "");
        imageUrl = sharedPreferences.getString(Constants.fb_profile_key, "");
        nav_header_Name.setText(fb_first_name + " " + fb_last_name);
        nav_header_Email.setText(fb_email);
        Glide.with(getApplicationContext()).load(imageUrl).into(circleImageView);
        return false;

    }

    boolean isGoogleLogin() {

        //Getting data from SharedPreference
        google_first_name = sharedPreferences.getString(Constants.Name, "");
        google_email = sharedPreferences.getString(Constants.Email, "");
        google_imageUrl = sharedPreferences.getString(Constants.profile_pic, "");
        nav_header_Name.setText(google_first_name);
        nav_header_Email.setText(google_email);
        Glide.with(getApplicationContext()).load(google_imageUrl).into(circleImageView);
        return false;
    }

    @Override
    public void initView() {
        /*FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Getting reference to Firebase DatabaseUtil
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://mytodoapp-1d9b3.firebaseio.com/").child(getString(R.string.userData));
        databaseReference.keepSynced(true);*/
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://mytodoapp-1d9b3.firebaseio.com/").child(getString(R.string.userData));
        databaseReference.keepSynced(true);
        firebaseAuth = FirebaseAuth.getInstance();
        uId = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        View view = getLayoutInflater().inflate(R.layout.activity_todonotes_cards, null, false);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fabAddNotes);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewNotes);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        titleTextView = (AppCompatTextView) view.findViewById(R.id.title_TextView);
        dateTextview = (AppCompatTextView) view.findViewById(R.id.date_TextView);
        contentTextview = (AppCompatTextView) view.findViewById(R.id.content_TextView);
        cardView = (CardView) view.findViewById(R.id.myCardView);
        nav_header_Name = (AppCompatTextView) header.findViewById(R.id.nav_header_appName);
        nav_header_Email = (AppCompatTextView) header.findViewById(R.id.nav_header_emailId);
        circleImageView = (CircleImageView) header.findViewById(R.id.profile_image);
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        delete_ToolBar= (Toolbar) findViewById(R.id.deleteToolbar);
        setSupportActionBar(toolbar);


        googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        setClicklistener();
        downloadImage();

    }

    @Override
    public void setClicklistener() {
        floatingActionButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_notes_action, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();


        View searchBar = searchView.findViewById(R.id.search_bar);
        if (searchBar != null && searchBar instanceof LinearLayout) {
            ((LinearLayout) searchBar).setLayoutTransition(new LayoutTransition());
        }

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.changeview) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.profile_image:

                profilePictureSet();

                break;

        }
    }

    void profilePictureSet() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(android.Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.choose_option));
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals(getString(R.string.take_photo))) {
                            dialog.dismiss();
                            ClickImageFromCamera();
                        } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                            dialog.dismiss();
                            GetImageFromGallery();
                        } else if (options[item].equals(getString(R.string.cancel))) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(this, getString(R.string.camera_permission_error), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.camera_permission_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void ClickImageFromCamera() {

        CamIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        file = new File(Environment.getExternalStorageDirectory(),
                "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = Uri.fromFile(file);

        CamIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        CamIntent.putExtra("return-data", true);

        startActivityForResult(CamIntent, PICK_IMAGE_CAMERA);

    }

    public void GetImageFromGallery() {

        GalIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(Intent.createChooser(GalIntent, getString(R.string.select_img_from_gallery)), PICK_IMAGE_GALLERY);

    }

    public void ImageCropFunction() {

        // Image Crop Code
        try {

            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri, "image/*");
            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 200);
            CropIntent.putExtra("outputY", 200);
            CropIntent.putExtra("aspectX", 4);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);
            startActivityForResult(CropIntent, CROP_IMAGE);


        } catch (ActivityNotFoundException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String titleData = "";
        String contentData = "";
        String recentTimeData = "";

        Log.i(TAG, "onActivityResult: " + requestCode + "  " + resultCode);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == PICK_IMAGE_GALLERY) {
                uri = data.getData();
                ImageCropFunction();
            } else if (requestCode == PICK_IMAGE_CAMERA) {
                uri = data.getData();
                ImageCropFunction();
            } else if (requestCode == CROP_IMAGE) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    Bitmap photo = bundle.getParcelable("data");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    uploadImage();

                } else {
                    Bundle bundle = data.getBundleExtra("bundle");
                    if (bundle != null) {
                        titleData = bundle.getString(Constants.title_data);
                        contentData = bundle.getString(Constants.content_data);
                        recentTimeData = bundle.getString(Constants.date_data);
                    }
                    NotesModel note = new NotesModel();
                    note.setTitle(titleData);
                    note.setContent(contentData);
                    note.setDate(recentTimeData);
                    recyclerAdapter.addNotes(note);
                    recyclerAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(recyclerAdapter);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notes:
                notesFragment = new NotesFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, notesFragment, NotesFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                recyclerView.setAdapter(recyclerAdapter);
                setTitle(getString(R.string.notes));
                //Toast.makeText(this, getString(R.string.notes), LENGTH_SHORT).show();
                drawer.closeDrawers();
                break;

            case R.id.logout:

                deleteAccessToken();
                break;

            case R.id.reminder:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, new ReminderFragment(this),
                                ReminderFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                setTitle(getString(R.string.reminder));
                //Toast.makeText(this, getString(R.string.reminder), Toast.LENGTH_SHORT).show();
                drawer.closeDrawers();
                break;

            case R.id.trash:

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, new TrashFragment(this),
                                TrashFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                setTitle(getString(R.string.trash));
                //Toast.makeText(this, getString(R.string.trash), Toast.LENGTH_SHORT).show();
                drawer.closeDrawers();

                break;

            case R.id.archive:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, new ArchiveFragment(this),
                                ArchiveFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                setTitle(getString(R.string.archive));

                drawer.closeDrawers();

                break;
            case R.id.about:

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, new AboutFragment(),
                                AboutFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                setTitle(getString(R.string.about));

                //Toast.makeText(this, getString(R.string.about), Toast.LENGTH_SHORT).show();
                drawer.closeDrawers();
                break;
            case R.id.nav_share:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.anim_slide_in_from_left,
                                R.anim.anim_slide_out_from_left)
                        .replace(R.id.frameLayout_container, new ShareFragment(),
                                ShareFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                setTitle(getString(R.string.share));

                //Toast.makeText(this, getString(R.string.about), Toast.LENGTH_SHORT).show();
                drawer.closeDrawers();
                //Toast.makeText(this, getString(R.string.logic), Toast.LENGTH_SHORT).show();
                break;

        }

        return true;
    }

    void deleteAccessToken() {

        LoginManager.getInstance().logOut();//fb logout

        firebaseAuth.signOut();//Firebase logout

        //google logout
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Toast.makeText(TodoMainActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT)
                        .show();
            }
        });

        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.keys,
                Context.MODE_PRIVATE);
        editor.clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.key_fb_login, false);
        editor.putBoolean(Constants.key_google_login, false);
        editor.putBoolean(Constants.key_firebase_login, false);
        editor.apply();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    public void setData(NotesModel notesModel) {
        recyclerAdapter.addNotes(notesModel);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } else {

            getFragmentManager().popBackStack();
        }
        if(toolbar.getVisibility()!=View.VISIBLE){
            delete_ToolBar.setVisibility(View.GONE);
            toolbar.setVisibility(View.VISIBLE);
        }

    }

    public void showOrHideFab(boolean show) {
        if (show) {
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }
    }
    public void showOrHideToolBar(boolean show){
        if(show){
            toolbar.setVisibility(View.VISIBLE);
            delete_ToolBar.setVisibility(View.INVISIBLE);

        }
        else {
            delete_ToolBar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.INVISIBLE);
        }
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
        setTitle(title);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        searchTagListener.onSearchTagChange(newText.trim());

        return false;
    }

    @Override
    public void showDialog(String message) {
        progressDialog = new ProgressDialog(this);
        if (!isFinishing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }


    @Override
    public void hideDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void getNotesListFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getNotesListSuccess(List<NotesModel> modelList) {
        allNotes = modelList;
        ArrayList<NotesModel> todoHomeDataModel = new ArrayList<>();
        for (NotesModel note : allNotes) {
            if (!note.isArchived()) {
                todoHomeDataModel.add(note);
            }
        }
       /* recyclerAdapter = new RecyclerAdapter(TodoMainActivity.this, todoHomeDataModel);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();*/
    }

    public void setSearchTagListener(OnSearchTextChange searchTagListener) {
        this.searchTagListener = searchTagListener;
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        Toast.makeText(this, "" + String.valueOf(color), Toast.LENGTH_SHORT).show();
//        notesFragment.setColorForFragment(String.valueOf(color));
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        Toast.makeText(this, "Dialog dismissed", Toast.LENGTH_SHORT).show();
    }

    public void uploadImage() {

        if (uri != null) {
            String uEmail = firebaseUser.getEmail();
            String uName=uEmail.substring(0, uEmail.lastIndexOf("@"));
            StorageReference riversRef = storageReference.child("images/"+uName+".jpg");
            riversRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.i(TAG, "onSuccess: "+downloadUrl);
                            //downloadUrl2=downloadUrl;
                            FirebaseDatabase.getInstance().getReference().child("userProfilePic").setValue(String.valueOf(downloadUrl));
                            Glide.with(TodoMainActivity.this)
                                    .load(downloadUrl)
                                    .placeholder(R.drawable.mann)
                                    .crossFade()
                                    .into(circleImageView);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });

        } else {
            //TODo display error Toast
        }
    }
    private void downloadImage() {
        File localFile = null;
        String uEmail = firebaseUser.getEmail();
        String uName=uEmail.substring(0, uEmail.lastIndexOf("@"));
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File finalLocalFile = localFile;
        StorageReference mReference=storageReference.child("images/"+uName+".jpg");
        mReference.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Glide.with(TodoMainActivity.this).load(finalLocalFile).into(circleImageView);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });

    }
}