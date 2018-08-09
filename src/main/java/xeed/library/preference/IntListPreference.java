package xeed.library.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public final class IntListPreference extends CommonDialogPreference<Integer> {
    private CharSequence[] mEntries;

    public IntListPreference(Context c, AttributeSet as) {
        super(c, as);
        TypedArray ta = c.obtainStyledAttributes(as, new int[]{android.R.attr.entries});
        mEntries = ta.getTextArray(0);
        ta.recycle();
    }

    @Override
    protected Integer getPersisted() {
        return getPersistedInt(0);
    }

    @Override
    protected void setPersisted(Integer value) {
        persistInt(value);
    }

    public CharSequence[] getValues() {
        return mEntries;
    }

    @Override
    protected final Object onGetDefaultValue(TypedArray ta, int i) {
        return ta.getInt(i, 0);
    }
}
