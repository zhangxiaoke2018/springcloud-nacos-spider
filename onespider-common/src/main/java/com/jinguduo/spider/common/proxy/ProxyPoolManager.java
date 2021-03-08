package com.jinguduo.spider.common.proxy;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.InitializingBean;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.metric.MetricFactory;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.common.thread.AsyncTask;
import com.jinguduo.spider.common.util.HostUtils;
import com.jinguduo.spider.data.loader.ProxyStoreLoader;
import com.jinguduo.spider.data.table.Proxy;

import lombok.extern.apachecommons.CommonsLog;

/**
 * HttpProxy管理
 * 
 * 从Supervisor获取HttpProxy列表
 * 
 */
@CommonsLog
public class ProxyPoolManager implements InitializingBean {
    
    private FastProxyPool proxyPool;

    private VpsProxyPool vpsProxyPool;

    private KuaidailiProxyPool kuaidailiProxyPool;
    
    private ProxyStoreLoader proxyStoreLoader;
    
    // 代理验证任务运行周期
    private long periodForInspector = TimeUnit.SECONDS.toMillis(180);
    // 代理验证任务并发线程
    private int inspectorThread = 2;
    
    // 加载Proxy任务时间间隔
    private long periodForLoader = TimeUnit.SECONDS.toMillis(30);

    // 加载VPS Proxy 任务的时间间隔
    private long vpsPeriodForLoader = TimeUnit.SECONDS.toMillis(5);
    private int vpsInspectorThread = 40;

    // 加载快代理任务的时间间隔
    private long kuaidailiPeriodForLoader = TimeUnit.SECONDS.toMillis(60);

    // 垃圾清理时间
    private long periodForCleaner = TimeUnit.SECONDS.toMillis(30);
    private int cleanerThread = 20;
    
    public FastProxyPool getHttpProxyPool() {
        return proxyPool;
    }

    public VpsProxyPool getVpsProxyPool() {
        return vpsProxyPool;
    }

    public KuaidailiProxyPool getKuaidailiProxyPool() {
        return kuaidailiProxyPool;
    }

    /**
     * 检查代理是否可用
     */
    class HttpProxyInspector extends TimerTask {
    	// 线程冗余防阻塞
        AsyncTask inspectorAsyncTask = new AsyncTask(inspectorThread * 4, "ProxyInspector");
        
        @Override
        public void run() {
            try {
                List<Proxy> proxies = proxyStoreLoader.load(ProxyState.Standby);
                if (proxies == null || proxies.isEmpty()) {
                    return;
                }
                if (proxies.size() > inspectorThread) {
                    Collections.shuffle(proxies);
                    proxies = proxies.subList(0, inspectorThread);
                }
                for (Proxy proxy : proxies) {
                    inspectorAsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean isAvailabled = false;
                            try {
                                // checkout
                                proxy.setState(ProxyState.Checkout);
                                proxyStoreLoader.save(proxy);
                                // validate
                                isAvailabled = ProxyHelper.validateProxy(proxy);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            } finally {
                                if (isAvailabled) {
                                	proxy.setState(ProxyState.Availabled);
                                } else {
                                	proxy.setState(ProxyState.Broken);
                                }
                                proxyStoreLoader.save(proxy);
                                proxyPool.addProxy(proxy);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            
        }
    }
    
    /**
     * 加载可用代理 
     */
    class HttpProxyLoader extends TimerTask {
        final Metrizable proxyPoolSize = MetricFactory.builder()
        		.namespace("onespider_fast_proxy")
                .metricName("fast_proxy_pool_size")
                .addDimension("Host", HostUtils.getHostName())
                .build();

        @Override
        public void run() {
            try {
                List<Proxy> proxies = proxyStoreLoader.load(ProxyState.Availabled);
                if (proxies != null && !proxies.isEmpty()) {
                    Collections.shuffle(proxies);
                    proxyPool.addProxy(proxies.toArray(new Proxy[proxies.size()]));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                proxyPoolSize.getAndSet(proxyPool.getPoolSize());
            }
        }
    }

    /**
     * 加载快代理
     */
    class KDLHttpProxyLoader extends TimerTask {

        final Metrizable metrizable = MetricFactory.builder()
                .namespace("onespider_kdl_proxy")
                .metricName("kdl_proxy_pool_size")
                .addDimension("Host", HostUtils.getHostName())
                .build();

        @Override
        public void run() {
            try {
                List<Proxy> proxies = proxyStoreLoader.load(ProxyState.Kuaidaili);
                if (proxies != null && !proxies.isEmpty()) {
                    kuaidailiProxyPool.clear();
                    kuaidailiProxyPool.addProxy(proxies.toArray(new Proxy[proxies.size()]));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                metrizable.getAndSet(kuaidailiProxyPool.getPoolSize());
            }
        }
    }


    /**
     * 加载可用VPS代理
     */
    class VpsHttpProxyLoader extends TimerTask {
    	AsyncTask async = new AsyncTask(vpsInspectorThread, "VpsInspectorThread");
    	final long ALIVED_TIME_MILLIS = TimeUnit.SECONDS.toMillis(50);
    	
    	Metrizable metrizable = MetricFactory.builder()
    			.namespace("onespider_vps_proxy")
    			.metricName("vps_proxy_pool_size")
    			.addDimension("Host", HostUtils.getHostName())
    			.build();

        @Override
        public void run() {
            try {
            	// 来自VPS的代理
            	List<Proxy> proxies = proxyStoreLoader.load(ProxyState.Vps);
            	if (proxies == null || proxies.isEmpty()) {
            		return;
            	}
            	final Stats stats = new Stats(proxies.size());
            	final long now = System.currentTimeMillis();
            	try {
            		for (Proxy proxy : proxies) {
            			if (vpsProxyPool.contains(proxy)) {
            				stats.good++;
            				continue;
            			}
            			vpsProxyPool.remove(proxy);  // 暂停旧的使用
            			if ((now - proxy.getUpdatedAt().getTime()) >= ALIVED_TIME_MILLIS) {
            				// 太长时间未更新，已失效
            				stats.bad++;
            				continue;
            			}
            			// 新的代理IP
            			async.execute(new Runnable() {
            				@Override
            				public void run() {
            					boolean v = false;
            					try {
            						v = ProxyHelper.validateSocketConnection(proxy);
            						if (v) {
            							vpsProxyPool.addProxy(proxy);
            						}
            					} finally {
            						if (v) {
            							stats.good++;
            						} else {
            							stats.bad++;
            						}
            						if (stats.isCompleted()) {
            							metrizable.getAndSet(stats.good);
            						}
            					}
            				}
            			});
            		}
				} finally {
					if (stats.isCompleted()) {
						metrizable.getAndSet(stats.good);
					}
				}
            } catch (Exception e) {
                log.error(e.getMessage(), e);
			}
        }
        
        class Stats {
        	int total = 0;
        	int bad = 0;
        	int good = 0;
        	public Stats(int t) {
				total = t;
			}
        	boolean isCompleted() {
        		return total == (bad + good);
        	}
        }
    }

    /**
     * 清理本地不可用代理
     */
    class HttpProxyCleaner extends TimerTask {
        // 冗余一倍的线程
        AsyncTask cleanerAsyncTask = new AsyncTask(cleanerThread * 2, "ProxyCleaner");

        @Override
        public void run() {
            try {
                List<Proxy> proxies = proxyPool.findAllWithBroken();
                if (proxies == null || proxies.isEmpty()) {
                    return;
                }

                if (proxies.size() > cleanerThread) {
                    proxies = proxies.subList(0, cleanerThread);
                }
                for (Proxy proxy : proxies) {
                    if (!proxy.isBroken()) {
                        continue;
                    }
                    cleanerAsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean isAvaliabled =  false;
                            try {
                                proxyPool.remove(proxy);
                                isAvaliabled = ProxyHelper.validateProxy(proxy);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            } finally {
                                if (isAvaliabled) {
                                	proxy.setState(ProxyState.Availabled);
                                    proxyPool.addProxy(proxy);
                                } else {
                                	proxy.setState(ProxyState.Broken);
                                }
                                proxyStoreLoader.save(proxy);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public void setProxyPool(FastProxyPool proxyPool) {
        this.proxyPool = proxyPool;
    }

    public void setHttpProxyStoreLoader(ProxyStoreLoader proxyStoreLoader) {
        this.proxyStoreLoader = proxyStoreLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (proxyPool == null) {
            proxyPool = new FastProxyPool();
        }
        if (vpsProxyPool == null) {
            vpsProxyPool = new VpsProxyPool();
        }
        if (kuaidailiProxyPool == null) {
            kuaidailiProxyPool = new KuaidailiProxyPool();
        }

        if (proxyStoreLoader != null) {
        	long delay = RandomUtils.nextLong(0, TimeUnit.SECONDS.toMillis(60));
            new Timer("ProxyPoolMangerLoader").scheduleAtFixedRate(new HttpProxyLoader(), 0, periodForLoader);
            new Timer("ProxyPoolMangerInspector").scheduleAtFixedRate(new HttpProxyInspector(), delay, periodForInspector);
            new Timer("ProxyPoolMangerCleaner").scheduleAtFixedRate(new HttpProxyCleaner(), periodForCleaner, periodForCleaner);

            new Timer("VpsProxyPoolMangerLoader").scheduleAtFixedRate(new VpsHttpProxyLoader(), 0, vpsPeriodForLoader);

            new Timer("KDLProxyPoolMangerLoader").scheduleAtFixedRate(new KDLHttpProxyLoader(), 0, kuaidailiPeriodForLoader);

        }
    }

}
