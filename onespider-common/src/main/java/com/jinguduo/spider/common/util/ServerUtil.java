package com.jinguduo.spider.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class ServerUtil {

    private static String SERVER_IP = "";
    private static String LOCAL_IP = "";

    /***
     * 得到服务器Ip
     * @return
     */
    public static String getServerIp(){
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces
                        .nextElement();
                ip = (InetAddress) ni.getInetAddresses().nextElement();
                SERVER_IP = ip.getHostAddress();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                        && ip.getHostAddress().indexOf(":") == -1) {
                    SERVER_IP = ip.getHostAddress();
                    break;
                } else {
                    ip = null;
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage(), e);
        }
        return SERVER_IP;
    }

    /***
     * 得到本地Id
     * @return
     */
    public static String getLocalIP(){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }

        byte[] ipAddr = addr.getAddress();
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return LOCAL_IP = ipAddrStr;
    }
}
