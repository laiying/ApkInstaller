package com.strod.apkinstaller.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public final static String YYYYMMDD_HHMMSS = "yyyyMMdd_HHmmss";
    public final static String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String HH_MM_SS = "HH时mm分ss秒";


    public static String formatTime(long time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(time));
    }

    public static String formatTime(String date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(date));
    }

    private static boolean isTimeInThisYear(long millis) {

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        int year = c.get(Calendar.YEAR);
        if (year - getCurrentYear() == 0) {//在同一年
            return true;
        } else {
            return false;
        }
    }

    public static int getCurrentYear() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return c.get(Calendar.YEAR);
    }

    /**
     * 当前月份
     *
     * @return
     */
    public static int getCurrentMonth() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return c.get(Calendar.MONTH);
    }

    /**
     * 当前日期
     *
     * @return
     */
    public static int getCurrentDay() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取每天的某时某分某秒的时间戳
     *
     * @return
     */
    public static long getTimesInDay(int hour, int min, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getTimesAt(int year, int month, int day, int hour, int min, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    /** 秒 时间戳*/
    public static long getTimesSec(int year, int month, int day, int hour, int min, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        long sec = cal.getTimeInMillis()/1000;
        return sec;
    }

    /***
     * 往某个时间上增加
     * @param time
     * @param seconds
     * @return
     */
    public static long getTimesUp(long time, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.SECOND, seconds);

        return calendar.getTimeInMillis();
    }

    public static long getTimesNextMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MONTH, 1);

        return calendar.getTimeInMillis();
    }

    public static long getTimesNextMonth(long time, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MONTH, month);

        return calendar.getTimeInMillis();
    }

    public static long getTimesPreMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MONTH, -1);

        return calendar.getTimeInMillis();
    }

    public static long getTimesPreMonth(long time, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.MONTH, -month);

        return calendar.getTimeInMillis();
    }

    public static long getTime(String time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            //ignore
        }

        return 0;
    }

    public static int getHour(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MINUTE);
    }

    public static int getSecond(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.SECOND);
    }


    /**
     * 判断是否超出当前时间
     *
     * @param time
     * @return
     */
    public static boolean isEndTimeByCurrent(long time) {
        long currentTime = System.currentTimeMillis();

        if (currentTime > time) {
            return true;
        }
        return false;
    }

    /**
     * 秒转成时长hh:mm:ss格式
     */
    @SuppressLint("DefaultLocale")
    public static String transferToDurationFromSecond(long second) {
        if (0 == second) {
            return "";
        }
        String s = String.format("%02d", second % 60);
        String m = String.format("%02d", (second / 60) % 60);
        String h = String.format("%02d", (second / 3600) % 24);
        return h + ":" + m + ":" + s;
    }

    /**
     * 是否是今天
     *
     * @param date
     * @return
     */
    public static boolean isToday(Date date) {
        return isSameDay(new Date(), date);
    }

    public static boolean isYesterday(Date date) {
        return isSameDay(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), date);
    }

    /**
     * 是否是同一天
     *
     * @param src
     * @param dest
     * @return
     */
    public static boolean isSameDay(Date src, Date dest) {
        Calendar calDateA = Calendar.getInstance();
        calDateA.setTime(src);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTime(dest);

        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
                && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
                && calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
    }
}
