package com.jinguduo.spider.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.jinguduo.spider.common.util.DnsLookup;

public class FractionDnsResolver extends SystemDefaultDnsResolver {
	
	public final static FractionDnsResolver INSTANCE = new FractionDnsResolver();

    private final static ImmutableMap<String, String> MAPPING = new Builder<String, String>()
            // v.qq.com
            .put("v.qq.com", "114.114.114.114")
            .put("s.video.qq.com", "114.114.114.114")
            .put("sns.video.qq.com", "114.114.114.114")
            .put("ncgi.video.qq.com", "114.114.114.114")
            .put("data.video.qq.com", "114.114.114.114")
            // pptv.com
            .put("v.pptv.com", "114.114.114.114")
            .put("epg.api.pptv.com", "114.114.114.114")
            .build();
    
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException {
        String dnsServer = MAPPING.get(host);
        if (dnsServer != null) {
            InetAddress[] addresses = DnsLookup.resolve(host, dnsServer);
            if (addresses != null && addresses.length > 0) {
                return addresses;
            }
        }
        return super.resolve(host);
    }
}
