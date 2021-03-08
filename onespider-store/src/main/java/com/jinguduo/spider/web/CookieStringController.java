package com.jinguduo.spider.web;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.CookieString;
import com.jinguduo.spider.service.CookieStringService;

@Controller
@ResponseBody
public class CookieStringController {
    
    @Autowired
    private CookieStringService cookieStringService;

    @RequestMapping(value = "cookie_string", method = RequestMethod.GET)
    public Iterable<CookieString> list(@RequestParam(name = "domain") String domain,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "100") Integer size) {
        
        Page<CookieString> cookies = cookieStringService.findAllByDomain(domain, page, size);
        if (cookies == null || cookies.getSize() == 0) {
            return new ArrayList<>();
        } else {
            return cookies.getContent();
        }
    }
    
    @RequestMapping(value = "cookie_string", method = RequestMethod.POST)
    public CookieString post(
            @RequestBody CookieString cookieString) {
        return cookieStringService.save(cookieString);
    }
}
