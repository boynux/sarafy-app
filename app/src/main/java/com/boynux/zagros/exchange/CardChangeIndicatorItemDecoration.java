package com.boynux.zagros.exchange;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CardChangeIndicatorItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDivider;

    /**
     * Default divider will be used
     */
    public CardChangeIndicatorItemDecoration(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }

    /**
     * Custom divider will be used
     */
    public CardChangeIndicatorItemDecoration(Context context, int resId) {
        mDivider = ContextCompat.getDrawable(context, resId);
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn after the item views are drawn
     * and will thus appear over the views.
     *
     * @param c      Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of RecyclerView.
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            final int left = child.getPaddingLeft();
            final int right = child.getWidth() - child.getPaddingRight();

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getTop() + params.topMargin;
            int bottom = top + 16; //mDivider.getIntrinsicHeight();

            mDivider.setFilterBitmap(true);
            mDivider.setBounds(left, top, right, bottom);
            mDivider.setColorFilter(0xffff0000, PorterDuff.Mode.SRC_IN);

            mDivider.draw(c);
        }
    }
}

