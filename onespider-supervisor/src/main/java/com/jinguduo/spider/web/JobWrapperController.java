package com.jinguduo.spider.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.job.JobManager;

/**
 * 爬虫在运行任务及状态API(Maybe Restful)
 * 
 *
 */
@RestController
public class JobWrapperController {
    
    @Autowired
    private JobManager jobManager;

    @RequestMapping(value = "/job", method = RequestMethod.GET)
    public Paginator<JobWrapper> doList(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size) throws UnsupportedEncodingException {
        
        Paginator<JobWrapper> p;
        if (StringUtils.hasText(code)) {
            //youku 以 ==结尾的 code被编码过，需要解码
            String decode = UriUtils.decode(code, "UTF-8");
            Collection<JobWrapper> jobWrappers = jobManager.getJobByCode(decode);
            if (jobWrappers == null) {
                jobWrappers = new ArrayList<>();
            }
            p = new Paginator<>(1, jobWrappers.size());
            p.setEntites(jobWrappers);
        } else {
            p = jobManager.getJobsByPaginator(page, size);
        }
        return p;
    }
}
