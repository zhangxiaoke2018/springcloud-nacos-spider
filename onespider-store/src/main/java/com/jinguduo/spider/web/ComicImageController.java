package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.db.repo.ComicRepo;
import com.jinguduo.spider.service.ComicImageService;
import com.jinguduo.spider.service.QiniuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;

@Slf4j
@RequestMapping("/comic")
@Controller
public class ComicImageController {

    @Autowired
    private ComicImageService comicImageService;

    @RequestMapping("/U17Comics")
    public void getU17ImageUrl(){
        List<Comic> comics = comicImageService.getComics(28);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/WyComics")
    public void getWyImageUrl(){
        List<Comic> comics = comicImageService.getComics(29);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/KKComics")
    public void getKKImageUrl(){
        List<Comic> comics = comicImageService.getComics(30);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/TxComics")
    public void getTxImageUrl(){
        List<Comic> comics = comicImageService.getComics(32);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/DmzjComics")
    public void getDmzjImageUrl(){
        List<Comic> comics = comicImageService.getComics(34);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/MmComics")
    public void getMmImageUrl(){
        List<Comic> comics = comicImageService.getComics(35);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/XmComics")
    public void getXmImageUrl(){
        List<Comic> comics = comicImageService.getComics(36);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/BdComics")
    public void getBdImageUrl(){
        List<Comic> comics = comicImageService.getComics(48);
        comicImageService.saveComic(comics);
    }
    @RequestMapping("/WbComics")
    public void getWbImageUrl(){
        List<Comic> comics = comicImageService.getComics(51);
        comicImageService.saveComic(comics);
    }

    //咚漫 单独处理 在请求中加上 refer
    @RequestMapping("/DmComics")
    public void getDmImageUrl(){
        List<Comic> comics = comicImageService.getComics(52);
        comicImageService.saveComic(comics);
    }
}
