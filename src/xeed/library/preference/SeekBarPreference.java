package xeed.library.preference;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.preference.DialogPreference;
import android.widget.*;
import xeed.library.common.R;
import xeed.library.common.Utils;
import xeed.library.view.TextSeekBar;

public final class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private final FrameLayout mView;
	private final TextSeekBar mBar;

	private int mDefault, mValue, mMult;

	@SuppressLint("InflateParams")
    public SeekBarPreference(final Context c, final AttributeSet as)
	{
		super(c, as);
		final TypedArray ta = c.getResources().obtainAttributes(as, new int[] { android.R.attr.defaultValue, R.attr.textValueMult });
		mDefault = ta.getInt(0, 300);
		mMult = ta.getInt(1, 1);
		ta.recycle();
		mView = new FrameLayout(getContext());
        mBar = new TextSeekBar(c, as);
        mBar.setOnSeekBarChangeListener(this);
        mBar.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        mBar.setTextSize(Utils.spPx(getContext(), 20));
        int px = (int)(mBar.getTextSize() * 3 / 2);
        mBar.setPadding(px, 0, px, 0);
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        px = (int)Utils.dpPx(getContext(), 10);
        lp.setMargins(px, px * 2, px, px);
        mBar.setLayoutParams(lp);
        mView.addView(mBar);
	}

    @Override
	protected void onPrepareDialogBuilder(final Builder b)
	{
        b.setTitle(getSummary());
		mValue = getPersistedInt(mDefault);
		mBar.setProgress(mValue / mMult);
		if (mView.getParent() != null) ((ViewGroup)mView.getParent()).removeView(mView);
		b.setView(mView);
	}
	
	@Override
	protected final void onSetInitialValue(final boolean restore, final Object def)  
	{
		super.onSetInitialValue(restore, def);
		if (restore) mValue = getPersistedInt(mDefault);
		else persistInt(mValue = mDefault);
	}

	@Override
	public final void onProgressChanged(final SeekBar sb, final int value, final boolean user)
	{
		mValue = value * mMult;
	}

	@Override
	public final void onClick(final DialogInterface di, final int pos)
	{
		if (pos == Dialog.BUTTON_POSITIVE) persistInt(mValue);
	}

	@Override
	public final void onStartTrackingTouch(final SeekBar seek) { }

	@Override
	public final void onStopTrackingTouch(final SeekBar seek) { }
}