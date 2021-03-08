package com.jinguduo.spider.common.util;

import java.util.Map.Entry;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

@ActiveProfiles("test")
public class NumberHelperTests {

    @Test
    public void testRemoveAllNonNumeric() {
        ImmutableMap<String, String> cases = new ImmutableMap.Builder<String, String>()
            .put("-0192313", "-0192313")
            .put("+123123", "123123")
            .put(" +123123 ", "123123")
            .put(" +123,123 ", "123123")
            .put(" +9,123,123 ", "9123123")
            .put(" +0,123,123 ", "0123123")
            .put(" -123123 ", "-123123")
            .put(" -1231.23 ", "-1231.23")
            .put(" +1,123,123次", "1123123")
            .put("总播放：2,123,123 次", "2123123")
            .build();
           
        for (Entry<String, String> entry : cases.entrySet()) {
            String v = NumberHelper.removeAllNonNumeric(entry.getKey());
            Assert.isTrue(entry.getValue().equals(v), String.format("[%s]: [%s] != [%s]", entry.getKey(), entry.getValue(), v));
        }
    }
    
    @Test
    public void testParseShortNumber() {
        ImmutableMap<String, Long> cases = new ImmutableMap.Builder<String, Long>()
                .put("2千", 2000L)
                .put("2千4", 2004L)
                .put("2万", 20000L)
                .put("2万2", 20002L)
                //.put("2万2百", 20200L) Bad!
                .put("2.2万", 22000L)
                .put("2十万", 200000L)
                .put(" 2千万 ", 20000000L)
                .put(" 24千万 ", 240000000L)
                .put(" 2.75亿 ", 275000000L)
                //.put(" 2千4百万 ", 24000000L) Bad!
                //.put(" 2千4十万 ", 20400000L) Bad!
                .put(" 2万人 ", 20000L)
                .put(" 3百万次", 3000000L)
                .put("总播放：430万 次", 4300000L)
                .put(" 2.2.2万 ", 0L)
                .put(" 2.22万.2 ", 0L)
                .build();
               
            for (Entry<String, Long> entry : cases.entrySet()) {
                long v = NumberHelper.parseShortNumber(entry.getKey(), 0L);
                Assert.isTrue(entry.getValue().equals(v), String.format("[%s]: [%s] != [%s]", entry.getKey(), entry.getValue(), v));
            }
    }
}
