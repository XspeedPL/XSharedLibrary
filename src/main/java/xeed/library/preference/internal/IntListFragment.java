package xeed.library.preference.internal;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import xeed.library.preference.IntListPreference;

public class IntListFragment extends CommonDialogFragment<Integer, IntListPreference> {
    @Override
    protected final void onPrepareDialogBuilder(AlertDialog.Builder b) {
        super.onPrepareDialogBuilder(b);
        b.setSingleChoiceItems(getPreference().getValues(), mCurrentValue, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface di, int pos) {
                mCurrentValue = pos;
                IntListFragment.this.onClick(null, DialogInterface.BUTTON_POSITIVE);
                if (getDialog() != null) {
                    getDialog().dismiss();
                }
            }
        });
        b.setPositiveButton(null, null);
    }
}
