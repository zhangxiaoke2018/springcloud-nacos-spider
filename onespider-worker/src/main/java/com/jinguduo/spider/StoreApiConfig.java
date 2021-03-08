package com.jinguduo.spider;

import com.jinguduo.spider.cluster.pipeline.CollectionStorePipeline;
import com.jinguduo.spider.cluster.pipeline.StorePipeline;
import com.jinguduo.spider.cluster.spider.RemoteSpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.SpiderSettingLoader;
import com.jinguduo.spider.data.loader.*;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.data.table.bookProject.*;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.data.text.WeiboHotSearchText;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@SuppressWarnings("all")
public class StoreApiConfig {

    @Value("${onespider.store.ad_link_video.url}")
    private String adLinkedVideoStoreUrl;

    @Value("${onespider.store.ad_log.url}")
    private String adLogStoreUrl;

    @Value("${onespider.store.show_log.url}")
    private String showLogStoreUrl;

    @Value("${onespider.store.douban_log.url}")
    private String doubanLogStoreUrl;

    @Value("${onespider.store.tieba_log.url}")
    private String tiebaLogStoreUrl;

    @Value("${onespider.store.baidu_news_log.url}")
    private String baiduNewsLogStoreUrl;

    @Value("${onespider.store.baidu_video_log.url}")
    private String baiduVideoLogStoreUrl;

    @Value("${onespider.store.news_360_log.url}")
    private String news360LogStoreUrl;

    @Value("${onespider.store.weibo_official_log.url}")
    private String weiboOfficialLogStoreUrl;

    @Value("${onespider.store.show.url}")
    private String showStoreUrl;

    @Value("${onespider.store.comment_log.url}")
    private String commentLogStoreUrl;

    @Value("${onespider.store.barrage_log.url}")
    private String barrageLogStoreUrl;

    @Value("${onespider.store.barrage_texts.url}")
    private String barrageTextsStoreUrl;

    @Value("${onespider.store.comment_texts.url}")
    private String commentTextsStoreUrl;

    @Value("${onespider.store.spider_setting.url}")
    private String spiderSettingUrl;

    @Value("${onespider.store.doubanshow.url}")
    private String douBanShowUrl;

    @Value("${onespider.store.doubanactor.url}")
    private String douBanActorUrl;

    @Value("${onespider.store.exponent_log.url}")
    private String exponentLogStoreUrl;

    @Value("${onespider.store.cookie_string.url}")
    private String cookieStringStoreUrl;

    @Value("${onespider.store.user_agent.url}")
    private String userAgentStoreUrl;

    @Value("${onespider.store.weibotaglog.url}")
    private String weiboTagLogStoreUrl;

    @Value("${onespider.store.news_article_log.url}")
    private String newsArticleStoreUrl;

    @Value("${onespider.store.news_article_message_log.url}")
    private String newsArticleMessageStoreUrl;

    @Value("${onespider.store.weibo_attr.url}")
    private String weiboAttrStoreUrl;

    @Value("${onespider.store.weibo_province_compare}")
    private String weiboProvinceCompareStoreUrl;

    @Value("${onespider.store.tieba_article_logs}")
    private String tiebaArticleLogsStoreUrl;

    @Value("${onespider.store.index_360_logs}")
    private String index360LogsUrl;

    @Value("${onespider.store.media_360_logs}")
    private String media360LogsUrl;

    @Value("${onespider.store.news_toutiao_logs}")
    private String newsToutiaoLogsUrl;

    @Value("${onespider.store.screenshot_logs}")
    private String screenshotLogsUrl;

    @Value("${onespider.store.proxy.url}")
    private String proxyStoreUrl;

    @Value("${onespider.store.douban_comment.url}")
    private String doubanCommentStoreUrl;

    @Value("${onespider.store.weibo_text.url}")
    private String weiboTextStoreUrl;

    @Value("${onespider.store.weibo_hot_search_text.url}")
    private String weiboHotSearchTextStoreUrl;

    @Value("${onespider.store.sougouWechat_search_text}")
    private String sougouWechatSearchTextUrl;

    @Value("${onespider.store.sougouWechat_article_text}")
    private String sougouWechatArticleTextUrl;

    @Value("${onespider.store.show_actors}")
    private String showActorsUrl;

    @Value("${onespider.store.vip_episode}")
    private String vipEpisodeUrl;

    @Value("${onespider.store.customer_360_logs}")
    private String customer360LogsUrl;

    @Value("${onespider.store.bilibili.search.click}")
    private String bilibiliSearchClickUrl;

    @Value("${onespider.store.bilibili.search.dm}")
    private String bilibiliSearchDmUrl;

    @Value("${onespider.store.bilibili.search.stow}")
    private String bilibiliSearchStowUrl;

    @Value("${onespider.store.bilibili.search.count}")
    private String bilibiliSearchCountUrl;

    @Value("${onespider.store.bilibili.search.fans}")
    private String bilibiliFansCountUrl;

    @Value("${onespider.store.comic.kuaikan}")
    private String ComicKuaikanUrl;

    @Value("${onespider.store.comic.u17}")
    private String comicU17Url;

    @Value("${onespider.store.comic.zymk}")
    private String comicZymkUrl;

    @Value("${onespider.store.comic.tengxun}")
    private String comicTengxunUrl;

    @Value("${onespider.store.comic}")
    private String comicUrl;

    @Value("${onespider.store.comic.wangyi}")
    private String comic163Url;

    @Value("${onespider.store.autofind.logs}")
    private String autoFindLogsUrl;

    @Value("${onespider.store.comic.dmzj}")
    private String comicDmzjUrl;

    @Value("${onespider.store.comic.mmmh}")
    private String comicMmmhUrl;

    @Value("${onespider.store.comic.kanmanhua}")
    private String comicKanmanhuaUrl;

    @Value("${onespider.store.maoyan.box}")
    private String maoyanBoxUrl;

    @Value("${onespider.store.fiction_meta}")
    private String fictionMetaUrl;

    @Value("${onespider.store.fiction_comment}")
    private String fictionCommentUrl;

    @Value("${onespider.store.fiction_income}")
    private String fictionIncomeUrl;

    @Value("${onespider.store.fiction_comment_text}")
    private String fictionCommentTextUrl;

    @Value("${onespider.store.wechat_sougou_logs}")
    private String wechatSougouLogsUrl;

    @Value("${onespider.store.cbooo.boxOffice}")
    private String chinaBoxOfficeUrl;

    @Value("${onespider.store.bilibili.search.score}")
    private String bilibiliVideoScoreUrl;

    @Value("${onespider.store.maoyan.actor}")
    private String maoyanActorUrl;

    @Value("${onespider.store.douyin.author.url}")
    private String douyinAuthorStoreUrl;

    @Value("${onespider.store.douyin.music_billboard.url}")
    private String douyinMusicBillboardStoreUrl;

    @Value("${onespider.store.douyin.challenge.url}")
    private String douyinChallengeStoreUrl;

    @Value("${onespider.store.douyin.device.url}")
    private String douyinDeviceStoreUrl;

    @Value("${onespider.store.douyin.music.url}")
    private String douyinMusicStoreUrl;

    @Value("${onespider.store.douyin.hot_search.url}")
    private String douyinHotSearchStoreUrl;

    @Value("${onespider.store.douyin.video.url}")
    private String douyinVideoStoreUrl;

    @Value("${onespider.store.douyin.video_challenge.url}")
    private String douyinVideoChallengeStoreUrl;

    @Value("${onespider.store.douyin.video_digg.url}")
    private String douyinVideoDiggStoreUrl;

    @Value("${onespider.store.douyin.video_statistic.url}")
    private String douyinVideoStatisticStoreUrl;

    @Value("${onespider.store.douyin.video_music.url}")
    private String douyinVideoMusicStoreUrl;

    @Value("${onespider.store.weiboFeedKeywordTagsBox.url}")
    private String weiboFeedKeywordTagsBoxUrl;

    @Value("${onespider.store.show_popular_logs.url}")
    private String ShowPopularLogsUrl;

    @Value("${onespider.store.comic.bodong}")
    private String comicBodongUrl;

    @Value("${onespider.store.weibo_index_hour_log}")
    private String weiboIndexHourLogUrl;

    @Value("${onespider.store.comic_comment_text.insert_comment_text}")
    private String comicCommentTextUrl;

    @Value("${onespider.store.fiction_attribute.click}")
    private String fictionAttributeClick;

    @Value("${onespider.store.fiction_attribute.favorite}")
    private String fictionAttributeFavorite;

    @Value("${onespider.store.fiction_attribute.recommend}")
    private String fictionAttributeRecommend;

    @Value("${onespider.store.fiction_attribute.rate}")
    private String fictionAttributeRate;

    @Value("${onespider.store.fiction_attribute.chapter}")
    private String fictionAttributeChapter;

    @Value("${onespider.store.comic.dmmh}")
    private String comicDmmhUrl;

    @Value("${onespider.store.comic.weibo}")
    private String comicWeiboUrl;

    @Value("${onespider.store.comic.dmmh.comment}")
    private String comicDmmhCommentUrl;

    @Value("${onespider.store.comic_episode_info}")
    private String comicEpisodeInfoUrl;


    @Value("${onespider.store.stock_bulletin}")
    private String stockBulletinUrl;

    @Value("${onespider.store.comic.best_selling_rank}")
    private String comicBestSellingRankUrl;


    @Value("${onespider.store.comic.r_comic_author}")
    private String rComicAuthorUrl;


    @Value("${onespider.store.audio.meta}")
    private String audioMetaUrl;

    @Value("${onespider.store.audio.volume}")
    private String audioVolumeUrl;

    @Value("${onespider.store.audio.playcount}")
    private String audioPlayCountUrl;
    
    @Value("${onespider.store.banner}")
    private String bannerSaveUrl;

    @Value("${onespider.store.sogou_wechat.cookie.url}")
    private String wechatCookieUrl;


    @Value("${onespider.store.comic.original.billboard}")
    private String comicOriginalBillboardUrl;


    @Value("${onespider.store.fiction_original_billboard}")
    private String fictionOriginalBillboardUrl;

    @Value("${onespider.store.comic.banner}")
    private String comicBannerUrl;


    @Value("${onespider.store.jdGoods.url}")
    private String jdGoodsUrl;


    @Value("${onespider.store.comic.bilibili}")
    private String comicBilibiliUrl;

    //==================================================
    @Value("${onespider.store.children_book}")
    private String childrenBookUrl;

    @Value("${onespider.store.children_book_billboard}")
    private String childrenBookBillboardUrl;

    @Value("${onespider.store.children_book_comment}")
    private String childrenBookCommentUrl;

    @Value("${onespider.store.dangdang_category}")
    private String dangdangCategoryUrl;

    @Value("${onespider.store.dangdang_category_relation}")
    private String dangdangCategoryRelationUrl;

    @Value("${onespider.store.douban_book}")
    private String doubanBookUrl;

    @Value("${onespider.store.jianshu_book_logs}")
    private String jianshuBookLogsUrl;


    @Value("${onespider.store.children_book_weibo}")
    private String childrenBookWeiboUrl;


    @Value("${onespider.store.wechat_book_logs}")
    private String wechatBookLogsUrl;

    @Value("${onespider.store.children_book_comment_text}")
    private String childrenBookCommentTextUrl;

    @Value("${onespider.store.cartoon_bulletin}")
    private String cartoonBulletinUrl;




    @Bean
    public Pipeline childrenBookCommentTextPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ChildrenBookCommentText.class.getSimpleName(),
                childrenBookCommentTextUrl);
        return pipeline;
    }



    @Bean
    public Pipeline cartoonBulletinPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, CartoonBulletin.class.getSimpleName(),
                cartoonBulletinUrl);
        return pipeline;
    }


    @Bean
    public Pipeline wechatBookLogsPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, WechatBookLogs.class.getSimpleName(),
                wechatBookLogsUrl);
        return pipeline;
    }

    @Bean
    public Pipeline childrenBookWeiboPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ChildrenBookWeibo.class.getSimpleName(),
                childrenBookWeiboUrl);
        return pipeline;
    }


    @Bean
    public Pipeline jianshuBookLogsPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, JianshuBookLogs.class.getSimpleName(),
                jianshuBookLogsUrl);
        return pipeline;
    }

    @Bean
    public Pipeline doubanBookPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, DoubanBook.class.getSimpleName(),
                doubanBookUrl);
        return pipeline;
    }

    @Bean
    public Pipeline childrenBookPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ChildrenBook.class.getSimpleName(),
                childrenBookUrl);
        return pipeline;
    }

    @Bean
    public Pipeline childrenBookBillboardPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ChildrenBookBillboard.class.getSimpleName(),
                childrenBookBillboardUrl);
        return pipeline;
    }
    @Bean
    public Pipeline childrenBookCommentPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ChildrenBookComment.class.getSimpleName(),
                childrenBookCommentUrl);
        return pipeline;
    }
    @Bean
    public Pipeline dangdangCategoryPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, DangdangCategory.class.getSimpleName(),
                dangdangCategoryUrl);
        return pipeline;
    }
    @Bean
    public Pipeline dangdangCategoryRelationPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, DangdangCategoryRelation.class.getSimpleName(),
                dangdangCategoryRelationUrl);
        return pipeline;
    }
    //==================================================
    @Bean
    public Pipeline comicBilibiliPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ComicBilibili.class.getSimpleName(),
                comicBilibiliUrl);
        return pipeline;
    }


    @Bean
    public Pipeline jdGoodsPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, JdGoods.class.getSimpleName(),
                jdGoodsUrl);
        return pipeline;
    }


    @Bean
    public Pipeline comicBannerPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicBanner.class.getSimpleName(),
                comicBannerUrl);
        return pipeline;
    }

    @Bean
    public Pipeline fictionOriginalBillboardPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionOriginalBillboard.class.getSimpleName(),
                fictionOriginalBillboardUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicOriginalBillboardPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicOriginalBillboard.class.getSimpleName(),
                comicOriginalBillboardUrl);
        return pipeline;
    }

    @Bean
    public Pipeline rComicAuthorPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicAuthorRelation.class.getSimpleName(),
                rComicAuthorUrl);
        return pipeline;
    }


    @Bean
    public Pipeline comicBestSellingRankPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicBestSellingRank.class.getSimpleName(),
                comicBestSellingRankUrl);
        return pipeline;
    }


    @Bean
    public Pipeline stockBulletinPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, StockBulletin.class.getSimpleName(),
                stockBulletinUrl);
        return pipeline;
    }


    @Bean
    public Pipeline comicEpisodeInfoPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicEpisodeInfo.class.getSimpleName(),
                comicEpisodeInfoUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicDmmhCommentPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicDmmhComment.class.getSimpleName(),
                comicDmmhCommentUrl);
        return pipeline;
    }


    @Bean
    public Pipeline comicWeiboPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ComicWeibo.class.getSimpleName(),
                comicWeiboUrl);
        return pipeline;
    }


    @Bean
    public Pipeline comicDmmhPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicDmmh.class.getSimpleName(),
                comicDmmhUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicCommentTextPipeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, ComicCommentText.class.getSimpleName(),
                comicCommentTextUrl);
        return pipeline;
    }


    @Bean
    public Pipeline weiboIndexHourLogPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, WeiboIndexHourLog.class,
                weiboIndexHourLogUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicBodongPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicBodong.class,
                comicBodongUrl);
        return pipeline;
    }

    @Bean
    public Pipeline showPopularLogsUrlPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ShowPopularLogs.class,
                ShowPopularLogsUrl);
        return pipeline;
    }

    @Bean
    public Pipeline weiboFeedKeywordTagsBoxPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, WeiboFeedKeywordTag.class,
                weiboFeedKeywordTagsBoxUrl);
        return pipeline;
    }

    @Bean
    public Pipeline bilibiliVideoScorePopeline(RestTemplate simleHttp) {
        Pipeline pipeline = new StorePipeline(simleHttp, BilibiliVideoScore.class, bilibiliVideoScoreUrl);
        return pipeline;
    }

    @Bean
    public Pipeline chinaBoxOfficeLogsPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, BoxOfficeLogs.class,
                chinaBoxOfficeUrl);
        return pipeline;

    }

    @Bean
    public Pipeline wechatSougouLogsPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, WechatSougouLog.class,
                wechatSougouLogsUrl);
        return pipeline;
    }

    @Bean
    public Pipeline fictionMetaPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, Fiction.class,
                fictionMetaUrl);
        return pipeline;
    }

    @Bean
    public Pipeline fictionCommentPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionCommentLogs.class,
                fictionCommentUrl);
        return pipeline;
    }

    @Bean
    public Pipeline fictionCommentTextPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new CollectionStorePipeline(simpleHttp, FictionCommentText.class,
                fictionCommentTextUrl);
        return pipeline;
    }

    @Bean
    public Pipeline fictionIncomePipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionIncomeLogs.class,
                fictionIncomeUrl);
        return pipeline;
    }

    @Bean
    public Pipeline maoyanBoxPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, MaoyanBox.class,
                maoyanBoxUrl);
        return pipeline;
    }


    @Bean
    public Pipeline adLinkedVideoInfosPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, AdLinkedVideoInfos.class,
                adLinkedVideoStoreUrl);
        return pipeline;
    }

    @Bean
    public Pipeline adLogPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, AdLogs.class,
                adLogStoreUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicKanmanhuaPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicKanmanhua.class,
                comicKanmanhuaUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicMmmhPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicMmmh.class,
                comicMmmhUrl);
        return pipeline;
    }

    @Bean
    public Pipeline comicDmzjPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, ComicDmzj.class,
                comicDmzjUrl);
        return pipeline;
    }


    @Bean
    public Pipeline autoFindLogsPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new CollectionStorePipeline(simpleHttp, AutoFindLogs.class,
                autoFindLogsUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline comic163Pipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Comic163.class,
                comic163Url);
        return pipeline;
    }

    @Bean
    public StorePipeline comicPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Comic.class,
                comicUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline comicTengxunPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ComicTengxun.class,
                comicTengxunUrl);
        return pipeline;
    }


    @Bean
    public StorePipeline comicZymkPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ComicZymk.class,
                comicZymkUrl);
        return pipeline;
    }


    @Bean
    public StorePipeline comicU17Pipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ComicU17.class,
                comicU17Url);
        return pipeline;
    }

    @Bean
    public StorePipeline comicKuaikanStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ComicKuaiKan.class,
                ComicKuaikanUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline bilibiliSearchCountStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BilibiliVideoCount.class,
                bilibiliSearchCountUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline bilibiliFansCountStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BilibiliFansCount.class,
                bilibiliFansCountUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline bilibiliSearchStowStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BilibiliVideoStow.class,
                bilibiliSearchStowUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline bilibiliSearchDmStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BilibiliVideoDm.class,
                bilibiliSearchDmUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline bilibiliSearchClickStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BilibiliVideoClick.class,
                bilibiliSearchClickUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline customer360LogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Customer360Logs.class,
                customer360LogsUrl);
        return pipeline;
    }


    @Bean
    public StorePipeline vipEpisodeStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, VipEpisode.class,
                vipEpisodeUrl);
        return pipeline;
    }


    @Bean
    public StorePipeline showActorsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ShowActors.class,
                showActorsUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline sougouWechatSearchTextStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, SougouWechatSearchText.class,
                sougouWechatSearchTextUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline sougouWechatArticleTextStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, SougouWechatArticleText.class,
                sougouWechatArticleTextUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboTextStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, WeiboText.class,
                weiboTextStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboHotSearchTextStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, WeiboHotSearchText.class,
                weiboHotSearchTextStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline screenshotLogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ScreenshotLogs.class,
                screenshotLogsUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline newsToutiaoLogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ToutiaoNewLogs.class,
                newsToutiaoLogsUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline media360LogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Media360Logs.class,
                media360LogsUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline index360LogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Index360Logs.class,
                index360LogsUrl);
        return pipeline;
    }


    @Bean
    public StorePipeline tiebaArticleLogsStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, TiebaArticleLogs.class,
                tiebaArticleLogsStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboAttrStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, WeiboAttribute.class,
                weiboAttrStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboProvinceCompareStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, WeiboProvinceCompare.class,
                weiboProvinceCompareStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboTagLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, WeiboTagLog.class,
                weiboTagLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline douBanShowPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, DouBanShow.class,
                douBanShowUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline douBanActorPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, DouBanActor.class,
                douBanActorUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline showLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                ShowLog.class, showLogStoreUrl);

        return pipeline;
    }

    @Bean
    public StorePipeline doubanLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                DoubanLog.class, doubanLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline tiebaLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                TiebaLog.class, tiebaLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline baiduNewsLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                BaiduNewsLog.class, baiduNewsLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline baiduVideoLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                BaiduVideoLog.class, baiduVideoLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline news360LogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                News360Log.class, news360LogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline weiboOfficialLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp,
                WeiboOfficialLog.class, weiboOfficialLogStoreUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline showStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Show.class, showStoreUrl);

        return pipeline;
    }

    @Bean
    public StorePipeline commentLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, CommentLog.class, commentLogStoreUrl);

        return pipeline;
    }

    @Bean
    public StorePipeline barrageLogStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BarrageLog.class, barrageLogStoreUrl);

        return pipeline;
    }

    @Bean
    public StorePipeline exponentLogPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, ExponentLog.class, exponentLogStoreUrl);

        return pipeline;
    }

    @Bean
    public Pipeline barrageTextsStorePipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new CollectionStorePipeline(simpleHttp, BarrageText.class, barrageTextsStoreUrl);

        return pipeline;
    }

    @Bean
    public Pipeline commentTextsStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, CommentText.class, commentTextsStoreUrl);
    }

    @Bean
    public Pipeline proxyStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, Proxy.class, proxyStoreUrl);
    }

    @Bean
    public Pipeline newsArticleLogStorePipline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, NewsArticleLog.class, newsArticleStoreUrl);

        return pipeline;
    }
    @Bean
    public Pipeline newsArticleMessageLogStorePipline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, NewsArticleMessageLogs.class, newsArticleMessageStoreUrl);

        return pipeline;
    }
    @Bean
    public StorePipeline doubanCommentPipeline(RestTemplate simpleHttp) {
        return new StorePipeline(simpleHttp, DoubanCommentsText.class, doubanCommentStoreUrl);
    }

    @Bean
    public StorePipeline maoyanActorStorePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, MaoyanActor.class,
                maoyanActorUrl);
        return pipeline;
    }

    @Bean
    public Pipeline douyinAuthorStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinAuthor.class, douyinAuthorStoreUrl);
    }

    @Bean
    public Pipeline douyinMusicBillboardStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinMusicBillboard.class, douyinMusicBillboardStoreUrl);
    }

    @Bean
    public Pipeline douyinChallengeStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinChallenge.class, douyinChallengeStoreUrl);
    }

    @Bean
    public Pipeline douyinDeviceStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinDevice.class, douyinDeviceStoreUrl);
    }

    @Bean
    public Pipeline douyinMusicStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinMusic.class, douyinMusicStoreUrl);
    }

    @Bean
    public Pipeline douyinHotSearchStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinHotSearch.class, douyinHotSearchStoreUrl);
    }

    @Bean
    public Pipeline douyinVideoStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinVideo.class, douyinVideoStoreUrl);
    }

    @Bean
    public Pipeline douyinVideoDiggStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinVideoDigg.class, douyinVideoDiggStoreUrl);
    }

    @Bean
    public Pipeline douyinVideoStatisticStorePipeline(RestTemplate simpleHttp) {
        return new CollectionStorePipeline(simpleHttp, DouyinVideoStatistic.class, douyinVideoStatisticStoreUrl);
    }

    @Bean
    public SpiderSettingLoader spiderSettingLoader(RestTemplate restTemplate) {
        return new RemoteSpiderSettingLoader(restTemplate, spiderSettingUrl);
    }

    @Bean
    public CookieStringStoreLoader cookieStoreLoader(RestTemplate restTemplate) {
        return new CookieStringStoreLoader(restTemplate, cookieStringStoreUrl);
    }

    @Bean
    public UserAgentStoreLoader userAgentStoreLoader(RestTemplate restTemplate) {
        return new UserAgentStoreLoader(restTemplate, userAgentStoreUrl);
    }

    @Bean
    public ProxyStoreLoader proxyStoreLoader(RestTemplate restTemplate) {
        return new ProxyStoreLoader(restTemplate, proxyStoreUrl);
    }

    @Bean
    public DouyinDeviceStoreLoader douyinDeviceStoreLoader(RestTemplate restTemplate) {
        return new DouyinDeviceStoreLoader(restTemplate, douyinDeviceStoreUrl);
    }

    @Bean
    public SogouWechatCookieStoreLoader sogouWechatCookieStoreLoader(RestTemplate restTemplate) {
        return new SogouWechatCookieStoreLoader(restTemplate, wechatCookieUrl);
    }

    @Bean
    public Pipeline fictionClickPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionPlatformClick.class.getSimpleName(),
                fictionAttributeClick);
        return pipeline;
    }

    @Bean
    public Pipeline fictionFavoritePipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionPlatformFavorite.class.getSimpleName(),
                fictionAttributeFavorite);
        return pipeline;
    }

    @Bean
    public Pipeline fictionRecommendPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionPlatformRecommend.class.getSimpleName(),
                fictionAttributeRecommend);
        return pipeline;
    }

    @Bean
    public Pipeline fictionRatePipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionPlatformRate.class.getSimpleName(),
                fictionAttributeRate);
        return pipeline;
    }

    @Bean
    public Pipeline fictionChapterPipeline(RestTemplate simpleHttp) {
        Pipeline pipeline = new StorePipeline(simpleHttp, FictionChapters.class.getSimpleName(),
                fictionAttributeChapter);
        return pipeline;
    }

    @Bean
    public StorePipeline audioMetaPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, Audio.class,
                audioMetaUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline audioVolumePipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, AudioVolumeLog.class,
                audioVolumeUrl);
        return pipeline;
    }

    @Bean
    public StorePipeline audioPlayCountPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, AudioPlayCountLog.class,
                audioPlayCountUrl);
        return pipeline;
    }
    
    @Bean
    public StorePipeline bannerRecommendationPipeline(RestTemplate simpleHttp) {
        StorePipeline pipeline = new StorePipeline(simpleHttp, BannerRecommendation.class,
                bannerSaveUrl);
        return pipeline;
    }
}