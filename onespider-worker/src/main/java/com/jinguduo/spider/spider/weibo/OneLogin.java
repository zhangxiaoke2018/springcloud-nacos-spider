package com.jinguduo.spider.spider.weibo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.util.ServerUtil;
import com.jinguduo.spider.data.table.MediaAccount;

import lombok.extern.apachecommons.CommonsLog;


/**
 * 统一登录入口
 * todo 传入不同登录工具类，登录
 */
@Component
@CommonsLog
public class OneLogin {

    private final RestTemplate restTemplate = new RestTemplate();

    private static Boolean lock = false;

    private Integer accountType;

    private Integer status;

    private String workIp;

    private String cookie;

    private MediaAccount mediaAccount = null;

    @Value("${onespider.store.queryAccount.url}")
    private String queryAccountUrl;

    @Value("${onespider.store.editAccount.url}")
    private String editAccountUrl;

    public OneLogin() {
    }

    {
        this.setWorkIp(null);
    }

    /***
     * 创建账号
     * @param mediaAccount
     * @return
     */
    public MediaAccount build(MediaAccount mediaAccount) {

        this.mediaAccount = mediaAccount;

        String url = "";
        String domain = mediaAccount.getDomain();
        try {
            url = UriComponentsBuilder
                    .fromHttpUrl(queryAccountUrl)
                    .queryParam("accountType", 1)
                    .queryParam("domain",domain)
                    .queryParam("status", status)
                    .queryParam("workIp", workIp)
                    .build()
                    .toUriString();
            this.mediaAccount = restTemplate.getForObject(url, MediaAccount.class);
        } catch ( Exception e ) {
            log.error(e.getMessage(), e);
            return null;
        }
        if (null == this.mediaAccount) {
            log.error(" no any free mediaAccount! domain:" + domain);
            return null;
        }
        return this.mediaAccount;
    }

    /**
     * 修改
     */
    public synchronized void edit() {
        String editUrl = UriComponentsBuilder
                .fromHttpUrl(editAccountUrl)
                .queryParam("id", this.mediaAccount.getId())
                .queryParam("domain", this.mediaAccount.getDomain())
                .queryParam("accountType", this.mediaAccount.getAccountType())
                .queryParam("status", this.getStatus())
                .queryParam("userName", this.mediaAccount.getUserName())
                .queryParam("workIp", this.getWorkIp())
                .build()
                .toUriString();
        this.restTemplate.getForObject(editUrl, MediaAccount.class);
    }

    /***
     * 过期，失效
     */
    public synchronized void onInvalid() {
        this
                .setStatus(Status.OVERDUE.code)
                .setWorkIp(null)
                .edit();
    }

    /***
     * 成功
     */
    public synchronized void onSuccess() {
        this
                .setStatus(Status.SUCCESS.code)
                .setWorkIp(null)
                .edit();
    }

    /***
     * 失败
     */
    public synchronized void onFail() {
        this
                .setStatus(Status.FAIL.code)
                .setWorkIp(null)
                .edit();
    }

    /**
     * 登录类型 1：微博
     */
    public OneLogin setAccountType(Integer accountType) {
        this.accountType = accountType;
        return this;
    }

    /**
     * 服务器Ip地址
     */
    public OneLogin setWorkIp(String workIp) {
        if (null == workIp) {
            workIp = ServerUtil.getLocalIP();
//            workIp = ServerUtil.getServerIp();
        }
        this.workIp = workIp;
        return this;
    }

    /**
     * 账号登录状态； -1：登录失败，0：未登录，1：已登录， 2：cookie失效
     */
    public OneLogin setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public String getWorkIp() {
        return workIp;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public enum Status{
        INIT(0,"未登陆"),
        SUCCESS(1,"登陆成功"),
        FAIL(-1,"登陆失败"),
        OVERDUE(2,"过期或者被封");
        private Integer code;
        private String desc;
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 返回有效cookie
     * 新浪改进登录，无效方法
     * @param cookie
     * @return
     */
    @Deprecated
    public String getSub(String cookie){
        String sub = cookie.replaceAll(".*;SUB=(.*);SUBP=.*", "$1");
        sub = "SUB=" + sub + ";";
        return sub;
    }

}
