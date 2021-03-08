package com.jinguduo.spider.common.code;

import com.jinguduo.spider.common.util.Md5Util;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/10/2016 12:06 PM
 */
public class TieBaFetchCode implements FetchCode {
    @Override
    public String get(String url) {

        return DigestUtils.md5Hex(url);

    }
}
