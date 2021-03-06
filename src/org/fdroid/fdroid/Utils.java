
package org.fdroid.fdroid;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import net.binaryparadox.kerplapp.FDroidApp;

import org.fdroid.fdroid.data.Repo;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public final class Utils {

    /* this stuff is already included in FDroid */
    public static final int BUFFER_SIZE = 4096;

    // The date format used for storing dates (e.g. lastupdated, added) in the
    // database.
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static Uri getSharingUri(Context context, Repo repo) {
        Uri uri = Uri.parse(repo.address.replaceFirst("http", "fdroidrepo"));
        Uri.Builder b = uri.buildUpon();
        if (!TextUtils.isEmpty(repo.fingerprint))
            b.appendQueryParameter("fingerprint", repo.fingerprint);
        if (!TextUtils.isEmpty(FDroidApp.bssid)) {
            b.appendQueryParameter("bssid", Uri.encode(FDroidApp.bssid));
            if (!TextUtils.isEmpty(FDroidApp.ssid))
                b.appendQueryParameter("ssid", Uri.encode(FDroidApp.ssid));
        }
        return b.build();
    }

    public static class CommaSeparatedList implements Iterable<String> {
        private String value;

        private CommaSeparatedList(String list) {
            value = list;
        }

        public static CommaSeparatedList make(List<String> list) {
            if (list == null || list.size() == 0)
                return null;
            else {
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < list.size(); i ++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(list.get(i));
                }
                return new CommaSeparatedList(sb.toString());
            }
        }

        public static CommaSeparatedList make(String list) {
            if (list == null || list.length() == 0)
                return null;
            else
                return new CommaSeparatedList(list);
        }

        public static String str(CommaSeparatedList instance) {
            return (instance == null ? null : instance.toString());
        }

        @Override
        public String toString() {
            return value;
        }

        public String toPrettyString() {
            return value.replaceAll(",", ", ");
        }

        @Override
        public Iterator<String> iterator() {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(value);
            return splitter.iterator();
        }

        public boolean contains(String v) {
            for (String s : this) {
                if (s.equals(v))
                    return true;
            }
            return false;
        }
    }

    // this is all new stuff being added
    public static String hashBytes(byte[] input, String algo) {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            byte[] hashBytes = md.digest(input);
            String hash = toHexString(hashBytes);

            md.reset();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            Log.e("FDroid", "Device does not support " + algo + " MessageDisgest algorithm");
            return null;
        }
    }

    public static String getBinaryHash(File apk, String algo) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            fis = new FileInputStream(apk);
            bis = new BufferedInputStream(fis);

            byte[] dataBytes = new byte[524288];
            int nread = 0;

            while ((nread = bis.read(dataBytes)) != -1)
                md.update(dataBytes, 0, nread);

            byte[] mdbytes = md.digest();
            return toHexString(mdbytes);
        } catch (IOException e) {
            Log.e("FDroid", "Error reading \"" + apk.getAbsolutePath() + "\" to compute SHA1 hash.");
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.e("FDroid", "Device does not support " + algo + " MessageDisgest algorithm");
            return null;
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    return null;
                }
        }
    }

    /**
     * Computes the base 16 representation of the byte array argument.
     *
     * @param bytes an array of bytes.
     * @return the bytes represented as a string of hexadecimal digits.
     */
    public static String toHexString(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static String getDefaultRepoName() {
        return (Build.BRAND + " " + Build.MODEL).replaceAll(" ", "-");
    }

}
