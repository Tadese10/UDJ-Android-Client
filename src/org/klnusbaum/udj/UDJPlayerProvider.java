/**
 * Copyright 2011 Kurtis L. Nusbaum
 *
 * This file is part of UDJ.
 *
 * UDJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * UDJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UDJ.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.klnusbaum.udj;

import android.content.ContentProvider;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.content.Context;
import android.content.ContentResolver;


/**
 * Content provider used to maintain the content asociated
 * with the current event the user is logged into.
 */
public class UDJPlayerProvider extends ContentProvider{

  /** Name of the database */
  private static final String DATABASE_NAME = "player.db";
  /** Database version number */
  private static final int DATABASE_VERSION = 1;

  /** URI for the playlist */
  public static final Uri PLAYLIST_URI = 
    Uri.parse("content://org.klnusbaum.udj/playlist");

  public static final Uri VOTES_URI = 
    Uri.parse("content://org.klnusbaum.udj/playlist/votes");


  /** PLAYLIST TABLE */

  /** Name of the playlist table. */
  private static final String PLAYLIST_TABLE_NAME = "playlist";

  /** Used to identify bad library ids */
  public static final long INVALID_LIB_ID = -1;

  /** Constants used for various Playlist column names */
  public static final String PLAYLIST_ID_COLUMN = "_id";
  public static final String PRIORITY_COLUMN = "priority";
  public static final String TIME_ADDED_COLUMN ="time_added";
  public static final String TITLE_COLUMN = "title";
  public static final String ARTIST_COLUMN = "artist";
  public static final String ALBUM_COLUMN = "album";
  public static final String DURATION_COLUMN = "duration";
  public static final String ADDER_ID_COLUMN = "adder_id";
  public static final String ADDER_USERNAME_COLUMN = "adder_username";
  public static final String IS_CURRENTLY_PLAYING_COLUMN = "adder_username";


  /** SQL statement for creating the playlist table. */
  private static final String PLAYLIST_TABLE_CREATE = 
    "CREATE TABLE " + PLAYLIST_TABLE_NAME + "("+
    PLAYLIST_ID_COLUMN + " INTEGER PRIMARY KEY , " +
    PRIORITY_COLUMN + " INTEGER NOT NULL, "  +
    TIME_ADDED_COLUMN + " TEXT NOT NULL, " +
    DURATION_COLUMN + " INTEGER NOT NULL, " +
    TITLE_COLUMN + " TEXT NOT NULL, " +
    ARTIST_COLUMN + " TEXT NOT NULL, " + 
    ALBUM_COLUMN + " TEXT NOT NULL, " +
    ADDER_ID_COLUMN + " INTEGER NOT NULL, " +
    ADDER_USERNAME_COLUMN + " STRING NOT NULL, " +
    IS_CURRENTLY_PLAYING_COLUMN + " INTEGER NOT NULL DEFAULT=0);";

   /** VOTES TABLE */

   /** Name of votes table */
   private static final String VOTES_TABLE_NAME = "votes";

   /** Constants used for various column names in the votes table */
   public static final String VOTE_ID_COLUMN = "_id";
   public static final String VOTE_PLAYLIST_ENTRY_ID_COLUMN = "playlist_id";
   public static final String VOTE_WEIGHT_COLUMN = "vote_weight";
   public static final String VOTER_ID_COLUMN = "voter_id";


   /** SQL statement for creating the votes table */
  private static final String VOTES_TABLE_CREATE = 
    "CREATE TABLE " + VOTES_TABLE_NAME + " (" +
    VOTE_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    VOTE_PLAYLIST_ENTRY_ID_COLUMN + " INTEGER REFERENCES " +
      PLAYLIST_TABLE_NAME + "(" + PLAYLIST_ID_COLUMN + ") ON DELETE CASCADE, " +
    VOTE_WEIGHT_COLUMN + " INTEGER NOT NULL, " + 
    VOTER_ID_COLUMN + " INTEGER NOT NULL);";
  
  /** UPVOTES View */
  /** Name of upvotes view */
  private static final String UPVOTES_VIEW_NAME = "upvotes";
  
  /** Constants used for various column names in the upvotes view */
  public static final String UPCOUNT_COLUMN = "upcount";
  
  /** SQL statement for creating the up votes view */
  public static final String UPVOTES_VIEW_CREATE = "create view " + UPVOTES_VIEW_NAME + " " +
    "as select " + PLAYLIST_TABLE_NAME + ".*, count(" + VOTE_WEIGHT_COLUMN + ") as " 
    + UPCOUNT_COLUMN + " from " + VOTES_TABLE_NAME + "where " + VOTE_WEIGHT_COLUMN + "=1 group by " +
    VOTE_PLAYLIST_ENTRY_ID_COLUMN + ";";
  
  /** UPVOTES View */
  /** Name of upvotes view */
  private static final String DOWNVOTES_VIEW_NAME = "downvotes";
  
  /** Constants used for various column names in the upvotes view */
  public static final String DOWNCOUNT_COLUMN = "upcount";
  
  /** SQL statement for creating the up votes view */
  public static final String DOWNVOTES_VIEW_CREATE = "create view " + DOWNVOTES_VIEW_NAME + " " +
    "as select " + PLAYLIST_TABLE_NAME + ".*, count(" + VOTE_WEIGHT_COLUMN + ") as " 
    + DOWNCOUNT_COLUMN + " from " + VOTES_TABLE_NAME + "where " + VOTE_WEIGHT_COLUMN + "=-1 group by " +
    VOTE_PLAYLIST_ENTRY_ID_COLUMN + ";";
  

  /** Playlist View */

  /** Name of view */
  private static final String PLAYLIST_VIEW_NAME = "playlist_view";
 
  /** SQL statement for creating the playlist view */
  
  private static final String PLAYLIST_VIEW_CREATE = 
    "CREATE VIEW " + PLAYLIST_VIEW_NAME + " AS SELECT " + PLAYLIST_TABLE_NAME + ".*, " +
    UPVOTES_VIEW_NAME + "." + UPCOUNT_COLUMN + ", " + DOWNVOTES_VIEW_NAME + "." + DOWNCOUNT_COLUMN + " " +
    "FROM " + PLAYLIST_TABLE_NAME + " LEFT JOIN " + UPVOTES_VIEW_NAME + " ON " + 
    PLAYLIST_VIEW_NAME + "." + PLAYLIST_ID_COLUMN + "=" + UPVOTES_VIEW_NAME + "." + PLAYLIST_ID_COLUMN + " " +
    " LEFT JOIN " + DOWNVOTES_VIEW_NAME + " ON " + 
    PLAYLIST_VIEW_NAME + "." + PLAYLIST_ID_COLUMN + "=" + DOWNVOTES_VIEW_NAME + "." + PLAYLIST_ID_COLUMN + ";";



  /** Helper for opening up the actual database. */
  private EventDBHelper dbOpenHelper;

  /**
   * A class for helping open a PartDB.
   */
  private class EventDBHelper extends SQLiteOpenHelper{

    /**
     * Constructs a new EventDBHelper object.
     *
      * @param context The context in which the HostsDBOpenHelper is used.
      */
    EventDBHelper(Context context){
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
      db.execSQL(PLAYLIST_TABLE_CREATE);
      db.execSQL(VOTES_TABLE_CREATE);
      db.execSQL(PLAYLIST_VIEW_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    
    }
  }

  @Override
  public boolean onCreate(){
    dbOpenHelper = new EventDBHelper(getContext());
    return true;
  }

  @Override
  public String getType(Uri uri){
    return "none";
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs){
    if(uri.equals(PLAYLIST_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      return db.delete(PLAYLIST_TABLE_NAME, where, whereArgs);
    } 
    else if(uri.equals(VOTES_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      return db.delete(VOTES_TABLE_NAME, where, whereArgs);
    }
    throw new IllegalArgumentException("Unknown URI " + uri);
  }

  @Override
  public Uri insert(Uri uri, ContentValues initialValues){
    Uri toReturn = null;
    if(uri.equals(PLAYLIST_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      long rowId = db.insert(PLAYLIST_TABLE_NAME, null, initialValues);
      if(rowId >=0){
        toReturn = Uri.withAppendedPath(
          PLAYLIST_URI, String.valueOf(rowId));
      }
    }
    else if(uri.equals(VOTES_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      long rowId = db.insert(VOTES_TABLE_NAME, null, initialValues);    
      if(rowId >=0){
        toReturn = Uri.withAppendedPath(
          VOTES_URI, String.valueOf(rowId));
      }
    }
    else{
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    return toReturn;
  }
  
  @Override
  public Cursor query(Uri uri, String[] projection, 
    String selection, String[] selectionArgs, String sortOrder)
  {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    if(uri.equals(PLAYLIST_URI)){
      qb.setTables(PLAYLIST_VIEW_NAME);
    }
    else if(uri.equals(VOTES_URI)){
      qb.setTables(VOTES_TABLE_NAME);
    }
    else{
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
    Cursor toReturn = qb.query(
      db, projection, selection, selectionArgs, null,
      null, sortOrder);

    toReturn.setNotificationUri(getContext().getContentResolver(), uri);
    return toReturn;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, 
    String[] whereArgs)
  {
    if(uri.equals(PLAYLIST_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      int numRowsChanged = 
        db.update(PLAYLIST_TABLE_NAME, values, where, whereArgs);
      return numRowsChanged;
    }
    else if(uri.equals(VOTES_URI)){
      SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
      int numRowsChanged = 
        db.update(VOTES_TABLE_NAME, values, where, whereArgs);
      return numRowsChanged;
    } 
    throw new IllegalArgumentException("Unknown URI " + uri);
     //TODO implement this
  }

  public static void eventCleanup(ContentResolver cr){
    cr.delete(PLAYLIST_URI, null, null);
    cr.delete(VOTES_URI, null, null);
  }
}
