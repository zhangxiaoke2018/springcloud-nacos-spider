package com.jinguduo.spider.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.data.table.*;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@CommonsLog
@Component
public class MediaService {

    @Autowired
    private KWordsService kWordsService;

    @Autowired
    private SearchSiteService searchSiteService;

    @Autowired
    private PlatformService platformService;

    @Resource
    private SeedService seedService;

    @Autowired
    private ShowService showService;

    @Autowired
    private ActorService actorService;

    /***
     * 添加媒体指数处理逻辑
     * @param keyWords
     * @param urlJsonArray
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String, Object> addMediaJobProcess(String keyWords, String bi_show_id, String category, String urlJsonArray) {

        /** 自定义变量 */
        Integer status = 200;
        String message = "success";
        Integer biShow_id = -1;
        boolean urlnull = false;//校验参数,同时为空返回，只要有一个参数不为空，继续添加
        JSONArray url_jsonArray = null;//最终需要插入的url结果集

        /** 添加失败的url容器 */
        Map<String, Object> failMap = Maps.newHashMap();

        /** 校验参数 */
        if (StringUtils.isBlank(bi_show_id)) {
            status = 500;
            message = "Bi show_id can not be null !";
            return ImmutableMap.of("status", status, "message", message);
        }
        if (StringUtils.isBlank(urlJsonArray)) {
            urlnull = true;
            message = "urls is null";
        }

        /** 存在官方媒体url,不存在生成新的容器存放系统生成的url */
        if (!urlnull) {
            url_jsonArray = JSONArray.parseArray(urlJsonArray);
        } else {
            url_jsonArray = new JSONArray();
        }

        /** bi show Id */
        biShow_id = Integer.valueOf(bi_show_id);

        /** 添加关键字 */
        Integer keywordId = this.insertKeywords(keyWords);

        /** 生成其他媒体URL,根据kId */
        this.createOtherMediaUrlProcess(keywordId, keyWords, url_jsonArray, failMap);

        /** 插入所有媒体url，生成任务 */
        this.createAllMediaJobProcess(keywordId, biShow_id, url_jsonArray, failMap);

        return ImmutableMap.of("status", status, "message", message, "failUrl", failMap);
    }

    /**
     * 通过URL，actorName判断艺人表中书否已经插入了该URL媒体任务数据
     * @param url
     * @param actorName
     * @return
     */
    public boolean existActor(String url, String actorName){
        try {
            url = URLDecoder.decode(url,"UTF-8");
            String code = FetchCodeEnum.getCode(url);
            return actorService.exist(code, actorName);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public List<Actor> getActorMediaList(int actorId){
        List<Actor> actorList = actorService.getActorListByActorId(actorId);
        return actorList;
    }

    /**
     * 添加艺人的媒体指数
     * @param actorName
     * @param actorId
     * @param url
     * @return
     */
    public void addActorMediaJobProcess(String actorName, Integer actorId, String url){
        createOtherActorMediaUrlProcess(actorName, actorId);
        try {
            url = URLDecoder.decode(url,"UTF-8");
            String code = FetchCodeEnum.getCode(url);
            Integer platformId = platformService.find(url).getId();
            /** 生成Actor */
            Actor actor = new Actor();
            actor.setCode(code);
            actor.setPlatformId(platformId);
            actor.setName(actorName);
            actor.setLinkedId(actorId);
            actor.setUrl(url);
            actorService.save(actor);

            /** 生成Seed */
            Seed seed = new Seed();
            seed.setUrl(url);
            seed.setPlatformId(platformId);
            seed.setCode(code);
            Integer seedId = seedService.addSeed(seed).getId();
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 艺人添加其他媒体任务
     * @param actorName
     * @param actorId
     */
    public void createOtherActorMediaUrlProcess(String actorName, Integer actorId){
        /** 查询所有启用的媒体网站前缀 */
        List<SearchSite> listSite = searchSiteService.findMediaSearchSiteByEnable(0);//启用

        if (null == listSite || listSite.isEmpty()) {
            log.warn(" there is no search site need captrue data ");
            return;
        }

        for (SearchSite searchSite : listSite) {

            try {
                String url = String.format(searchSite.getSearchUrlPrefix(), URLEncoder.encode(actorName, "utf-8"));
                String code = FetchCodeEnum.getCode(url);
                Integer platformId = platformService.find(url).getId();
                /** 生成Actor */
                Actor actor = new Actor();
                actor.setCode(code);
                actor.setPlatformId(platformId);
                actor.setName(actorName);
                actor.setLinkedId(actorId);
                actor.setUrl(url);
                actorService.save(actor);

                /** 生成Seed */
                Seed seed = new Seed();
                seed.setUrl(url);
                seed.setCode(code);
                seed.setPlatformId(platformId);
                Integer seedId = seedService.addSeed(seed).getId();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
        }
    }

    /**
     * 生成媒体任务
     *
     * @param keywordId
     * @param biShow_id
     * @param urlJsonArray
     * @param failMap
     */
    private void createAllMediaJobProcess(Integer keywordId, Integer biShow_id, JSONArray urlJsonArray, Map<String, Object> failMap) {
        /** 自定义变量 */
        String url = null, title = null, code = null;

        for (Object object : urlJsonArray) {
            Integer platformId = 0;
            Integer showId = 0;
            Integer seedId = 0;
            try {
                JSONObject jsonObject = (JSONObject) object;

                url = jsonObject.getString("officialUrl");
                code = jsonObject.getString("code");
                title = jsonObject.getString("title");

                /** 查询平台Id By Url */
                Platform platform = platformService.find(url);
                if (null != platform) {
                    platformId = platform.getId();
                } else {
                    log.warn("no match platform!!,url:[" + url + "]");
                    failMap.put(url, " please tell admin, there is no platform of this url! ");
                    continue;
                }

                /** 生成show */
                Show show = new Show();
                show.setCode(code);
                show.setPlatformId(platformId);
                show.setName(title);
                show.setCategory(Category.MEDIA_DATA.name());
                show.setLinkedId(biShow_id);
                show.setUrl(url);
                show.setSource(1);//默认都是从前端录入
                showId = showService.insertOrUpdate(show).getId();

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                failMap.put(url, e.getLocalizedMessage());
                continue;
            }
        }
    }

    /***
     * 生成其他媒体 url 根据剧名
     * code 采用BASE64编码，截取64位
     * @param keyWords
     * @param url_jsonArray
     * @param failMap
     *
     * @tag 此处不要抛出异常，其他逻辑正常执行
     */
    private void createOtherMediaUrlProcess(Integer keywordId, String keyWords, JSONArray url_jsonArray, Map<String, Object> failMap) {

        /** 查询所有启用的媒体网站前缀 */
        List<SearchSite> listSite = searchSiteService.findMediaSearchSiteByEnable(0);//启用

        /** 临时变量 */
        String urlKeywords = null;

        if (null == listSite || listSite.isEmpty()) {
            log.warn(" there is no search site need captrue data ");
            return;
        }

        for (SearchSite searchSite : listSite) {

            try {
                JSONObject jsonObject = new JSONObject();
                urlKeywords = String.format(searchSite.getSearchUrlPrefix(), URLEncoder.encode(keyWords, "utf-8"));
                jsonObject.put("officialUrl", urlKeywords);
                jsonObject.put("code", FetchCodeEnum.getCode(urlKeywords));
                jsonObject.put("title", keyWords);
                jsonObject.put("keywordId", keywordId);

                url_jsonArray.add(jsonObject);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                failMap.put(urlKeywords, keyWords);//失败的
                continue;
            }
        }
    }

    /**
     * 插入关键字并返回Id
     *
     * @param keyWords
     * @return
     */
    private Integer insertKeywords(String keyWords) {

        Integer id = -1;

        KWords kWords_result = kWordsService.insertKWord(new KWords(keyWords));

        if (null != kWords_result) {
            id = kWords_result.getId();
        }
        return id;
    }









}
