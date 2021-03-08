package com.jinguduo.spider.data.table;

@Deprecated
public class ShowCategoryCode {


    public enum IqiyiCategoryEnum {
        FILM("1", "电影"), ZONGYI("6,13", "综艺"), TELEPLAY("2", "网络剧"), ORIGINAL_TELEPLAY("27", "原创网络剧"),TALK_SHOW("31","脱口秀");
        private String code;
        private String des;

        private IqiyiCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum MgTvCategoryEnum {
        ZONGYI("6", "综艺"), TELEPLAY("2", "网络剧");
        private String code;
        private String des;

        private MgTvCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum YouKuCategoryEnum {
        ZONGYI("85", "综艺"), FILM("96", "网络电影"), TELEPLAY("97", "网络剧");
        private String code;
        private String des;

        private YouKuCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum TenXuCategoryEnum {
        ZONGYI_FUN("0", "综艺(娱乐)"), FILM("1", "网络电影"), TELEPLAY("2", "网络剧");
        private String code;
        private String des;

        private TenXuCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum SohuCategoryEnum {
        // ZONGYI_FUN("0","综艺"),
        FILM("2", "电视或电影"),
        // TELEPLAY("2","网络剧"),
        NET_FILM("100", "网络大电影（vip观看）");
        private String code;
        private String des;

        private SohuCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum PPTvCategoryEnum {
        FILM("1", "网络电影"), TELEPLAY("2", "网络剧"),VIP("70599","会员");
        private String code;
        private String des;

        private PPTvCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }

    public enum XCkankanCategoryEnum {
        ZONGYI("tv", "综艺"), TELEPLAY("teleplay", "网络剧"), FILM("movie", "电影"),VFILM("vmovie","网络电影"),ANIME("anime","动漫");
        private String code;
        private String des;

        private XCkankanCategoryEnum(String code, String des) {
            this.code = code;
            this.des = des;
        }

        public String getCode() {
            return code;
        }

        public String getDes() {
            return des;
        }
    }
}
