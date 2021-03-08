package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.net.Socket;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "proxies")
@Data
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt", "state"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Proxy implements Serializable {

    private static final long serialVersionUID = -1066212721412853868L;
    
    @Id
    private String host;

    @Enumerated(EnumType.ORDINAL)
    private ProxyState state = ProxyState.Standby;
    
    @Enumerated(EnumType.STRING)
    @Column(length=8)
    private ProxyType ptype = ProxyType.unknown;

    @Column(length=32)
    private String username;
    
    @Column(length=32)
    private String password;

    @Column(length=32)
    private String serverName;
    
    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(nullable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    
    @Transient
    @JsonIgnore
    private transient Socket socket;



    @Transient
    @JsonIgnore
    public static Proxy newHttpProxy() {
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.http);
        return proxy;
    }
    
    @Transient
    @JsonIgnore
    public static Proxy newSocks5Proxy() {
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.socks5);
        return proxy;
    }
    
    @Transient
    @JsonIgnore
    public static Proxy newSocks4Proxy() {
        Proxy proxy = new Proxy();
        proxy.setPtype(ProxyType.socks4);
        return proxy;
    }
     
    @Transient
    @JsonIgnore
    public String getHostName() {
        if (StringUtils.hasText(host)) {
            return host.substring(0, host.lastIndexOf(':'));
        }
        return null;
    }
 
    @Transient
    @JsonIgnore
    public int getPort() {
        return Integer.valueOf(host.substring(host.indexOf(':') + 1, host.length()));
    }
    
    @Transient
    @JsonIgnore
    public boolean isBroken() {
        return this.state == ProxyState.Broken;
    }
}
