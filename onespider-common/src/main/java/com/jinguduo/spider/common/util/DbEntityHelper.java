package com.jinguduo.spider.common.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.data.table.Pulse;
import com.jinguduo.spider.data.table.Seed;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.data.text.CommentText;

public class DbEntityHelper {

    public static Job derive(Job job, Job newJob) {
        newJob.setFrequency(job.getFrequency());
        newJob.setShowId(job.getShowId());
        newJob.setPlatformId(job.getPlatformId());
        newJob.setCode(job.getCode());
        return newJob;
    }

    public static Job derive(Seed seed, Job job) {
        Assert.notNull(seed.getUrl(), "The Seed's URL is null.");

        job.setCode(seed.getCode());
        job.setFrequency(seed.getFrequency());
        job.setUrl(seed.getUrl());
        job.setMethod(seed.getMethod());
        job.setPlatformId(seed.getPlatformId());
        job.setKind(JobKind.Forever);

        return job;
    }

    public static Job derive(Pulse pulse, Job job){

        job.setUrl(pulse.getUrl());
        job.setFrequency(pulse.getFrequency());
        job.setKind(JobKind.Forever);
        job.setCode(Md5Util.getMd5(pulse.getUrl()));

        return job;
    }


    public static ShowLog derive(Job expected, ShowLog showLog) {
        showLog.setPlatformId(expected.getPlatformId());
        showLog.setCode(expected.getCode());
        showLog.setShowId(expected.getShowId());
        return showLog;
    }

    public static BarrageText derive(Job expected, BarrageText barrageText) {
        barrageText.setPlatformId(expected.getPlatformId());
        barrageText.setCode(expected.getCode());
        barrageText.setShowId(expected.getShowId());
        return barrageText;
    }

    public static CommentText derive(Job job, CommentText commentText) {
        commentText.setPlatformId(job.getPlatformId());
        commentText.setCode(job.getCode());
        commentText.setShowId(job.getShowId());
        return commentText;
    }

    public static CommentLog derive(Job expected, CommentLog commentLog) {
        commentLog.setPlatformId(expected.getPlatformId());
        commentLog.setCode(expected.getCode());
        commentLog.setShowId(expected.getShowId());
        return commentLog;
    }

    public static ExponentLog derive(Job expected, ExponentLog exponentLog) {
        exponentLog.setCode(expected.getCode());
        return exponentLog;
    }

    public static <T> T copy(Object src, T target, String[] ignoreProperties) {
        BeanUtils.copyProperties(src, target, ignoreProperties);
        return target;
    }

    public static <T> T copySpecialProperties(final Object source, T target, final String... includeProperties) {
        HashSet<String> includes = new HashSet<String>();
        includes.addAll(Arrays.asList(includeProperties));

        final Collection<String> excludes = new ArrayList<String>();
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(source.getClass());
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propName = propertyDescriptor.getName();
            if (!includes.contains(propName)) {
                excludes.add(propName);
            }
        }
        BeanUtils.copyProperties(source, target, excludes.toArray(new String[excludes.size()]));
        return target;
    }

    public static BarrageLog derive(Job expected, BarrageLog barrageLog) {
        barrageLog.setPlatformId(expected.getPlatformId());
        barrageLog.setCode(expected.getCode());
        barrageLog.setShowId(expected.getShowId());

        return barrageLog;
    }

    public static Job deriveNewJob(Job oldJob, String url) {
        Job newJob = new Job(url);
        DbEntityHelper.derive(oldJob, newJob);
        return newJob;
    }

    public static <M> void merge(M target, M destination) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

            if (descriptor.getWriteMethod() != null) {
                Object originalValue = descriptor.getReadMethod()
                        .invoke(target);

                if (originalValue == null) {
                    Object defaultValue = descriptor.getReadMethod().invoke(
                            destination);
                    descriptor.getWriteMethod().invoke(target, defaultValue);
                }

            }
        }
    }
}
