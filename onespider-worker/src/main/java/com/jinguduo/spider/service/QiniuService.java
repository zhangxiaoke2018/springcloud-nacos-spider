package com.jinguduo.spider.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Created by zhangpeng on 16-4-26.
 */
@Service("qiniuService")
@CommonsLog
public class QiniuService {
    public static final String QINIU_AK = "zp_FGY16eBCRZTuKtmWjksMl_Bg0u3OvkLv2VGF9";
    public static final String QINIU_SK = "ST7PT4N-Jqer8Q8ndbGqYirJd-_79jO3VWZpU4bh";
    public static final String QINIU_BN = "spider";
    public static final String QINIU_URL = "https://static.spider.guduomedia.com/";

    public String upload(String picUrl) {
        final String key = getKey(picUrl);
        if (exists(key)) {
            return QINIU_URL + key;
        }
        return upload(download(picUrl), picUrl);
    }

    private boolean exists(String key) {
        BucketManager bucketManager = new BucketManager(Auth.create(QINIU_AK, QINIU_SK));
        try {
            FileInfo fileInfo = bucketManager.stat(QINIU_BN, key);
            return fileInfo.fsize > 0;
        } catch (QiniuException e) {
            log.error(key, e);
        }
        return false;
    }

    public String upload(byte[] bytes, String url) {
        if (bytes != null) {
            try {
                UploadManager uploadManager = new UploadManager();

                Response res = uploadManager.put(bytes, getKey(url), getToken());
                if (res.isOK()) {
                    String key = res.jsonToMap().get("key").toString();
                    return QINIU_URL + key;
                }
                return null;
            } catch (QiniuException e) {
                log.error("upload image from url ["+url+"] catch a QiniuException:e.response is null! e:"+e.toString());
                if (e.response.statusCode == 614) {
                    //614 exist error code
                    String key = getKey(url);
                    return QINIU_URL + key;
                }
            }
        }
        return null;
    }

    public String getToken() {
        Auth auth = Auth.create(QINIU_AK, QINIU_SK);

        String returnBody = "{\n" + "    \"hash\": $(etag),\n" + "    \"w\": $(imageInfo.width),\n"
                + "    \"h\": $(imageInfo.height),\n" + "    \"key\": $(key)\n" + "}";

        String token = auth.uploadToken(QINIU_BN, null, 3600, new StringMap().put("returnBody", returnBody));
        return token;
    }

    private String getKey(String url) {
        return FilenameUtils.getName(url);
    }

    private byte[] download(String url1) {
        if (StringUtils.isBlank(url1)) {
            return null;
        }
        InputStream is = null;
        try {
            URL url = new URL(url1);
            URLConnection con = url.openConnection();
            // 设置请求超时为5s
            con.setConnectTimeout(5 * 1000);
            // 输入流
            is = con.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);

            return bytes;
        } catch (Exception e) {
            log.error("download url [" + url1 + "] catch error [ " + e.getMessage() + "]", e);
            return null;
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


}
