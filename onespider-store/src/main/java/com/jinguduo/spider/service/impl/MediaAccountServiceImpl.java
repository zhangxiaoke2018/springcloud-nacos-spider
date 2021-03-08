package com.jinguduo.spider.service.impl;

import com.google.common.collect.Lists;
import com.jinguduo.spider.data.table.MediaAccount;
import com.jinguduo.spider.db.repo.MediaAccountRepo;
import com.jinguduo.spider.service.MediaAccountService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MediaAccountServiceImpl implements MediaAccountService {

    private static Logger logger = LoggerFactory.getLogger(MediaAccountServiceImpl.class);

    @Autowired
    private MediaAccountRepo mediaAccountRepo;

    /***
     * 查询爬虫账号，根据账号类型和账号状态
     * 只能筛选，随机拿取可用的账号
     *
     * @param type
     * @param status
     * @return
     */
    @Override
    public List<MediaAccount> queryByTypeAndStatusForWork(Integer type, Integer status, String workIp) {

        List<MediaAccount> mediaAccounts = Lists.newArrayList();
        List<MediaAccount> list = null;

        /*if( null != status ){
            list = mediaAccountRepo.findByAccountTypeAndStatus(type,status);
        } else {
            list = this.queryByAccountType(type);
        }*/
        //只查找类型，忽略状态
        list = this.queryByAccountType(type);

        for ( MediaAccount ma : list ) {
            //1.如果没有使用的，直接返回账号
            if ( StringUtils.isBlank(ma.getWorkIp()) ) {
                mediaAccounts.add(ma);
                continue;
            }
            //2.如果状态未登陆，直接返回
            /*if ( 0 == ma.getStatus() ) {
                mediaAccounts.add(ma);
                continue;
            }*/
            //3.如果workIp相同，返回（每个work登陆过后，写入记录）
            if ( workIp.equals(ma.getWorkIp()) ) {
                mediaAccounts.add(ma);
                continue;
            }
        }

        if ( mediaAccounts.isEmpty() ) {
            logger.error("no free account for work");
        }

        return mediaAccounts;
    }

    /***
     * 查询爬虫账号，根据账号类型
     *
     * @param type
     * @return
     */
    @Override
    public List<MediaAccount> queryByAccountType(Integer type) {
        return mediaAccountRepo.findByAccountType(type);
    }

    /***
     * 修改登陆状态
     *
     * @param mediaAccount
     * @return
     */
    @Override
    public MediaAccount updateAccount(MediaAccount mediaAccount) {
        return this.mediaAccountRepo.save(mediaAccount);
    }

    /***
     * 查询登陆账号根据登陆机器Ip
     *
     * @param accountId
     * @param workIp
     * @return
     */
    @Override
    public MediaAccount queryByAccountTypeAndWorkIp(Integer accountId, String workIp) {
        return this.mediaAccountRepo.findByAccountTypeAndWorkIp(accountId,workIp);
    }

    /***
     * 查询账号根据用户名
     *
     * @param userName
     * @return
     */
    @Override
    public MediaAccount queryByUserName(String userName) {
        return this.mediaAccountRepo.findByUserName(userName);
    }

    /**
     * 保存或修改
     * @param ma
     * @return
     */
    @Override
    public MediaAccount saveOrUpdate(MediaAccount ma) {

        MediaAccount account = this.mediaAccountRepo.findByUserName(ma.getUserName());

        if( null == account ){
            return null;
        }

        ma.setId(account.getId());
        ma.setUserName(account.getUserName());
        ma.setPassword(account.getPassword());
        ma.setAccountType(account.getAccountType());
        ma.setCreateTime(account.getCreateTime());

        if (ma.isSuccess()) {
            Integer count = account.getLoginCount();
            count = (null == count ? 0 : count);
            ma.setLoginCount(count++);
        }
        if ( ma.isOverDue() || ma.isFail() ) {
            Integer count = account.getFailCount();
            count = (null == count ? 0 : count);
            count++;
            account.setFailCount(count);
        }
        if ( StringUtils.isBlank(ma.getCookie()) && StringUtils.isNotBlank(account.getCookie()) ){
            ma.setCookie(account.getCookie());
        }
        if ( StringUtils.isBlank(ma.getUserAgent()) && StringUtils.isNotBlank(account.getUserAgent()) ){
            ma.setUserAgent(account.getUserAgent());
        }
        if ( StringUtils.isBlank(ma.getHeaders()) && StringUtils.isNotBlank(account.getHeaders()) ){
            ma.setHeaders(account.getHeaders());
        }

        return this.mediaAccountRepo.save(ma);
    }
}
