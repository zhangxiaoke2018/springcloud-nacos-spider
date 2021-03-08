package com.jinguduo.spider.spider.sohu;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.config.CookieSpecs;
import org.jsoup.select.Elements;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;


@Worker
@Slf4j
public class SohuMyMediaSpider extends CrawlSpider {

    private final static String ITEMS_VIDEO_PLAY_COUNT = "http://vstat.my.tv.sohu.com/dostat.do?method=getVideoPlayCount&v=%s&n=_stat";//分集播放量
    private final static String ITEMS_VIDEO_COMMENT_COUNT = "http://changyan.sohu.com/api/tvproxy/reply/cnts.do?videoId=%s";//评论数
    private final static String TOTAL_VIDEO_PLAY_COUNT = "http://api.tv.sohu.com/v4/user/playlist.json?api_key=f351515304020cad28c92f70f002261c&user_id=%s&page_size=20&is_pgc=1&sort_type=2";

    //http://my.tv.sohu.com/pl/9294859/index.shtml
    private Site site = SiteBuilder.builder()
            .setDomain("my.tv.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("/pl/.*?/index\\.shtml", page -> listPage(page));

    /***
     * 主题页
     * @param page
     */
    private void listPage(Page page) {
        Job mainJ = ((DelayRequest) page.getRequest()).getJob();

        Html html = page.getHtml();
        Elements elements = html.getDocument().getElementsByTag("script");
        Pattern pattern = Pattern.compile("uid='(\\d+)';");
        Matcher matcher =pattern.matcher(elements.get(3).html().replace(" ",""));
        if(matcher.find()){
            String uid = matcher.group(1);

            //生成总播放任务
            Job playCountTotalJob = new Job(String.format(TOTAL_VIDEO_PLAY_COUNT, uid));
            DbEntityHelper.derive(mainJ, playCountTotalJob);
            putModel(page, playCountTotalJob);
        }else {
            log.error("The sohuSelfUid play count not found: " + mainJ.getUrl());
        }

        List<Selectable> list = html.xpath("//ul[@class='uList']/li").nodes();
        for (Selectable li : list) {
            String episode = li.xpath("///strong/a/@title").regex(".*?\\s*(\\d+).*?").get();
            if (episode == null) {
                // 暂时不需要跳过
                return;
            }
            String title = li.xpath("///strong/a/@title").get();
            String url = li.xpath("///a[@class='bcount']/@href").get();
            String showCode = li.xpath("///a[@class='bcount']/@rel").get();
            
            // episode show
            Show show = new Show();
            show.setPlatformId(mainJ.getPlatformId());
            show.setDepth(2);
            show.setName(title);
            show.setUrl(url);
            show.setCode(showCode);
            show.setEpisode(Integer.valueOf(episode));
            show.setParentId(mainJ.getShowId());
            show.setParentCode(mainJ.getCode());
            putModel(page, show);
            
            // episode playCount Job
            Job playJob = new Job(String.format(ITEMS_VIDEO_PLAY_COUNT, showCode));
            DbEntityHelper.derive(mainJ, playJob);
            playJob.setCode(showCode);
            putModel(page, playJob);
            
            
            // episode commentCount Job
            Job commentJob = new Job(String.format(ITEMS_VIDEO_COMMENT_COUNT, "b"+showCode));
            DbEntityHelper.derive(mainJ, commentJob);
            commentJob.setCode(showCode);
            putModel(page, commentJob);
        }
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
