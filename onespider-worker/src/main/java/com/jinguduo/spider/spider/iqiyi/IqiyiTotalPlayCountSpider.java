package com.jinguduo.spider.spider.iqiyi;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 2018-09-25 爱奇艺停止吐数
 *
 * 一个不精确的总播放量数据
 * http://iface2.iqiyi.com/views/3.0/player_tabs?app_k=204841020bd16e319191769268fb56ee&app_v=6.8.3&platform_id=11&dev_os=4.4.4&dev_ua=Nexus+5&net_sts=1&qyid=358239054455227&secure_p=GPad&secure_v=1&core=1&dev_hw=%7B%22mem%22%3A%22457.3MB%22%2C%22cpu%22%3A0%2C%22gpu%22%3A%22%22%7D&scrn_sts=0&scrn_res=1080,1794&scrn_dpi=480&page_part=2&album_id=205642201
 */
@Deprecated
@Worker
@CommonsLog
public class IqiyiTotalPlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("iface2.iqiyi.com")
            .addHeader("sign", "f02a81e5688c22a3742d1faaa2471ab3")
            .addHeader("t", "430634055")
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("player_tabs",page -> getTotalPlayCount(page))
            ;

    private void getTotalPlayCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        if(StringUtils.isBlank(rawText)){
            log.error("response body is null");
            return;
        }

        String strPc = RegexUtil.getStringByPattern(rawText, Pattern.compile("vv\":\"(.*?)\""), 1);

        long playCount = NumberHelper.parseShortNumber(strPc, -1l);

        ShowLog showLog = new ShowLog();
        showLog.setPlayCount(playCount);
        DbEntityHelper.derive(job, showLog);

        putModel(page, showLog);

    }




    @Override
    public PageRule getPageRule() {
        return rules;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }
}
