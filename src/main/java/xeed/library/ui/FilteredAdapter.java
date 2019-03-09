package xeed.library.ui;

import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;

import java.util.ArrayList;

public abstract class FilteredAdapter<T> extends BaseAdapter implements Filterable {
    private final StringFilter mFilter;
    protected ArrayList<T> mData = new ArrayList<>(0);
    protected ArrayList<T> mFiltered = new ArrayList<>(0);

    protected FilteredAdapter() {
        mFilter = new StringFilter();
    }

    @Override
    public final int getCount() {
        return mFiltered.size();
    }

    @Override
    public final Object getItem(int pos) {
        return mFiltered.get(pos);
    }

    @Override
    public final Filter getFilter() {
        return mFilter;
    }

    protected abstract boolean isDataReady();

    protected abstract String getItemDescLower(T item);

    public final void registerView(SearchView sv) {
        sv.setOnQueryTextListener(mFilter);
    }

    private final class StringFilter extends Filter implements SearchView.OnQueryTextListener {
        @Override
        protected FilterResults performFiltering(CharSequence txt) {
            FilterResults ret = new FilterResults();
            ArrayList<T> l;
            if (!isDataReady()) l = new ArrayList<>(0);
            else if (txt.length() > 0) {
                l = new ArrayList<>();
                for (T item : mData)
                    if (getItemDescLower(item).contains(txt))
                        l.add(item);
            } else l = mData;
            ret.values = l;
            ret.count = l.size();
            return ret;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults fr) {
            mFiltered.clear();
            mFiltered.ensureCapacity(fr.count);
            mFiltered.addAll((ArrayList<T>) fr.values);
            notifyDataSetChanged();
        }

        @Override
        public final boolean onQueryTextChange(String txt) {
            return onQueryTextSubmit(txt);
        }

        @Override
        public final boolean onQueryTextSubmit(String txt) {
            filter(txt);
            return true;
        }
    }
}
