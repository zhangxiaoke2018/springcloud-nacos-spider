package com.jinguduo.spider.job.fetch;

import com.jinguduo.spider.access.OneAccessor;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Pulse;
import com.jinguduo.spider.job.JobManager;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 10:42
 */
@Component
@CommonsLog
@RefreshScope
public class FetchJobFromPulse implements FetchJob {

    @Autowired
    private JobManager jobManager;

    @Autowired
    private OneAccessor oneAccessor;

    private final Timer timer = new Timer("FetchJobFromPulseTimer");

    private long delay = TimeUnit.SECONDS.toMillis(10);
    private long period = TimeUnit.MINUTES.toMillis(5);

    @Override
    public void process() {
        timer.scheduleAtFixedRate(new Task(), delay, period);
    }

    class Task extends TimerTask {

        @Override
        public void run() {
            try {
                List<Pulse> pulses = oneAccessor.fetchPulse();
                pulses.forEach(
                        p -> {
                            Job job = new Job();
                            DbEntityHelper.derive(p, job);
                            jobManager.add(job);
                        }
                );
                log.info("get_pulse_size: " + pulses.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
