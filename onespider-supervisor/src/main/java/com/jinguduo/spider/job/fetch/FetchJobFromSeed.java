package com.jinguduo.spider.job.fetch;

import com.jinguduo.spider.access.AccessorClient;
import com.jinguduo.spider.access.OneAccessor;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.StatusEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Seed;
import com.jinguduo.spider.job.JobManager;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 28/07/2017 10:08
 */
@Component
@CommonsLog
@RefreshScope
public class FetchJobFromSeed implements FetchJob {

    @Autowired
    private AccessorClient oneAccessor;

//    @Autowired
//    private OneAccessor oneAccessor;

    @Autowired
    private JobManager jobManager;

    private final Timer timer = new Timer("FetchJobFromSeedTimer");
    private long allLoadDelay = TimeUnit.SECONDS.toMillis(10);  // 秒
    private long allLoadPeriod = TimeUnit.HOURS.toMillis(4);

    private long littleLoadDelay = TimeUnit.MINUTES.toMillis(5);
    private long littleLoadPeriod = littleLoadDelay;

    @Override
    public void process() {
        // 加载所有seed
        timer.scheduleAtFixedRate(new LoadAllSeedTask(), allLoadDelay, allLoadPeriod);
        // 加载最近有更新的seed
        timer.scheduleAtFixedRate(new LoadSeedLastestTask(littleLoadPeriod), littleLoadDelay, littleLoadPeriod);
    }

    class LoadAllSeedTask extends TimerTask {
        @Override
        public void run() {
            try {
                Collection<Seed> seeds = oneAccessor.fetchSeeds(0L);
                addAll(seeds);
                if (log.isDebugEnabled() && seeds != null) {
                    log.debug("get all seed, size: " + seeds.size());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    class LoadSeedLastestTask extends TimerTask {
        private long loadTime;
        private final long offset = TimeUnit.MINUTES.toMillis(1); // 服务器之间的时间差
        private final long period;

        public LoadSeedLastestTask(long period) {
            this.period = period;
            loadTime = nextTime();
        }

        @Override
        public void run() {
            try {
                Collection<Seed> seeds = oneAccessor.fetchSeeds(loadTime);
                addAll(seeds);
                if (log.isDebugEnabled() && seeds != null) {
                    log.debug("get lastest seeds: " + seeds.size());
                }
                loadTime = nextTime();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        long nextTime() {
        	return System.currentTimeMillis() - (period + offset);
        }
    }

    public void addAll(Iterable<Seed> seeds) {
        if (seeds == null) {
            return;
        }
        for (Seed seed : seeds) {
            try {
                add(seed);
            } catch (Exception e) {
                // catch 异常继续执行后面的seed
                log.error(seed.toString(), e);
            }
        }
    }

    private void add(Seed seed) throws URISyntaxException {
        Job job = DbEntityHelper.derive(seed, new Job());
        if (seed.getStatus() == StatusEnum.STATUS_DEL.getValue()) {
            jobManager.remove(job);
            return;
        }
        jobManager.add(job);
    }
}
