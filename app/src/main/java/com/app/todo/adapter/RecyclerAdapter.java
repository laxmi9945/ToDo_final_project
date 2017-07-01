package com.app.todo.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.app.todo.R;
import com.app.todo.model.NotesModel;
import com.app.todo.notesedit.view.NotesEditActivity;
import com.app.todo.todoMain.view.fragment.ArchiveFragment;
import com.app.todo.todoMain.view.fragment.NotesFragment;
import com.app.todo.todoMain.view.fragment.ShareFragment;
import com.app.todo.todoMain.view.fragment.TrashFragment;
import com.app.todo.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.TaskViewHolder> {
    Context context;
    List<NotesModel> model = new ArrayList<>();
    Fragment notesFragment, archiveFragment;
    private int lastPosition = -1;
    ShareFragment shareFragment;
    TrashFragment trashFragment;
    private SparseBooleanArray selectedItems;
    boolean isSelected=true;
    public List<NotesModel> usersList = new ArrayList<>();
    public List<NotesModel> selected_usersList = new ArrayList<>();

    public RecyclerAdapter(Context context, List<NotesModel> model,NotesFragment notesFragment) {
        this.model = model;
        this.context = context;
        this.notesFragment=notesFragment;
    }
    public RecyclerAdapter(Context context, List<NotesModel> model) {
        this.model = model;
        this.context = context;
    }
    public RecyclerAdapter(Context context, List<NotesModel> model, ShareFragment shareFragment) {
        this.context = context;
        this.model = model;

        this.shareFragment = shareFragment;
    }
    public RecyclerAdapter(Context context, List<NotesModel> model, TrashFragment trashFragment) {
        this.context = context;
        this.model = model;
        this.trashFragment = trashFragment;
        this.usersList=usersList;
        this.selected_usersList=selected_usersList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.activity_todonotes_cards,
                parent, false);

        TaskViewHolder myViewHolder = new TaskViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerAdapter.TaskViewHolder holder, final int position) {

        //final Item item=items.get(position);

        holder.titleTextView.setText(model.get(position).getTitle());
        holder.dateTextView.setText(model.get(position).getDate());
        holder.timeTextView.setText(model.get(position).getTime());
        holder.contentTextView.setText(model.get(position).getContent());
        if (model.get(position).getIsPinned()) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        }
        if (model.get(position).getColor() != null) {
            holder.linearLayoutNoteItem.setBackgroundColor(Integer.parseInt(model.get(position).getColor()));
        }

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);

        if (shareFragment != null) {

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final CharSequence[] options = {"Share note", "Cancel"};
                    android.support.v7.app.AlertDialog.Builder builder = new
                            android.support.v7.app.AlertDialog.Builder(context);
                    builder.setTitle("Select Option");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (options[item].equals("Share note")) {
                                NotesModel note = model.get(holder.getAdapterPosition());
                                //Toast.makeText(context, "Yes...", Toast.LENGTH_SHORT).show();
                                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                String title = note.getTitle();
                                String content = note.getContent();
                                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                        note.getTitle());
                                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                                        "Title: " + title + "\n" + "Content: " + content);
                                context.startActivity(Intent.createChooser(shareIntent,
                                        context.getResources().getString(R.string.share_using)));
                                dialog.dismiss();
                            } else if (options[item].equals("Cancel")) {
                                dialog.dismiss();
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        } else if(trashFragment!=null)
        {
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.card_checkBox.setVisibility(View.VISIBLE);
                    trashFragment.hideToolBar();
                    holder.card_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isSelected) {
                                trashFragment.getIncrimentCount(position);
                            }else {
                                trashFragment.getDecrementCount(position);
                            }
                        }
                    });
                    return true;
                }
            });

        }

    }


    @Override
    public int getItemCount() {

        return model.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        model.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<NotesModel> list) {
        model.addAll(list);
        notifyDataSetChanged();
    }


    public void addNotes(NotesModel note) {

        model.add(note);
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        try {
            model.remove(position);


        } catch (Exception e) {
            //notifyDataSetChanged();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, model.size());
        }
    }

    public void archiveItem(int position) {

        notifyDataSetChanged();
        notifyItemRangeChanged(position, model.size());
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<NotesModel> newList) {

        model = new ArrayList<>();
        model.addAll(newList);
        notifyDataSetChanged();

    }

    public void setAnimation(View viewToAnimate, int position) {

        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            //to make duration random number between [0,501)
            anim.setDuration(new Random().nextInt(501));
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }

    public void setNoteList(List<NotesModel> notesmodel) {
        this.model.clear();
        notifyDataSetChanged();
        this.model.addAll(notesmodel);
        notifyDataSetChanged();
    }

    public List<NotesModel> getallnotesdata() {
        return model;
    }

    public void dragNotes(int startPosition, int endPosition) {
        model.add(startPosition, model.remove(endPosition));
        notifyItemMoved(endPosition, startPosition);
    }
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatTextView titleTextView, dateTextView, contentTextView, timeTextView;
        CardView cardView;
        AppCompatImageView pinIcon;
        LinearLayout linearLayoutNoteItem;
        AppCompatCheckBox card_checkBox;

        public TaskViewHolder(final View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            titleTextView = (AppCompatTextView) itemView.findViewById(R.id.title_TextView);
            dateTextView = (AppCompatTextView) itemView.findViewById(R.id.date_TextView);
            timeTextView = (AppCompatTextView) itemView.findViewById(R.id.time_TextView);
            contentTextView = (AppCompatTextView) itemView.findViewById(R.id.content_TextView);
            cardView = (CardView) itemView.findViewById(R.id.myCardView);
            linearLayoutNoteItem = (LinearLayout) itemView.findViewById(R.id.linearLayoutNoteItem);
            pinIcon = (AppCompatImageView) itemView.findViewById(R.id.pin);
            card_checkBox= (AppCompatCheckBox) itemView.findViewById(R.id.checkBox);
            //cardView.setOnLongClickListener(new MyClickListener());
            cardView.setOnClickListener(this);
            card_checkBox.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.myCardView:
                    if (shareFragment != null) {

                    } else if (notesFragment != null) {

                        notesFragment = ((AppCompatActivity) context).getSupportFragmentManager()
                                .findFragmentByTag(NotesFragment.TAG);
                        archiveFragment = ((AppCompatActivity) context).getSupportFragmentManager()
                                .findFragmentByTag(ArchiveFragment.TAG);
                   /* if (notesFragment.isVisible()) {*/
                        Intent intent = new Intent(context, NotesEditActivity.class);
                        Bundle args = new Bundle();
                        NotesModel note = model.get(getAdapterPosition());
                        args.putString(Constants.notes_titile, note.getTitle());
                        args.putString(Constants.notes_content, note.getContent());
                        args.putString(Constants.notes_date, note.getDate());
                        args.putString(Constants.notes_time, note.getTime());
                        args.putInt(Constants.id, note.getId());
                        args.putString(Constants.reminderDate, note.getReminderDate());
                        args.putString(Constants.reminderTime, note.getReminderTime());
                        args.putString(Constants.colorKey, note.getColor());
                        args.putBoolean(Constants.pinned, note.getIsPinned());
                        intent.putExtras(args);
                        ActivityOptionsCompat options = ActivityOptionsCompat
                                .makeSceneTransitionAnimation((AppCompatActivity) context,
                                        cardView, context.getString(R.string.custom_transition));
                        context.startActivity(intent, options.toBundle());
                    }
                    break;
            }
        }
    }
}



