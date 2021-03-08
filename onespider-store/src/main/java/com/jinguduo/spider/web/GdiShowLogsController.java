package com.jinguduo.spider.web;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.GdiActorLogs;
import com.jinguduo.spider.data.table.GdiShowLogs;
import com.jinguduo.spider.service.GdiLogsService;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月11日 下午5:09:59
 *
 */
@RestController
@RequestMapping("/gdi")
public class GdiShowLogsController {
    
    @Autowired
    private GdiLogsService gdiLogsService;

    @RequestMapping(value = "/show/find", method = RequestMethod.GET)
    public GdiShowLogs getGdiShowLogsByLinkedIdAndDay(@RequestParam Integer linkedId,@RequestParam String day) {
        try {
            Date date = DateUtils.parseDate(day,"yyyy-MM-dd");
            return gdiLogsService.getGdiShowLogsByUk(linkedId, date);
        } catch (ParseException e) {
            return null;
        }
    }
    
    @RequestMapping(value = "/actor/find", method = RequestMethod.GET)
    public GdiActorLogs getGdiActorLogsByLinkedIdAndDay(@RequestParam Integer linkedId,@RequestParam String day) {
        try {
            Date date = DateUtils.parseDate(day,"yyyy-MM-dd");
            return gdiLogsService.getGdiActorLogsByUk(linkedId, date);
        } catch (ParseException e) {
            return null;
        }
    }
}
