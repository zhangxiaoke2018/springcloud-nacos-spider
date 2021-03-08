package com.jinguduo.spider.spider.mgtv;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * 芒果相关扩产类
 */
@Slf4j
public class MgExtend {

    /**
     * 创建一个初始弹幕任务
     * @param vid
     * @param cid
     * @return
     */
    static Job barrageTextJob(String vid, String cid) {
        Job barrageJob = new Job(
                    String.format(
                            MgConstant.BARRAGE_URL,
                            vid,
                            cid
                    )
            );
        barrageJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        return barrageJob;
    }
}
