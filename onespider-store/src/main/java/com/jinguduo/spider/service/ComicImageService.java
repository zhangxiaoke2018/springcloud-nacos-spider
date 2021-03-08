package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.db.repo.ComicRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ComicImageService {

    @Autowired
    private ComicRepo comicRepo;

    @Autowired
    private QiniuService qiniuService;

    public List<Comic> getComics(Integer platformId) {
        List<Comic> comicList = comicRepo.findByHeaderImgAndInnerImgUrlAndPlatformId(platformId);
        return comicList;
    }

    public void saveComic(List<Comic> comics) {
        for (Comic comic : comics) {
            String code = comic.getCode();
            String headImage = comic.getHeaderImg();
            String imageUrl = headImage + "/comic/code/" + code;
            byte[] bytes = qiniuService.download(imageUrl, comic.getPlatformId());
            if (bytes == null || bytes.length == 0) {
                log.error("download error,save innerImgUrl == HeaderImage  ");
                comic.setInnerImgUrl(headImage);
            } else {
                String s = qiniuService.upload(bytes, imageUrl);
                log.info("download success,upload success url "+ s);
                comic.setInnerImgUrl(s);
            }
            comicRepo.save(comic);
        }
    }

}
