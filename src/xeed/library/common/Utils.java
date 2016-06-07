package xeed.library.common;

import java.util.ArrayList;

import android.content.Context;
import android.util.TypedValue;

public final class Utils
{
    public static final float dpPx(final Context c, final float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
    
    public static final float spPx(final Context c, final float sp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }
    
    public static final String serialize(final Iterable<AppInfo<String>> data)
    {
        final StringBuilder sb = new StringBuilder();
        for (final AppInfo<String> ai : data)
            sb.append(ai.data).append(" ");
        return sb.toString();
    }
    
    public static final ArrayList<AppInfo<String>> deserialize(final String data)
    {
        final String[] arr = data.split(" ");
        final ArrayList<AppInfo<String>> ret = new ArrayList<AppInfo<String>>(arr.length);
        for (final String s : arr) if (s.length() > 0) ret.add(new AppInfo<String>(s));
        return ret;
    }
}
