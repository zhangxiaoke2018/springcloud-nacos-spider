package com.jinguduo.spider.web;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.service.CommentTextService;

import lombok.extern.apachecommons.CommonsLog;

@Controller
@ResponseBody
@CommonsLog
public class CommentTextController {
    
    @Autowired
    private CommentTextService commentTextService;

    @RequestMapping(path = "/comment_texts", method = RequestMethod.POST)
    public boolean post(@RequestBody List<CommentText> commentTexts) {
        boolean r = false;
        
        try {
            commentTextService.save(commentTexts);
            r = true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return r;
    }
}
