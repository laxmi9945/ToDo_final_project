package com.app.todo.todoMain.view.fragment;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.todo.R;
import com.app.todo.adapter.RecyclerAdapter;
import com.app.todo.localdatabase.DataBaseUtility;
import com.app.todo.model.NotesModel;
import com.app.todo.todoMain.presenter.TrashFragmentPresenter;
import com.app.todo.todoMain.presenter.TrashFragmentPresenterInterface;
import com.app.todo.todoMain.view.activity.TodoMainActivity;
import com.app.todo.utils.Constants;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.facebook.FacebookSdk.getApplicationContext;


public class TrashFragment extends Fragment implements TrashFragmentInterface,
        View.OnLongClickListener,SearchView.OnQueryTextListener,View.OnClickListener{
    public static final String TAG = "TrashFragment";
    ProgressDialog progressDialog;
    TrashFragmentPresenterInterface presenter;
    RecyclerView trash_recyclerView;
    RecyclerAdapter trash_recyclerAdapter;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    String uId;
    AppCompatTextView trash_textView;
    List<NotesModel> allNotes = new ArrayList<>();
    List<NotesModel> filteredNotes = new ArrayList<>();
    ArrayList<NotesModel> deleteNoteList = new ArrayList<>();
    AppCompatImageView trash_imageView;
    TodoMainActivity todoMainActivity;
    TrashFragmentPresenter trashFragmentPresenter;
    NotesModel notesModel = new NotesModel();
    RecyclerAdapter recyclerAdapter;
    DataBaseUtility dataBaseUtility;
    Snackbar snackbar;
    LinearLayout linearLayout;
    boolean isGrid = false;
    SharedPreferences sharedPreferences;
    ActionMode actionMode;
    AppCompatImageView deleteIcon;
    Toolbar toolbar;
    AppCompatTextView countTextView;
    ArrayList<Integer> selectedCards = new ArrayList<Integer>();
    int Count=0;
    public TrashFragment(TodoMainActivity todoMainActivity) {
        this.todoMainActivity = todoMainActivity;
        presenter = new TrashFragmentPresenter(todoMainActivity, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Fabric.with(getActivity(), new Crashlytics());

        View view = inflater.inflate(R.layout.fragment_trash, container, false);
        setHasOptionsMenu(true);
        initView(view);
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.userdata);
        dataBaseUtility = new DataBaseUtility(getActivity());
        getActivity().setTitle("Trash");
        if (sharedPreferences.getBoolean("isList", false)) {
            isGrid = false;

            trash_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,
                    StaggeredGridLayoutManager.VERTICAL));
        } else {
            isGrid = true;
            trash_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
        }

        return view;

    }

    private void initView(View view) {
        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.keys,
                Context.MODE_PRIVATE);
        linearLayout= (LinearLayout) view.findViewById(R.id.root_trash_recycler);
        //databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.userData));
        trash_recyclerView = (RecyclerView) view.findViewById(R.id.deleteItem_recyclerView);
        firebaseAuth = FirebaseAuth.getInstance();
        uId = firebaseAuth.getCurrentUser().getUid();
        trashFragmentPresenter=new TrashFragmentPresenter(getContext(),this);
        trashFragmentPresenter.getDeleteNote(uId);
        trash_textView = (AppCompatTextView) view.findViewById(R.id.trashTextView);
        trash_imageView = (AppCompatImageView) view.findViewById(R.id.trash_icon);
        trash_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        recyclerAdapter = new RecyclerAdapter(getActivity(), filteredNotes,this);
        trash_recyclerView.setAdapter(recyclerAdapter);
        toolbar= (Toolbar) getActivity().findViewById(R.id.deleteToolbar);
        deleteIcon= (AppCompatImageView) getActivity().findViewById(R.id.delete_icon);
        countTextView= (AppCompatTextView) getActivity().findViewById(R.id.count_textView);
        deleteIcon.setOnClickListener(this);
        initSwipe();


    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    notesModel = deleteNoteList.get(position);
                    //notesModel.setDeleted(true);
                    presenter.deleteNote(notesModel,uId);
                    recyclerAdapter.deleteItem(position);
                    dataBaseUtility.delete(notesModel);
                    trash_recyclerView.setAdapter(recyclerAdapter);
                    snackbar = Snackbar
                            .make(linearLayout, getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            databaseReference.child(uId).child(notesModel.getDate())
                                    .child(String.valueOf(notesModel.getId())).setValue(notesModel);
                        }
                    });

                            snackbar.show();


                }
                if (direction == ItemTouchHelper.RIGHT) {

                    notesModel = deleteNoteList.get(position);
                    presenter.moveNotefromTrashToNotes(notesModel,uId);
                    snackbar = Snackbar
                            .make(linearLayout, getString(R.string.item_moved), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    deleteNoteList.remove(position);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(trash_recyclerView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TodoMainActivity) getActivity()).showOrHideFab(false);
    }

    @Override
    public void showDialog(String message) {
        progressDialog = new ProgressDialog(getActivity());

        if (!getActivity().isFinishing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    @Override
    public void hideDialog() {
        if (!getActivity().isFinishing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void deleteSuccess(List<NotesModel> modelList) {
        deleteNoteList.clear();
        for (NotesModel notesModel : modelList) {
            if (notesModel.isDeleted()) {
                deleteNoteList.add(notesModel);
            }
        }
        allNotes.clear();
        allNotes.addAll(deleteNoteList);
        trash_recyclerAdapter = new RecyclerAdapter(todoMainActivity, deleteNoteList,this);
        trash_recyclerView.setAdapter(trash_recyclerAdapter);

        if (deleteNoteList.size() != 0) {
            trash_textView.setVisibility(View.INVISIBLE);
            trash_imageView.setVisibility(View.INVISIBLE);
            linearLayout.setGravity(Gravity.START);
        } else {
            trash_textView.setVisibility(View.VISIBLE);
            trash_imageView.setVisibility(View.VISIBLE);
            linearLayout.setGravity(Gravity.CENTER);

        }
    }

    @Override
    public void deleteFailure(String message) {

    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(todoMainActivity, "clicked...", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.todo_notes_action,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        View searchBar = searchView.findViewById(R.id.search_bar);
        if (searchBar != null && searchBar instanceof LinearLayout) {
            ((LinearLayout) searchBar).setLayoutTransition(new LayoutTransition());
        }

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener((SearchView.OnQueryTextListener) this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.changeview:
                toggle(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    void toggle(MenuItem item) {

        if (!isGrid) {
            trash_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,
                    StaggeredGridLayoutManager.VERTICAL));
            item.setIcon(R.drawable.ic_action_straggered);
            isGrid = true;
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("isList", true);
            edit.apply();

        } else {
            trash_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
            item.setIcon(R.drawable.ic_action_list);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("isList", false);
            edit.apply();
            isGrid = false;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchTag) {
        searchTag = searchTag.toLowerCase();
        ArrayList<NotesModel> newList = new ArrayList<>();
        if (!TextUtils.isEmpty(searchTag)) {
            for (NotesModel notesModel : allNotes) {
                String title = notesModel.getTitle().toLowerCase();

                if (title.contains(searchTag))
                    newList.add(notesModel);
            }
            filteredNotes.clear();
            filteredNotes.addAll(newList);
        } else {
            filteredNotes.clear();
            filteredNotes.addAll(allNotes);
        }
        trash_recyclerAdapter.clear();
        trash_recyclerAdapter.addAll(filteredNotes);
        trash_recyclerAdapter.notifyDataSetChanged();
        return false;
    }

    public void hideToolBar() {
        ((TodoMainActivity)getActivity()).showOrHideToolBar(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_icon:
                //presenter.deleteNote(notesModel,uId);
                break;
        }
    }

    public void getIncrimentCount(int position) {
            Count++;
            selectedCards.add(position);
            countTextView.setText(Count);
    }

    public void getDecrementCount(int position) {
        Count--;
        selectedCards.add(position);
        countTextView.setText(Count);
    }
}
