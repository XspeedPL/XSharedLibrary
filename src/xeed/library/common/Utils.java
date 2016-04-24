package xeed.library.common;

import android.content.Context;
import android.util.TypedValue;

public final class Utils
{
    public static final int getPx(final Context c, final float dp)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
}
