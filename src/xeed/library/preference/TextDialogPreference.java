package xeed.library.preference;

import android.content.Context;
import android.util.AttributeSet;

public final class TextDialogPreference extends android.preference.DialogPreference
{
    public TextDialogPreference(final Context c, final AttributeSet as)
    {
        super(c, as);
        setNegativeButtonText(null);
    }
}
