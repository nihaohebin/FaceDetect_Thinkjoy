package com.tjoydoor.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间处理工具
 * Created by Nereo on 2015/4/8.
 */

public class TimeUtil {

    private static Calendar mCalendar;


    public static String getNumberTime() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        return sf.format(new Date());
    }

    public static String getPostTime() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sf.format(new Date());
    }


    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }
        String sec;
        if (mCalendar.get(Calendar.SECOND) < 10) {
            sec = "0" + mCalendar.get(Calendar.SECOND);
        } else {
            sec = mCalendar.get(Calendar.SECOND) + "";
        }
        return mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 1) + "-" + mCalendar.get(Calendar.DAY_OF_MONTH)
                + " " + hour + ":" + min + ":" + sec;

    }


    /**
     * 获取当前年月
     */
    public static String getTimeYearMoth() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        return format.format(date);
    }

    /**
     * 获取当前年月日
     */
    public static String getCurrentTimeYMD() {
        mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.YEAR) + "年" + (mCalendar.get(Calendar.MONTH) + 1) + "月" + mCalendar.get(Calendar.DAY_OF_MONTH) + "日";
    }

    /**
     * 获取年
     */
    public static String getYear() {
        mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.YEAR) + "";
    }

    /**
     * 获取月
     */
    public static String getMonth() {
        mCalendar = Calendar.getInstance();

        String month = "";

        if (mCalendar.get(Calendar.MONTH) + 1 < 10) {
            month = "0" + (mCalendar.get(Calendar.MONTH) + 1);
        } else {
            month = "" + (mCalendar.get(Calendar.MONTH) + 1);
        }
        return month;
    }

    /**
     * 获取日
     */
    public static String getDay() {
        mCalendar = Calendar.getInstance();
        String day = "";
        if (mCalendar.get(Calendar.DAY_OF_MONTH) < 10) {
            day = "0" + mCalendar.get(Calendar.DAY_OF_MONTH);
        } else {
            day = "" + mCalendar.get(Calendar.DAY_OF_MONTH);
        }


        return day;
    }

    /**
     * 获取季度
     */
    public static String getSeason() {
        mCalendar = Calendar.getInstance();
        String season = "";
        switch (mCalendar.get(Calendar.MONTH) + 1) {
            case 1:
            case 2:
            case 3:
                season = "一季度";

                break;
            case 4:
            case 5:
            case 6:
                season = "二季度";
                break;
            case 7:
            case 8:
            case 9:
                season = "三季度";
                break;
            case 10:
            case 11:
            case 12:
                season = "四季度";
                break;
        }

        return season;
    }

    /**
     * 星期几
     * Date 日期
     *
     * @return 星期一到星期日
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }

    public static Date getCurrentDate() {

        Date date = new Date(System.currentTimeMillis());
        return date;
    }

    /**
     * 获取当前年月日
     *
     * @return
     */
    public static String getCurrentTimeofYear() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }

        return mCalendar.get(Calendar.YEAR) + "年" + (mCalendar.get(Calendar.MONTH) + 1) + "月" + mCalendar.get(Calendar.DAY_OF_MONTH) + "日";

    }

    /**
     * 获取当前时分
     */
    public static String getCurrentTimeofHour() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }

        return hour + ":" + min;

    }

    public static String getCurrentHour() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

//        String min;
//        if (mCalendar.get(Calendar.MINUTE) < 10) {
//            min = "0" + mCalendar.get(Calendar.MINUTE);
//        } else {
//            min = mCalendar.get(Calendar.MINUTE) + "";
//        }
        return hour;
    }


    /**
     * 获取当前时分
     */
    public static String getCurrentTimeHM() {
        mCalendar = Calendar.getInstance();

        String hour;
        if (mCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + mCalendar.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = mCalendar.get(Calendar.HOUR_OF_DAY) + "";
        }

        String min;
        if (mCalendar.get(Calendar.MINUTE) < 10) {
            min = "0" + mCalendar.get(Calendar.MINUTE);
        } else {
            min = mCalendar.get(Calendar.MINUTE) + "";
        }

        return hour + ":" + min;
    }

    /**
     * 获取唯一标记
     */
    public static String getMsgId() {

        mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.YEAR) + (mCalendar.get(Calendar.MONTH) + 1) + mCalendar.get(Calendar.DAY_OF_MONTH)
                + mCalendar.get(Calendar.HOUR_OF_DAY) + mCalendar.get(Calendar.MINUTE) + mCalendar.get(Calendar.SECOND) + "";
    }

    /**
     * 获取处理date传过来的时间解码
     */
    public static String getDealTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return sdf.format(date);
    }


    /**
     * 处理2016-1-1 变成2016-01-01
     */

    public static String dtime(String time) {
        try {
            int first = time.indexOf("-");
            int second = time.lastIndexOf("-");
            String mon = time.substring(first + 1, second);
            String day = time.substring(second + 1, time.length());

            int month = Integer.parseInt(mon);
            int day2 = Integer.parseInt(day);
            if (month < 10) {
                mon = "0" + month;
            } else {
                mon = month + "";
            }
            if (day2 < 10) {
                day = "0" + day2;
            } else {
                day = day2 + "";
            }
            return time.substring(0, first) + "-" + mon + "-" + day;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
