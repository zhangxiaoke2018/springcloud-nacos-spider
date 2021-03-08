package com.jinguduo.spider.web;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;

import jdk.net.SocketFlow;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.google.common.collect.Lists;
import com.jinguduo.spider.common.constant.StatusEnum;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.data.table.Seed;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.service.SeedService;
import com.jinguduo.spider.service.ShowService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/17 下午3:55
 */
@RestController
@ResponseBody
public class SeedController {

    @Resource
    private SeedService seedService;

    @Resource
    private ShowService showService;

    @RequestMapping(value = "seed",method = RequestMethod.POST)
    public Object addSeed(@RequestBody Seed seed){
        Seed s = seedService.insertOrUpdate(seed);
        return s;
    }

    @RequestMapping(value = "seed/code",method = RequestMethod.GET)
    public Object getCode(@RequestParam String url){
        String code = seedService.findCode(url);
        return code;
    }
    @RequestMapping(value = "seed",method = RequestMethod.GET)
    public Object get(@RequestParam String url){
        return seedService.find(url,StatusEnum.STATUS_OK.getValue());
    }

    @RequestMapping(value = "seed/list",method = RequestMethod.GET)
    public Object find(@RequestParam Integer linkedId){
        List<Show> shows = showService.findByLinkedId(linkedId);
        List seeds = Lists.newArrayList();
        Seed seed = null;
        for (Show show : shows){
            if(show.isCheckIgnored()){
                continue;
            }
            seed = seedService.findByCode(show.getCode());
            if (seed != null) {
                seeds.add(seed);
            }
        }
        return seeds;
    }
    
    @RequestMapping(value = "seed/getlist",method = RequestMethod.GET)
    public Paginator<Seed> doList(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size) throws UnsupportedEncodingException{
        Paginator<Seed> p;
        if (StringUtils.hasText(code)) {
            String decode = UriUtils.decode(code, "UTF-8");
            List<Seed> list = Lists.newArrayList();
            Seed seed = seedService.findByCode(decode);
            if (seed != null) {
                list.add(seed);
            }
            p = new Paginator<>(1,size,list.size());
            p.setEntites(list);
        } else {
            Page<Seed> pages = seedService.findSeedPage(page-1, size);
            p = new Paginator<>(page, size);
            p.setPageCount(pages.getTotalPages());
            p.setEntites(pages.getContent());

        }
        return p;
    }
    
    @RequestMapping(value = "seed/modify",method = RequestMethod.GET)
    public Object modify(
            @RequestParam("id") Integer id,
            @RequestParam(name = "frequency", required = false) Integer frequency,
            @RequestParam(name = "status", required = false) Integer status){
        if(frequency==null&&status==null){
            return "修改参数不能为空!";
        }
        Seed ns = seedService.getOne(id);
        if(ns==null){
            return "ID无效!";
        }
        if(frequency!=null){
            ns.setFrequency(frequency);
        }
        if(status!=null){
            ns.setStatus(status);
        }
        Seed seed = seedService.updateSeed(ns);
        if(seed.getFrequency()!=frequency&&seed.getStatus()!=status){
            return "更新失败!";
        }else{
            Show show = showService.findByCodeAndDepthAndPlatform(ns.getCode(), 1,seed.getPlatformId());
            //是否seed 标志为删除，若是则 连带标志删除show表中相应的数据
            boolean delete = seed.getStatus()==StatusEnum.STATUS_DEL.getValue();
            if(show!=null){
                showService.updateDeleted(show.getId(), delete);
            }
        }
        return "SUCCESS";
    }
    
    //supervisor获取种子任务
    @GetMapping(value = "seed/allbytime")
    public Object getAllSeedByTime(@RequestParam("loadtime") Long loadtime){
        return seedService.findAllSeedByTime(loadtime);
    }
    
    @GetMapping(value = "seed/delete")
    public Object deleteSeedByCode(@RequestParam("code") String code){
        Seed seed = seedService.findByCode(code);
        if(seed==null){
            return "未找到code:"+code+"相关的任务";
        }
        if(seed.getStatus()==StatusEnum.STATUS_OK.getValue()){
            seed.setStatus(StatusEnum.STATUS_DEL.getValue());
            Seed updatedSeed = seedService.updateSeed(seed);
            if(updatedSeed.getStatus()!=StatusEnum.STATUS_DEL.getValue()){
                return "删除失败!";
            }else{
                Show show = showService.findByCodeAndDepthAndPlatform(updatedSeed.getCode(), 1,updatedSeed.getPlatformId());
                if(show!=null){
                    showService.updateDeleted(show.getId(), true);
                }
            }
        }
        return "SUCCESS";
    }

    @GetMapping(value= "seed/urlOperation")
    public Object operateUrlByCode(@RequestParam("code")String code){
        Seed seed=seedService.findByCode(code);
        if(seed==null){
            return "未找到code:"+code+"相关的任务";
        }
        if(seed.getStatus()==StatusEnum.STATUS_OK.getValue()){
            seed.setStatus(StatusEnum.STATUS_DEL.getValue());
            Seed updatedSeed=seedService.updateSeed(seed);
            if(updatedSeed.getStatus()!=StatusEnum.STATUS_DEL.getValue()){
                return "删除失败!";
            }else{
                Show show=showService.findByCodeAndDepthAndPlatform(updatedSeed.getCode(),1,updatedSeed.getPlatformId());
                if(show!=null){
                    showService.updateDeleted(show.getId(),true);
                }
            }
        }else if(seed.getStatus()==StatusEnum.STATUS_DEL.getValue()){
            seed.setStatus(StatusEnum.STATUS_OK.getValue());
            Seed updatedSeed=seedService.updateSeed(seed);
            if(updatedSeed.getStatus()!=StatusEnum.STATUS_OK.getValue()){
                return "恢复失败!";
            }else{
                Show show=showService.findByCodeAndDepthAndPlatform(updatedSeed.getCode(),1,updatedSeed.getPlatformId());
                if(show!=null){
                    showService.updateDeleted(show.getId(),false);
                }
            }
        }
        return "SUCCESS";
    }
}
