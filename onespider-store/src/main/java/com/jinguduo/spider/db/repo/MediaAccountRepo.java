package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.MediaAccount;

import java.util.List;

@Component
public interface MediaAccountRepo extends JpaRepository<MediaAccount, Integer> {

    /** 查询爬虫账号，根据账号类型和账号状态 */
    List<MediaAccount> findByAccountTypeAndStatus(Integer accountType, Integer status);

    /** 查询爬虫账号，根据账号类型和账号状态 */
    List<MediaAccount> findByAccountType(Integer accountType);

    /** 查询爬虫账号，根据登陆机器Ip */
    MediaAccount findByAccountTypeAndWorkIp(Integer accountId, String workIp);

    /** 查询爬虫账号，根据账号名 */
    MediaAccount findByUserName(String userName);
}
