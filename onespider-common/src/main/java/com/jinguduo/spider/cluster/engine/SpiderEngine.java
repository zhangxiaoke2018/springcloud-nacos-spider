package com.jinguduo.spider.cluster.engine;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.util.StringUtils;

import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.exception.BadHttpResponseException;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.downloader.Downloader;
import com.jinguduo.spider.webmagic.downloader.HttpClientDownloader;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;
import com.jinguduo.spider.webmagic.scheduler.QueueScheduler;
import com.jinguduo.spider.webmagic.scheduler.Scheduler;
import com.jinguduo.spider.webmagic.thread.CountableThreadPool;
import com.jinguduo.spider.webmagic.utils.UrlUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 执行引擎，协调爬行逻辑（scheduler，downloader，spiders）
 * 改写SpiderListener，暴露更多内部元素
 * 对于WebMagic的命名不满意，改为更贴近Scrapy的命名
 * 
 * @see Downloader
 * @see Scheduler
 * @see Spider
 * @see Pipeline
 * @since 0.1.0
 */
@Slf4j
public class SpiderEngine implements Runnable, Task {

    protected Downloader downloader;

    protected List<Pipeline> pipelines = new ArrayList<Pipeline>();

    protected Spider spider;

    protected List<Request> startRequests;

    protected Site site;

    protected String uuid;

    protected Scheduler scheduler = new QueueScheduler();
    
    protected ThreadStrategy threadStrategy;

    protected CountableThreadPool threadPool;

    protected int threadNum = 1;

    protected AtomicInteger stat = new AtomicInteger(STAT_INIT);

    protected boolean exitWhenComplete = true;  // for test

    protected final static int STAT_INIT = 0;

    protected final static int STAT_RUNNING = 1;

    protected final static int STAT_STOPPED = 2;

    protected boolean spawnUrl = false;

    protected boolean destroyWhenExit = true;

    private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();

    private List<SpiderListener> spiderListeners;

    private Metrizable pageCounter;

    private Date startTime;

    private int emptySleepTime = 500;
    
    private String threadName = null;

    /**
     * create a spider with spider.
     *
     * @param spider spider
     * @return new SpiderEngine
     * @see Spider
     */
    public static SpiderEngine create(Spider spider) {
        return new SpiderEngine(spider);
    }

    /**
     * create a spider with spider.
     *
     * @param spider spider
     */
    public SpiderEngine(Spider spider) {
        this.spider = spider;
        this.site = spider.getSite();
    }

    /**
     * Set startUrls of SpiderEngine.<br>
     * Prior to startUrls of Site.
     *
     * @param startRequests startRequests
     * @return this
     */
    public SpiderEngine startRequest(List<Request> startRequests) {
        checkIfRunning();
        this.startRequests = startRequests;
        return this;
    }

    /**
     * Set an uuid for spider.<br>
     * Default uuid is domain of site.<br>
     *
     * @param uuid uuid
     * @return this
     */
    public SpiderEngine setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }


    /**
     * set scheduler for SpiderEngine
     *
     * @param scheduler scheduler
     * @return this
     * @since 0.2.1
     */
    public SpiderEngine setScheduler(Scheduler scheduler) {
        checkIfRunning();
        Scheduler oldScheduler = this.scheduler;
        this.scheduler = scheduler;
        if (oldScheduler != scheduler && oldScheduler != null) {
            Request request;
            while ((request = oldScheduler.poll(this)) != null) {
                this.scheduler.push(request, this);
            }
        }
        return this;
    }

    /**
     * add a pipeline for SpiderEngine
     *
     * @param pipeline pipeline
     * @return this
     * @see Pipeline
     * @since 0.2.1
     */
    public SpiderEngine addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }

    /**
     * set pipelines for SpiderEngine
     *
     * @param pipelines pipelines
     * @return this
     * @see Pipeline
     * @since 0.4.1
     */
    public SpiderEngine setPipelines(List<Pipeline> pipelines) {
        checkIfRunning();
        this.pipelines = pipelines;
        return this;
    }

    /**
     * clear the pipelines set
     *
     * @return this
     */
    public SpiderEngine clearPipeline() {
        pipelines = new ArrayList<Pipeline>();
        return this;
    }

    /**
     * set the downloader of spider
     *
     * @param downloader downloader
     * @return this
     * @see Downloader
     */
    public SpiderEngine setDownloader(Downloader downloader) {
        checkIfRunning();
        this.downloader = downloader;
        return this;
    }

    protected void initComponent() {
    	if (!StringUtils.hasText(threadName) && site != null) {
    		threadName = site.getDomain().replace('.', '-');
    	}
    	Thread.currentThread().setName(threadName);
        if (downloader == null) {
            this.downloader = new HttpClientDownloader();
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        if (threadStrategy == null) {
        	threadStrategy = new FixedThreadStrategy(threadNum, threadName + "-t");
		}
        threadPool = threadStrategy.createThreadPool();
        // TODO: 增加site主机信息,用于不同的host配置
        //downloader.setThread(threadNum);
        if (startRequests != null) {
            for (Request request : startRequests) {
                scheduler.push(request, this);
            }
            startRequests.clear();
        }
        startTime = new Date();
    }

    @Override
    public void run() {
        checkRunningStat();
        onStart();
        initComponent();
        log.info("SpiderEngine " + site.getDomain() + " " + getUUID() + " started!");
        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
            Request request = scheduler.poll(this);
            if (request == null) {
                if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                final Request requestFinal = request;
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                    	boolean wellDone = false;
                        try {
                            onRequest(requestFinal);
                            wellDone = processRequest(requestFinal);
                            //onSuccess(requestFinal);
                        } catch (Exception e) {
                        	onProcessFail(requestFinal);
                        	wellDone = true;
                            onError(requestFinal, e);
                            // 这里会写出大量的log
                            log.error(requestFinal.toString(), e);
                        } finally {
                        	signalNewUrl();
                            if (pageCounter != null) {
                                pageCounter.addAndGet(1);
                            }
                            if (!wellDone) {
                            	onProcessFail(requestFinal);
							}
                            if (site.getSleepTime() > 0) {
                                sleep(site.getSleepTime());
                            }
                        }
                    }
                });
            }
            threadStrategy.resize(threadNum);
        }
        stat.set(STAT_STOPPED);
        onExit();
        // release some resources
        if (destroyWhenExit) {
            close();
        }
    }
    
    protected void onStart() {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onStart(this);
            }
        }
    }
    
    protected void onRequest(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onRequest(request, this);
            }
        }
    }
    
    protected void onResponse(Request request, Page page) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onResponse(request, page, this);
            }
        }
    }

    protected void onError(Request request, Exception e) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request, e, this);
            }
        }
    }
    
    protected void onExit() {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onExit(this);
            }
        }
    }

    private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("SpiderEngine is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }

    public void close() {
        destroyEach(downloader);
        destroyEach(spider);
        destroyEach(scheduler);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private boolean processRequest(Request request) throws Exception {
        // no catch exception, throw it
        Page page = downloader.download(request, this);
        onResponse(request, page);
    	if (page != null && page.isDownloadSuccess()){
    		onDownloadSuccess(request, page);
    	} else {
    		onDownloaderFail(request);
    	}
    	return true;
    }

    private void onDownloadSuccess(Request request, Page page) throws Exception {
    	if (log.isDebugEnabled()) {
    		log.debug("Response Status Code {} {}", page.getStatusCode(), page.getUrl());
    	}
        if (site.getAcceptStatCode().contains(page.getStatusCode())){
        	// no catch exception, throw it
            try {
                spider.process(page);
                //extractAndAddRequests(page, spawnUrl);
            } finally {
                if (!page.getResultItems().isSkip()) {
                	Exception last =  null;
                    for (Pipeline pipeline : pipelines) {
                    	try {
                    		pipeline.process(page.getResultItems(), this);
						} catch (Exception e) {
							if (last != null) {
								log.error(last.getMessage(), last);
							}
							last = e;
						}
                    }
                    if (last != null) {
                    	throw last;
					}
                }
            }
        } else {
        	throw new BadHttpResponseException("Response Status Code " + page.getStatusCode());
        }
    }
    
    private void onDownloaderFail(Request request) {
    	doCycleRetry(request);
    }
    
    private void onProcessFail(Request request) {
    	doCycleRetry(request);
    }

    private void doCycleRetry(Request request) {
    	if (site.getCycleRetryTimes() <= 0) {
    		return;
    	}
        if (log.isDebugEnabled()) {
            log.debug("CycleRetry: " + request.toString());
        }
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            addCycleRequest(request, 1);
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes <= site.getCycleRetryTimes()) {
                addCycleRequest(request, cycleTriedTimes);
            }
        }
    }

    private void addCycleRequest(Request request, final int cycleTriedTimes) {
        Request req = SerializationUtils.clone(request).setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes);
        if (log.isDebugEnabled()) {
            log.debug(req.toString());
        }
        if (req instanceof DelayRequest) {
            DelayRequest r = (DelayRequest)req;
            r.setDelay(site.getRetryDelayTime());
            r.getJob().setKind(JobKind.Once);
        }
        addRequest(req);
    }

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addRequest(Request request) {
        if (site.getDomain() == null && request != null && request.getUrl() != null) {
            site.setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }

    protected void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("SpiderEngine is already running!");
        }
    }

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * Add urls with information to crawl.<br>
     *
     * @param requests requests
     * @return this
     */
    public SpiderEngine addRequest(Request... requests) {
        for (Request request : requests) {
            addRequest(request);
        }
        signalNewUrl();
        return this;
    }

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
    	newUrlLock.lock();
        try {
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    public void start() {
        runAsync();
    }

    public void stop() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            log.info("SpiderEngine " + getUUID() + " stop success!");
        } else {
            log.info("SpiderEngine " + getUUID() + " stop fail!");
        }
    }

    /**
     * start with more than one threads
     *
     * @param threadNum threadNum
     * @return this
     */
    public SpiderEngine thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    /**
     * start with more than one threads
     *
     * @param executorService executorService to run the spider
     * @param threadNum threadNum
     * @return this
     */
    public SpiderEngine thread(ThreadStrategy threadStrategy, int threadNum) {
        checkIfRunning();
        this.threadStrategy = threadStrategy;
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    public boolean isExitWhenComplete() {
        return exitWhenComplete;
    }

    /**
     * Exit when complete. <br>
     * True: exit when all url of the site is downloaded. <br>
     * False: not exit until call stop() manually.<br>
     *
     * @param exitWhenComplete exitWhenComplete
     * @return this
     */
    public SpiderEngine setExitWhenComplete(boolean exitWhenComplete) {
        this.exitWhenComplete = exitWhenComplete;
        return this;
    }

    public boolean isSpawnUrl() {
        return spawnUrl;
    }

    /**
     * Get running status by spider.
     *
     * @return running status
     * @see SpiderStatus
     * @since 0.4.1
     */
    public SpiderStatus getStatus() {
        return SpiderStatus.fromValue(stat.get());
    }


    /**
     * Get thread count which is running
     *
     * @return thread count which is running
     * @since 0.4.1
     */
//    public int getThreadAlive() {
//        if (threadPool == null) {
//            return 0;
//        }
//        return threadPool.getThreadAlive();
//    }

    /**
     * Whether add urls extracted to download.<br>
     * Add urls to download when it is true, and just download seed urls when it is false. <br>
     * DO NOT set it unless you know what it means!
     *
     * @param spawnUrl spawnUrl
     * @return this
     * @since 0.4.0
     */
    public SpiderEngine setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }

    @Override
    public String getUUID() {
        if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return site.getDomain();
        }
        uuid = UUID.randomUUID().toString();
        return uuid;
    }

//    public SpiderEngine setExecutorService(ExecutorService executorService) {
//        checkIfRunning();
//        this.executorService = executorService;
//        return this;
//    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<SpiderListener> getSpiderListeners() {
        return spiderListeners;
    }

    public SpiderEngine setSpiderListeners(List<SpiderListener> spiderListeners) {
        this.spiderListeners = spiderListeners;
        return this;
    }
    
    public SpiderEngine addSpiderListeners(List<SpiderListener> spiderListeners) {
        if (spiderListeners == null || spiderListeners.isEmpty()) {
            return this;
        }
        if (this.spiderListeners == null) {
            this.spiderListeners = spiderListeners;
        } else {
            this.spiderListeners.addAll(spiderListeners);
        }
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Set wait time when no url is polled.<br><br>
     *
     * @param emptySleepTime In MILLISECONDS.
     */
    public SpiderEngine setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
        return this;
    }

    public Metrizable getPageCounter() {
        return pageCounter;
    }

    public SpiderEngine setPageCounter(Metrizable pageCounter) {
        this.pageCounter = pageCounter;
        return this;
    }

    public SpiderEngine setThreadName(String name) {
        this.threadName = name;
        return this;
    }
}

