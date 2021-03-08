package com.jinguduo.spider.common.util;

import java.util.HashMap;
import java.util.Map;

import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.JobSchedulerCommand;

public class EnumUtils {

    //将platform转为map 供页面匹配
    public static Map<Integer,String> covertPlatformToMap(){
        Map<Integer,String> map = new HashMap<Integer, String>();
        Platform[] values = Platform.values();
        for (Platform p : values) {
            map.put(p.getCode(), p.getDes());
        }
        return map;
    }
}
