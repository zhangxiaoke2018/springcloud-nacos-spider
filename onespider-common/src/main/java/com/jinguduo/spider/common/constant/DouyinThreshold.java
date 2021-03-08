package com.jinguduo.spider.common.constant;

/**
 * 抖音各种阈值
 */
public class DouyinThreshold {

	// magic number
	public final static int CHALLENGE_USER_COUNT = 10;  // 挑战参与用户数
	public final static int MUSIC_USER_COUNT = 10;  // 使用音乐的用户数
	
	public final static int VIDEO_DIGG_COUNT = 2 * 10000;  // 短视频获赞数
	public final static int USER_TOTAL_FAVORITED = 5 * 10000;  // 用户获赞总数
}
