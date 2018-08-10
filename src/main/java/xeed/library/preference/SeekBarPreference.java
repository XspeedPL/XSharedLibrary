package xeed.library.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import xeed.library.common.R;
import xeed.library.view.TextSeekBar;

public final class SeekBarPreference extends CommonDialogPreference<Integer> {
    private int mDefault, mMultiplier;
    private TextSeekBar mBar;

    @SuppressLint("ResourceType")
    public SeekBarPreference(Context c, AttributeSet as) {
        super(c, as);
        TypedArray ta = c.getResources().obtainAttributes(as, new int[]{android.R.attr.defaultValue, R.attr.textValueMult});
        mDefault = ta.getInt(0, 300);
        mMultiplier = ta.getInt(1, 1);
        mBar = new TextSeekBar(c, as);
        ta.recycle();
    }

    public TextSeekBar getSeekBar() {
        return mBar;
    }

    public int getMultiplier() {
        return mMultiplier;
    }

    @Override
    protected Integer getPersisted() {
        return getPersistedInt(mDefault);
    }

    @Override
    protected void setPersisted(Integer value) {
        persistInt(value);
    }
}
