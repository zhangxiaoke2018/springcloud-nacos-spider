package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicSex;
import com.jinguduo.spider.db.repo.ComicRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 18:17
 */
@Service
@CommonsLog
public class ComicService {

    @Autowired
    private ComicRepo comicRepo;


    public static List<String> SKIP_LIST = new ArrayList<>();

    {
        SKIP_LIST.add("qq-505430");
        SKIP_LIST.add("qq-623654");
        SKIP_LIST.add("qq-505432");
        SKIP_LIST.add("bodong-505432");
        SKIP_LIST.add("bodong-505430");
        SKIP_LIST.add("u17-166130");
        SKIP_LIST.add("wb-72587");
        SKIP_LIST.add("kuaikan-78");
        SKIP_LIST.add("kuaikan-583");
        SKIP_LIST.add("kuaikan-1946");
        SKIP_LIST.add("kuaikan-2222");
        SKIP_LIST.add("kuaikan-148");
        SKIP_LIST.add("kuaikan-313");
        SKIP_LIST.add("kuaikan-558");
        SKIP_LIST.add("kuaikan-553");
        SKIP_LIST.add("kuaikan-198");
        SKIP_LIST.add("kuaikan-784");
        SKIP_LIST.add("kuaikan-719");
        SKIP_LIST.add("kuaikan-545");
        SKIP_LIST.add("kuaikan-180");
        SKIP_LIST.add("kuaikan-603");
        SKIP_LIST.add("kuaikan-1594");
        SKIP_LIST.add("kuaikan-1806");
    }

    public Comic insertOrUpdate(Comic comic) {

        boolean isSkip = this.skipByCode(comic.getCode());
        if (isSkip) {
            return comic;
        }


        Comic c = comicRepo.findByCode(comic.getCode());

        //insert
        if (c == null) {
            if (comic.getPlatformId() == null) {
                log.info("the_comic_platform_id is null: -> " + comic.toString());
                return null;
            }
            if (comic.getIntro() != null) {
                comic.setIntro(TextUtils.removeBadText(comic.getIntro()));
            }
            if (null == comic.getSex() || comic.getSex() == 0) {
                Byte sex = this.getSexByComic(comic);
                comic.setSex(sex);
            }
            return comicRepo.save(comic);
        }


        //update
        try {
            if (!StringUtils.isEmpty(comic.getName())) {
                c.setName(comic.getName());
            }
            if (!StringUtils.isEmpty(comic.getHeaderImg())) {
                c.setHeaderImg(comic.getHeaderImg());
            }
            if (!StringUtils.isEmpty(comic.getSubject())) {
                c.setSubject(comic.getSubject());
            }
            if (!StringUtils.isEmpty(comic.getTags())) {
                c.setTags(comic.getTags());
            }
            if (!StringUtils.isEmpty(comic.getAuthor())) {
                c.setAuthor(comic.getAuthor());
            }
            if (null != comic.getExclusive()) {
                c.setExclusive(comic.getExclusive());
            }
            if (null != comic.getFinished()) {
                c.setFinished(comic.getFinished());
            }
            if (null != comic.getEpisode() && comic.getEpisode() != 0) {
                c.setEpisode(comic.getEpisode());
            }
            if (null != comic.getEndEpisodeTime()) {
                c.setEndEpisodeTime(comic.getEndEpisodeTime());
            }
            if (null !=comic.getIntro()){
                c.setIntro(comic.getIntro());
            }
            if (null == comic.getSex() || comic.getSex() == 0) {
                Byte sex = this.getSexByComic(comic);
                c.setSex(sex);
            }
            return comicRepo.save(c);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 跳过航海王
     *
     * @param code qq-505430
     *             kuaikan-1338
     */
    public static boolean skipByCode(String code) {
        if (SKIP_LIST.contains(code)) {
            return true;
        }
        return false;
    }

    private Byte getSexByComic(Comic c) {
        Byte sex = null;
        //快看
        if (StringUtils.contains(c.getCode(), "kuaikan-") && StringUtils.isNotBlank(c.getSubject()) && (null == c.getSex() || c.getSex() == 0)) {
            sex = this.getSexOfKuaikan(c);
        }
        //腾讯
        else if (c.getCode().contains("qq-") && StringUtils.isNotBlank(c.getTags()) && (null == c.getSex() || c.getSex() == 0)) {
            sex = this.getSexOfTengxun(c);
        }
        //有妖气
        else if (c.getCode().contains("u17-") && StringUtils.isNotBlank(c.getSubject()) && (null == c.getSex() || c.getSex() == 0)) {
            sex = this.getSexOfU17(c);
        }
        //网易漫画
        else if (c.getCode().contains("163-") && StringUtils.isNotBlank(c.getSubject()) && (null == c.getSex() || c.getSex() == 0)) {
            sex = this.getSexOf163(c);
        }
        //动漫之家漫画
        else if (c.getCode().contains("dmzj-") && StringUtils.isNotBlank(c.getSubject()) && (null == c.getSex() || c.getSex() == 0)) {
            sex = this.getSexOf163(c);
        } else {
            sex = ComicSex.unKnowSex.getSqlEnum();
        }
        return sex;
    }

    private Byte getSexOf163(Comic c) {
        String subject = c.getSubject();
        if (StringUtils.contains(subject, "恋爱")
                || StringUtils.contains(subject, "治愈")
                || StringUtils.contains(subject, "校园")
                || StringUtils.contains(subject, "古风")
                || StringUtils.contains(subject, "耽美")) {
            return ComicSex.girl.getSqlEnum();
        } else if (StringUtils.contains(subject, "后宫")
                || StringUtils.contains(subject, "恐怖")
                || StringUtils.contains(subject, "玄幻")
                || StringUtils.contains(subject, "热血")
                || StringUtils.contains(subject, "科幻")
                || StringUtils.contains(subject, "战斗")
                || StringUtils.contains(subject, "武侠")
                || StringUtils.contains(subject, "冒险")
                || StringUtils.contains(subject, "悬疑")) {
            return ComicSex.boy.getSqlEnum();
        }
        return ComicSex.unKnowSex.getSqlEnum();
    }

    private Byte getSexOfU17(Comic c) {
        String subject = c.getSubject();
        if (StringUtils.contains(subject, "少女")) {
            return ComicSex.girl.getSqlEnum();
        } else if (StringUtils.contains(subject, "少年")) {
            return ComicSex.boy.getSqlEnum();
        }
        return ComicSex.unKnowSex.getSqlEnum();
    }

    private Byte getSexOfTengxun(Comic c) {
        String tags = c.getTags();
        //女
        if (StringUtils.contains(tags, "恋爱")
                || StringUtils.contains(tags, "纯爱")
                || StringUtils.contains(tags, "治愈")
                || StringUtils.contains(tags, "百合")
                || StringUtils.contains(tags, "百合")
                || StringUtils.contains(tags, "虐心")
                || StringUtils.contains(tags, "生活")
                || StringUtils.contains(tags, "宫斗")
                || StringUtils.contains(tags, "明星")
                || StringUtils.contains(tags, "浪漫")) {
            return ComicSex.girl.getSqlEnum();
        }
        //男
        else if (StringUtils.contains(tags, "热血")
                || StringUtils.contains(tags, "冒险")
                || StringUtils.contains(tags, "科幻")
                || StringUtils.contains(tags, "推理")
                || StringUtils.contains(tags, "武侠")
                || StringUtils.contains(tags, "格斗")
                || StringUtils.contains(tags, "战争")
                || StringUtils.contains(tags, "竞技")
                || StringUtils.contains(tags, "恐怖")
                || StringUtils.contains(tags, "机甲")
                || StringUtils.contains(tags, "僵尸")
                || StringUtils.contains(tags, "玄幻")) {
            return ComicSex.boy.getSqlEnum();
        }
        //未在以上类别中
        return ComicSex.unKnowSex.getSqlEnum();
    }


    private Byte getSexOfKuaikan(Comic c) {
        String subject = c.getSubject();
        //女
        if (StringUtils.contains(c.getSubject(), "三次元")
                || StringUtils.contains(subject, "剧情")
                || StringUtils.contains(subject, "古风")
                || StringUtils.contains(subject, "总裁")
                || StringUtils.contains(subject, "恋爱")
                || StringUtils.contains(subject, "日常")
                || StringUtils.contains(subject, "校园")
                || StringUtils.contains(subject, "治愈")
                || StringUtils.contains(subject, "都市")) {
            return ComicSex.girl.getSqlEnum();
        }
        //男
        else if (StringUtils.contains(subject, "少年")
                || StringUtils.contains(subject, "灵异")) {
            return ComicSex.boy.getSqlEnum();
        }
        return ComicSex.unKnowSex.getSqlEnum();
    }

    public List<Comic> findByPlatformId(Integer platformId) {
        return comicRepo.findByPlatformId(platformId);
    }
}
