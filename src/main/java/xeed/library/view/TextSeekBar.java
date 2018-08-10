package xeed.library.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.AbsSeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;

import xeed.library.common.R;
import xeed.library.common.Utils;

public final class TextSeekBar extends AppCompatSeekBar {
    private final TextView mAttrs;
    private String mSuffix;
    private Paint mPaint = null;
    private final int mMult;

    public TextSeekBar(Context c) {
        this(c, null);
    }

    public TextSeekBar(Context c, AttributeSet as) {
        super(c, as);
        mAttrs = new TextView(c);
        TypedArray ta = c.getResources().obtainAttributes(as, R.styleable.TextSeekBar);
        mMult = ta.getInteger(R.styleable.TextSeekBar_textValueMult, 1);
        mSuffix = ta.getString(R.styleable.TextSeekBar_textSuffix);
        int max = getMax() == 100 ? ta.getInteger(R.styleable.TextSeekBar_maxValue, 100) : getMax();
        ta.recycle();
        if (mSuffix == null) mSuffix = "";
        setMax(max / mMult);
        mPaint = new Paint();
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAntiAlias(true);
        setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        setTextSize(Utils.dpPx(getContext(), 16));
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    public final void setPadding(int i1, int i2, int i3, int i4) {
        super.setPadding(i1, (int) (i2 + getTextSize() + Utils.dpPx(getContext(), 5)), i3, i4);
    }

    public final void setTextAppearance(int resId) {
        mAttrs.setTextAppearance(getContext(), resId);
        mPaint.setColor(mAttrs.getCurrentTextColor());
        mPaint.setTextSize(mAttrs.getTextSize());
        mPaint.setTypeface(mAttrs.getTypeface());
    }

    public final void setTextSize(float px) {
        mPaint.setTextSize(px);
    }

    public final float getTextSize() {
        return mPaint == null ? 0 : mPaint.getTextSize();
    }

    public final Drawable getThumbCompat() {
        if (Build.VERSION.SDK_INT > 15) return getThumb16();
        else {
            try {
                Field f = AbsSeekBar.class.getDeclaredField("mThumb");
                f.setAccessible(true);
                return (Drawable) f.get(this);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @TargetApi(16)
    public final Drawable getThumb16() {
        return getThumb();
    }

    @Override
    public final void onDraw(Canvas c) {
        super.onDraw(c);
        Rect r = getThumbCompat().getBounds();
        c.drawText(String.valueOf(mMult * getProgress()) + mSuffix, r.left + getPaddingLeft(), getPaddingTop() - Utils.dpPx(getContext(), 5), mPaint);
    }
}
