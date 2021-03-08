package com.jinguduo.spider.spider.weibo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class Weibo {

    public static volatile String cookies_str = "";

    private static DefaultHttpClient httpclient = null;

    private String userName;

    private String passWord;

    private static final String LONGIN_URL = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)&_=";

    private static final String preLoginUrl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=&rsakt=mod&client=ssologin.js(v1.4.18)&_=1446099453139";

    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36";

    public Weibo(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;
    }

    public boolean login(){

        boolean flag = true;

        final String loginUrl = String.format(LONGIN_URL,System.currentTimeMillis());

        if (StringUtils.isNotBlank(cookies_str)){
            return true;
        }

        httpclient = new DefaultHttpClient();
        HttpClientParams.setCookiePolicy(httpclient.getParams(),CookiePolicy.BROWSER_COMPATIBILITY);

        try {
            synchronized (cookies_str){
                String loginUrl2 = stepOne(loginUrl);
                if ( StringUtils.isNotBlank(loginUrl2) ) {
                    String loginUrl3 = stepTwo(loginUrl2);
                    if (StringUtils.isNotBlank(loginUrl3)){
                        flag = stepSecond(loginUrl3);
                    }
                } else {
                    flag = false;
                }
            }
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            flag = false;
            return flag;
        }
        log.debug("login success username :" + userName);
        return flag;
    }

    private boolean stepSecond(String loginUrl3) throws Exception {
        boolean flag = true;
        HttpGet loginGet3 = new HttpGet(loginUrl3);
        try {
            HttpResponse response3 = httpclient.execute(loginGet3);
            HttpEntity httpEntity3 = response3.getEntity();
            cookies_str = analyseWeiboCookies(httpclient.getCookieStore().getCookies());
            log.debug(userName+" login success...");
            flag = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            flag = false;
            throw new Exception(String.format("login fail by username : %s",userName),e.getCause());
        } finally {
            loginGet3.abort();
        }
        return flag;
    }

    private String stepTwo(String url) throws Exception {

        HttpGet loginGet2 = new HttpGet(url);
        String loginUrl3 = "" ;
        String loginHtml2 = "" ;
        try {
            HttpResponse response2 = httpclient.execute(loginGet2);
            HttpEntity httpEntity2 = response2.getEntity();
            loginHtml2 = EntityUtils.toString(httpEntity2);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            loginGet2.abort();
        }
        log.debug("login second step two 2  ,usename : "+userName+"");

        //"userdomain":"?wvr=5&lf=reg"}
        Pattern pattern3 = Pattern.compile("\"userdomain\":\"(.*?)\"}");
        Matcher m3 = pattern3.matcher(loginHtml2);
        if (m3.find()) {
            loginUrl3="http://weibo.com/"+m3.group(1);
            log.debug("login third step 3 url："+loginUrl3);
        } else {
            log.debug("=====================================loginHtml2================================");
            log.debug(loginHtml2);
            log.debug("========================================loginHtml2==============================");
            throw new Exception("login fail getSecondUrl");
        }
        return loginUrl3;
    }

    private String stepOne(String loginUrl) throws Exception {

        String loginUrl2 = "" ;

        HttpPost httppost = new HttpPost(loginUrl);

        List<NameValuePair> formparams =new ArrayList<NameValuePair>();
        //构建请求Body
        buildBodyForm(formparams);
        //构建请求报文和参数
        buildPostHeader(formparams,httppost);
        //获取登陆应答内容
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            httppost.abort();
        }
        log.debug("login return status code ："+response.getStatusLine().getStatusCode());

        if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            return loginUrl2;
        }

        HttpEntity httpEntity = response.getEntity();
        String responseStr = EntityUtils.toString(httpEntity);

        String loginHtml1 =  new String(responseStr.getBytes("iso8859-1"), "GBK");
        log.debug("login first step one 1  ,usename : "+userName+"");

        Pattern pattern1 = Pattern.compile("location.replace\\('(.*?)'\\);");
        //Pattern pattern1 = Pattern.compile("loca／tion.replace\\(\"(.*?)\"\\);");
        Matcher m1 = pattern1.matcher(loginHtml1);
        if (m1.find()) {
            loginUrl2=m1.group(1);
            log.debug("login second step two 2  url："+loginUrl2);
        } else {
            if(loginHtml1.contains("4049")){
                log.error("need input verification code by username : " + userName);
            }
            log.debug("=====================================loginHtml1================================");
            log.debug(loginHtml1);
            log.debug("========================================loginHtml1==============================");
            throw new Exception(String.format("need verification by username : %s",userName));
        }
        return loginUrl2;
    }

    private void buildPostHeader(List<NameValuePair> formparams, HttpPost httppost) {

        httppost.setHeader("Accept", "*/*");
        httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4,zh-TW;q=0.2");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setHeader("Host", "login.sina.com.cn");
        httppost.setHeader("Origin", "http://weibo.com");
        try {
            httppost.setEntity(new UrlEncodedFormEntity(formparams,  HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void buildBodyForm(List<NameValuePair> formparams) {

        //预登陆信息
        String preJosn = getPreLoginJson();

        JSONObject preObj = JSONObject.parseObject(preJosn);
        String pubkey = preObj.getString("pubkey");
        String servertime = preObj.getString("servertime");
        String nonce = preObj.getString("nonce");
        String rsakv = preObj.getString("rsakv");

        formparams.add(new BasicNameValuePair("entry", "weibo"));
        formparams.add(new BasicNameValuePair("password", passWord));
        formparams.add(new BasicNameValuePair("entry", "weibo"));
        formparams.add(new BasicNameValuePair("gateway", "1"));
        formparams.add(new BasicNameValuePair("from", ""));
        formparams.add(new BasicNameValuePair("savestate", "7"));
        formparams.add(new BasicNameValuePair("useticket", "1"));
        formparams.add(new BasicNameValuePair("pagerefer", "http://s.weibo.com/weibo/%25E6%259E%2597%25E4%25B8%25B9?topnav=1&wvr=6&b=1"));
        formparams.add(new BasicNameValuePair("vsnf", "1"));
        formparams.add(new BasicNameValuePair("su", getEncodeUserName(this.userName)));
        formparams.add(new BasicNameValuePair("service", "miniblog"));
        formparams.add(new BasicNameValuePair("servertime", servertime));
        formparams.add(new BasicNameValuePair("nonce", nonce));
        formparams.add(new BasicNameValuePair("pwencode", "rsa2"));
        formparams.add(new BasicNameValuePair("rsakv", rsakv));
        formparams.add(new BasicNameValuePair("sp", getSP(this.passWord, pubkey, servertime, nonce)));
        formparams.add(new BasicNameValuePair("encoding", "UTF-8"));
        formparams.add(new BasicNameValuePair("sr", "1366*768"));
        formparams.add(new BasicNameValuePair("prelt", "1011"));
        formparams.add(new BasicNameValuePair("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
        formparams.add(new BasicNameValuePair("domain", "weibo.com"));
        formparams.add(new BasicNameValuePair("returntype", "META"));
    }

    private String analyseWeiboCookies(List<Cookie> cookies) {
        StringBuffer buffer = new StringBuffer();
        for (Cookie cookie : cookies) {
            buffer.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }
        if(buffer.length()>0){
            buffer.deleteCharAt(buffer.length()-1);
        }
        return buffer.toString();
    }

    private String getPreLoginJson(){

        String json = "" ;
        String responseBody = "";

        HttpGet httpget = new HttpGet(preLoginUrl);
        httpget.setHeader("User-Agent", userAgent);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        try {
            responseBody = httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseBody = null;
        } finally {
            httpget.abort();
        }

        json = getPatternCompileFirst(responseBody,"\\((.*)\\)");
        log.debug("before login param ："+json);

        return json;
    }

    private String getPatternCompileFirst(String origin, String regExp) {
        String result = "";
        Pattern pattern = Pattern.compile(regExp);
        Matcher m = pattern.matcher(origin);
        if (m.find()) {
            result=m.group(1);
        }
        return result;
    }

    private String getEncodeUserName(String userName){
        try {
            userName = java.net.URLEncoder.encode(userName, "UTF-8");
            byte[] isoret = userName.getBytes("UTF-8");
            return Base64.encodeBase64String(isoret);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return "error";
        }

    }

    private String getSP(String pwd,String pubkey,String servertime,String nonce) {
        String t = "10001";
        String message = servertime + "\t" + nonce + "\n" + pwd;
        String result = null;
        try {
            result = rsa(pubkey, t , message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private String rsa(String pubkey, String exponentHex, String pwd)
            throws IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException,
            UnsupportedEncodingException {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        BigInteger m = new BigInteger(pubkey, 16);
        BigInteger e = new BigInteger(exponentHex, 16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);

        //创建公钥
        RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
        Cipher enc = Cipher.getInstance("RSA");
        enc.init(Cipher.ENCRYPT_MODE, pub);

        byte[] encryptedContentKey = enc.doFinal(pwd.getBytes("UTF-8"));

        return new String(encodeHex(encryptedContentKey));
    }

    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String getSub(String cookie){
        String sub = cookie.replaceAll(".*;SUB=(.*);SUBP=.*", "$1");
        sub = "SUB=" + sub + ";";
        return sub;
    }

    /*public static void main(String[] args) {
        Weibo weibo = new Weibo("guduochuanmei@sina.com", "1234567ok");
        weibo.login();
    }*/
}
