package xeed.library.preference;

import android.content.Context;
import android.util.AttributeSet;

public final class TextDialogPreference extends CommonDialogPreference<Void> {
    public TextDialogPreference(Context c, AttributeSet as) {
        super(c, as);
        setNegativeButtonText(null);
    }

    @Override
    protected Void getPersisted() {
        return null;
    }

    @Override
    protected void setPersisted(Void value) {
    }
}
