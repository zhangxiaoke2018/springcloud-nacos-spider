package com.jinguduo.spider.common.util;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.jinguduo.spider.data.table.CookieString;

public class CookieMapper {
    
    public static CookieString writeCookieString(CookieStore cookieStore, String domain) {
        if (cookieStore == null) {
            return null;
        }
        final List<Cookie> cookies = cookieStore.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        String s = JSON.toJSONString(cookies, new SerializeFilter[] {nameFilter, acronymFilter});
        CookieString cookieString = new CookieString();
        cookieString.setDomain(domain);
        cookieString.setValue(s);
        return cookieString;
    }

    public static CookieStore readCookieStore(CookieString cookieString, CookieStore cookieStore) {
        CookieStore store = cookieStore;
        if (cookieString == null || !StringUtils.hasText(cookieString.getValue())) {
            return store;
        }
        if (store == null) {
            store = new BasicCookieStore();
        }
        List<JSONObject> cookies = JSON.parseArray(cookieString.getValue(), JSONObject.class);
        for (JSONObject j : cookies) {
            BasicClientCookie cookie = new BasicClientCookie(j.getString("na"), j.getString("va"));
            cookie.setDomain(j.getString("do"));
            cookie.setPath(j.getString("pa"));
            cookie.setExpiryDate(j.getDate("ex")); // expiryDate
            cookie.setVersion(j.getIntValue("ve"));
            cookie.setAttribute(ClientCookie.DOMAIN_ATTR, j.getString("do"));
            if (j.containsKey("se")) {
                cookie.setSecure(j.getBooleanValue("se"));
            }
            store.addCookie(cookie);
        }
        return store;
    }
    
    private final static SimplePropertyPreFilter nameFilter = new SimplePropertyPreFilter(
            Cookie.class, "name", "value", "domain", "path", "expiryDate", "version", "secure");
    
    private final static AcronymNameFilter acronymFilter = new AcronymNameFilter();
    
    static class AcronymNameFilter implements NameFilter {
        @Override
        public String process(Object object, String name, Object value) {
            if (name == null || name.length() <= 2) {
                return name;
            }
            return name.substring(0, 2);
        }
        
    }
}
