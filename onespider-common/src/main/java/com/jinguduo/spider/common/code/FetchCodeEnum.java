package com.jinguduo.spider.common.code;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:24
 */
@CommonsLog
public enum FetchCodeEnum {

    TENGXUN("qq.com",TengXunFetchCode.class),
    IQIYI("qiyi.com",IqiyiFetchCode.class),
    YOUKU("youku.com",YoukuFetchCode.class),
    TUDOU("tudou.com",TudouFetchCode.class),
    SOHU("sohu.com",SohuFetchCode.class),
    LE("le.com",LeFetchCode.class),
    MGTV("mgtv.com",MgtvFetchCode.class),
    KANKAN("kankan.com",KanKanFetchCode.class),
    FENGXING("fun.tv",FengXingFetchCode.class),
    FIVE_SIX_MOVIE("56.com",FiveSixMovieFetchCode.class),
    PPTV("pptv.com",PPTVFetchCode.class),
    TAOMI("v.61.com",TaomiFetchCode.class),
    KUMI("www.kumi.cn",KumiFetchCode.class),
    ACFAN("www.acfun.tv",AcfanFetchCode.class),
    BILIBILI("bangumi.bilibili.com",BiliBiliFetchCode.class),
    BILIBILI2("www.bilibili.com",BiliBili2FetchCode.class),
    DOUBAN("movie.douban.com",DouBanFetchCode.class),
    TIEBA("tieba.baidu.com",TieBaFetchCode.class),
    WEIBO("http://weibo.com",WeiBoFetchCode.class),
    HTTPS_WEIBO("https://weibo.com",WeiBoFetchCode.class),
    WEIINDEX("data.weibo.com",WeiIndexFetchCode.class),
    BAIDU_NEWS("news.baidu.com",BaiduNewsFetchCode.class),
    SOU_360_NEWS("news.so.com",SoNewsFetchCode.class),
    WEIBO_SEARCH("s.weibo.com",WeiboSearchFetchCode.class),
    SOUGOU_WEIXIN("weixin.sogou.com",SougouWeixinFetchCode.class),
    BAIDU_VEDIO("v.baidu.com",BaiduVedioSearchFetchCode.class),
    BAIDU_SEARCH("www.baidu.com",BaiduSearchFetchCode.class),
    INDEX_360("index.haosou.com", Index360FetchCode.class),
    CBOOO("www.cbooo.cn",ChinaBoxOfficeCode.class),
    TOUTIAO_VIDEO("www.toutiao.com", ToutiaoVideoFetchCode.class)

    ;

    private final String host;

    private final Class<? extends FetchCode> clazz;


    FetchCodeEnum(String host, Class<? extends FetchCode> clazz) {
        this.host = host;
        this.clazz = clazz;
    }

    public String getHost() {
        return host;
    }

    public FetchCode getClazz() throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    public static String getCode(String url) {
        try {
            for (FetchCodeEnum fetchCodeEnum : FetchCodeEnum.values()) {
                if (url.contains(fetchCodeEnum.getHost())) {
                    return fetchCodeEnum.getClazz().get(url);
                }
            }
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

}
