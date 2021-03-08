package com.jinguduo.spider.spider.bodongComic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicBodong;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

@Slf4j
@Worker
@SuppressWarnings("all")
public class BodongComicSpider extends CrawlSpider {


    private static final String BASE_URL = "https://comicapp.vip.qq.com/cgi-bin/comicapp_async_cgi?fromWeb=1&param=%s";

    private static final String DETAIL_PARAM = "{\"0\":{\"module\":\"comic_basic_operate_mt_svr\",\"method\":\"getComicSummaryInfoWithTag\",\"param\":{\"comicID\":[\"formatSpace\"],\"limitType\":8}},\"1\":{\"module\":\"comic_basic_operate_mt_svr\",\"method\":\"getComicSeriesInfo\",\"param\":{\"minTotalSecNum\":30,\"startAndEndNum\":3,\"adjacentNum\":3,\"id\":\"formatSpace\",\"type\":1}},\"2\":{\"module\":\"comic_topic_square_mt_svr\",\"method\":\"getComicTopicInfoV2\",\"param\":{\"id\":\"formatSpace\"}},\"3\":{\"module\":\"comic_comment_v2_mt_svr\",\"method\":\"GetCommentTotalNumBatch\",\"param\":{\"postList\":[{\"commentFrom\":{\"type\":\"detail\",\"id\":\"formatSpace\"}}]}},\"4\":{\"module\":\"comic_kol_comment_mt_svr\",\"method\":\"GetKolCommentsListV2\",\"param\":{\"commentFrom\":{\"type\":\"new_detail\",\"id\":\"formatSpace\"},\"commentId\":\"\"}}}";

    private Site site = SiteBuilder.builder()
            .setDomain("comicapp.vip.qq.com")
            .addHeader("Origin", "https://bodong.vip.qq.com")
            .addSpiderListener(new BodongComicDownLoaderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("GetComicCategoryListV2", page -> getComicIdList(page))
            .add("comic_basic_operate_mt_svr", page -> getDetail(page));



    private void getComicIdList(Page page) throws UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getJson().get());
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject jsonObject1 = data.getJSONObject("0");
        JSONObject retBody = jsonObject1.getJSONObject("retBody");
        JSONObject data1 = retBody.getJSONObject("data");
        JSONArray list = data1.getJSONArray("list");

        for (Object comicIdObj : list) {
            String comicId = (String) comicIdObj;
            String param = DETAIL_PARAM.replaceAll("formatSpace",comicId);
            param = URLEncoder.encode(param,"utf-8");
            String url = String.format(BASE_URL, param);
            Job newJob = new Job(url);
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        }


    }

    private void getDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getJson().get());
        JSONObject data = jsonObject.getJSONObject("data");
        Comic comic = new Comic();
        comic.setPlatformId(48);
        ComicBodong bd = new ComicBodong();
        bd.setDay(DateHelper.getTodayZero(Date.class));

        for (String key : data.keySet()) {
            JSONObject value = data.getJSONObject(key);
            JSONObject retBody = value.getJSONObject("retBody");
            JSONObject retData = retBody.getJSONObject("data");

            if (StringUtils.equals("getComicSummaryInfoWithTag", value.getString("method"))) {
                JSONArray comicinfos = retData.getJSONArray("comicinfos");
                for (Object comicInfoObj : comicinfos) {
                    JSONObject comicInfo = (JSONObject) comicInfoObj;

                    String comicId = comicInfo.getString("id");
                    comic.setCode("bodong-"+comicId);
                    bd.setCode("bodong-"+comicId);

                    String name = comicInfo.getString("name");
                    comic.setName(name);

                    String coverImg = comicInfo.getString("coverImg");
                    comic.setHeaderImg(coverImg);

                    JSONArray categorys = comicInfo.getJSONArray("category");
                    String tags = "";
                    for (int i = 0; i < categorys.size(); i++) {
                        JSONObject category = (JSONObject) categorys.get(i);
                        tags += (i == 0) ? category.getString("name") : "/" + category.getString("name");
                    }
                    comic.setTags(tags);

                    String author = comicInfo.getString("author");
                    comic.setAuthor(author);

                    String desc = comicInfo.getString("desc");
                    comic.setIntro(desc);

                    Long collectCount = comicInfo.getLong("collectCount");
                    bd.setCollectCount(collectCount);
                    //莫名其妙的阅读量
                    Long readCount = comicInfo.getLong("readCount");
                    bd.setReadCount(readCount);
                    //热度
                    Long readPicCount = comicInfo.getLong("readPicCount");
                    bd.setReadPicCount(readPicCount);


                    ComicAuthorRelation car = new ComicAuthorRelation();
                    car.setAuthorId(author);
                    car.setAuthorName(author);
                    car.setPlatformId(48);
                    car.setComicCode("bodong-"+comicId);

                    putModel(page,car);

                }
            } else if (StringUtils.equals("GetCommentTotalNumBatch", value.getString("method"))) {
                JSONArray list = retData.getJSONArray("list");
                for (int i = 0;i< list.size();i++) {
                    JSONObject in = (JSONObject) list.get(i);
                    Long commentCount = in.getLong("totalNum");
                    bd.setCommentCount(commentCount);
                }

            }

        }
        putModel(page,comic);
        putModel(page,bd);
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
