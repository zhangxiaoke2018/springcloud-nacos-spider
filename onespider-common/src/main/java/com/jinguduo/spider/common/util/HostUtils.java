package com.jinguduo.spider.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class HostUtils {

    public static String getHostName() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "bad-" + RandomStringUtils.randomAlphanumeric(3);
            log.error(e.getMessage(), e);
        }
        return hostname;
    }
}
