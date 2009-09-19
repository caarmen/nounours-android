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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class PhoneHome {
    static final String TAG = PhoneHome.class.getName();
    private static final String PREF_LAST_PHONE_HOME = "lastPhoneHome";
    private static final boolean DEBUG_PHONE_HOME_TEST_URL = false;
    private static final String PREF_UID = "uid";
    private static final boolean DEBUG_PHONE_HOME_ALWAYS = false;
    private static final String PACKAGE_NAME = "ca.rmen.nounours";


    private static final String SEP = "/";
    static final String SDK_VERSION_AND_BUILD_NUMBER = (Build.VERSION.SDK + SEP + Build.VERSION.INCREMENTAL).replace(
            ' ', '+');
    static final String DEVICE = Build.DEVICE + SEP + Build.MODEL + SEP + Build.PRODUCT + SEP + Build.BRAND;

    private static final String PHONE_HOME_URL = DEBUG_PHONE_HOME_TEST_URL ? "http://192.168.0.11:8080/ph/ph"
            : "http://r24591.ovh.net/ph/ph";

    public static void phoneHome(final Context context, final SharedPreferences sharedPreferences,
            final String otherInfo, final String otherInfo2) {
        final long lastPhoneHome = sharedPreferences.getLong(PREF_LAST_PHONE_HOME, -1);
        if (DEBUG_PHONE_HOME_ALWAYS) {
            if (lastPhoneHome != -1 && System.currentTimeMillis() - lastPhoneHome < 1000 * 4) {
                return;
            }
        } else {
            if (lastPhoneHome != -1 && System.currentTimeMillis() - lastPhoneHome < 1000 * 60 * 60 * 24) {
                return;
            }
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    String uidStr;
                    if (!sharedPreferences.contains(PREF_UID)) {
                        uidStr = generateAndSaveUid(sharedPreferences);
                    } else {
                        uidStr = sharedPreferences.getString(PREF_UID, "nouid");
                    }

                    final int versionCode = getVersionCode(context);
                    if (versionCode == -1) {
                        // packageManager.getPackageInfo() went berserk
                        return;
                    }

                    final String networkOperator = getNetworkOperator(context);

                    HttpClient httpClient = null;
                    try {
                        httpClient = getHttpClient();
                        final String url = PHONE_HOME_URL // url
                                + "?a=" + URLEncoder.encode(PACKAGE_NAME, "utf-8") // package (app) name
                                + "&v=" + URLEncoder.encode("" + versionCode, "utf-8") // versionCode
                                + "&u=" + URLEncoder.encode(uidStr, "utf-8") // uid
                                + "&s=" + URLEncoder.encode(SDK_VERSION_AND_BUILD_NUMBER, "utf-8") // sdk
                                + "&d=" + URLEncoder.encode(DEVICE, "utf-8") // device
                                + "&n=" + URLEncoder.encode(networkOperator, "utf-8") // network
                                + (otherInfo == null ? "" : "&o=" + URLEncoder.encode(otherInfo, "utf-8")) // other info
                                + (otherInfo2 == null ? "" : "&o2=" + URLEncoder.encode(otherInfo2, "utf-8")) // other info 2
                        ;

                        final HttpHead httpHead = new HttpHead(url);
                        httpClient.execute(httpHead);

                        final Editor editor = sharedPreferences.edit();
                        editor.putLong(PREF_LAST_PHONE_HOME, System.currentTimeMillis());
                        editor.commit();
                    } catch (final ClientProtocolException e) {
                        Log.w(TAG, "Could not phone home", e);
                    } catch (final IOException e) {
                        Log.w(TAG, "Could not phone home", e);
                    } finally {
                        if (httpClient != null && httpClient.getConnectionManager() != null) {
                            httpClient.getConnectionManager().shutdown();
                        }
                    }
                } catch (final Throwable t) {
                    // catch-all block, because we really *really* don't want to cause any problem to the caller
                    Log.e(TAG, "Unexpected Throwable while phoning home", t);
                }
            }
        }.start();
    }

    public static int getVersionCode(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(PACKAGE_NAME, 0);
        } catch (final NameNotFoundException e) {
            // this can't happen
            Log.e(TAG, "packageManager.getPackageInfo() went berserk", e);
            return -1;
        }
        return packageInfo.versionCode;
    }

    static String getNetworkOperator(final Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator() + SEP + telephonyManager.getNetworkOperatorName() + SEP
                + telephonyManager.getNetworkCountryIso();
    }

    static HttpClient getHttpClient() {
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
        HttpConnectionParams.setSoTimeout(httpParams, 8000);
        return new DefaultHttpClient(httpParams);
    }

    static String generateAndSaveUid(final SharedPreferences sharedPreferences) {
        final UUID uuid = UUID.randomUUID();
        final String uidStr = uuid.toString();
        final Editor editor = sharedPreferences.edit();
        editor.putString(PREF_UID, uidStr);
        editor.commit();
        return uidStr;
    }
}
