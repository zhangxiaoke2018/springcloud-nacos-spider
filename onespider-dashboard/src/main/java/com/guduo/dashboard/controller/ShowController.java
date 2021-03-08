package com.guduo.dashboard.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/03/2017 3:54 PM
 */
@Controller
@RequestMapping("/show")
@CommonsLog
public class ShowController {


    @Value("${onespider.store.host}")
    private String storePath;

    @GetMapping("")
    public String toShowPage(){
        return "show";
    }

    @GetMapping("/list")
    @ResponseBody
    public Object getShow(@RequestParam("name") String name) throws IOException {

        List<JSONObject> lists = JSONObject.parseArray(
                Request.Get(storePath + "/show/" + name).execute().returnContent().asString()
                , JSONObject.class);

        List<JSONObject> objects = lists.stream().filter(l -> !l.getString("category").equals("MEDIA_DATA")).collect(Collectors.toList());

        return objects;
    }

    @GetMapping("/offline")
    @ResponseBody
    public Object offlineShow(@RequestParam String showId) throws IOException {

        String url = UriComponentsBuilder.fromHttpUrl(storePath + "/show/downShow").queryParam("showId",showId).build().toUriString();

        Request.Get(url).execute().returnContent().asString();

        return "SUCCESS";

    }




}
