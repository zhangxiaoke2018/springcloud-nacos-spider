package com.jinguduo.spider.spider.weibo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.common.util.ServerUtil;
import com.jinguduo.spider.data.table.MediaAccount;

import lombok.extern.apachecommons.CommonsLog;

/**
 * TODO  后期改为公共登陆类，传入不同的登陆请求实体
 * mediaAccount login
 * login v 0.0.1
 */
@Deprecated
@Component
@CommonsLog
public class WeiboLogin {

    volatile static Boolean lock = false;

    private final RestTemplate restTemplate = new RestTemplate();

    private Integer accountType;

    private Integer status;

    private String workIp;

    private String cookie;

    private MediaAccount mediaAccount ;

    @Value("${onespider.store.queryAccount.url}")
    private String queryAccountUrl;

    @Value("${onespider.store.editAccount.url}")
    private String editAccountUrl;

    public WeiboLogin ( ) {
    }

    /***
     * 登陆微博，得到可用的cookie
     * @return
     */
    public  MediaAccount buildMediaAccount () {
        if(StringUtils.isNotBlank(Weibo.cookies_str)){
            mediaAccount.setCookie(Weibo.cookies_str);
            log.debug("WeiboLogin:"+Weibo.cookies_str);
            return this.mediaAccount;
        }
        //条件查询登陆账号
        String queryAccount = UriComponentsBuilder
                .fromHttpUrl(queryAccountUrl)
                .queryParam("accountType", accountType)
                .queryParam("status", status)
                .queryParam("workIp", workIp)
                .build()
                .toUriString();
        this.mediaAccount = restTemplate.getForObject(queryAccount, MediaAccount.class);

        if ( null == mediaAccount ) {
            log.error(" no any weiAccount can be login! ");
            return null;
        }
        //登陆微博
        Weibo weibo = new Weibo(mediaAccount.getUserName(),mediaAccount.getPassword());
        try {
            if ( weibo.login() ) {
                mediaAccount.setCookie(Weibo.cookies_str);
                //登陆成功后修改状态
                if(StringUtils.isNotBlank(mediaAccount.getCookie())){
                    String editUrl = UriComponentsBuilder
                            .fromHttpUrl(editAccountUrl)
                            .queryParam("accountType", accountType)
                            .queryParam("status", 1)
                            .queryParam("userName",mediaAccount.getUserName())
                            .queryParam("workIp", workIp)
                            .queryParam("cookie", mediaAccount.getCookie())
                            .build()
                            .toUriString();
                    restTemplate.getForObject(editUrl, MediaAccount.class);
                }
            } else {
                log.debug("weibo login fail username :["+mediaAccount.getUserName()+"]");
            }
        } catch ( Exception e ) {
            log.debug("weibo login Exception username :["+mediaAccount.getUserName()+"]");
            log.error(e.getMessage(), e);
        }
        return this.mediaAccount;
    }

    /***
     * 修改账号状态
     */
    public synchronized void editMediaAccount () {
        //条件查询登陆账号
        String editUrl = UriComponentsBuilder
                .fromHttpUrl(editAccountUrl)
                .queryParam("accountType", this.getAccountType())
                .queryParam("status", this.getStatus())
                .queryParam("userName","")
                .queryParam("workIp", this.getWorkIp())
                .queryParam("cookie", "")
                .build()
                .toUriString();
        restTemplate.getForObject(editUrl, MediaAccount.class);
    }

    /***
     * spider感知页面不符合目标解析页面，cookie失效处理，强制清除cookie，重新登陆
     */
    public synchronized void onCookieInvalid(){
        Weibo.cookies_str = "";//强制重新登陆
        log.error(" maybe need login again ");
        this
                .setAccountType(1)
                .setStatus(2)//cookie 失效
                .setWorkIp(null)
                .editMediaAccount();
    }

    /** 登录类型 1：微博 */
    public WeiboLogin setAccountType(Integer accountType) {
        this.accountType = accountType;
        return this;
    }

    /** 服务器Ip地址 */
    public WeiboLogin setWorkIp(String workIp) {
        if ( null == workIp ) {
            workIp = ServerUtil.getLocalIP();
//            workIp = ServerUtil.getServerIp();
        }
        this.workIp = workIp;
        return this;
    }

    /** 账号登录状态； 1：已登录， 2：cookie失效， 0：未登录， -1：登录失败，-2：异常， */
    public WeiboLogin setStatus(Integer status) {
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
}



