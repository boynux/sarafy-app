package com.boynux.sarafy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExchangeRateContract {
	private ExchangeRateDbHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mContext;

	public ExchangeRateContract(Context context) {
		mContext = context;
	}

	private static final String TEXT_TYPE = " TEXT";
    private static final String DATE_TIME_TYPE = " DATETIME";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String COMMA_SEP = ", ";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ ExchangeEntry.TABLE_NAME + " (" + ExchangeEntry._ID
			+ " INTEGER PRIMARY KEY, " + ExchangeEntry.COLUMN_NAME_CATEGORY
			+ TEXT_TYPE + COMMA_SEP + ExchangeEntry.COLUMN_NAME_TITLE
			+ TEXT_TYPE + COMMA_SEP + ExchangeEntry.COLUMN_NAME_VALUE1
			+ TEXT_TYPE + COMMA_SEP + ExchangeEntry.COLUMN_NAME_VALUE2
			+ TEXT_TYPE + COMMA_SEP + ExchangeEntry.COLUMN_NAME_VALUE3
			+ TEXT_TYPE + COMMA_SEP + ExchangeEntry.COLUMN_NAME_LAST_UPDATE
            + DATE_TIME_TYPE + ")";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ ExchangeEntry.TABLE_NAME;

	public ExchangeRateContract open() throws SQLException {
		mDbHelper = new ExchangeRateDbHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();

		return this;
	}

	public Cursor getCommoditiesByCategory(String cat) {
		Cursor cursor = mDb.query(ExchangeEntry.TABLE_NAME,
				new String[] {
				ExchangeEntry.COLUMN_NAME_ID,
				ExchangeEntry.COLUMN_NAME_TITLE,
				ExchangeEntry.COLUMN_NAME_VALUE1,
				ExchangeEntry.COLUMN_NAME_VALUE2,
				ExchangeEntry.COLUMN_NAME_VALUE3,
                ExchangeEntry.COLUMN_NAME_LAST_UPDATE },
				ExchangeEntry.COLUMN_NAME_CATEGORY + "= ?",
				new String[] { cat }, null, null, null, null);

		return cursor;
	}

	public boolean updateExchangeRate(String cat, String title,
			String[] commodityValues) {
		Cursor cursor = mDb.query(true, ExchangeEntry.TABLE_NAME,
				new String[] { ExchangeEntry.COLUMN_NAME_ID },
				ExchangeEntry.COLUMN_NAME_CATEGORY + "= ? AND " + ExchangeEntry.COLUMN_NAME_TITLE + "= ?",
				new String[] {cat, title}, null, null, null, null);

		ContentValues values = new ContentValues();
		values.put(ExchangeEntry.COLUMN_NAME_CATEGORY, cat);
		values.put(ExchangeEntry.COLUMN_NAME_TITLE, title);
		values.put(ExchangeEntry.COLUMN_NAME_VALUE1, commodityValues.length > 0 ? commodityValues[0] : "-");
		values.put(ExchangeEntry.COLUMN_NAME_VALUE2, commodityValues.length > 1 ? commodityValues[1] : "-");
		values.put(ExchangeEntry.COLUMN_NAME_VALUE3, commodityValues.length > 2  ? commodityValues[2] : "-");
        values.put(ExchangeEntry.COLUMN_NAME_LAST_UPDATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()));

		if (cursor.moveToFirst()) {
			mDb.update(ExchangeEntry.TABLE_NAME, values, ExchangeEntry.COLUMN_NAME_ID + "=" + cursor.getLong(0), null);
		} else {
			mDb.insert(ExchangeEntry.TABLE_NAME, null, values);
		}

		cursor.close();
		return true;
	}

	public class ExchangeRateDbHelper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION = 3;
		public static final String DATABASE_NAME = "ExchangeRate.db";

		public ExchangeRateDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_ENTRIES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}
	}

	public static abstract class ExchangeEntry implements BaseColumns {
		public static final String AUTHORITY = "com.boynux.sarafy";
		public static final String TABLE_NAME = "entry";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_CATEGORY = "category";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_VALUE1 = "value1";
		public static final String COLUMN_NAME_VALUE2 = "value2";
		public static final String COLUMN_NAME_VALUE3 = "value3";
        public static final String COLUMN_NAME_LAST_UPDATE = "last_update";
	}
}
