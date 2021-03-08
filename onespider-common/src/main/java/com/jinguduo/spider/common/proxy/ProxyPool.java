package com.jinguduo.spider.common.proxy;

import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;

public interface ProxyPool {
    int getPoolSize();
    
    void addProxy(Proxy... proxies);
    
    void remove(Proxy proxy);

    /**
    *
    * Return proxy to Provider when complete a download.
    * @param proxy the proxy config contains host,port and identify info
    * @param page the download result
    */
   void returnProxy(Proxy proxy, Page page);

   /**
    * Get a proxy for task by some strategy.
    * @param task the download task
    * @return proxy 
    */
   Proxy getProxy();
}
