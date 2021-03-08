package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Actor;
import com.jinguduo.spider.service.MediaService;
import com.jinguduo.spider.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private ShowService showService;

    @RequestMapping(path = "/add_medias",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> saveCommentLog(
            @RequestParam(name = "keyWords") String keyWords,
            @RequestParam(name = "show_id") String show_id ,
            @RequestParam(name = "category") String category ,
            @RequestParam(name = "urlJsonArray") String urlJsonArray){

        return mediaService.addMediaJobProcess(keyWords,show_id,category,urlJsonArray);
    }

    /**
     * 通过URL，actorName判断艺人表中书否已经插入了该URL媒体任务数据
     * @param url
     * @param actorName
     * @return
     */
    @RequestMapping(path = "/exist_actor", method = RequestMethod.GET)
    @ResponseBody
    public boolean existActor(
            @RequestParam String url,
            @RequestParam String actorName){
        return mediaService.existActor(url, actorName);
    }

    @RequestMapping(path = "/get_actor_media_list", method = RequestMethod.GET)
    @ResponseBody
    public List<Actor> getActorMediaList(
            @RequestParam int actor_id){
        return mediaService.getActorMediaList(actor_id);
    }
    @RequestMapping(path = "/add_actor_medias",method = RequestMethod.GET)
    @ResponseBody
    public String saveActorCommentLog(
           @RequestParam String actor_name,
           @RequestParam Integer actor_id,
           @RequestParam String url){
        mediaService.addActorMediaJobProcess(actor_name, actor_id, url);

        return "SUCCESS";
    }

    @RequestMapping(value = "/media/keys/{platform_id}",method = RequestMethod.GET)
    public Object allKeys(@PathVariable("platform_id") String platformId){
        return showService.findByPlatformId(Integer.valueOf(platformId))
                .stream()
                .map(s -> s.getName())
                .collect(Collectors.toList());
    }


}
