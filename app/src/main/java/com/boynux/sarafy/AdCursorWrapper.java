package com.boynux.sarafy;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import java.util.Random;

/**
 * Created by mamad on 12/26/2014.
 */
public class AdCursorWrapper extends CursorWrapper {
    final int advPosition;
    int currentPosition = -1;

    public AdCursorWrapper(Cursor cursor) {
        super(cursor);

        if (super.getCount() == 0) {
            advPosition = 0;
        } else {
            advPosition = new Random().nextInt(super.getCount());
        }
    }

    @Override
    public int getCount() {
        if (super.getCount() == 0) {
            return 0;
        }

        return super.getCount() + 1;
    }

    @Override
    public String getString(int columnIndex) {
        if (currentPosition == advPosition) {
            return "ADVERT";
        }

        return super.getString(columnIndex);
    }

    @Override
    public boolean moveToPosition(int position) {
        Log.d("Cursor", String.format("Requested Position: %d and ad position %d", position, advPosition));
        int realPosition = currentPosition = position;

        if (position > advPosition) {
            realPosition--;
        }

        return super.moveToPosition(realPosition);
    }
}
