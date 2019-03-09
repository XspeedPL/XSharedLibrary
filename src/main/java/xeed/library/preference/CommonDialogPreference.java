package xeed.library.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public abstract class CommonDialogPreference<T> extends DialogPreference {
    private T mCurrentValue;

    public CommonDialogPreference(Context c, AttributeSet as) {
        super(c, as);
        mCurrentValue = getPersisted();
    }

    public T getValue() {
        return mCurrentValue;
    }

    public void setValue(T value) {
        boolean changed = mCurrentValue != value;
        if (changed) {
            mCurrentValue = value;
            setPersisted(value);
            notifyChanged();
        }
    }

    protected abstract T getPersisted();

    protected abstract void setPersisted(T value);

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    protected final void onSetInitialValue(boolean restore, Object def) {
        if (restore) mCurrentValue = getPersisted();
        else {
            mCurrentValue = (T) def;
            setPersisted(mCurrentValue);
        }
    }
}
