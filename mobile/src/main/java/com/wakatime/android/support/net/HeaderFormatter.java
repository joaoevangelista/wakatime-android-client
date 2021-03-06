package com.wakatime.android.support.net;

import android.util.Base64;

import com.wakatime.android.api.Key;

import io.realm.Realm;

import static java.lang.String.format;

/**
 * @author Joao Pedro Evangelista
 */
public class HeaderFormatter {

    public static String get(Realm realm) {
        Key first = realm.where(Key.class).findFirst();
        if (first != null) {
            String key = first.getKey();
            byte[] encoded = Base64.encode(key.getBytes(), Base64.DEFAULT);
            String strEncoded = new String(encoded);
            String s = strEncoded.replaceAll("\\r|\\t|\\n", "");
            return toHeader(s);
        } else {
            return "";
        }

    }

    private static String toHeader(String encoded) {
        return format("Basic %s", encoded);
    }
}
