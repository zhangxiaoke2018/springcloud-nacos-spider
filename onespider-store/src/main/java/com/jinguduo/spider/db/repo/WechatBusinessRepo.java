package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WechatBusiness;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 14/03/2017 11:02 AM
 */
@Repository
public interface WechatBusinessRepo extends JpaRepository<WechatBusiness, Integer> {

    WechatBusiness findByWechatId(String wechatId);

    List<WechatBusiness> findByGreatest(Boolean greatest);




}
