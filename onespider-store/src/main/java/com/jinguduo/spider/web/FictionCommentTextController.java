package com.jinguduo.spider.web;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.service.FictionCommentTextService;

import lombok.extern.apachecommons.CommonsLog;

@Controller
@ResponseBody
@CommonsLog
public class FictionCommentTextController {
	@Autowired
	private FictionCommentTextService fictionCommentTextSevice;
	
	@RequestMapping(path = "/fiction_comment_texts", method = RequestMethod.POST)
    public boolean post(@RequestBody List<FictionCommentText> commentTexts) {
        boolean r = false;
        
        try {
        	fictionCommentTextSevice.save(commentTexts);
            r = true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return r;
    }
}
