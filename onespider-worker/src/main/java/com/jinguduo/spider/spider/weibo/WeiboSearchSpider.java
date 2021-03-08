package com.jinguduo.spider.spider.weibo;

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
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.WeiboFeedKeywordTag;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.DocFlavor;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Worker
public class WeiboSearchSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("s.weibo.com")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36")
            .addSpiderListener(new UserAgentSpiderListener())
            .addHeader("Cookie", "SWB=usrmdinst_16; WBStorage=cd7f674a73035f73|undefined; _s_tentry=-; Apache=4808511058549.696.1501047997301; SINAGLOBAL=4808511058549.696.1501047997301; ULV=1501047997306:1:1:1:4808511058549.696.1501047997301:")
            .addDownloaderListener(new ResetCookieDownloaderListener()
                    .setProbability(0.1))
            .build();

    private PageRule rules = PageRule.build()
            .add(".", page -> search(page));

    final static String PREFIX = "\"pl_weibo_direct\"";

    final static String YANZHENGMA = "yzm_submit";

    final static String WEIBO_TAG_URL = "http://huati.weibo.com/k/%s";
    final static String WEIBO_TAG_NEW_URL = "https://m.weibo.cn/api/container/getIndex?jumpfrom=weibocom&display=0&retcode=6102&containerid=%s#%s";

    private final Pattern TAG_PATTERN = Pattern.compile("#([^\\#|.]+)#");

    private final Pattern HUATI_PATTERN = Pattern.compile("huati.weibo.com/");

    private HashFunction hash = Hashing.md5();

    private void search(Page page) throws AntiSpiderException, UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String code = job.getCode();

        List<WeiboText> weiboTexts = Lists.newArrayList();
        //http://s.weibo.com/weibo/%s&b=1&nodup=1  生成
        //http://s.weibo.com/weibo/%s&b=1&nodup=1#core 生成
        //http://s.weibo.com/weibo/%s?typeall=1&suball=1&timescope=custom:2017-11-10-11:2017-11-10-12&Refer=g
        String url = job.getUrl();
        int aliasIdx = url.indexOf("?");
        String queryName;
        if (aliasIdx > 0) {
            queryName = url.substring(url.indexOf("weibo/") + 6, aliasIdx);
        } else {
            queryName = url.substring(url.indexOf("weibo/") + 6, url.lastIndexOf("?"));
        }

        //如果包含# 或者 %2523 (#的转义)
        queryName = StringUtils.replaceAll(queryName, "#", "");
        queryName = StringUtils.replaceAll(queryName, "%2523", "");
        String containsStr = URLDecoder.decode(queryName, "utf-8").trim();
        Document document = page.getHtml().getDocument();
        Elements classes = document.getElementsByClass("card-wrap");
        for (int i = 0; i < classes.size(); i++) {
            boolean isSave = true;
            boolean originalSave = true;
            WeiboText weiboText = new WeiboText();
            weiboText.setCode(code);

            String mid = classes.get(i).getElementsByClass("card-wrap").get(0).attr("mid");
            if (StringUtils.isBlank(mid) || mid.length() < 16) {
                continue;
            }
            weiboText.setMid(mid);
            weiboText.setUrl(this.weiboId2Url(weiboText.getMid()));

            //微博正文区 + 用户信息 + 转发区
            Elements content = classes.get(i).getElementsByClass("content");
            //用户昵称
            String nickName = content.get(0).getElementsByTag("p").get(0).attr("nick-name");
            weiboText.setNickName(nickName);
            //用户头像
            Element acator = classes.get(i).getElementsByClass("avator").get(0).getElementsByTag("a").get(0).getElementsByTag("img").get(0);
            String touxiang = acator.attr("src");
            weiboText.setTouxiang(touxiang);
            //用户类型
            Elements info = classes.get(i).getElementsByClass("info").get(0).getElementsByTag("div").get(2).getElementsByTag("a");
            if (info.size() == 1) {
                weiboText.setLocation("普通用户");
            } else if (info.size() > 1) {
                Element leixingClass = info.get(1);
                String a = leixingClass.getElementsByTag("i").get(0).attr("class");
                weiboText.setUserType(this.getUserType(a));
            }
            //微博正文
            Elements contentText = content.get(0).getElementsByTag("p");
            List<Element> lists = Lists.newArrayList();
            for (Element e : contentText) {
                Elements yuan = e.getElementsByAttributeValue("nick-name", nickName);
                if (yuan != null) {
                    for (Element s : yuan) {
                        lists.add(s);
                    }
                }
            }
            String textFull = null;
            String text = null;
            for (Element e : lists) {
                String a = e.attr("node-type");
                if (a.equals("feed_list_content_full")) {
                    textFull = this.delHTMLTag(e.toString());
                } else if (a.equals("feed_list_content")) {
                    text = this.delHTMLTag(e.toString());
                }
                if (textFull == null || textFull.equals("")) {
                    isSave = false;
                } else {
                    weiboText.setContent(textFull.trim());
                    System.out.println(textFull.trim());
                    isSave = textFull.trim().contains(containsStr);
                    break;
                }
                if (text == null || text.equals("")) {
                    isSave = false;
                } else {
                    weiboText.setContent(text.trim());
                    System.out.println(text.trim());
                    isSave = text.trim().contains(containsStr);
                }
            }
            //发送时间
            //如果有两个from 第一个为转发微博时间 第二个为此微博时间
            Elements from = classes.get(i).getElementsByClass("from");
            if (from.size() == 2) {
                Element postTime = from.get(1).getElementsByTag("a").get(0);
                Pattern p = Pattern.compile("<a[^>]*>([^<]*)</a>");
                Matcher m = p.matcher(postTime.toString());
                if (m.find()) {
                    String dateString = m.group(1);
                    weiboText.setPostTime(this.getWeiboPostTimeByCreatedTimeStr(dateString));
                }
            } else {
                Element postTime = from.get(0).getElementsByTag("a").get(0);
                Pattern p = Pattern.compile("<a[^>]*>([^<]*)</a>");
                Matcher m = p.matcher(postTime.toString());
                if (m.find()) {
                    String dateString = m.group(1);
                    weiboText.setPostTime(this.getWeiboPostTimeByCreatedTimeStr(dateString));
                }
            }

            if (contentText != null) {
                Elements huatiElementAs = contentText.get(0).getElementsByTag("a");
                if (huatiElementAs.size() != 0) {
                    for (Element huatiElementA : huatiElementAs) {
//                            Element huatiElements = huatiElementAs.get(0);
                        String href = huatiElementA.attr("href");
                        Matcher m = HUATI_PATTERN.matcher(href);
                        if (m.find()) {
                            String huatiString = "";
                            if (href.contains("huati.weibo.com")) {
                                huatiString = huatiElementA.text().trim().substring(1);
                            }
                            if (huatiString.contains(URLDecoder.decode(containsStr, "utf-8")) && huatiString.length() <= 16) {
                                String tagEncode = URLEncoder.encode(huatiString, "utf-8");
                                String tagCode = hash.newHasher().putString(huatiString, Charset.forName("UTF-8")).hash().toString();
                                String s = 100808 + tagCode;
                                String tagUrl = String.format(WEIBO_TAG_NEW_URL, s,tagEncode);
                                Job tagJob = new Job(tagUrl);
                                DbEntityHelper.derive(job, tagJob);
                                tagJob.setCode(tagCode);
                                tagJob.setParentCode(code);
                                putModel(page, tagJob);

                                WeiboFeedKeywordTag tagPojo = new WeiboFeedKeywordTag();
                                tagPojo.setCode(code);
                                tagPojo.setKeyword(URLDecoder.decode(queryName, "utf-8"));
                                tagPojo.setTag(huatiString);
                                tagPojo.setDay(weiboText.getPostTime());
                                putModel(page, tagPojo);
                            }
                        }
                    }
                }
            }


            //微博发送平台
            Elements postPlatform = from.get(0).getElementsByTag("a");
            if (postPlatform.size() > 1) {
                weiboText.setPostPlatform(this.delHTMLTag(postPlatform.get(1).toString()));
            }
            //uid
            Elements zhuanpingzan = classes.get(i).getElementsByClass("card-act");
            Element ul = zhuanpingzan.get(0).getElementsByTag("ul").get(0);
            String uidSudaData = ul.getElementsByTag("li").get(1).getElementsByTag("a").get(0).attr("action-data");
            String uid = uidSudaData.substring(uidSudaData.indexOf("&uid=") + 5);
            weiboText.setOriginalUserId(uid);
            //zhuan 转，评数字在 li中的a标签中的文本中，赞在li的a标签的em标签中 防止特殊情况 一律按有em标签处理
            Elements zhuanAElement = ul.getElementsByTag("li").get(1).getElementsByTag("a");
            Elements zhuanElement = zhuanAElement.get(0).getElementsByTag("em");
            weiboText.setZhuan(this.getElementsNumber(zhuanElement, zhuanAElement));
            //ping
            Elements pingAElement = ul.getElementsByTag("li").get(2).getElementsByTag("a");
            Elements pingElement = zhuanAElement.get(0).getElementsByTag("em");
            weiboText.setPing(this.getElementsNumber(pingElement, pingAElement));
            //zan
            Elements zanAElement = ul.getElementsByTag("li").get(3).getElementsByTag("a");
            Elements zanElement = zanAElement.get(0).getElementsByTag("em");
            weiboText.setZan(this.getElementsNumber(zanElement, zanAElement));
//            //code
//            weiboText.setCode(code);
            //是否转发
            Elements cardComment = content.get(0).getElementsByClass("card-comment");
            if (cardComment.size() != 0) {
                weiboText.setIsForward(1);
                Element zhuanfaComment = cardComment.get(0);
                Elements feedListForwardContentEles = zhuanfaComment.getElementsByAttributeValue("node-type", "feed_list_forwardContent");
                Element feedListForwardContentEle = feedListForwardContentEles.get(0);
                //用户昵称
                String zhuanfaNickName = feedListForwardContentEle.getElementsByTag("a").get(0).attr("nick-name");
                weiboText.setOriginalNickName(zhuanfaNickName);
                //用户类型
                Element leixing = feedListForwardContentEle.getElementsByTag("a").get(1);
                if (leixing == null) {
                    weiboText.setOriginalUserType("普通用户");
                } else {
                    String originalUserTypeClass = leixing.getElementsByTag("i").get(0).attr("class");
                    weiboText.setOriginalUserType(this.getUserType(originalUserTypeClass));
                }
                //微博文本
                Elements zhuanfaContentText = feedListForwardContentEle.getElementsByTag("p");
                String zhuanfaTextFull = null;
                String zhuanfaText = null;
                for (Element e : zhuanfaContentText) {
                    String a = e.attr("node-type");
                    if (a.equals("feed_list_content_full")) {
                        zhuanfaTextFull = this.delHTMLTag(e.toString());
                    } else if (a.equals("feed_list_content")) {
                        zhuanfaText = this.delHTMLTag(e.toString());
                    }

                    if (zhuanfaTextFull == null || zhuanfaTextFull.equals("")) {
                        originalSave = false;
                    } else {
                        weiboText.setOriginalContent(zhuanfaTextFull.trim());
                        System.out.println(zhuanfaTextFull.trim());
                        originalSave = zhuanfaTextFull.trim().contains(containsStr);
                        break;
                    }
                    if (zhuanfaText == null || zhuanfaText.equals("")) {
                        originalSave = false;
                    } else {
                        weiboText.setOriginalContent(zhuanfaText.trim());
                        System.out.println(zhuanfaText.trim());
                        originalSave = zhuanfaText.trim().contains(containsStr);
                    }
                }
                //微博发送时间
                Elements func = zhuanfaComment.getElementsByClass("func");
                Element zhuanfaFuncElement = func.get(0).getElementsByClass("from").get(0);
                Element setOriginalPostTime = zhuanfaFuncElement.getElementsByTag("a").get(0);
                Pattern p2 = Pattern.compile("<a[^>]*>([^<]*)</a>");
                Matcher m2 = p2.matcher(setOriginalPostTime.toString());
                if (m2.find()) {
                    String dateString = m2.group(1);
                    Date originalPostTimeDate = this.getWeiboPostTimeByCreatedTimeStr(dateString);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    weiboText.setOriginalPostTime(sdf.format(originalPostTimeDate));
                }
                //微博发送平台
                Elements originalPostPlatform = zhuanfaFuncElement.getElementsByTag("a");
                if (originalPostPlatform.size() > 1) {
                    Element paltform = originalPostPlatform.get(1);
                    weiboText.setPostPlatform(this.delHTMLTag(paltform.toString()));
                }
                //转评赞 ul
                Element originalZhuanPingZanUl = func.get(0).getElementsByClass("act s-fr").get(0);
                //转发
                Elements originalZhuanA = originalZhuanPingZanUl.getElementsByTag("li").get(0).getElementsByTag("a");
                Elements originalZhuanEm = originalZhuanA.get(0).getElementsByTag("em");
                weiboText.setOriginalZhuan(this.getElementsNumber(originalZhuanEm, originalZhuanA));
                //评论
                Elements originalPingA = originalZhuanPingZanUl.getElementsByTag("li").get(1).getElementsByTag("a");
                Elements originalPingEm = originalPingA.get(0).getElementsByTag("em");
                weiboText.setOriginalPing(this.getElementsNumber(originalPingEm, originalPingA));
                //赞
                Elements originalZanA = originalZhuanPingZanUl.getElementsByTag("li").get(2).getElementsByTag("a");
                Elements originalZanEm = originalZanA.get(0).getElementsByTag("em");
                weiboText.setOriginalZan(this.getElementsNumber(originalZanEm, originalZanA));
                // rmid
                String rMid = originalZhuanPingZanUl.getElementsByTag("li").get(2).getElementsByTag("a").get(0).attr("action-data");
                if (rMid.startsWith("mid")) {
                    rMid = rMid.replace("mid=", "");
                }
                weiboText.setOriginalMid(rMid);
                weiboText.setOriginalUrl(this.weiboId2Url(rMid));
            } else {
                weiboText.setIsForward(0);
            }
            if (isSave || originalSave) {
                weiboTexts.add(weiboText);
            }
//                weiboTexts.add(weiboText);
            if (StringUtils.isNotBlank(weiboText.getContent())) {
                Matcher m = TAG_PATTERN.matcher(weiboText.getContent());
                if (m.find()) {
                    String tagNameMatch = m.group();
                    String tag = StringUtils.replaceAll(tagNameMatch, "#", "");
                    //如果标签包含剧名、并且 长度小于等于 16 查询标签
                    if (tag.contains(URLDecoder.decode(containsStr, "utf-8")) && tag.length() <= 16) {
                        String tagEncode = URLEncoder.encode(tag, "utf-8");
                        String tagCode = hash.newHasher().putString(tag, Charset.forName("UTF-8")).hash().toString();
                        String s = 100808 + tagCode;
                        String tagUrl = String.format(WEIBO_TAG_NEW_URL, s,tagEncode);
                        Job tagJob = new Job(tagUrl);
                        DbEntityHelper.derive(job, tagJob);
                        tagJob.setCode(tagCode);
                        tagJob.setParentCode(code);
                        putModel(page, tagJob);

                        WeiboFeedKeywordTag tagPojo = new WeiboFeedKeywordTag();
                        tagPojo.setCode(code);
                        tagPojo.setKeyword(URLDecoder.decode(queryName, "utf-8"));
                        tagPojo.setTag(tag);
                        tagPojo.setDay(weiboText.getPostTime());
                        putModel(page, tagPojo);
                    }
                }
            }
        }

        putModel(page, weiboTexts);
        //    log.info("s.weibo.com search :"+queryName+",count -->"+weiboTexts.size()+".");
        String oldUrl = job.getUrl();
        //如果是重复任务，则跳过
        int idx = oldUrl.indexOf("&b");
        if (idx <= 0) {
            return;
        }
        String newUrl = "http://s.weibo.com/weibo/%s?typeall=1&suball=1&timescope=custom:%s:%s&Refer=g";
        Calendar calendar = Calendar.getInstance();
        String startTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd-HH");
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String endTime = DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd-HH");
        newUrl = String.format(newUrl, queryName, startTime, endTime);
        Job newJob = new Job(newUrl);
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(code);
        putModel(page, newJob);
    }

    private Integer getElementsNumber(Elements e1, Elements e2) {
        Integer num = 0;
        if (e1.size() != 0) {
            Element pingEm = e1.get(0);
            String em = pingEm.text();
            if (em != null && !em.equals("")) {
                if (this.isInteger(em.trim())) {
                    num = Integer.valueOf(e1.text());
                }
            } else {
                num = 0;
            }
        } else {
            String pingElementText = e2.get(0).text();
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(pingElementText);
            String pingString = matcher.replaceAll("");
            if (pingString != null && !pingString.equals("")) {
                num = Integer.valueOf(pingString);
            } else {
                num = 0;
            }
        }
        return num;
    }


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

    private String getUserType(String typeClass) {
        String location = "";
        try {
            if (typeClass.trim().contains("icon-member")) {
                location = "微博会员";
            } else if (typeClass.trim().contains("icon-vip-g")) {
                location = "金V认证";
            } else if (typeClass.trim().contains("icon-vip-y")) {
                location = "橙V认证";
            } else if (typeClass.trim().contains("icon-daren")) {
                location = "微博达人";
            } else if (typeClass.trim().contains("icon-vip-b")) {
                location = "蓝V认证";
            }
        } catch (Exception e) {
            location = "";
        }
        return location;
    }

    private String weiboId2Url(String mid) {

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

    //删除微博内容中的html标签
    private String delHTMLTag(String htmlStr) {
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

    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
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
