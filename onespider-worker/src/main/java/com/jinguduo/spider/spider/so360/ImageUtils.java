package com.jinguduo.spider.spider.so360;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

import com.jinguduo.spider.common.util.Md5Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.*;
import java.util.List;
import java.util.Map;


/**
 * Created by lc on 2017/6/1.
 */
@Slf4j
public class ImageUtils {
    public static String getCountStr(InputStream imageStreams, List<HaoSouNumberModel> cssList, Map<String, Integer> numMap) {
        String countStr = null;
        try {
            // 读取源图像
            BufferedImage bi = ImageIO.read(imageStreams);
            //关流
            imageStreams.close();
            int srcWidth = bi.getWidth(); // 源图宽度
            int srcHeight = bi.getHeight(); // 源图高度
            if (srcWidth <= 0 || srcHeight <= 0) {
                return null;
            }
            //源图片
            Image image = bi.getScaledInstance(srcWidth, srcHeight,
                    Image.SCALE_DEFAULT);
            //返回值
            countStr = "";
            for (HaoSouNumberModel model : cssList) {
                ImageFilter cropFilter = new CropImageFilter(model.getPosition(), 0, model.getWidth(), model.getWidth() + model.getHight());
                Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
                BufferedImage tag = new BufferedImage(model.getWidth(), model.getWidth() + model.getHight(), BufferedImage.TYPE_BYTE_BINARY);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, model.getWidth(), model.getWidth() + model.getHight(), Color.WHITE, null); // 绘制切割后的图
                g.dispose();
                // 输出为流
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(tag, "PNG", out);
                byte[] bytes = out.toByteArray();
                try {
                    out.close();
                } catch (IOException e) {
                    out = null;
                }
                //获取到数字
                Integer num = null;
                try {
                    num = numMap.get(Md5Util.getMd5(new String(bytes)));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                if (null == num) {
                    return null;
                }
                countStr += num.toString();
            }
        } catch (IOException e) {
            imageStreams = null;

        }
        return countStr;
    }

    /**
     * 图像类型转换：GIF->JPG、GIF->PNG、PNG->JPG、PNG->GIF(X)、BMP->PNG
     *
     * @param srcImageFile  源图像地址
     * @param formatName    包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param destImageFile 目标图像地址
     */
    public final static void convert(String srcImageFile, String formatName, String destImageFile) {
        try {
            File f = new File(srcImageFile);
            f.canRead();
            f.canWrite();
            BufferedImage src = ImageIO.read(f);
            ImageIO.write(src, formatName, new File(destImageFile));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 图片转byte[]
     */
    public static byte[] image2byte(String path) {
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            try {
                input = new FileImageInputStream(new File(path));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }

    /**
     * base64 string 生成图片流
     */
    public static InputStream GenerateImage(String imgStr) {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成流
            InputStream sbs = new ByteArrayInputStream(b);
            return sbs;
        } catch (Exception e) {
            return null;
        }
    }
}