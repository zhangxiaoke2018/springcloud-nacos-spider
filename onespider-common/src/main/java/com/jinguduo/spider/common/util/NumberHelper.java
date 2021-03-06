package com.jinguduo.spider.common.util;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class NumberHelper {

    public static int parseInt(String s, int defaultValue) {
        int r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            r = Integer.parseInt(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }

    public static long parseLong(String s, int defaultValue) {
        long r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            r = Long.parseLong(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }

    public static long bruteParse(String s, long defaultValue) {
        long r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            String t = removeAllNonNumeric(s);
            if (StringUtils.hasLength(t)) {
                r = Long.parseLong(t);
            }
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }

    public static int bruteParse(String s, int defaultValue) {
        int r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            String t = removeAllNonNumeric(s);
            if (StringUtils.hasLength(t)) {
                r = Integer.parseInt(t);
            }
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }

    public static double bruteParse(String s, double defaultValue) {
        double r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            String t = removeAllNonNumeric(s);
            if (StringUtils.hasLength(t)) {
                r = Double.parseDouble(t);
            }
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }
    
    public static float bruteParse(String s, float defaultValue) {
        float r = defaultValue;
        if (!StringUtils.hasText(s)) {
            return r;
        }
        try {
            String t = removeAllNonNumeric(s);
            if (StringUtils.hasLength(t)) {
                r = Float.parseFloat(s);
            }
        } catch (NumberFormatException e) {
            // ignore
            log.error(s, e);
        }
        return r;
    }

    public static String removeAllNonNumeric(String s) {
        return s.replaceAll("[^.0-9-]", "");
    }

    public static String removeAllNonShortNumeric(String s) {
        return s.replaceAll("[^.0-9-]", "");
    }

    /**
     * ????????????(??????????????????)
     * <p>
     * <li>2.2??? -> 22000
     * <li>2?????? -> 200000
     * <li>2?????? -> 20000000
     * <li>24?????? -> 24000000
     * <li>3????????? -> 3000000
     * 
     * <b>??????????????????????????????</b> <i>2???2??? <i>2???4??????
     * 
     * @param shortNumber
     * @param defaultValue
     * @return
     */
    private static Map<Character, Integer> mapping = ImmutableMap.<Character, Integer> of('???', 10, '???', 100, '???', 1000,
            '???', 10000, '???', 100000000);

    public static long parseShortNumber(String shortNumber, long defaultValue) {
        if (!StringUtils.hasText(shortNumber)) {
            return defaultValue;
        }
        double value = 0;
        int radixPoint = 0;
        boolean nearsideIsDigit = false;
        for (int i = 0; i < shortNumber.length(); i++) {
            char ch = shortNumber.charAt(i);
            if (Character.isDigit(ch)) {
                double v = (double) Character.getNumericValue(ch);
                if (radixPoint > 0) {
                    // ex: 2.2 or 2.222
                    v = v * Math.pow(0.1D, radixPoint);
                    value += v;
                    radixPoint++;
                } else {
                    if (nearsideIsDigit) {
                        // ex: 22
                        value = (value * 10) + v;
                    } else {
                        // ex: 2???2
                        value += v;
                    }
                }
                nearsideIsDigit = true;
            } else if ('.' == ch) {
                if (radixPoint != 0) {
                    // bad! ex: 2.1.2??? 2..2???
                    return defaultValue;
                }
                radixPoint = 1;
            } else {
                Integer n = mapping.get(ch);
                if (n != null) {
                    if (radixPoint == 1) {
                        // bad! ex: 2.???
                        return defaultValue;
                    } else if (radixPoint > 1) {
                        radixPoint = -1;
                    }
                    value *= n;
                    nearsideIsDigit = false;
                }
            }
        }
        return (long) value;
    }

    /***
     * ??????????????????????????????
     *   ????????? org.apache.commons.lang3.StringUtils.isNumeric()
     * 
     * @param str
     * @return
     */
    final static Pattern pattern = Pattern.compile("[0-9]*");
    @Deprecated 
    public static boolean isNumeric(String str) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return false;
        }
        return pattern.matcher(str).matches();
    }

    /**
     * ?????????????????????????????????????????????????????? --> 109060???
     * 
     * @author xiaoyun
     * @param chineseNumber
     * @return
     */
    public static Integer chineseNumber2Int(String chineseNumber) {

        if (org.apache.commons.lang3.StringUtils.isBlank(chineseNumber)) {
            return 0;
        }

        int result = 0;
        int temp = 1;// ???????????????????????????????????????
        int count = 0;// ???????????????chArr
        char[] cnArr = new char[] { '???', '???', '???', '???', '???', '???', '???', '???', '???' };
        char[] chArr = new char[] { '???', '???', '???', '???', '???' };
        for (int i = 0; i < chineseNumber.length(); i++) {
            boolean b = true;// ???????????????chArr
            char c = chineseNumber.charAt(i);
            for (int j = 0; j < cnArr.length; j++) {// ?????????????????????
                if (c == cnArr[j]) {
                    if (0 != count) {// ????????????????????????????????????????????????????????????????????????
                        result += temp;
                        temp = 1;
                        count = 0;
                    }
                    // ??????+1?????????????????????
                    temp = j + 1;
                    b = false;
                    break;
                }
            }
            if (b) {// ??????{'???','???','???','???','???'}
                for (int j = 0; j < chArr.length; j++) {
                    if (c == chArr[j]) {
                        switch (j) {
                            case 0:
                                temp *= 10;
                                break;
                            case 1:
                                temp *= 100;
                                break;
                            case 2:
                                temp *= 1000;
                                break;
                            case 3:
                                temp *= 10000;
                                break;
                            case 4:
                                temp *= 100000000;
                                break;
                            default:
                                break;
                        }
                        count++;
                    }
                }
            }
            if (i == chineseNumber.length() - 1) {// ???????????????????????????
                result += temp;
            }
        }
        return result;
    }

    public static boolean isDouble(String str) {
        return Pattern.compile("^[-\\+]?[.\\d]*$").matcher(str).matches();
    }
    
    //LG?????????mac??????
    private static final String[] macAdrPrefLG = {
    		"70:05:14","74:A7:22","88:C9:D0","8C:3A:E3",
    		"8C:54:1D","90:6D:C8","94:44:44","98:93:CC",
    		"98:D6:F7","A8:16:B2","A8:92:2C","B0:61:C7",
    		"B0:89:91","B0:98:9F","B4:0E:DC","58:A2:B5"};
    
    public static String generateFakeMacAddress() {
    	String address = macAdrPrefLG[RandomUtils.nextInt(0,16)];
    	address += (String.format(":%02X:%02X:%02X", RandomUtils.nextInt(0,256),RandomUtils.nextInt(0,256),RandomUtils.nextInt(0,256)));
    	return address;
    }
    
    public static String generateRandomNumberSeries(int len) {
    	String[] number = new String[len];
    	for(int i=0;i<len;i++) {
    		number[i] = String.valueOf(RandomUtils.nextInt(0,10));
    	}
    	return String.join("", number);
    } 
}
