package com.jinguduo.spider.job;

import java.util.Collection;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobPackage;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.common.type.Sequence;
import com.jinguduo.spider.worker.Worker;
import com.jinguduo.spider.worker.WorkerManager;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 
 * <p>关于JobTrackerRebalancer：
 *   <p>默认使用非激进的温和模式（friendliest）分配任务——为了任务的粘性，
 *     单个Worker重启不至于所有任务再均衡两次
 *   <p>但在极端情况下（如Supervisor首次分配任务但部分Worker重启未上线）
 *     会导致任务极度不均衡，并且无机会修正。
 *   <p>于是增加一个定期任务，用激进模式（aggressive）的重分配所有任务
 *
 */
@Component
@CommonsLog
public class JobTracker {
	
	@Autowired
	private WorkerManager workerManager;
	
	@Autowired
	private JobManager jobManager;
	
	private Sequence syncVersion = new Sequence();
	
	private final Timer timer = new Timer("JobTrackerRebalancer");
	private long friendliestDelay = TimeUnit.SECONDS.toMillis(25);
    private long friendliestPeriod = TimeUnit.SECONDS.toMillis(5);
    
    private long aggressiveDelay = TimeUnit.HOURS.toMillis(1);
	private long aggressivePeriod = TimeUnit.HOURS.toMillis(1);

	// Job均衡模式
	private final static boolean FRIENDLIEST = false;  // 非激进的温和模式
	private final static boolean AGGRESSIVE = true;  // 激进模式
	
	public JobTracker() {
        timer.schedule(new Rebalancer(FRIENDLIEST), friendliestDelay, friendliestPeriod);
		timer.schedule(new Rebalancer(AGGRESSIVE), aggressiveDelay, aggressivePeriod);
	}
	
	public synchronized void rebalance(boolean isAggressive) {
	    Set<Worker> workers = workerManager.getAllActivedWorkers();
        if (workers == null || workers.isEmpty()) {
            log.info("The Worker is null.");
            return;
        }
        jobManager.allocateJobs(workers.toArray(new Worker[workers.size()]), isAggressive);
	}
	
	public JobPackage sync(String workerUuid, int clientVersion, Collection<JobRef> jobRefs) {
		Worker worker = workerManager.getActivedWorkerByUuid(workerUuid);
		if (worker == null) {
			return null;
		}
		final String domain = worker.getDomain();
		// 客户端和服务端，任务合并下发
		Collection<Job> newJobs = jobManager.takeAllocatedJobs(worker, jobRefs);
		
		return new JobPackage(syncVersion.incrementAndGet(workerUuid), workerUuid, domain, newJobs);
	}
	
	class Rebalancer extends TimerTask {
		private boolean style;

		Rebalancer(boolean style){
			this.style = style;
		}

        @Override
        public void run() {
            try {
                rebalance(style);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
	}
}
