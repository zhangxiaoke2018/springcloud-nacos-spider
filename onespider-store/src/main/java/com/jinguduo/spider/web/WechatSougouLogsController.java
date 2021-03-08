package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.WechatSougouLog;
import com.jinguduo.spider.service.WechatSougouLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 05/12/2016 7:37 PM
 */
@RestController
@RequestMapping("/wechat_sougou_logs")
public class WechatSougouLogsController {

    @Autowired
    private WechatSougouLogsService wechatSougouLogsService;

    @RequestMapping(value = "",method = RequestMethod.POST)
    public void save(@RequestBody WechatSougouLog wechatSougouLog){
        wechatSougouLogsService.save(wechatSougouLog);
    }


}
