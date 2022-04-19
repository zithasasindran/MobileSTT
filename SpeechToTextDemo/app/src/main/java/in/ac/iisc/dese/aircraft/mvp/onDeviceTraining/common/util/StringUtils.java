package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.common.util;

import android.text.format.DateFormat;

public final class StringUtils {

    private StringUtils() {

    }

    public static String convertMillisToDateFormat(String dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }
}
