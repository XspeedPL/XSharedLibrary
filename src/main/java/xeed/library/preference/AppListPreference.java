package xeed.library.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import java.util.ArrayList;

import xeed.library.common.AppInfo;

public final class AppListPreference extends CommonDialogPreference<String> {
    private final ArrayList<AppInfo<String>> data = new ArrayList<>();
    private ListView lv = null;

    public AppListPreference(Context c, AttributeSet as) {
        super(c, as);
    }

    @Override
    public String getPersisted() {
        return getPersistedString("");
    }

    @Override
    public void setPersisted(String value) {
        persistString(value);
    }
}
