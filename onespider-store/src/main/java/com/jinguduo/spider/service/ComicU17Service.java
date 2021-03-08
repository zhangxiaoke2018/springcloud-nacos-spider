package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicU17;
import com.jinguduo.spider.db.repo.ComicU17Repo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 09:31
 */
@Service
@Slf4j
public class ComicU17Service {

    @Autowired
    private ComicU17Repo comicU17Repo;

    public ComicU17 save(ComicU17 comicU17) {

        ComicU17 save = comicU17Repo.save(comicU17);

        return save;
    }


    public ComicU17 insertOrUpdate(ComicU17 comicU17) {
        if (null == comicU17
                ||null == comicU17.getDay()
                ||StringUtils.isBlank(comicU17.getCode())) {
            return null;
        }
        boolean isSkip = skipByCode(comicU17.getCode());
        if (isSkip) {
            return comicU17;
        }
        ComicU17 cu = comicU17Repo.findByCodeAndDay(comicU17.getCode(),comicU17.getDay());
        //如果有该数据,插入id
        if (null != cu) {
            if (comicU17.getCommentCount() != null) {
                cu.setCommentCount(comicU17.getCommentCount());
            }
            if (comicU17.getMonthlyTicket() != null) {
                cu.setMonthlyTicket(comicU17.getMonthlyTicket());
            }
            if (comicU17.getTotalClick() != null) {
                cu.setTotalClick(comicU17.getTotalClick());
            }
            if (comicU17.getTotalLike() != null) {
                cu.setTotalLike(comicU17.getTotalLike());
            }
            return comicU17Repo.save(cu);
        } else {
            return comicU17Repo.save(comicU17);
        }
    }

}
