package com.jinguduo.spider.spider.kuaikan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/12/14
 * Time:10:07
 */

/**
 * 详情页
 * https://api.kkmh.com/v1/topics/793/#19
 */
@Slf4j
@Worker
public class KuaikanDetailSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("api.kkmh.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/topics/", page -> analyzeDetail(page));

    /**
     * 解析详情页
     */
    private void analyzeDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String url = job.getUrl();

        JSONObject json = JSONObject.parseObject(page.getJson().get());

        if (!json.getInteger("code").equals(200)) {
            //log.error("www.kuaikanmanhua.com ,this url -->{},作品已下架", url);
            return;
        }




        json = json.getJSONObject("data");

        ComicKuaiKan kk = new ComicKuaiKan();
        Integer comicId = json.getInteger("id");

        Integer platformId= 30;
        String code = "kuaikan-" + comicId;

        kk.setCode(code);
        kk.setHotNum(json.getLong("view_count"));
        kk.setPraiseNum(json.getLong("likes_count"));
        kk.setCommentNum(json.getLong("comments_count"));
        kk.setFavCount(json.getLong("fav_count"));

        //漫画作者

        List<String> authorList = new ArrayList<>();
        List<ComicAuthorRelation> authorRelationList = new ArrayList();

        JSONArray related_authors = json.getJSONArray("related_authors");

        if(null == related_authors || related_authors.size() == 0){
            JSONObject user = json.getJSONObject("user");
            String nickname = user.getString("nickname");
            ComicAuthorRelation car = new ComicAuthorRelation();
            car.setPlatformId(platformId);
            car.setComicCode(code);
            car.setAuthorId(user.getString("id"));
            car.setAuthorName(nickname);
            authorRelationList.add(car);
        }else {
            for (Object auObj : related_authors) {
                JSONObject auJson = (JSONObject) auObj;
                String authorName = auJson.getString("nickname");
                authorList.add(authorName);

                String authorId = auJson.getString("id");
                ComicAuthorRelation car = new ComicAuthorRelation();
                car.setPlatformId(platformId);
                car.setAuthorId(authorId);
                car.setComicCode(code);
                car.setAuthorName(authorName);
                authorRelationList.add(car);
            }
        }

        String authorsStr = StringUtils.join(authorList, "/");


        //分集漫画最后更新时间
        JSONArray comics = json.getJSONArray("comics");
        List<ComicEpisodeInfo> episodeList = new ArrayList();
        Integer comicsSize = comics.size() - 1;

        if (null != comics && comics.size() > 0) {
            Date day = DateUtil.getDayStartTime(new Date());
            JSONObject episodeObject = (JSONObject) comics.get(0);
            Long created_at = episodeObject.getLong("created_at");
            Date endTime = new Date(created_at * 1000);
            Comic comic = new Comic();
            comic.setCode(code);
            comic.setPlatformId(platformId);
            comic.setEndEpisodeTime(endTime);
            comic.setAuthor(authorsStr);
            putModel(page, comic);

            int j = 1;
            for (int i = comicsSize; i >= 0; i--, j++) {
                JSONObject episodeComic = (JSONObject) comics.get(i);
                String title = episodeComic.getString("title");
                Boolean is_free = episodeComic.getBoolean("is_free");
                Integer vipStatus = is_free ? 0 : 1;
                long episodeCreateTime = episodeComic.getLong("created_at") * 1000L;

                // TODO: 2019/3/26 暂时不用这个数
                Integer likes_count = episodeComic.getInteger("likes_count");

                ComicEpisodeInfo info = new ComicEpisodeInfo();
                info.setCode(code);
                info.setPlatformId(platformId);
                info.setDay(day);
                info.setName(title);
                info.setEpisode(j);
                info.setVipStatus(vipStatus);
                info.setComicCreatedTime(new Date(episodeCreateTime));
                info.setLikeCount(likes_count);
                episodeList.add(info);
            }
        }


        putModel(page, kk);
        putModel(page, episodeList);
        putModel(page, authorRelationList);

    }


    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
