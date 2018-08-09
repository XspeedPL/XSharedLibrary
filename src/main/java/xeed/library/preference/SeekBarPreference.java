package xeed.library.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import xeed.library.common.R;

public final class SeekBarPreference extends CommonDialogPreference<Integer> {
    private int mDefault, mMultiplier;

    @SuppressLint({"InflateParams", "ResourceType"})
    public SeekBarPreference(Context c, AttributeSet as) {
        super(c, as);
        TypedArray ta = c.getResources().obtainAttributes(as, new int[]{android.R.attr.defaultValue, R.attr.textValueMult});
        mDefault = ta.getInt(0, 300);
        mMultiplier = ta.getInt(1, 1);
        ta.recycle();
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
