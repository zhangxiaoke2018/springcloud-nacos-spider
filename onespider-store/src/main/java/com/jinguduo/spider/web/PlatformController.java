package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Platform;
import com.jinguduo.spider.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/20 下午2:48
 */
@Controller
@ResponseBody
public class PlatformController {

    @Autowired
    private PlatformService platformService;

    @RequestMapping(value = "platform",method = RequestMethod.GET)
    public Object getByUrl(@RequestParam String url){

        Platform platform = platformService.find(url);

        return platform;
    }

}
