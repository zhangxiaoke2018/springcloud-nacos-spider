package com.jinguduo.spider.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsLookup {
    
    private static Cache cache = new Cache(DClass.IN);

    public static InetAddress[] resolve(String host, String dnsServer) throws UnknownHostException {
        try {
            Lookup lookup = new Lookup(host, Type.A);
            SimpleResolver resolver = new SimpleResolver(dnsServer);
            resolver.setTimeout(5);
            lookup.setResolver(resolver);
            lookup.setCache(cache);
            final Record[] records=lookup.run();
            
            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                List<InetAddress> r = new ArrayList<>();
                for (final Record rec : records) {
                    if (rec.getType() == Type.A) {
                        InetAddress addr = ((ARecord)rec).getAddress();
                        r.add(addr);
                    }
                }
                if (r != null && r.size() > 0) {
                    return r.toArray(new InetAddress[r.size()]);
                }
            }
        } catch (TextParseException e) {
            throw new UnknownHostException("Invalid address: " + host);
        }
        
        return null;
    }
}
