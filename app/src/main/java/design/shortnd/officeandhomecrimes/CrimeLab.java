package design.shortnd.officeandhomecrimes;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import design.shortnd.officeandhomecrimes.database.CrimeBaseHelper;
import design.shortnd.officeandhomecrimes.database.CrimeCursorWrapper;
import design.shortnd.officeandhomecrimes.database.CrimeDbSchema;
import design.shortnd.officeandhomecrimes.database.CrimeDbSchema.CrimeTable;
import design.shortnd.officeandhomecrimes.database.CrimeDbSchema.CrimeTable.Cols;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteDatabase = new CrimeBaseHelper(mContext)
                .getWritableDatabase();
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Cols.UUID, crime.getId().toString());
        contentValues.put(Cols.TITLE, crime.getTitle());
        contentValues.put(Cols.DATE, crime.getDate().getTime());
        contentValues.put(Cols.SOLVED, crime.isSolved() ? 1 : 0);
        contentValues.put(Cols.SUSPECT, crime.getSuspect());

        return contentValues;
    }

    public void addCrime(Crime crime) {
        ContentValues contentValues = getContentValues(crime);
        mSQLiteDatabase.insert(CrimeTable.NAME, null, contentValues);
    }

    public List<Crime> getCrime() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursorWrapper = queryCrimes(null, null);

        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()){
                crimes.add(cursorWrapper.getCrime());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }
        return crimes;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + "= ?",
                new String[] {id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Crime crime) {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, crime.getPhotoFilename());
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues contentValues = getContentValues(crime);

        mSQLiteDatabase.update(CrimeTable.NAME, contentValues,
                Cols.UUID + " = ?",
                new String[]{ uuidString });
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    public void deleteCrime(Crime crime) {
        String uuidString = crime.getId().toString();

        mSQLiteDatabase.delete(CrimeTable.NAME,
                Cols.UUID + "=?",
                new String[] {uuidString});
    }
}
