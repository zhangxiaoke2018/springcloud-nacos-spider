package com.jinguduo.spider.spider.dongmanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.DigestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Worker
public class DongManComicSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("www.dongmanmanhua.cn")
            .build();

    private String infoUrl = "https://apis.dongmanmanhua.cn/app/title/info2?titleNo=%s&v=2&platform=APP_ANDROID&serviceZone=CHINA&language=zh-hans&md5=%s&expires=%s";
    //pageSize为集数 最大取50000基本包含全集
    private String listUrl = "http://apis.dongmanmanhua.cn/app/episode/list/v3?titleNo=%s&startIndex=0&pageSize=50000&serviceZone=CHINA&v=7&platform=APP_ANDROID&language=zh-hans&md5=%s&expires=%s";

    //榜单任务
    private String billboardUrl = "https://apis.dongmanmanhua.cn/app/title/ranking2?rankingType=ALL&serviceZone=CHINA&v=3&platform=APP_ANDROID&language=zh-hans&md5=%s&expires=%s";

    private PageRule rules = PageRule.build()
            .add("genre", page -> getAll(page))
            .add("www.dongmanmanhua.cn$", page -> processHomeBanner(page))
            .add("/likeAndCount", page -> getEpisodeLiks(page));

    private void processHomeBanner(Page page) {
        Document document = page.getHtml().getDocument();
        Elements largeBanner = document.getElementsByClass("_largeBanner");
        String codePrefix = "dmmh-";
        Integer platformId = 52;
        Date day = DateUtil.getDayStartTime(new Date());
        String source = "HOME";

        for (Element element : largeBanner) {
            String href = element.getElementsByTag("a").attr("href");
            String code = codePrefix + UrlUtils.getParam(href, "title_no");
            ComicBanner cb = new ComicBanner(code,platformId,day,null,source);
            putModel(page,cb);
        }

    }

    private void getEpisodeLiks(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        JSONArray datas = jsonObject.getJSONArray("data");
        if (null == datas || datas.isEmpty()) {
            return;
        }

        List<ComicEpisodeInfo> episodeInfos = Lists.newArrayList();
        Date day = DateUtil.getDayStartTime(new Date());
        for (int i = 0; i < datas.size(); i++) {
            JSONObject json = (JSONObject) datas.get(i);
            ComicEpisodeInfo info = new ComicEpisodeInfo();
            info.setCode(job.getCode());
            info.setPlatformId(52);
            info.setDay(day);
            info.setEpisode(json.getInteger("episodeNo"));
            info.setLikeCount(json.getInteger("count"));
            episodeInfos.add(info);
        }


        putModel(page, episodeInfos);

    }


    private void getAll(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Elements cardList = document.getElementsByClass("card_lst");

        List<Job> jobs = Lists.newArrayList();

        // 类型循环：恋爱/少年/古风...
        for (Element card : cardList) {

            Elements comics = card.getElementsByTag("li");
            // 一类漫画下的全部
            for (Element comic : comics) {

                Job newJob = new Job();

                newJob.setPlatformId(52);

                String titleNo = comic.attr("data-title-no");
                String code = "dmmh-" + titleNo;
                newJob.setCode(code);

                String expires = String.valueOf(System.currentTimeMillis() / 1000 + 3600);
                String md5 = this.md5("/app/title/info2", expires);
                String url = String.format(infoUrl, titleNo, md5, expires);

                newJob.setUrl(url);

                jobs.add(newJob);

                Job episodeListJob = new Job();
                episodeListJob.setCode(code);
                String listMd5 = this.md5("/app/episode/list/v3", expires);
                String episodeListUrl = String.format(listUrl, titleNo, listMd5, expires);
                episodeListJob.setUrl(episodeListUrl);
                jobs.add(episodeListJob);
            }
        }

        //生成手机端榜单任务
        Job billboardJob = new Job();
        billboardJob.setPlatformId(52);
        billboardJob.setCode(job.getCode());

        Long time = System.currentTimeMillis() / 1000 + 3600;
        String md5 = md5("/app/title/ranking2", time.toString());
        String mobileBillboardUrl = String.format(billboardUrl, md5, time);

        billboardJob.setUrl(mobileBillboardUrl);
        jobs.add(billboardJob);
        putModel(page, jobs);


    }


    /**
     * long expires = 1548752038;
     * String path = "/app/title/info2";
     *
     * @param path
     * @param expires
     * @return
     */
    public String md5(String path, String expires) {
        String scr = "the_scret_key";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        stringBuilder.append(expires);
        stringBuilder.append(" ");
        stringBuilder.append(path);
        stringBuilder.append(" ");
        stringBuilder.append(scr);
        String result = stringBuilder.toString();
        String s = Base64.getEncoder().encodeToString(DigestUtils.md5Digest(result.getBytes())).replace("/", "_").replace("=", "")
                .replace("+", "-");
        return s;
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
