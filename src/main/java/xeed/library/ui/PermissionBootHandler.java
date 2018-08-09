package xeed.library.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import xeed.library.common.SettingsManager;
import xeed.library.common.Utils;

public class PermissionBootHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Utils.TAG, "Boot received, fixing permissions");
        SettingsManager.getInstance(context).fixFolderPermissionsAsync();
    }
}
