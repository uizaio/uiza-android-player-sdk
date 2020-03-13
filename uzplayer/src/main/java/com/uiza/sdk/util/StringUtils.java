package com.uiza.sdk.util;


import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import timber.log.Timber;


public final class StringUtils {

    /**
     * Email validation pattern.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$");

    // default constructor
    private StringUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    /**
     * Validates if the given input is a valid email address.
     *
     * @param email The email to validate.
     * @return {@code true} if the input is a valid email. {@code false} otherwise.
     */
    public static boolean isEmailValid(@Nullable CharSequence email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * convert html to plain text
     *
     * @param htmlText : html String
     * @return plain text
     */
    public static String htmlToPlainText(String htmlText) {
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(htmlText);
        }
        char[] chars = new char[spanned.length()];
        TextUtils.getChars(spanned, 0, spanned.length(), chars, 0);
        return new String(chars);
    }

    /**
     * Convert UTC time string to long value
     *
     * @param timeStr the time with format <code>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</code>
     * @return UTC time as long value
     */
    public static long convertUTCMs(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = dateFormat.parse(timeStr);
            return date == null ? -1 : date.getTime();
        } catch (ParseException e) {
            Timber.e(e);
            return -1;
        }
    }

    public static String convertSecondsToHMmSs(long seconds) {
        if (seconds <= 0) {
            return "0:00";
        }
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if (h == 0) {
            return String.format(Locale.getDefault(), "%d:%02d", m, s);
        } else {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
        }
    }

    public static String convertMlsecondsToHMmSs(long mls) {
        return convertSecondsToHMmSs(mls / 1000);
    }

    public static String groupingSeparatorLong(long value) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("###,###", decimalFormatSymbols);
        return decimalFormat.format(value);
    }

    public static String doubleFormatted(double value, int precision) {
        return new DecimalFormat(
                "#0." + (precision <= 1 ? "0" : precision == 2 ? "00" : "000")).format(value);
    }

    public static String humanReadableByteCount(long bytes, boolean si, boolean isBits) {
        int unit = !si ? 1000 : 1024;
        if (bytes < unit) return bytes + " KB";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return isBits ? String.format(Locale.getDefault(), "%.1f %sb", bytes / Math.pow(unit, exp), pre)
                : String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}