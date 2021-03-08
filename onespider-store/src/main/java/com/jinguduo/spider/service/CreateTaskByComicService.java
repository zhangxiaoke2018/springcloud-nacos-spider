package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.data.table.Comic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by lc on 2019/1/14
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class CreateTaskByComicService {
    @Autowired
    ComicService comicService;

    /**
     * 平台Id
     */
    private final static Integer PLATFORM_ID_OF_U17 = 28;
    //private final static Integer PLATFORM_ID_OF_WANGYI = 29;
    private final static Integer PLATFORM_ID_OF_KUAIKAN = 30;
    private final static Integer PLATFORM_ID_OF_TENGXUN = 32;
    private final static Integer PLATFORM_ID_OF_XIAOMINGTAIJI = 36;
    private final static Integer PLATFORM_ID_OF_BODONG = 48;
    private final static Integer PLATFORM_ID_OF_WEIBO = 51;
    private final static Integer PLATFORM_ID_OF_MMMH = 35;


    private final static String KUAIKAN_TASK_URL = "https://api.kkmh.com/v1/topics/%s";

    private static final String XIAOMINGTAIJI_TASK_SUPPORT_URL = "http://comic.321mh.com/app_api/v5/getcomicinfo_influence/?rank_type=all&comic_id=%s&platformname=android&productname=kmh";
    private static final String XIAOMINGTAIJI_TASK_BODY_URL = "http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_body/?comic_id=%s&platformname=android&productname=kmh";
    private static final String XIAOMINGTAIJI_TASK_COMMENT_URL = "http://community-hots.321mh.com/comment/count/?appId=1&commentType=2&ssid=%s&ssidType=0";
    private static final String XIAOMINGTAIJI_TASK_AUTHOR_URL = "http://kanmanapi-main.321mh.com/app_api/v5/getcomicinfo_role/?comic_id=%s&platformname=android&productname=kmh";

    private final static String TENGXUN_TASK_URL = "https://ac.qq.com/Comic/comicInfo/id/%s";

    //private final static String U17_TASK_URL = "http://www.u17.com/comic/%s.html";

    private static final String BODONG_TASK_BASE_URL = "https://comicapp.vip.qq.com/cgi-bin/comicapp_async_cgi?fromWeb=1&param=%s";

    private static final String BODONG_TASK_DETAIL_PARAM = "{\"0\":{\"module\":\"comic_basic_operate_mt_svr\",\"method\":\"getComicSummaryInfoWithTag\",\"param\":{\"comicID\":[\"formatSpace\"],\"limitType\":8}},\"1\":{\"module\":\"comic_basic_operate_mt_svr\",\"method\":\"getComicSeriesInfo\",\"param\":{\"minTotalSecNum\":30,\"startAndEndNum\":3,\"adjacentNum\":3,\"id\":\"formatSpace\",\"type\":1}},\"2\":{\"module\":\"comic_topic_square_mt_svr\",\"method\":\"getComicTopicInfoV2\",\"param\":{\"id\":\"formatSpace\"}},\"3\":{\"module\":\"comic_comment_v2_mt_svr\",\"method\":\"GetCommentTotalNumBatch\",\"param\":{\"postList\":[{\"commentFrom\":{\"type\":\"detail\",\"id\":\"formatSpace\"}}]}},\"4\":{\"module\":\"comic_kol_comment_mt_svr\",\"method\":\"GetKolCommentsListV2\",\"param\":{\"commentFrom\":{\"type\":\"new_detail\",\"id\":\"formatSpace\"},\"commentId\":\"\"}}}";

    private final static String WEIBO_TASK_URL = "http://apiwap.vcomic.com/wbcomic/comic/comic_show?comic_id=%s&_request_from=pc";

    private final static String MMMH_TASK_URL = "https://api.manmanapp.com/v3?%s";
    private final static String MMMH_DETAIL_TASK_BODY = "{\"worksId\":\"%s\",\"limit\":\"3000\",\"api\":\"works/index\",\"token\":\"manmanDefaultToken\",\"info\":\"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao\"}";

    private static Integer MMMH_COMIC_COUNT = 0;
    private static Integer MMMH_THIS_REQUEST_TIME = 0;

    //private final static String WANGYI_TASK_URL = "";

    public List<Job> createTaskBase(List<Comic> comics, String initUrl) {
        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();

        for (Comic comic : comics) {
            String code = comic.getCode();
            String comicId = StringUtils.substring(code, StringUtils.indexOf(code, "-") + 1);
            String jobUrl = String.format(initUrl, comicId);

            Job jobModel = new Job();
            jobModel.setCode(code);
            jobModel.setUrl(jobUrl);
            jobModel.setKind(JobKind.Once);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;

    }

    public List<Job> createKuaikanTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_KUAIKAN);
        List<Job> tasks = this.createTaskBase(comics, KUAIKAN_TASK_URL);
        return tasks;
    }

    public List<Job> createXiaoMingTaiJiTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_XIAOMINGTAIJI);
        List<Job> tasks = this.createTaskBase(comics, XIAOMINGTAIJI_TASK_SUPPORT_URL);
        List<Job> tasks2 = this.createTaskBase(comics, XIAOMINGTAIJI_TASK_BODY_URL);
        List<Job> tasks3 = this.createTaskBase(comics, XIAOMINGTAIJI_TASK_COMMENT_URL);
        List<Job> tasks4 = this.createTaskBase(comics, XIAOMINGTAIJI_TASK_AUTHOR_URL);
        tasks.addAll(tasks2);
        tasks.addAll(tasks3);
        tasks.addAll(tasks4);
        return tasks;

    }

    public List<Job> createTengXunTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_TENGXUN);
        List<Job> tasks = this.createTaskBase(comics, TENGXUN_TASK_URL);
        return tasks;

    }
    // TODO: 2019/8/16 太多了。。。3W+漫画，占据一半爬虫资源 还没什么用。。。。
//    public List<Job> createU17Task() {
//        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_U17);
//        List<Job> tasks = this.createTaskBase(comics, U17_TASK_URL);
//        return tasks;
//
//    }

    public List<Job> createWeiboTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_WEIBO);
        List<Job> tasks = this.createTaskBase(comics, WEIBO_TASK_URL);
        return tasks;

    }


    public List<Job> createBodongTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_BODONG);

        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();

        for (Comic comic : comics) {
            String code = comic.getCode();
            String comicId = StringUtils.substring(code, StringUtils.indexOf(code, "-") + 1);

            String param = BODONG_TASK_DETAIL_PARAM.replaceAll("formatSpace", comicId);
            try {
                param = URLEncoder.encode(param, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = String.format(BODONG_TASK_BASE_URL, param);

            Job jobModel = new Job();
            jobModel.setCode(code);
            jobModel.setUrl(url);
            jobModel.setKind(JobKind.Once);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }


    public List<Job> createMmmhTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_MMMH);

        List<Job> jobs = Lists.newArrayList();
        //分割任务
        for (Comic comic : comics) {
            String code = comic.getCode();
            String comicId = StringUtils.substring(code, StringUtils.indexOf(code, "-") + 1);

            String body = String.format(MMMH_DETAIL_TASK_BODY, comicId);
            try {
                body = URLEncoder.encode(body, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Job jobModel = new Job();
            jobModel.setCode(code);
            jobModel.setMethod("POST");
            jobModel.setUrl(String.format(MMMH_TASK_URL, body));
            jobModel.setKind(JobKind.Once);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;

    }
   /* public List<Job> createWangYiTask() {
        List<Comic> comics = comicService.findByPlatformId(PLATFORM_ID_OF_WANGYI);
        List<Job> tasks = this.createTaskBase(comics, WANGYI_TASK_URL);
        return tasks;
    }*/

    /**
     * 比较麻烦，每次只获取一部分漫画，轮询查找，防止任务过多
     * 每次发送100个漫画去查找
     */

}
