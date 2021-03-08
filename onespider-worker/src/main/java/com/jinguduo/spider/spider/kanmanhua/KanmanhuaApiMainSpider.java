package com.jinguduo.spider.spider.kanmanhua;

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
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@Worker
public class KanmanhuaApiMainSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("kanmanapi-main.321mh.com")
            .addSpiderListener(new ComicKanmanhuaApiMainDownloaderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("/getcomicinfo_role/", page -> getInfo(page))
            .add("/getchapterlikelistbycomic", page -> getEpisodeLikes(page));

    private void getEpisodeLikes(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String code = job.getCode();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        JSONArray datas = jsonObject.getJSONArray("data");

        List<ComicEpisodeInfo> episodeList =  new ArrayList<>();
        Date day = DateUtil.getDayStartTime(new Date());

        for (Object dataObj : datas) {
            JSONObject data = (JSONObject) dataObj;
            String chapter_id = data.getString("chapter_id");
            Integer chapter_likes = data.getInteger("chapter_likes");
            ComicEpisodeInfo info = new ComicEpisodeInfo();

            info.setCode(code);
            info.setPlatformId(36);
            info.setDay(day);
            info.setChapterId(chapter_id);
            info.setLikeCount(chapter_likes);
            episodeList.add(info);
        }

        putModel(page,episodeList);


    }


    public void getInfo(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getJson().get());
        JSONArray datas = json.getJSONArray("data");

        List<String> authors = new ArrayList<>();

        List<ComicAuthorRelation> authorRelationList = new ArrayList<>();


        for (Object dataObj : datas) {
            JSONObject data = (JSONObject) dataObj;
            String type = data.getString("type");
            if (StringUtils.isEmpty(type) || !StringUtils.equals("zuozhe", type)) {
                continue;
            }
            //作者
            String name = data.getString("name");
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            String user_id = data.getString("user_id");
            authors.add(name);

            ComicAuthorRelation car = new ComicAuthorRelation();
            car.setComicCode(job.getCode());
            car.setPlatformId(36);
            car.setAuthorName(name);
            car.setAuthorId(user_id);
            authorRelationList.add(car);

        }
        String authorsStr = StringUtils.join(authors, "/");
        Comic comic = new Comic();
        comic.setCode(job.getCode());
        comic.setPlatformId(36);
        comic.setAuthor(authorsStr);

        putModel(page, comic);
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
