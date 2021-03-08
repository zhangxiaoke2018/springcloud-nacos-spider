package com.jinguduo.spider.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/29 上午10:10
 */
public class DateHelper {

    public static Date lastDayWholePointDate(Date date, Integer beforeDay) {

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        if ((gc.get(gc.HOUR_OF_DAY) == 0) && (gc.get(gc.MINUTE) == 0)
                && (gc.get(gc.SECOND) == 0)) {
            return new Date(date.getTime() - beforeDay * (24 * 60 * 60 * 1000));
        } else {
            Date date2 = new Date(date.getTime() - gc.get(gc.HOUR_OF_DAY) * 60 * 60
                    * 1000 - gc.get(gc.MINUTE) * 60 * 1000 - gc.get(gc.SECOND)
                    * 1000 - beforeDay * (24 * 60 * 60 * 1000));
            return date2;
        }
    }
    /***
     * LocalDate ==> Date
     * @param localDate
     * @return
     */
    public static Date lDToUdate(LocalDateTime localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atZone(zone).toInstant();
        return Date.from(instant);
    }
    
    /**
     * 获得当日零点时间对象
     * 
     * @param type
     * @return
     */
    public static <T extends java.util.Date> T getTodayZero(Class<T> type) {
        return getBeforeDayZero(0, type);
    }
    
    public static <T extends java.util.Date> T getYesterdayZero(Class<T> type) {
        return getBeforeDayZero(1, type);
    }
    
    public static <T extends java.util.Date> T getTomorrowZero(Class<T> type) {
        return getBeforeDayZero(-1, type);
    }
    
    /**
     * 获得几日前零点对象
     * @param d
     * @param type
     * @return
     */
    public static <T extends java.util.Date> T getBeforeDayZero(int d, Class<T> type) {
        ZonedDateTime today = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault());
        if (d != 0) {
            today = today.minusDays(d);
        }
        try {
            long millis = today.toInstant().toEpochMilli();
            Constructor<T> constructor = type.getConstructor(long.class);
            if (constructor != null) {
                T t = constructor.newInstance(millis);
                return t;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
    /**
     * 获取今天是周几，中国周
     * */
    public static int getDayInWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayInWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayInWeek = 7;
        } else {
            dayInWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayInWeek;
    }
    /**
     * 获取某日期所在周的周日的日期
     * */
    public static Date getWeekEndDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.getFirstDayOfWeek() == 1) {
            cal.add(Calendar.DATE, -1);
        }
        cal.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR,1);
        Date sunday = cal.getTime();
        return sunday;
    }
    /**
     * 获取某日期所在周的周一的日期
     */
    public static Date getWeekStartDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.getFirstDayOfWeek() == 1) {
            cal.add(Calendar.DATE, -1);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monday = cal.getTime();
        return monday;
    }
}
