package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.JdGoods;
import com.jinguduo.spider.db.repo.JdGoodsRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/10/31
 */
@Service
public class JdGoodsService {

    @Autowired
    private JdGoodsRepo jdGoodsRepo;

    public JdGoods save(JdGoods goods) {
        JdGoods old = jdGoodsRepo.findByDayAndGoodsId(goods.getDay(), goods.getGoodsId());

        if (null == old) {
            return jdGoodsRepo.save(goods);
        } else {
            if (!StringUtils.isEmpty(goods.getTitle())) {
                old.setTitle(goods.getTitle());
            }
            if (null != goods.getCommentCount() && !goods.getCommentCount().equals(0)) {
                old.setCommentCount(goods.getCommentCount());
            }
            if (null != goods.getAfterCount() && !goods.getAfterCount().equals(0)) {
                old.setAfterCount(goods.getAfterCount());
            }
            if (null != goods.getGoodCount() && !goods.getGoodCount().equals(0)) {
                old.setGoodCount(goods.getGoodCount());
            }
            if (null != goods.getDefaultGoodCount() && !goods.getDefaultGoodCount().equals(0)) {
                old.setDefaultGoodCount(goods.getDefaultGoodCount());
            }
            if (null != goods.getGeneralCount() && !goods.getGeneralCount().equals(0)) {
                old.setGeneralCount(goods.getGeneralCount());
            }
            if (null != goods.getPoorCount() && !goods.getPoorCount().equals(0)) {
                old.setPoorCount(goods.getPoorCount());
            }

            return jdGoodsRepo.save(old);
        }
    }
}
