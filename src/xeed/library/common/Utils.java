package xeed.library.common;

import android.content.Context;
import android.util.TypedValue;

public final class Utils
{
    public static final float getPx(final Context c, final float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
}
