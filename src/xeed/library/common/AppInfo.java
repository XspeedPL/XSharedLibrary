package xeed.library.common;

import android.graphics.drawable.Drawable;

public final class AppInfo<T> implements Comparable<AppInfo<?>>
{
    public final T data;
    public String label, desc;
    public Drawable icon;
    
    public AppInfo(final T main)
    {
        data = main; icon = null; label = "..."; desc = main.toString();
    }
    
    @Override
    public final int hashCode()
    {
        return data.hashCode();
    }
    
    @Override
    public final boolean equals(final Object o)
    {
        return o instanceof AppInfo<?> && data.equals(((AppInfo<?>)o).data);
    }

    @Override
    public final int compareTo(AppInfo<?> ai)
    {
        return label.compareTo(ai.label);
    }
}