/*
This source is part of the
     _____  ___   ____
 __ / / _ \/ _ | / __/___  _______ _
/ // / , _/ __ |/ _/_/ _ \/ __/ _ `/
\___/_/|_/_/ |_/_/ (_)___/_/  \_, /
                             /___/
repository. It is licensed under a Creative Commons
Attribution-Noncommercial-Share Alike 3.0 Unported License:
http://creativecommons.org/licenses/by-nc-sa/3.0.
Contact BoD@JRAF.org for more information.

$Id: PhoneHome.java 625 2009-04-26 22:45$
*/

package ca.rmen.nounours.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GoogleAnalyticsHelper {
    private static final String TAG = GoogleAnalyticsHelper.class.getName();

    private static final String SEP = "/";
    private static final String SDK_VERSION_AND_BUILD_NUMBER = (Build.VERSION.SDK + SEP + Build.VERSION.INCREMENTAL)
            .replace(' ', '+');
    private static final String DEVICE = Build.DEVICE + SEP + Build.MODEL + SEP + Build.PRODUCT + SEP + Build.BRAND;

    private static final String PREF_LAST_PHONE_HOME = GoogleAnalyticsHelper.class.getName() + ".lastPhoneHome";

    public static void track(final Context context, final SharedPreferences sharedPreferences,
            final boolean alwaysTrack, final String analyticsTrackerId, final String otherInfo1, final String otherInfo2) {

        GoogleAnalyticsTracker tracker = null;

        try {
            final long lastPhoneHome = sharedPreferences.getLong(PREF_LAST_PHONE_HOME, -1);
            if (alwaysTrack) {
                if (lastPhoneHome != -1 && System.currentTimeMillis() - lastPhoneHome < 1000 * 4) {
                    return;
                }
            } else {
                if (lastPhoneHome != -1 && System.currentTimeMillis() - lastPhoneHome < 1000 * 60 * 60 * 24) {
                    return;
                }
            }

            final int versionCode = getVersionCode(context);
            if (versionCode == -1) {
                // packageManager.getPackageInfo() went berserk
                return;
            }

            final String networkOperator = getNetworkOperator(context);

            tracker = GoogleAnalyticsTracker.getInstance();
            tracker.start(analyticsTrackerId, context);

            tracker.trackPageView("/phonehome");

            tracker.trackEvent("versionCode", Integer.toString(versionCode), null, 0);
            tracker.trackEvent("sdk", SDK_VERSION_AND_BUILD_NUMBER, null, 0);
            tracker.trackEvent("device", DEVICE, null, 0);
            tracker.trackEvent("network", networkOperator, null, 0);
            if (otherInfo1 != null) {
                tracker.trackEvent("otherInfo1", otherInfo1, null, 0);
            }
            if (otherInfo2 != null) {
                tracker.trackEvent("otherInfo2", otherInfo2, null, 0);
            }

            // blocking?
            tracker.dispatch();

            final Editor editor = sharedPreferences.edit();
            editor.putLong(PREF_LAST_PHONE_HOME, System.currentTimeMillis());
            editor.commit();

        } catch (final Throwable t) {
            // catch-all block, because we really *really* don't want to cause any problem to the caller
            Log.e(TAG, "Unexpected Throwable while phoning home", t);
        } finally {
            if (tracker != null) {
                tracker.stop();
            }
        }
    }

    private static int getVersionCode(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (final NameNotFoundException e) {
            // this can't happen
            Log.e(TAG, "packageManager.getPackageInfo() went berserk", e);
            return -1;
        }
        return packageInfo.versionCode;
    }

    private static String getNetworkOperator(final Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator() + SEP + telephonyManager.getNetworkOperatorName() + SEP
                + telephonyManager.getNetworkCountryIso();
    }
}
