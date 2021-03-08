package com.jinguduo.spider.common.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by csonezp on 2017/3/1.
 */
public class DateUtil {
    public static String getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return DateFormatUtils.format(cal, "yyyy-MM-dd");
    }

    public static String getToday() {
        Calendar cal = Calendar.getInstance();
        return DateFormatUtils.format(cal, "yyyy-MM-dd");
    }

    public static String getStrDay(Date date, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, offset);
        return DateFormatUtils.format(cal, "yyyy-MM-dd");
    }

    public static Date getDayStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getDayEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static Date plusHours(Date date, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, value);
        return calendar.getTime();
    }

    public static Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }


    private static Pattern YESTERDAY = Pattern.compile("昨天([0-9]{2}):([0-9]{2})");
    private static Pattern MINUTE = Pattern.compile("([0-9]+)分钟前");
    private static Pattern HOUR = Pattern.compile("([0-9]+)小时前");
    private static Pattern DAY = Pattern.compile("([0-9]+)天前");
    private static Pattern WEEK = Pattern.compile("([0-9]+)星期前");
    private static Pattern MONTH = Pattern.compile("([0-9]+)个月前");
    private static Pattern YEAR = Pattern.compile("([0-9]+)年前");
    private static Pattern DATE = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2})");
    public static Date dateParse(String dateStr) {
    	Calendar c = Calendar.getInstance();
    	int number;
    	Date d = null;
    	if("刚刚".equals(dateStr)) {
    		return c.getTime();
    	}else if(YESTERDAY.matcher(dateStr).matches()) {
    		Integer hour = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, YESTERDAY, 1));
    		Integer minute = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, YESTERDAY, 2));
    		c.set(Calendar.MINUTE,minute);
    		c.set(Calendar.HOUR, hour);
    		c.add(Calendar.DATE, -1);
    		d = c.getTime();
    	}else if(DATE.matcher(dateStr).matches()) {
    		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    		try {
				return f.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return c.getTime();
    	}else if(MINUTE.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, MINUTE, 1));
    		c.add(Calendar.MINUTE, -1*number);
    		d = c.getTime();
    	}else if(HOUR.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, HOUR, 1));
    		c.set(Calendar.MINUTE,0);
    		c.add(Calendar.HOUR, -1*(number-1));
    		d = c.getTime();
    	}else if(DAY.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, DAY, 1));
    		c.set(Calendar.MINUTE,0);
    		c.set(Calendar.HOUR, 0);
    		c.add(Calendar.DATE, -1*number);
    		d = c.getTime();
    	}else if(WEEK.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, WEEK, 1));
    		c.set(Calendar.MINUTE,0);
    		c.set(Calendar.HOUR, 0);
    		c.add(Calendar.DATE, -7*number);
    		d = c.getTime();
    	}else if(MONTH.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, MONTH, 1));
    		c.set(Calendar.MINUTE,0);
    		c.set(Calendar.HOUR, 0);
    		c.add(Calendar.DATE, -30*number);
    		d = c.getTime();
    	}else if(YEAR.matcher(dateStr).matches()) {
    		number = Integer.valueOf(RegexUtil.getStringByPattern(dateStr, YEAR, 1));
    		c.set(Calendar.MINUTE,0);
    		c.set(Calendar.HOUR, 0);
    		c.add(Calendar.DATE, -365*number);
    		d = c.getTime();
    	}
    	return d;
    }
}
