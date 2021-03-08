package com.jinguduo.spider.service;

import java.util.List;

import com.jinguduo.spider.data.table.MediaAccount;

public interface MediaAccountService {

    /***
     * 查询爬虫账号，根据账号类型和账号状态
     * @param type
     * @param status
     * @return
     */
    List<MediaAccount> queryByTypeAndStatusForWork(Integer type, Integer status, String workIp);

    /***
     * 查询爬虫账号，根据账号类型
     * @param type
     * @return
     */
    List<MediaAccount> queryByAccountType(Integer type);

    /***
     * 修改登陆状态
     * @param mediaAccount
     * @return
     */
    MediaAccount updateAccount(MediaAccount mediaAccount);

    /***
     *
     * @param accountId
     * @param workIp
     * @return
     */
    MediaAccount queryByAccountTypeAndWorkIp(Integer accountId, String workIp);

    /***
     * 查询账号根据用户名
     * @param userName
     * @return
     */
    MediaAccount queryByUserName(String userName);

    /***
     * 保存或修改
     * @param mediaAccount
     * @return
     */
    MediaAccount saveOrUpdate(MediaAccount mediaAccount);
}
