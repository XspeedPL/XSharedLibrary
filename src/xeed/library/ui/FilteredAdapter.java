package xeed.library.ui;

import java.util.ArrayList;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.widget.*;

public abstract class FilteredAdapter<T> extends BaseAdapter implements Filterable
{
    private final StringFilter mFilter;
    protected ArrayList<T> mData = new ArrayList<T>(0);
    protected ArrayList<T> mFiltered = new ArrayList<T>(0);
    
    protected FilteredAdapter()
    {
        mFilter = new StringFilter();
    }

    @Override
    public final int getCount() { return mFiltered.size(); }

    @Override
    public final Object getItem(final int pos) { return mFiltered.get(pos); }

    @Override
    public final Filter getFilter() { return mFilter; }
    
    protected abstract boolean isDataReady();
    
    protected abstract String getItemDescLower(final T item);
    
    public final void registerView(final SearchView sv)
    {
        sv.setOnQueryTextListener(mFilter);
    }
    
    private final class StringFilter extends Filter implements OnQueryTextListener
    {
        @Override
        protected FilterResults performFiltering(final CharSequence txt)
        {
            final FilterResults ret = new FilterResults();
            final ArrayList<T> l;
            if (!isDataReady()) l = new ArrayList<T>(0);
            else if (txt.length() > 0)
            {
                l = new ArrayList<T>();
                for (final T item : mData)
                    if (getItemDescLower(item).contains(txt))
                        l.add(item);
            }
            else l = mData;
            ret.values = l;
            ret.count = l.size();
            return ret;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults fr)
        {
            mFiltered.clear();
            mFiltered.ensureCapacity(fr.count);
            mFiltered.addAll((ArrayList<T>)fr.values);
            notifyDataSetChanged();
        }

        @Override
        public final boolean onQueryTextChange(final String txt)
        {
            return onQueryTextSubmit(txt);
        }

        @Override
        public final boolean onQueryTextSubmit(final String txt)
        {
            filter(txt);
            return true;
        }
    }
}
