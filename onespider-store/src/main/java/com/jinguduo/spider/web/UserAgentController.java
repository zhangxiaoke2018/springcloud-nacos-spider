package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.common.constant.UserAgentKind;
import com.jinguduo.spider.data.table.UserAgent;
import com.jinguduo.spider.service.UserAgentService;

@Controller
@ResponseBody
public class UserAgentController {
    
    @Autowired
    private UserAgentService userAgentService;

    @RequestMapping(value = "/user_agent", method = RequestMethod.GET)
    public Iterable<UserAgent> list(
            @RequestParam(name = "kind", required = false) UserAgentKind kind) {
        
        if (kind == null) {
            kind = UserAgentKind.PC;  // default
        }
        return userAgentService.findByKind(kind);
    }
}
