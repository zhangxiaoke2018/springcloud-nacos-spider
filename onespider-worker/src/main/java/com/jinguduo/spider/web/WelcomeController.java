package com.jinguduo.spider.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@ResponseBody
public class WelcomeController {

    @RequestMapping(value = {"/", "/index"})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String welcome() {
        return null;
    }
}
