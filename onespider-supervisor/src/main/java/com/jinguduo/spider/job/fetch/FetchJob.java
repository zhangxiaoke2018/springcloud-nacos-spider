package com.jinguduo.spider.job.fetch;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 18:50
 */
@Component
public interface FetchJob {

    void process();

}
