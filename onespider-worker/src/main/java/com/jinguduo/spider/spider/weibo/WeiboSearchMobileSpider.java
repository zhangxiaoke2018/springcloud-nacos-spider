package com.jinguduo.spider.spider.weibo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.cluster.downloader.listener.ResetCookieDownloaderListener;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.bookProject.ChildrenBookWeibo;
import com.jinguduo.spider.data.table.WeiboFeedKeywordTag;
import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lc on 2018/8/16
 */
@Slf4j
@Worker
@SuppressWarnings("all")
public class WeiboSearchMobileSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("m.weibo.cn")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36")
            .addSpiderListener(new UserAgentSpiderListener())
            //.setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addDownloaderListener(new ResetCookieDownloaderListener()
                    .setProbability(0.1))
            .build();

    private PageRule rules = PageRule.build()
            .add(".", page -> search(page))
            .add("/getIndex", page -> processTag(page));

//    final static String WEIBO_TAG_URL = "http://huati.weibo.com/k/%s";
    final static String WEIBO_TAG_NEW_URL = "https://m.weibo.cn/api/container/getIndex?jumpfrom=weibocom&display=0&retcode=6102&containerid=%s#%s";

    private final Pattern TAG_PATTERN = Pattern.compile("#([^\\#|.]+)#");

    private HashFunction hash = Hashing.md5();


    /**
     * 微博手机版搜索页
     */
    private void search(Page page) throws Exception {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String code = job.getCode();
        String url = job.getUrl();

        String decodeUrl = URLDecoder.decode(url, "utf-8");
        String queryName = StringUtils.substring(decodeUrl, StringUtils.indexOf(decodeUrl, "&q=") + 3, StringUtils.contains(decodeUrl, "&page") ? StringUtils.indexOf(decodeUrl, "&page") : decodeUrl.length());
        if (queryName.contains("#")) {
            queryName = queryName.substring(0, queryName.indexOf("#"));
        }
        String containsStr = URLDecoder.decode(queryName, "utf-8").trim();
        containsStr = containsStr.substring(0, Math.min(2, containsStr.length()));
        if (StringUtils.isEmpty(queryName)) {
            log.error("m.weibo.cn get query name error ,this url is ->{}", url);
            return;
        }

        JSONObject jsonObject = JSONObject.parseObject(page.getJson().get());
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray cards = data.getJSONArray("cards");
        if (cards.isEmpty() || 0 == cards.size()) {
            return;
        }


        //童书搜索
        if (url.contains("children_book")) {
            this.processBook(page, cards, containsStr, code, queryName, job);
        } else {
            //剧集搜索
            this.processShows(page, cards, containsStr, code, queryName, job);
        }


        //不含有分页标志(page=)翻3页
        // TODO: 2020/2/17 翻页有问题
        //https://m.weibo.cn/api/container/getIndex?containerid=100103type=60&q=写给儿童的中国历史#children_book
        String titleType = StringUtils.substring(decodeUrl, StringUtils.indexOf(decodeUrl, "type=") + 5, StringUtils.indexOf(decodeUrl, "&q="));
        if (!StringUtils.contains(decodeUrl, "&page=")) {
            String suff = "";
            if (decodeUrl.contains("children_book")) {
                decodeUrl = decodeUrl.replaceAll("#children_book", "");
                suff = "#children_book";
                url = url.replaceAll("#children_book", "");
            }
            for (int i = 1; i <= 3; i++) {
                String pageUrl = url + "&page=" + i + suff;
                Job pageJob = new Job(pageUrl);
                DbEntityHelper.derive(job, pageJob);
                pageJob.setCode(code);
                pageJob.setKind(JobKind.Once);
                putModel(page, pageJob);
            }
        }

    }


    //处理童书
    private void processBook(Page page, JSONArray cards, String containsStr, String code, String queryName, Job job) {
        for (Object cardInit : cards) {
            JSONObject card = (JSONObject) cardInit;
            //show_type 表示是否显示 0 -》不显示； 1-》显示
            Integer show_type = card.getInteger("show_type");
            if (null == show_type || show_type.equals(0)) continue;

            JSONArray groups = card.getJSONArray("card_group");
            if (null == groups) {
                groups = cards;
            }
            for (Object group : groups) {
                JSONObject groupObject = (JSONObject) group;
                JSONObject mblog = groupObject.getJSONObject("mblog");
                JSONObject retweetedStatus = mblog.getJSONObject("retweeted_status");
                String mid = mblog.getString("mid");
                String postPlatform = mblog.getString("source");
                //需要根据此字段推测postTime
                String created_at = mblog.getString("created_at");
                String wtUrl = weiboId2Url(mid);

                JSONObject user = mblog.getJSONObject("user");
                String userId = user.getString("id");
                String nickName = this.unicode2String(user.getString("screen_name"));
                String touxiang = user.getString("profile_image_url");
                String userType = getWeiboUserType(user.getString("verified_type"), user.getString("verified_type_ext"));
                Integer isForward = null == retweetedStatus ? 0 : 1;

                String context = this.unicode2String(mblog.getString("text"));

                //需要created_at字段推测postTime
                Date postTime = getWeiboPostTimeByCreatedTimeStr(created_at);

                //转
                Integer zhuan = mblog.getInteger("reposts_count");
                Integer ping = mblog.getInteger("comments_count");
                Integer zan = mblog.getInteger("attitudes_count");

                ChildrenBookWeibo cbw = new ChildrenBookWeibo();
                cbw.setCode(code);
                cbw.setMid(mid);
                cbw.setPostTime(postTime);
                cbw.setZhuan(zhuan);
                cbw.setPing(ping);
                cbw.setZan(zan);
                cbw.setPlatformId(job.getPlatformId());
                cbw.setQueryName(queryName);
                putModel(page, cbw);
            }
        }
    }

    //处理剧
    private void processShows(Page page, JSONArray cards, String containsStr, String code, String queryName, Job job) {
        for (Object cardInit : cards) {
            JSONObject card = (JSONObject) cardInit;

            //show_type 表示是否显示 0 -》不显示； 1-》显示
            Integer show_type = card.getInteger("show_type");
            if (null == show_type || show_type.equals(0)) continue;


            JSONArray groups = card.getJSONArray("card_group");
            if (null == groups) {
                groups = cards;
            }
            for (Object group : groups) {
                try {
                    WeiboText wt = new WeiboText();
                    boolean isSave = true;
                    boolean originalSave = true;
                    JSONObject groupObject = (JSONObject) group;
                    JSONObject mblog = groupObject.getJSONObject("mblog");
                    JSONObject retweetedStatus = mblog.getJSONObject("retweeted_status");
                    String mid = mblog.getString("mid");
                    String postPlatform = mblog.getString("source");
                    //需要根据此字段推测postTime
                    String created_at = mblog.getString("created_at");
                    String wtUrl = weiboId2Url(mid);

                    JSONObject user = mblog.getJSONObject("user");
                    String userId = user.getString("id");
                    String nickName = this.unicode2String(user.getString("screen_name"));
                    String touxiang = user.getString("profile_image_url");
                    String userType = getWeiboUserType(user.getString("verified_type"), user.getString("verified_type_ext"));
                    Integer isForward = null == retweetedStatus ? 0 : 1;

                    String context = this.unicode2String(mblog.getString("text"));
                    //如果原创内容不包含关键字，跳过
                    isSave = context.contains(containsStr);

                    //需要created_at字段推测postTime
                    Date postTime = getWeiboPostTimeByCreatedTimeStr(created_at);

                    //转
                    Integer zhuan = mblog.getInteger("reposts_count");
                    Integer ping = mblog.getInteger("comments_count");
                    Integer zan = mblog.getInteger("attitudes_count");

                    wt.setMid(mid);
                    wt.setCode(code);
                    wt.setUrl(wtUrl);
                    wt.setUserId(userId);
                    wt.setUserType(userType);
                    wt.setIsForward(isForward);
                    wt.setNickName(nickName);
                    wt.setTouxiang(touxiang);
                    wt.setContent(delHTMLTag(context));
                    wt.setPostTime(postTime);
                    wt.setPostPlatform(postPlatform);
                    wt.setZhuan(zhuan);
                    wt.setPing(ping);
                    wt.setZan(zan);

                    if (StringUtils.isNotBlank(wt.getContent())) {
                        Matcher m = TAG_PATTERN.matcher(wt.getContent());
                        while (m.find()) {
                            String tagNameMatch = m.group();
                            String tag = StringUtils.replaceAll(tagNameMatch, "#", "");
                            //如果标签包含剧名、并且 长度小于等于 16 查询标签
                            if (tag.contains(URLDecoder.decode(queryName, "utf-8")) && tag.length() <= 16) {
                                String tagEncode = URLEncoder.encode(tag, "utf-8");
                                String tagCode = hash.newHasher().putString(tag, Charset.forName("UTF-8")).hash().toString();
                                String s = 100808 + tagCode;
                                String tagUrl = String.format(WEIBO_TAG_NEW_URL, s, tagEncode);
                                Job tagJob = new Job(tagUrl);
                                DbEntityHelper.derive(job, tagJob);

                                tagJob.setCode(tagCode);
                                tagJob.setParentCode(code);
                                putModel(page, tagJob);

                                WeiboFeedKeywordTag tagPojo = new WeiboFeedKeywordTag();
                                tagPojo.setCode(code);
                                tagPojo.setKeyword(URLDecoder.decode(queryName, "utf-8"));
                                tagPojo.setTag(tag);
                                tagPojo.setDay(wt.getPostTime());
                                putModel(page, tagPojo);
                            }
                        }
                    }
                    //转发内容
                    if (null != retweetedStatus) {
                        String originalMid = retweetedStatus.getString("mid");
                        String originalUrl = this.weiboId2Url(originalMid);
                        JSONObject orginalUser = retweetedStatus.getJSONObject("user");
                        String originalUserId = orginalUser.getString("id");

                        String originalUserType = getWeiboUserType(orginalUser.getString("verified_type"), orginalUser.getString("verified_type_ext"));
                        String originalNickName = this.unicode2String(orginalUser.getString("screen_name"));
                        String originalContent = this.unicode2String(retweetedStatus.getString("text"));

                        //如果转发内容不包含，则跳过
                        originalSave = originalContent.contains(containsStr);

                        //需要根据此字段推测postTime
                        String originalCreatedTime = retweetedStatus.getString("created_at");
                        //需要created_at字段推测postTime
                        Date originalPostTime = getWeiboPostTimeByCreatedTimeStr(originalCreatedTime);

                        String originalPostPlatform = retweetedStatus.getString("source");
                        //转
                        Integer originalZhuan = retweetedStatus.getInteger("reposts_count");
                        Integer originalPing = retweetedStatus.getInteger("comments_count");
                        Integer originalZan = retweetedStatus.getInteger("attitudes_count");

                        wt.setOriginalMid(originalMid);
                        wt.setOriginalUrl(originalUrl);
                        wt.setOriginalUserId(originalUserId);
                        wt.setOriginalUserType(originalUserType);
                        wt.setOriginalNickName(originalNickName);
                        wt.setOriginalContent(delHTMLTag(originalContent));
                        wt.setOriginalPostTime(DateFormatUtils.format(originalPostTime, "yyyy-MM-dd HH:mm:ss"));
                        wt.setOriginalPostPlatform(originalPostPlatform);
                        wt.setOriginalZhuan(originalZhuan);
                        wt.setOriginalPing(originalPing);
                        wt.setOriginalZan(originalZan);
                    }
                    if (isSave || originalSave) {
                        putModel(page, wt);
                    }
                } catch (Exception e) {
                    log.info("m.weibo.cn error ->{} , url is ->{}", containsStr, job);
                }
            }
        }
    }

    /**
     * 微博超话手机版
     */
    private void processTag(Page page) throws UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String decode = URLDecoder.decode(job.getUrl(), "UTF-8");

        String code = job.getCode();

        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)){
            log.error("weiboTag body is null");
            return;
        }
        JSONObject jsonObject = null;
        long readCount = -1L;
        int feedCount = -1;
        int followCount = -1;

        try {
            jsonObject = JSONObject.parseObject(rawText);
            JSONObject datas = JSONObject.parseObject(jsonObject.getString("data"));
            JSONObject pageInfo = JSONObject.parseObject(datas.getString("pageInfo"));
            String data = pageInfo.getString("desc_more").split(",")[0].trim();
            String ref = data.replace("[", "").replace("\"","").replace("　",",");
            String[] split = ref.split(",");
            for (String s : split) {
                if (s.contains("阅读")){
                    readCount = (long) numberFormat(s.substring(2));
                }else if (s.contains("帖子")){
                    feedCount = (int) numberFormat(s.substring(2));
                }else if (s.contains("粉丝")){
                    followCount = (int) numberFormat(s.substring(2));
                }
            }

            WeiboTagLog tag = new WeiboTagLog();
            String keyword = decode.substring(decode.lastIndexOf("#") + 1, decode.length());
            if (keyword.contains("&page")){
                keyword = keyword.substring(0,keyword.indexOf("&page"));
            }
            tag.setReadCount(readCount);
            tag.setFeedCount(feedCount);
            tag.setFollowCount(followCount);
            tag.setCode(code);
            tag.setParentCode(job.getParentCode());
            tag.setKeyword(keyword);
            putModel(page,tag);

        } catch (Exception e) {
            log.warn("weiboTag return errCode :[" + job.getCode());
        }
    }

    //删除微博内容中的html标签
    private static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }

    //根据返回的type和ext获取用户的等级
    private static String getWeiboUserType(String type, String ext) {
        String location;

        try {
            if (type.equals("-1")) {
                location = "普通用户";
            } else if (type.equals("220")) {
                location = "微博达人";
            } else if (type.equals("2") || type.equals("3")) {
                location = "蓝V认证";
            } else if (type.equals("0") && ext.equals("0")) {
                location = "橙V认证";
            } else if (type.equals("0") && ext.equals("1")) {
                location = "金V认证";
            } else {
                location = "普通用户";
            }
        } catch (Exception e) {
            location = "";
        }
        return location + "(移动端)";
    }

    //根据返回的created_time字符串，获得postTime
    private Date getWeiboPostTimeByCreatedTimeStr(String createdTimeStr) {
        Date postTime;
        if (StringUtils.isEmpty(createdTimeStr)) {
            return new Date();
        }
        createdTimeStr = createdTimeStr.replaceAll(" ", "");

        try {
            if (StringUtils.equals(createdTimeStr, "刚刚")) {
                postTime = DateUtils.addMinutes(new Date(), -1);
            } else if (StringUtils.contains(createdTimeStr, "分钟前")) {
                String timeStr = StringUtils.replace(createdTimeStr, "分钟前", "");
                postTime = DateUtils.addMinutes(new Date(), -Integer.valueOf(timeStr));

            } else if (StringUtils.contains(createdTimeStr, "小时前")) {
                String timeStr = StringUtils.replace(createdTimeStr, "小时前", "");
                postTime = DateUtils.addHours(new Date(), -Integer.valueOf(timeStr));

            } else if (StringUtils.contains(createdTimeStr, "昨天")) {
                String timeStr = StringUtils.replace(createdTimeStr, "昨天", "");
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.DATE, -1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                if (!StringUtils.isEmpty(timeStr)) {
                    String[] split = StringUtils.split(timeStr, ":");
                    String hour = split[0];
                    String minute = split[1];
                    cal.add(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
                    cal.add(Calendar.MINUTE, Integer.valueOf(minute));
                }
                postTime = cal.getTime();
            } else if (StringUtils.contains(createdTimeStr, "-")) {

                postTime = DateUtils.parseDate(Calendar.getInstance().get(Calendar.YEAR) + "-" + createdTimeStr, "yyyy-MM-dd", "yyyy-yyyy-MM-dd");
            } else {
                postTime = new Date();
            }
        } catch (Exception e) {
            postTime = new Date();
        }
        return postTime;

    }


    private String unicode2String(String unicode) {
        return StringEscapeUtils.unescapeJava(unicode);
    }

    private String weiboId2Url(String mid) {
        if (mid.length() < 16) {
            return "#" + mid;
        }
        long one = Long.valueOf(mid.substring(0, 2));
        long two = Long.valueOf(mid.substring(2, 9));
        long three = Long.valueOf(mid.substring(9, 16));

        String url = this.convert10to62(one) + this.convert10to62(two) + this.convert10to62(three);

        return url;
    }

    public String convert10to62(long number) {

        char[] charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        Long rest = number;
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder(0);
        while (rest != 0) {
            stack.add(charSet[new Long((rest - (rest / 62) * 62)).intValue()]);
            rest = rest / 62;
        }
        for (; !stack.isEmpty(); ) {
            result.append(stack.pop());
        }
        return result.toString();
    }

    public static Object numberFormat(String text) {
        if (text.contains("万")) {
            Double aDouble = Double.valueOf(text.replace("万", ""));
            Integer count = (int) (aDouble * 10000);
            return count;
        } else if (text.contains("千万")){
            Double aDouble = Double.valueOf(text.replace("千万", ""));
            long count = (long) (aDouble * 10000000);
            return count;
        } else if (text.contains("亿")) {
            Double aDouble = Double.valueOf(text.replace("亿", ""));
            long count = (long) (aDouble * 100000000);
            return count;
        } else {
            Integer c = Integer.valueOf(text);
            return c;
        }
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
