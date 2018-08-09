package xeed.library.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.internal.util.XmlUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class YSharedPreferences implements SharedPreferences {
    private static final String TAG = "YSharedPreferences";
    private final ParcelFileDescriptor mDataFile;
    private Map<String, Object> mMap;
    private boolean mLoaded = false;

    public YSharedPreferences(Context context, Uri uri) throws FileNotFoundException {
        this(context.getContentResolver().openFileDescriptor(uri, "r"));
    }

    public YSharedPreferences(ParcelFileDescriptor dataFile) {
        mDataFile = dataFile;
        startLoadFromDisk();
    }

    private void startLoadFromDisk() {
        synchronized (this) {
            mLoaded = false;
        }
        new Thread("YSharedPreferences-load") {
            @Override
            public void run() {
                synchronized (YSharedPreferences.this) {
                    loadFromDiskLocked();
                }
            }
        }.start();
    }

    @SuppressWarnings("unchecked")
    private void loadFromDiskLocked() {
        if (mLoaded) {
            return;
        }

        InputStream dataSource = null;
        try {
            dataSource = new FileInputStream(mDataFile.getFileDescriptor());
            mMap = XmlUtils.readMapXml(dataSource);
        } catch (Exception e) {
            Log.w(TAG, "getSharedPreferences", e);
        } finally {
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception ignored) {
                }
            }
            try {
                mDataFile.close();
            } catch (Exception ignored) {
            }
        }

        mLoaded = true;
        notifyAll();
    }

    private void awaitLoadedLocked() {
        while (!mLoaded) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public Map<String, ?> getAll() {
        synchronized (this) {
            awaitLoadedLocked();
            return new HashMap<>(mMap);
        }
    }

    @Override
    public String getString(String key, String defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            String v = (String) mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        synchronized (this) {
            awaitLoadedLocked();
            Set<String> v = (Set<String>) mMap.get(key);
            return v != null ? v : defValues;
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Integer v = (Integer) mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Long v = (Long) mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Float v = (Float) mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Boolean v = (Boolean) mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    @Override
    public boolean contains(String key) {
        synchronized (this) {
            awaitLoadedLocked();
            return mMap.containsKey(key);
        }
    }

    @Deprecated
    @Override
    public Editor edit() {
        throw new UnsupportedOperationException("Read-only implementation");
    }

    @Deprecated
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("Listeners are not supported in this implementation");
    }

    @Deprecated
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("Listeners are not supported in this implementation");
    }
}
