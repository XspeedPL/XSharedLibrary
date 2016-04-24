package xeed.library.view;

import java.lang.reflect.Field;

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
import xeed.library.common.R;
import xeed.library.common.Utils;

public final class TextSeekBar extends AppCompatSeekBar
{
    private final TextView mAttrs;
	private String mSuffix;
	private final Paint mPaint;
	private final int mMult, mMax;
	
	public TextSeekBar(Context c, AttributeSet as)
	{
		super(c, as);
		mAttrs = new TextView(c);
		final TypedArray ta = c.getResources().obtainAttributes(as, R.styleable.TextSeekBar);
		mMult = ta.getInteger(R.styleable.TextSeekBar_textValueMult, 1);
		mSuffix = ta.getString(R.styleable.TextSeekBar_textSuffix);
		mMax = getMax() == 100 ? ta.getInteger(R.styleable.TextSeekBar_maxValue, 100) : getMax();
		ta.recycle();
		if (mSuffix == null) mSuffix = "";
		setMax(mMax / mMult);
		mPaint = new Paint();
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setAntiAlias(true);
		setTextAppearance(R.style.TextAppearance_AppCompat_Caption);
		setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
	}

	@Override
	public final void setPadding(final int i1, final int i2, final int i3, final int i4)
	{
	    super.setPadding(i1, (int)(i2 + getTextSize() + Utils.getPx(getContext(), 2.5F)), i3, i4);
	}
	
    @TargetApi(23)
    @SuppressWarnings("deprecation")
	public final void setTextAppearance(final int resId)
	{
	    if (Build.VERSION.SDK_INT > 22) mAttrs.setTextAppearance(resId);
        else mAttrs.setTextAppearance(getContext(), resId);
	    mPaint.setColor(mAttrs.getCurrentTextColor());
        mPaint.setTextSize(mAttrs.getTextSize());
        mPaint.setTypeface(mAttrs.getTypeface());
	}
	
    public final float getTextSize()
    {
        return mPaint.getTextSize();
    }
    
    @TargetApi(16)
    public final Drawable getThumbCompat()
    {
        if (Build.VERSION.SDK_INT > 15) return getThumb();
        else
        {
            try
            {
                final Field f = AbsSeekBar.class.getDeclaredField("mThumb");
                f.setAccessible(true);
                return (Drawable)f.get(this);
            }
            catch (final Exception ex) { }
        }
        return null;
    }
	
	@Override
	public final void onDraw(final Canvas c)
	{
		super.onDraw(c);
		final Rect r = getThumbCompat().getBounds();
		c.drawText(String.valueOf(mMult * getProgress()) + mSuffix, r.left + getPaddingLeft(), getPaddingTop() - Utils.getPx(getContext(), 2.5F), mPaint);
	}
}
