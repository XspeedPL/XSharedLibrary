package xeed.library.common;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class MapPreferences implements SharedPreferences {
    private final LinkedList<OnSharedPreferenceChangeListener> mListeners = new LinkedList<>();
    private final HashMap<String, Object> mStorage = new HashMap<>();

    public void putAll(Map<String, Object> items) {
        mStorage.putAll(items);
    }

    @Override
    public HashMap<String, Object> getAll() {
        return new HashMap<>(mStorage);
    }

    @Override
    public String getString(String key, String defValue) {
        String res = (String) mStorage.get(key);
        return res == null ? defValue : res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> res = (Set<String>) mStorage.get(key);
        return res == null ? defValues : res;
    }

    @Override
    public int getInt(String key, int defValue) {
        Integer res = (Integer) mStorage.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public long getLong(String key, long defValue) {
        Long res = (Long) mStorage.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Float res = (Float) mStorage.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Boolean res = (Boolean) mStorage.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public boolean contains(String key) {
        return mStorage.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new MapEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.remove(listener);
    }

    private class MapEditor implements SharedPreferences.Editor {
        private final HashMap<String, Object> mAdditions = new HashMap<>();
        private final LinkedList<String> mRemovals = new LinkedList<>();
        private boolean mClear;

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            mAdditions.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            mAdditions.put(key, values);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            mAdditions.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            mAdditions.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            mAdditions.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mAdditions.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            mRemovals.add(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mClear = true;
            return this;
        }

        @Override
        public boolean commit() {
            if (mClear) {
                mRemovals.clear();
                mRemovals.addAll(mStorage.keySet());
                mStorage.clear();
            } else for (String key : mRemovals) mStorage.remove(key);
            mStorage.putAll(mAdditions);

            HashSet<String> changed = new HashSet<>(mRemovals);
            changed.addAll(mAdditions.keySet());

            for (OnSharedPreferenceChangeListener listener : mListeners)
                for (String key : changed)
                    listener.onSharedPreferenceChanged(MapPreferences.this, key);

            mClear = false;
            mRemovals.clear();
            mAdditions.clear();

            return true;
        }

        @Override
        public void apply() {
            commit();
        }
    }
}
