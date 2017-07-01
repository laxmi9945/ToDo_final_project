package com.app.todo.todoMain.view.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.adapter.RecyclerAdapter;
import com.app.todo.localdatabase.DataBaseUtility;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.presenter.NotesFragmentPresenter;
import com.app.todo.notesadd.view.NotesAddActivity;
import com.app.todo.todoMain.view.activity.TodoMainActivity;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static com.facebook.FacebookSdk.getApplicationContext;


public class NotesFragment extends Fragment implements NotesFragmentInterface, OnSearchTextChange,
        View.OnClickListener {
    public static final String TAG = "NotesFragment";
    private static final int REQUEST_CODE = 2;
    @BindView(R.id.recyclerViewNotes)
    RecyclerView recyclerViewNotes;
    @BindView(R.id.fabAddNotes)
    FloatingActionButton fabAddNotes;
    @BindView(R.id.list_empty_imageView)
    AppCompatImageView empty_list_imageView;
    @BindView(R.id.list_empty_textView)
    AppCompatTextView empty_list_textView;
    @BindView(R.id.coordinatorRootNotesFragment)
    CoordinatorLayout coordinatorRootNotesFragment;
    RecyclerAdapter recyclerAdapter;
    List<NotesModel> allNotes = new ArrayList<>();
    List<NotesModel> filteredNotes = new ArrayList<>();
    NotesFragmentPresenter presenter;
    String uId;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    NotesModel notesModel = new NotesModel();
    List<NotesModel> notesModel2 = new ArrayList<>();
    DataBaseUtility dataBaseUtility;
    Snackbar snackbar;
    View view;
    SharedPreferences sharedPreferences;
    boolean isGrid = false;
    CoordinatorLayout coordinatorLayout;
    ItemTouchHelper.SimpleCallback simpleCallback;
    // This is a handle so that we can call methods on our service
    ArrayList<NotesModel> user_list = new ArrayList<>();
    ArrayList<NotesModel> multiselect_list = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    int startPosition = -1;
    int endPosition = -1;
    boolean isDrag=false;

    //AlertDialogHelper alertDialogHelper;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Fabric.with(getActivity(), new Crashlytics());
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorRootNotesFragment);
        recyclerViewNotes = (RecyclerView) view.findViewById(R.id.recyclerViewNotes);

        // Create a new service client and bind our activity to this service
        dataBaseUtility = new DataBaseUtility(getActivity());
        notesModel2 = dataBaseUtility.getDatafromDB();
        presenter = new NotesFragmentPresenter(getContext(), this);
        //cardView= (CardView) view.findViewById(R.id.myCardView);
        databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.userData));
        // databaseReference.keepSynced(true);
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        presenter.getNoteList(uId);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Refresh items
                        refreshItems();
                    }
                }, 4000);

            }
        });
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        fabAnimate();
        initSwipeView();
        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.keys,
                Context.MODE_PRIVATE);

        changeview();

        recyclerAdapter = new RecyclerAdapter(getActivity(), filteredNotes,this);
        recyclerViewNotes.setAdapter(recyclerAdapter);

        ((TodoMainActivity) getActivity()).setSearchTagListener(this);
        fabAddNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotesAddActivity.class);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivityForResult(intent, 2, bundle);
            }
        });
        return view;
    }

    private void changeview() {
        if (sharedPreferences.getBoolean("isList", false)) {
            isGrid = false;

            recyclerViewNotes.setLayoutManager(new StaggeredGridLayoutManager(1,
                    StaggeredGridLayoutManager.VERTICAL));
        } else {
            isGrid = true;
            recyclerViewNotes.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
        }
    }

    private void refreshItems() {
        // Load items
        changeview();
        recyclerAdapter = new RecyclerAdapter(getActivity(), allNotes,this);
        recyclerViewNotes.setAdapter(recyclerAdapter);

        onItemsLoadComplete();
    }

    private void onItemsLoadComplete() {
        // Stop refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void fabAnimate() {

        //Animating Fab button while Scrolling
        recyclerViewNotes.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fabAddNotes.isShown()) {
                    fabAddNotes.hide();
                }
                //super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fabAddNotes.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }


        });
    }

    void initSwipeView() {

        simpleCallback = new ItemTouchHelper
                .SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP
                | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                startPosition = viewHolder.getAdapterPosition();
                endPosition = target.getAdapterPosition();
                recyclerAdapter.dragNotes(startPosition, endPosition);
                allNotes = recyclerAdapter.getallnotesdata();
                return true;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (startPosition != -1 && endPosition != -1) {
                    updateNoteSerialId();
                    isDrag=true;
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                isDrag=false;
                if (direction == ItemTouchHelper.LEFT) {

                    //databaseReference = FirebaseDatabase.getInstance().getReference();
                    notesModel = allNotes.get(position);
                    notesModel.setDeleted(true);
                    presenter.getDelete(notesModel,uId);
                    recyclerAdapter.deleteItem(position);
                    dataBaseUtility.delete(notesModel);
                    recyclerViewNotes.setAdapter(recyclerAdapter);
                    snackbar = Snackbar
                            .make(coordinatorRootNotesFragment, getString(R.string.item_deleted),
                                    Snackbar.LENGTH_LONG);
                    snackbar.show();

                }
                if (direction == ItemTouchHelper.RIGHT) {
                    allNotes = getWithoutArchiveItems();
                    notesModel = allNotes.get(position);
                    notesModel.setArchived(true);
                    presenter.setNoteArchive(notesModel,uId);
                    recyclerAdapter.archiveItem(position);
                    recyclerViewNotes.setAdapter(recyclerAdapter);
                    Snackbar snackbar = Snackbar
                            .make(coordinatorRootNotesFragment, getString(R.string.item_archieved),
                                    Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    notesModel.setArchived(false);

                                    presenter.getArchive(notesModel,uId);
                                    Snackbar snackbar1 = Snackbar.make(coordinatorRootNotesFragment,
                                            getString(R.string.item_restored), Snackbar.LENGTH_SHORT);
                                    snackbar1.show();
                                }
                            });
                    snackbar.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewNotes);

    }

    @Override
    public void onResume() {
        super.onResume();
        ((TodoMainActivity) getActivity()).setToolbarTitle("Notes");
        Log.i(TAG, "onResume:Notes  ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.coordinatorRootNotesFragment})

    public void onViewClicked(View view) {

        switch (view.getId()) {

            case R.id.fabAddNotes:

                break;
            case R.id.coordinatorRootNotesFragment:

                break;

        }
    }

    @Override
    public void showDialog(String message) {
        progressDialog = new ProgressDialog(getContext());
        if (isAdded()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    @Override
    public void hideDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void getNotesListSuccess(List<NotesModel> modelList) {
       if(!isDrag) {
           filteredNotes.clear();
           allNotes.clear();
           for (NotesModel note : modelList) {
               if (!note.isArchived() && !note.isDeleted()) {
                   allNotes.add(note);
               }
           }
           Collections.sort(allNotes, new Comparator<NotesModel>() {
               @Override
               public int compare(NotesModel model1, NotesModel model2) {
                   if (model1.getNoteSerialId() > model2.getNoteSerialId())
                       return 1;
                   if (model1.getNoteSerialId() < model2.getNoteSerialId())
                       return -1;
                   return 0;
               }
           });
           filteredNotes.addAll(allNotes);
           Log.i(TAG, "getNotesListSuccess: " + allNotes.size());
           recyclerAdapter.notifyDataSetChanged();
           if (filteredNotes.size() != 0) {
               empty_list_textView.setVisibility(View.INVISIBLE);
               empty_list_imageView.setVisibility(View.INVISIBLE);

           } else {
               empty_list_textView.setVisibility(View.VISIBLE);
               empty_list_imageView.setVisibility(View.VISIBLE);
           }
       }
    }

    @Override
    public void getNotesListFailure(String message) {
        snackbar = Snackbar
                .make(coordinatorLayout, getString(R.string.no_internet), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        snackbar.dismiss();

                    }
                });
        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        AppCompatTextView textView = (AppCompatTextView) sbView.
                findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
        //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        List<NotesModel> li = dataBaseUtility.getDatafromDB();

        recyclerAdapter = new RecyclerAdapter(getActivity(), filteredNotes,this);
        recyclerViewNotes.setAdapter(recyclerAdapter);
        recyclerAdapter.setNoteList(li);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getActivity(), "hii", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchTagChange(String searchTag) {
        searchTag = searchTag.toLowerCase();

        if (!TextUtils.isEmpty(searchTag)) {
            ArrayList<NotesModel> newList = new ArrayList<>();
            for (NotesModel model : allNotes) {
                String name = model.getTitle().toLowerCase();
                if (name.contains(searchTag))
                    newList.add(model);
                Log.i(TAG, "onSearchTagChange: " + newList);

            }
            filteredNotes.clear();
            filteredNotes.addAll(newList);

        } else {
            filteredNotes.clear();
            filteredNotes.addAll(allNotes);
        }
        recyclerAdapter.notifyDataSetChanged();

    }


    private void updateNoteSerialId() {
        for (NotesModel datamodel :
                allNotes) {
            presenter.updateSerialIdNote(datamodel,allNotes,uId);
        }
    }

    private List<NotesModel> getWithoutArchiveItems() {
        ArrayList<NotesModel> todoHomeDataModel = new ArrayList<>();
        for (NotesModel note : allNotes) {
            if (!note.isArchived()) {
                todoHomeDataModel.add(note);

                Log.i(TAG, "getWithoutArchiveItems: " + todoHomeDataModel);
            }
        }
        return todoHomeDataModel;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeview:
                toggle(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void toggle(MenuItem item) {

        if (!isGrid) {
            recyclerViewNotes.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
            item.setIcon(R.drawable.ic_action_list);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("isList", true);
            isGrid = true;
            edit.apply();

        } else {
            recyclerViewNotes.setLayoutManager(new StaggeredGridLayoutManager(1,
                    StaggeredGridLayoutManager.VERTICAL));
            item.setIcon(R.drawable.ic_action_straggered);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("isList", false);
            isGrid = false;
            edit.apply();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TodoMainActivity) getActivity()).showOrHideFab(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {

        }
    }

}
