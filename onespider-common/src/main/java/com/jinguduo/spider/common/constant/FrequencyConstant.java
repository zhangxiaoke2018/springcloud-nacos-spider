package com.jinguduo.spider.common.constant;

/**
 *  Seed和Job 频率（秒）
 */
public class FrequencyConstant {

    public final static int BLANK = 0;  // 用于未定义
    
	public final static int DEFAULT = 30 * 60;

	// 剧集基础信息
	public final static int NEW_SHOW_INFO = DEFAULT;
	public final static int GENERAL_SHOW_INFO = DEFAULT;
	public final static int GENERAL_SHOW_INFO_DEPTH_2 = DEFAULT;
	
	// 播放量爬虫
	public final static int NEW_PLAY_COUNT = DEFAULT;
	public final static int GENERAL_PLAY_COUNT = DEFAULT;
	public final static int GENERAL_PLAY_COUNT_DEPTH_2 = DEFAULT;

	/** 评论量爬虫频率 */
	public final static int COMMENT_BEFOR_PROCESS = DEFAULT;  //腾讯视频，step1:commentId ==> step2;commentCount
	public final static int COMMENT_COUNT = DEFAULT;  //评论数
	public final static int COMMENT_TEXT = 4 * 3600;  //评论文本

    /**弹幕爬虫*/
    public final static int DANMU_BEFOR_PROCESS= DEFAULT;
    public final static int DANMU_COUNT = DEFAULT;  //抓取评论数频率
    public final static int BARRAGE_TEXT = 3 * 3600;   // 弹幕文本任务

	/** 媒体指数频率 */
	public final static int MEDIA_DATA = 4*3600;
	public final static int EXPONENT_BASE = 12*3600;
	public final static int EXPONENT_PROCESS = 12*3600;

	// 艺人信息爬虫
	public final static int ACTOR_INFO = 4 * 3600;
	
	// Alias表任务
	public final static int ALIAS_DATA = 4 * 3600;
	
	// 广告爬虫频率
	public final static int ADVERTISEMENT = 3600;
	
	// 自动发现
	public final static int FINDING_SHOW = 3600;
	
	// 抖音任务
	//public final static int DOUYIN = 3600 * 2;
}
