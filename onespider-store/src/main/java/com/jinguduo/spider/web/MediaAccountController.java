package com.jinguduo.spider.web;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.table.MediaAccount;
import com.jinguduo.spider.service.MediaAccountService;

@Controller
@ResponseBody
public class MediaAccountController {

    @Autowired
    private MediaAccountService mediaAccountService;

    /***
     * 查找可登陆账号
     * @param domain
     * @param typeStr
     * @param statusStr
     * @param workIp
     * @return
     */
    @RequestMapping("/queryAccount")
    public MediaAccount getAccount (
            @RequestParam(name = "domain") String domain,
            @RequestParam(name = "accountType") String typeStr,
            @RequestParam(name = "status") String statusStr,
            @RequestParam(name = "workIp") String workIp ) {

        List<MediaAccount> mediaAccounts = null;
        MediaAccount mediaAccount = null;
        Integer status = null;

        if ( StringUtils.isBlank(typeStr) ) {
            return mediaAccount ;
        }

        Integer type = Integer.valueOf(typeStr);

        if ( StringUtils.isNotBlank(statusStr) ) {
            status = Integer.valueOf(statusStr);
        }

        List<MediaAccount> ms = this.mediaAccountService.queryByTypeAndStatusForWork(type, status, workIp);

        mediaAccounts = ms.stream().filter(m -> workIp.equals(m.getWorkIp())).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(mediaAccounts)){
            mediaAccount = mediaAccounts.stream().findFirst().orElse(new MediaAccount());
        } else {
            mediaAccount = ms.stream().findFirst().orElse(new MediaAccount());
        }

        return mediaAccount;
    }

    /***
     * 更新账号信息
     * @param id
     * @param type
     * @param statusStr
     * @param userName
     * @param workIp
     * @param cookie
     * @return
     */
    @RequestMapping("/edit")
    public MediaAccount edit (
            @RequestParam(name = "id") String id,
            @RequestParam(name = "accountType") String type,
            @RequestParam(name = "status") String statusStr,
            @RequestParam(name = "userName") String userName,
            @RequestParam(name = "workIp") String workIp,
            String domain , String cookie ) {

        MediaAccount mediaAccount = new MediaAccount();

        if ( StringUtils.isBlank(workIp) || StringUtils.isBlank(workIp) ) {
            return mediaAccount;
        }

        Integer status = Integer.valueOf(statusStr);

        mediaAccount.setId(Integer.valueOf(id));
        mediaAccount.setAccountType(Integer.valueOf(type));
        mediaAccount.setStatus(Integer.valueOf(status));
        mediaAccount.setUserName(userName);
        mediaAccount.setWorkIp(workIp);
        mediaAccount.setCookie(cookie);
        mediaAccount.setDomain(domain);
        return this.mediaAccountService.saveOrUpdate(mediaAccount);
    }

    /***
     * 更新状态
     * @param accountId
     * @param statusStr
     * @param workIp
     * @return
     */
    @Deprecated
    @RequestMapping("/editAccount")
    public MediaAccount editAccount (
            @RequestParam(name = "accountType") String accountId,
            @RequestParam(name = "status") String statusStr,
            @RequestParam(name = "userName") String userName,
            @RequestParam(name = "workIp") String workIp, String cookie ) {

        MediaAccount mediaAccount = new MediaAccount();

        if ( StringUtils.isBlank(workIp) || StringUtils.isBlank(workIp) ) {
            return mediaAccount;
        }

        /**
         * 有账号直接修改(登陆)
         */
        if (StringUtils.isNotBlank(userName)) {
            mediaAccount = this.mediaAccountService.queryByUserName(userName);
            Integer loginCount = mediaAccount.getLoginCount();
            loginCount = (null == loginCount ? 0 : loginCount);
            loginCount++;
            mediaAccount.setLoginCount(loginCount);
        } else {
            mediaAccount = this.mediaAccountService.queryByAccountTypeAndWorkIp(Integer.valueOf(accountId), workIp);
        }

        if ( null == mediaAccount || null == mediaAccount.getId() ) {
            return new MediaAccount();
        }

        if ( StringUtils.isNotBlank(cookie) ){
            mediaAccount.setCookie(cookie);
        }
        mediaAccount.setStatus(Integer.valueOf(statusStr));
        mediaAccount.setWorkIp(workIp);

        return this.mediaAccountService.updateAccount(mediaAccount);
    }

}
