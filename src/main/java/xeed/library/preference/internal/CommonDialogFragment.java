package xeed.library.preference.internal;

import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import xeed.library.preference.CommonDialogPreference;

public abstract class CommonDialogFragment<T, R extends CommonDialogPreference<T>> extends PreferenceDialogFragmentCompat {
    protected T mCurrentValue;

    @Override
    @SuppressWarnings("unchecked")
    public final R getPreference() {
        return (R) super.getPreference();
    }

    @Override
    public final void onDialogClosed(boolean result) {
        if (result && getPreference().callChangeListener(mCurrentValue)) {
            getPreference().setValue(mCurrentValue);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder b) {
        mCurrentValue = getPreference().getValue();
    }
}
