package com.jinguduo.spider.spider.douban;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import java.math.BigInteger;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lc on 2020/1/10
 *
 * getPlainText / getDetailUrlList 为外部调用方法，其他为辅助方法，请勿尝试外部调用
 */
@SuppressWarnings("all")
public class DoubanBookDecodeBodyUtil {

    private static char[] CHARS_SET = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();

    /**
     * 通过html原文获取到明文数据 (main) 外部主要调用方法
     * */
    protected static String getPlainText (String html)throws Exception {

        byte[] sdata = getMixtureData(html);
        //分割点
        int key_ind = (int) Math.max(Math.floor((sdata.length - 2 * 16) / 3), 0);
        byte[] key_bytes = getKey(sdata, key_ind);
        byte[] data = getData(sdata, key_ind);
        int[] plaintext_ints = decrypt_1(data, key_bytes);

        String plaintext = plaintextInts2Str(plaintext_ints);
        return plaintext;
    }

    /**
     * 正则获取半明文中的详情页url (list)
     * */
    protected static List<String> getDetailUrlList(String plaintext) {
        List<String> resList = new ArrayList<>();

        String pattern = "(https://book.douban.com/subject/+)(\\d+)(/+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(plaintext);
        while (m.find()) {
            String detailUrl = m.group(0);
            resList.add(detailUrl);
        }
        return resList;
    }



    private static int[] bytes2ints(byte[] bs) {
        int[] res = new int[bs.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = bs[i] & 255;
        }
        return res;
    }

    private static BigInteger toNumber(int[] a) {
        BigInteger[] longArr = int2BI(a);

        BigInteger res = longArr[0].add(longArr[1].shiftLeft(16));
        res = res.add(longArr[2].shiftLeft(32));
        res = res.add(longArr[3].shiftLeft(48));
        return res;
    }

    private static BigInteger[] int2BI(int[] a) {
        BigInteger[] b = new BigInteger[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = BigInteger.valueOf(a[i]);
        }
        return b;
    }

    private static BigInteger rotl(BigInteger a, int t) {
        int[] zi = {0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};
        BigInteger z = toNumber(zi);
        BigInteger i = a.shiftLeft(t).and(z);
        BigInteger j = a.shiftRight(64 - t).and(z);
        return i.or(j);

    }

    private static BigInteger fromBits(int t, int e, int r, int n) {
        if (r == 0) {
            int[] res = {65535 & t, t >> 16, 65535 & e, e >> 16};
            return toNumber(res);
        }
        int[] res = {0 | t, 0 | e, 0 | r, 0 | n};
        return toNumber(res);
    }

    private static String decrypt_key(int[] r) {
        BigInteger total_len = BigInteger.valueOf(16);
        Integer o = 0;
        Integer i = 16;

        int[] seedInit = new int[]{41405, 0, 0, 0};
        int[] cInit = new int[]{31225, 40503, 26545, 5718};
        int[] uInit = new int[]{60239, 10196, 44605, 49842};
        int[] sInit = new int[]{51847, 34283, 31153, 40503};
        int[] fInit = new int[]{44643, 49842, 51831, 34283};
        int[] lInit = new int[]{26565, 5718, 60207, 10196};
        int[] zInit = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};

        BigInteger seed = toNumber(seedInit);
        BigInteger c = toNumber(cInit);
        BigInteger u = toNumber(uInit);
        BigInteger s = toNumber(sInit);
        BigInteger f = toNumber(fInit);
        BigInteger l = toNumber(lInit);
        BigInteger z = toNumber(zInit);

        //int[] t = add(seed, l);
        BigInteger t = seed.add(l).and(z);
        BigInteger h = total_len;
        // int[] h = fromNumber(total_len);
        //t = add(t, h);

        t = t.add(h).and(z);

        while (o <= i - 8) {
            h = fromBits(r[o + 1] << 8 | r[o], r[o + 3] << 8 | r[o + 2]
                    , r[o + 5] << 8 | r[o + 4]
                    , r[o + 7] << 8 | r[o + 6]);
            //h = multiply(h, u);
            h = h.multiply(u).and(z);
            h = rotl(h, 31);

            //h = multiply(h, s);
            h = h.multiply(s).and(z);
            //t = xor(t, h);
            t = t.xor(h).and(z);

            t = rotl(t, 27);
            //t = multiply(t, s);
            t = t.multiply(s).and(z);

            //t = add(t, f);
            t = t.add(f).and(z);
            o += 8;
        }
        //int[] e = shiftRight(t, 33);
        BigInteger e = t.shiftRight(33).and(z);

        //t = xor(t, e);
        t = t.xor(e).and(z);

        //t = multiply(t, u);
        t = t.multiply(u).and(z);

        //e = shiftRight(t, 29);
        e = t.shiftRight(29).and(z);

        //t = xor(t, e);
        t = t.xor(e).and(z);

        //t = multiply(t, c);
        t = t.multiply(c).and(z);

        //e = shiftRight(t, 32);
        e = t.shiftRight(32).and(z);

        //t = xor(t, e);
        t = t.xor(e).and(z);

        char[] toHex = "0123456789abcdef".toCharArray();
        char[] n = new char[64];
        //e = new int[]{16, 0, 0, 0};
        BigInteger dd = BigInteger.valueOf(16);
        //r = t;
        BigInteger rr = t;

        for (int j = 63; j >= 0; j--) {
            //Map<String, int[]> divMap = div(rr, dd);
            BigInteger re = rr.mod(dd);
            rr = rr.divide(dd).and(z);
            //rr = divMap.get("res1");
            //int[] reminder = divMap.get("res2");

            //int i1 = (int) toNumber_2(re);

            int i1 = re.byteValue() & 0xf;

            n[j] = toHex[i1];
        }


        return new String(n);
    }

    private static int[] decrypt_1(byte[] data, byte[] key) {
        int data_length = data.length;
        byte[] a = new byte[data_length];

        int key_length = key.length;
        int n = 0;

        int[] o = new int[256];
        for (int i = 0; i < 256; i++) {
            o[i] = i;
        }
        int i = 0;
        for (int s = 0; s < 256; s++) {
            i = (i + o[s] + key[s % key_length]) % 256;
            n = o[s];
            o[s] = o[i];
            o[i] = n;
        }

        int s = 0;
        i = 0;

        for (int u = 0; u < data_length; u++) {
            s = (s + 1) % 256;
            i = (i + o[s]) % 256;
            n = o[s];
            o[s] = o[i];
            o[i] = n;
            a[u] = (byte) (data[u] ^ o[(o[s] + o[i]) % 256]);
        }


        int[] res = new int[a.length];

        for (int j = 0; j < a.length; j++) {
            res[j] = a[j] & 255;
        }
        return res;

    }

    private static byte[] getMixtureData(String html) throws Exception{
        if (StringUtils.isEmpty(html)) return null;

        html = StringUtils.substring(html, html.indexOf("window.__DATA__ =") + 19);
        html = StringUtils.substring(html, 0, html.indexOf(";") - 1);

        byte[] decode = Base64Utils.decode(html.getBytes());
        return decode ;
    }

    private static byte[] getKey(byte[] sdata, int key_ind) {
        byte[] key_data = new byte[16];
        System.arraycopy(sdata, key_ind, key_data, 0, 16);
        //key的byte转化为int数组并拿到key
        int[] key_data_2 = DoubanBookDecodeBodyUtil.bytes2ints(key_data);
        String s1 = DoubanBookDecodeBodyUtil.decrypt_key(key_data_2);
        String key = s1.substring(48);
        char[] chars = key.toCharArray();
        byte[] arr = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            arr[i] = (byte) chars[i];
        }
        return arr;

    }

    private static byte[] getData(byte[] sdata, int key_ind) {
        byte[] data = new byte[sdata.length - 16];
        System.arraycopy(sdata, 0, data, 0, key_ind);
        System.arraycopy(sdata, key_ind + 16, data, key_ind, sdata.length - 16 - key_ind);
        return data;
    }

    private static String plaintextInts2Str(int[] plaintext_ints) {

        CharBuffer cb = CharBuffer.allocate(plaintext_ints.length);

        for (int i = 0; i < plaintext_ints.length; i++) {
            if (plaintext_ints[i] >= 33 && plaintext_ints[i] <= 126) {
                cb.append(CHARS_SET[plaintext_ints[i] - 33]);
            }
        }

        return String.valueOf(cb.array()).trim();

    }

}
