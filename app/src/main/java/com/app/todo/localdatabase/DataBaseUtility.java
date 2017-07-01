package com.app.todo.localdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.todo.model.NotesModel;

import java.util.ArrayList;
import java.util.List;



public class DataBaseUtility extends SQLiteOpenHelper {

    private static final String DataBase_Name = "TodoNotes";//Databse name
    private static final int DATABASE_VERSION = 1;
    private static final String Todo_Notes = "Todo_notes";//table name
    private static final String Title_table = "title";
    private static final String Time="time";
    private static final String Date="date";
    private static final String Content_table = "content";
    private static final String Id="id";
    private static final String Reminder_date="reminderDate";
    //private static final String is_Deleted="isDeleted";
    //private static final String is_Archived="isArchived";
    private static final String Color="color";
    private static final String Reminder_time="reminderTime";
    private static final String Trash="trash";

    NotesModel model;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    ContentValues contentValues;


    public DataBaseUtility(Context context) {
        super(context, DataBase_Name, null, DATABASE_VERSION);

    }

    //Table Create
    @Override
    public void onCreate(SQLiteDatabase db) {

        String Todo_table = "CREATE TABLE " + Todo_Notes + "("+Id+ " INTEGER PRIMARY KEY, "
                + Title_table + " TEXT," + Content_table + " TEXT," + Time+ " TEXT," +Trash+
                " TEXT," +Date+ " TEXT," +Reminder_date+ " TEXT," +Reminder_time+ " TEXT,"
                 +Color+ " TEXT" +")";
        //+is_Deleted+ " TEXT," +is_Archived+ " TEXT,"
        db.execSQL(Todo_table);
    }

    //Upgrading DB
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop older table if existed
        db.execSQL("Drop table if exists" + Todo_Notes);
        //create table again
        onCreate(db);
    }

    public void addNotes(NotesModel model) {
        SQLiteDatabase database = this.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(Title_table, model.getTitle());//notes title
        contentValues.put(Content_table, model.getContent());//notes content
        contentValues.put(Time,model.getDate());//date & time
        contentValues.put(Color,model.getColor());
        //contentValues.put(Date,model.getDate());
        contentValues.put(Reminder_date,model.getReminderDate());
        contentValues.put(Reminder_time,model.getReminderTime());
        //inserting rows
        database.insert(Todo_Notes, null, contentValues);
        database.close();

    }

    /*public void addUserInfo(NotesModel model ) {
        SQLiteDatabase database = this.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(Title_table, model.getTitle());//notes title
        contentValues.put(Content_table, model.getContent());//notes content
        contentValues.put(Time,model.getDate());//date & time
        //inserting rows
        database.insert(Todo_Notes, null, contentValues);
        database.close();

    }*/


    public List<NotesModel> getDatafromDB() {
        List<NotesModel> dataBaseUtilities = new ArrayList<>();

        String selectQuery="SELECT * FROM " + Todo_Notes ;
        sqLiteDatabase = this.getReadableDatabase();
        //cursor = sqLiteDatabase.rawQuery("SELECT * FROM NOTES WHERE ID='"+Id+"'", null);
        cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                model = new NotesModel();
                model.setTitle(cursor.getString(1));
                model.setContent(cursor.getString(2));
                model.setDate(cursor.getString(3));
                model.setColor(cursor.getString(8));
                model.setReminderDate(cursor.getString(6));
                model.setReminderTime(cursor.getString(7));
                //model.setId(cursor.getInt(6));
                dataBaseUtilities.add(model);
            } while (cursor.moveToNext());

        }

        return dataBaseUtilities;
    }

    public void delete(NotesModel notesModel) {
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(Todo_Notes, Title_table + " =? ", new String[]{notesModel.getTitle()});
        sqLiteDatabase.close();


    }
    public void updateNote(NotesModel notesModel){
        sqLiteDatabase=this.getWritableDatabase();
        contentValues=new ContentValues();
        contentValues.put(Title_table,notesModel.getTitle());
        contentValues.put(Content_table,notesModel.getContent());
        contentValues.put(Color,notesModel.getColor());
        contentValues.put(Reminder_date,notesModel.getReminderDate());
        contentValues.put(Reminder_time,notesModel.getReminderTime());
        contentValues.put(Id,notesModel.getId());
        sqLiteDatabase.update(Todo_Notes,contentValues,null,null);
        sqLiteDatabase.close();
    }


    public Cursor selectNotes() {

        sqLiteDatabase = getReadableDatabase();

        cursor = sqLiteDatabase.rawQuery("select * from " + Todo_Notes, null);
        return cursor;
    }

}
