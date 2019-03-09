package xeed.library.preference.internal;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;
import xeed.library.preference.CommonDialogPreference;

public abstract class CommonDialogFragment<T, R extends CommonDialogPreference<T>> extends PreferenceDialogFragmentCompat {
    protected T mCurrentValue;

    @Override
    @SuppressWarnings("unchecked")
    public final R getPreference() {
        return (R) super.getPreference();
    }

    @Override
    @CallSuper
    public void onDialogClosed(boolean result) {
        if (result && getPreference().callChangeListener(mCurrentValue)) {
            getPreference().setValue(mCurrentValue);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder b) {
        mCurrentValue = getPreference().getValue();
    }
}
