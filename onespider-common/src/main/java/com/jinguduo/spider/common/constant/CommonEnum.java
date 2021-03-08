package com.jinguduo.spider.common.constant;

public class CommonEnum {

    public enum ExpSource {
        WEIBO(1, "微博"),
        BAIDU(2, "百度");
        private Integer code;
        private String des;

        private ExpSource(Integer code, String des) {
            this.code = code;
            this.des = des;
        }

        public Integer getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum ExpType {
        ALL(1, "整体趋势"),
        PC(2, "PC趋势"),
        MOBILE(3, "移动趋势");
        private Integer code;
        private String des;

        private ExpType(Integer code, String des) {
            this.code = code;
            this.des = des;
        }

        public Integer getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum Platform {
        TENG_XUN(1, "腾讯"),
        I_QI_YI(2, "爱奇艺"),
        YOU_KU(3, "优酷"),
        TU_DOU(4, "土豆"),
        SO_HU(5, "搜狐"),
        LE_TV(6, "乐视"),
        MG_TV(7, "芒果"),
        XIANG_CHAO(8, "响巢看看"),
        FENG_XING(9, "风行"),
        FIVE_SIX(10, "56网"),
        PPTV(11, "pptv"),

        DOU_BAN_MV(12, "豆瓣电影"),
        BAI_DU_NEWS(13, "百度新闻"),
        _360_NEWS(14, "360新闻"),
        WEI_BO_SEARCH(15, "微博搜索"),
        SO_GOU_WECHAT(16, "搜狗微信"),
        WEI_BO(17, "官方微博"),
        TIE_BA(18, "官方贴吧"),
        BAI_DU_VIDEO_SEARCH(19, "百度视频搜索"),
        WEI_BO_DATA(20, "微指数"),
        TAO_MI(21, "淘米"),
        KU_MI(22, "酷米"),
        AC_FUN(23, "A站"),
        BILI_BILI(24, "B站"),
        BAI_DU_SEARCH(25, "百度知乎搜索"),


        JIN_JIANG(37, "晋江文学城"),
        QI_DIAN(38, "起点中文网"),
        ZONG_HENG(39, "纵横中文网"),
        MO_TIE(40, "磨铁文化"),
        _17_K(41, "17k"),
        XIANG_5(42, "香网"),
        HONG_SHU(43, "红薯中文网"),
        IREADER(49, "掌阅"),
        QQ_READER(50, "QQ阅读"),

        XMLY(54, "喜马拉雅"),
        LRTS(55, "懒人听书"),
        QTFM(56, "蜻蜓FM"),
        SQ(57, "书旗小说网"),
    	KS_STORY(60,"凯叔说故事");
        private Integer code;
        private String des;

        private Platform(Integer code, String des) {
            this.code = code;
            this.des = des;
        }

        public Integer getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }

        public String getPlatformCn(Integer code) {
            for (Platform p : Platform.values()) {
                if (p.code.compareTo(code) == 0) {
                    return p.des;
                }
            }
            return "无";
        }
    }

    public enum FictionChannel {
        BOY("男频", 2),
        GIRL("女频", 1),
        SYN("综合", 0);
        private String channel;
        private Integer code;

        private FictionChannel(String channel, Integer code) {
            this.channel = channel;
            this.code = code;
        }

        public String getChannel() {
            return channel;
        }

        public Integer getCode() {
            return code;
        }
    }

    public enum FictionIncome {
        JINJIANG_NUTRITION(1, "营养液"),
        QIDIAN_YUEPIAO(2, "月票"),
        ZONGHENG_YUEPIAO(3, "月票"),
        MOTIE_DASHANG(4, "磨铁"),
        _17K_HONGBAO(5, "红包"),
        XIANG_5_HONGBAO(6, "红包"),
        HONGSHU_DASHANG(7, "打赏");

        private String des;
        private Integer code;

        private FictionIncome(Integer code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getDes() {
            return des;
        }

        public Integer getCode() {
            return code;
        }
    }
    
    public enum BannerType{
		WEB_HOME_BANNER(311),
		WEB_CHANNEL_BANNER(321),
		MOBILE_HOME_BANNER(211),
		MOBILE_HOME_RECOMMEND(212),
		MOBILE_CHANNEL_BANNER(221),
		MOBILE_CHANNEL_RECOMMEND(222);
		private Integer code;
		 BannerType(Integer code){
			 this.code = code;
		 }
		 public Integer getCode(){
			 return this.code;
		 }
	}
}
