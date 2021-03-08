package com.jinguduo.spider.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * copy worker->qiniuservice on 19-7-12.
 */
@Service("qiniuService")
@CommonsLog
public class QiniuService {

    public static final String QINIU_AK = "zp_FGY16eBCRZTuKtmWjksMl_Bg0u3OvkLv2VGF9";
    public static final String QINIU_SK = "ST7PT4N-Jqer8Q8ndbGqYirJd-_79jO3VWZpU4bh";
    public static final String QINIU_BN = "spider";
    public static final String QINIU_URL_COMIC = "http://static.spider.guduomedia.com/";

    //漫画 图片上传
    public String upload(byte[] bytes, String url) {
        if (bytes != null) {
            try {
                UploadManager uploadManager = new UploadManager();
                Response res = uploadManager.put(bytes, getKey(url), getToken());
                if (res.isOK()) {
                    return QINIU_URL_COMIC + getKey(url);
                }
                return null;
            } catch (QiniuException e) {
                log.error("upload image from url [" + url + "] catch a QiniuException:e.response is null! e:" + e.toString());
                if (e.response.statusCode == 614) {
                    //614 exist error code
                    return QINIU_URL_COMIC + getKey(url);
                }
            }
        }
        return null;
    }

    //截取图片地址
    private String getUrl(String headImage) {
        int i = headImage.indexOf("/comic/code/");
        String url = headImage.substring(0, i);
        return url;
    }

    //下载图片
    public byte[] download(String url1,Integer platfomId) {
        String url2 = getUrl(url1);
        InputStream is = null;
        try {
            URL url = new URL(url2);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (platfomId == 52) {
                con.setRequestProperty("Referer", "http://www.dongmanmanhua.cn");
            }
            // 输入流
            is = con.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);
            log.info("download photo to byte" + bytes);
            return bytes;
        } catch (Exception e) {
            log.error("download image from url [" + url2 + "] error " + e.getMessage());
            byte[] bytes = null;
            return bytes;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public String getToken() {
        Auth auth = Auth.create(QINIU_AK, QINIU_SK);

        String returnBody = "{\n" + "    \"hash\": $(etag),\n" + "    \"w\": $(imageInfo.width),\n"
                + "    \"h\": $(imageInfo.height),\n" + "    \"key\": $(key)\n" + "}";

        String token = auth.uploadToken(QINIU_BN, null, 3600, new StringMap().put("returnBody", returnBody));
        return token;
    }

    public String getKey(String url) {
        int i = url.indexOf("/comic/code/");
        String s = url.substring(i+1, url.length());
        return s;
    }

}
