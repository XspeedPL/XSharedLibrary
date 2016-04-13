package xeed.library.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public final class IntListPreference extends DialogPreference
{
    private CharSequence[] mEntries;
    
    public IntListPreference(final Context c, final AttributeSet as)
    {
        super(c, as);
        final TypedArray ta = c.obtainStyledAttributes(as, new int[] { android.R.attr.entries });
        mEntries = ta.getTextArray(0);
        ta.recycle();
    }
    
	@Override
	protected final void onPrepareDialogBuilder(final Builder b)
	{
		super.onPrepareDialogBuilder(b);
		int val;
		try { val = getPersistedInt(0); }
		catch (final Exception ex) { val = 0; }
		b.setSingleChoiceItems(mEntries, val, new OnClickListener()
		{
			@Override
			public final void onClick(final DialogInterface di, final int pos)
			{
				persistInt(pos);
				di.dismiss();
			}
		});
	}
	
	@Override
	protected final void onSetInitialValue(final boolean restore, final Object def)
	{
	    if (!restore && def instanceof Integer) persistInt((Integer)def);
	}
	
	@Override
	protected final Object onGetDefaultValue(final TypedArray ta, final int i)
	{
		return ta.getInt(i, 0);
	}
}
