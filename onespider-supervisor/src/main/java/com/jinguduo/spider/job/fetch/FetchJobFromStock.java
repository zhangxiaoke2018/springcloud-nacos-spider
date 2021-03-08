package com.jinguduo.spider.job.fetch;

import com.jinguduo.spider.access.OneAccessor;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.StockCompany;
import com.jinguduo.spider.job.JobManager;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Component
@CommonsLog
@RefreshScope
public class FetchJobFromStock implements FetchJob {

    @Autowired
    private JobManager jobManager;

    @Autowired
    private OneAccessor oneAccessor;

    private static final String initUrl = "http://www.neeq.com.cn/disclosureInfoController/infoResult.do?callback=JSON&disclosureType=5&page=0&companyCd=%s&startTime=%s";

    private final Timer timer = new Timer("FetchJobFromStockTimer");

    private long delay = TimeUnit.SECONDS.toMillis(10);
    private long period = TimeUnit.MINUTES.toMillis(5);

    @Override
    public void process() {
        timer.scheduleAtFixedRate(new Task(), delay, period);
    }

    class Task extends TimerTask {
        Date date = DateUtils.addMonths(new Date(), -1);
        String startTime = DateFormatUtils.format(date,"yyyy-MM-dd");

        @Override
        public void run() {
            try {
                List<StockCompany> tasks = oneAccessor.fetchStock();
                tasks.forEach(
                        p -> {
                            Job job = new Job();
                            String url = String.format(initUrl, p.getCode(), startTime);
                            job.setUrl(url);
                            job.setFrequency(14400);
                            job.setKind(JobKind.Forever);
                            job.setCode(Md5Util.getMd5(url));
                            jobManager.add(job);
                        }
                );
                log.info("get_Stock_Task_size: " + tasks.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
