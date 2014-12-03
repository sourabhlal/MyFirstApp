package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_CATEG = "relationship";
	public static final String KEY_URG = "urgent";
	public static final String KEY_PURP = "purpose";
	public static final String KEY_CONTACTTYPE = "contact_type";
	public static final String KEY_CONTACT = "contact";
	
	public static final String DATABASE_NAME = "VISITOR";
	public static final String DATABASE_TABLE = "userInfo";


	   public DBHelper(Context context)
	   {
	      super(context, DATABASE_NAME , null, 1);
	   }
	   
	   @Override
	   public void onCreate(SQLiteDatabase db) {
	      // TODO Auto-generated method stub
	      db.execSQL(
	      "create table userInfo " +
	      "(_id integer primary key, name text not null, relationship text not null DEFAULT \'Stranger\', urgent text, purpose text not null, contact_type text, contact text);"
	      );
	   }

	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      // TODO Auto-generated method stub
	      db.execSQL("DROP TABLE IF EXISTS userInfo");
	      onCreate(db);
	   }

	   public boolean insertUser  (Map<String,String> map)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();

	      contentValues.put("name", map.get("name"));
	      contentValues.put("relationship", "Stranger");
	      contentValues.put("urgent", map.get("urgent"));
	      contentValues.put("purpose", map.get("purpose"));
	      contentValues.put("contact_type", map.get("contactType"));
	      if(map.get("contactType").toLowerCase().contains("mail")) {
	    	  contentValues.put("contact", map.get("emailAddress"));
	      }
	      else {
	    	  StringBuilder temp = new StringBuilder();
	    	  for(int i = 0; i < map.get("phoneNumber").length(); i++)
	    	  {
	    		  if (i > 0)
	    			  temp.append(" ");
	    		  
	    		  temp.append(map.get("phoneNumber").charAt(i));
	    	  }
	    	  contentValues.put("contact", temp.toString());
	      }

	      db.insert("userInfo", null, contentValues);
	      return true;
	   }
	   public Cursor getData(String name){
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from userInfo where name='" + name + "'", null );
	      return res;
	   }
	   public int numberOfRows(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      int numRows = (int) DatabaseUtils.queryNumEntries(db, DATABASE_TABLE);
	      return numRows;
	   }
	   public boolean updateContact (Integer id, String name)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put("name", name);

	      db.update("userInfo", contentValues, "_id = ? ", new String[] { Integer.toString(id) } );
	      return true;
	   }

	   public Integer deleteContact (Integer id)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      return db.delete("userInfo", 
	      "_id = ? ", 
	      new String[] { Integer.toString(id) });
	   }
	   
	   public ArrayList<String> getAllContacts()
	   {
	      ArrayList<String> array_list = new ArrayList<String>();
	      //hp = new HashMap();
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from userInfo", null );
	      res.moveToFirst();
	      while(res.isAfterLast() == false){
	      array_list.add(res.getString(res.getColumnIndex(KEY_NAME)));
	      res.moveToNext();
	      }
	   return array_list;
	   }
}