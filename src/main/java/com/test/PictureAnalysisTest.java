package com.test;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by luweiliang on 2020/9/8
 */
public class PictureAnalysisTest {

    public static final int splitNums=4000000;
    public static final int splitWidthNum=1;
    public void analysisTest(){
        try {

                //用来存取cookies信息的变量
                CookieStore store;
                /**
                 * 请求第一次页面
                 */
                //第一次请求(登陆的请求)
                DefaultHttpClient client1 = new DefaultHttpClient();
                HttpPost request1 = new HttpPost("https://investorservice.cfmmc.com/");
                String dir = "D:/img/";
                String fileName = "test.jpg";
                store = client1.getCookieStore();
                HttpResponse response1 = client1.execute(request1);
                if (response1.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity1 = response1.getEntity();
                    //获取
                    String result1 = EntityUtils.toString(entity1);

                    //使用jsoup 进行语言转换
                    Document doc1 = Jsoup.parse(result1);
                    //图片标签
                    String string = doc1.getElementById("imgVeriCode").toString();


                    //爬取验证码  图片/sso/authimg
                    PictureAddressUtil addressUtil = new PictureAddressUtil();
                    List<String> list = addressUtil.filePath(string);

                    /**
                     * 通过验证码   下载下来   并用tess4j图片识别其中的验证码
                     */
                    DefaultHttpClient client2 = new DefaultHttpClient();
                    HttpPost request2 = new HttpPost("https://investorservice.cfmmc.com" + list.get(0) + "");
                    client2.setCookieStore(store);
                    HttpResponse response2 = client2.execute(request2);
                    downloadJPG(response2,dir,fileName);
                    //downloadHttpUrl("https://investorservice.cfmmc.com" + list.get(0),dir,fileName);

                    /*String ocrResult = dir.replaceAll("/","\\\\\\\\")+"test-result.jpg";
                    String imgUrl = dir.replaceAll("/","\\\\\\\\")+"test.jpg";*/

                    String ocrResult = "d:\\img\\2f3d520f23-1.jpg";
                    String imgUrl = "d:\\img\\2f3d520f23.jpg";

                    //处理图片
                    //removeBackground(imgUrl,ocrResult);
                    //PicDeal.splitPic(imgUrl,ocrResult);
                    picDeal(imgUrl,ocrResult);
                    cuttingImg(ocrResult);
                    //图片识别
                    String code = getImgContent(ocrResult);

                    System.out.println("验证码 = " + code.trim());
                    System.out.println("===============================");
                }
            }catch (Exception e){
            System.out.println("e="+e);
        }
    }

    public static void downloadJPG(HttpResponse httpResponse,String dir, String fileName) throws IOException {
        InputStream input = httpResponse.getEntity().getContent();
        /*OutputStream output = new FilterOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                new File(fileName);
            }
        });
        IOUtils.copy(input, output);
        if (output != null) {
            output.close();
        }
        output.flush();*/
        try {
            File f = new File(dir + fileName);
            FileOutputStream output = openOutputStream(f);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static String getImgContent(String imgUrl) {
        String content = "";
        File imageFile = new File(imgUrl);
        //读取图片数字
        ITesseract instance = new Tesseract();
        instance.setDatapath("D:\\Program Files\\Tesseract-OCR\\tessdata");
        instance.setLanguage("eng");//英文库识别数字比较准确
        try {
            Long startTime = System.currentTimeMillis();
            content = instance.doOCR(imageFile);
            System.out.println("doOCR result: "+content.trim()+"\n 耗时"+(System.currentTimeMillis() -startTime)+"ms");
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return content;
    }

    public static String downloadHttpUrl(String url, String dir, String fileName) {
        try {
            URL httpurl = new URL(url);
            File f = new File(dir + fileName);
            copyURLToFile(httpurl, f);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dir + fileName;
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.exists() == false) {
                if (parent.mkdirs() == false) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

    public static void copyURLToFile(URL source, File destination) throws IOException {
        InputStream input = source.openStream();
        try {
            FileOutputStream output = openOutputStream(destination);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * 验证码图片处理
     * @param imgUrl
     * @param resUrl
     */
    public static void removeBackground(String imgUrl, String resUrl){
        try{
            BufferedImage img = ImageIO.read(new File(imgUrl));

            img = img.getSubimage(1, 1, img.getWidth()-2, img.getHeight()-2);
            int width = img.getWidth();
            int height = img.getHeight();
            double subWidth = (double) width/(splitWidthNum+0.0);
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (int i = 0; i < splitWidthNum; i++) {

                //todo 以下是对图片进行二值化处理，在这里我的思路是规定，色差范围在splitNums到负splitNums之间的，
                // 算是同色，放入同一个色值，放入一个map中，map中的Key放色值，value放这个色值得个数，后期就根据这个色值来对验证码进行二值化
                for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth && x < width - 1; ++x) {
                    for (int y = 0; y < height; ++y) {
                        if (isWhite(img.getRGB(x, y)) == 1){
                            continue;
                        }
                        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
                        for (Integer color : map.keySet()) {
                            map2.put(color,map.get(color));
                        }

                        for (Integer color : map2.keySet()) {
                            System.out.println(Math.abs(color)-Math.abs(img.getRGB(x, y)));
                            if (Math.abs(color)-Math.abs(img.getRGB(x, y))<splitNums&&Math.abs(color)-Math.abs(img.getRGB(x, y))>-splitNums){
                                map.put(color, map.get(color) + 1);
                            }else{
                                map.put(img.getRGB(x, y), 1);
                            }
                        }
                        if (map.isEmpty()){
                            map.put(img.getRGB(x, y), 1);
                        }
                    }

                }
                System.out.println("==============================");

                int max = 0;
                int colorMax = 0;
                for (Integer color : map.keySet()) {
                    if (max < map.get(color)) {
                        max = map.get(color);
                        colorMax = color;
                    }
                }

                for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth&& x < width - 1; ++x) {
                    for (int y = 0; y < height; ++y) {
                        int ress=Math.abs(img.getRGB(x, y))-Math.abs(colorMax);
                        if (ress<splitNums&&ress>-splitNums) {
                            img.setRGB(x, y, Color.BLACK.getRGB());
                        } else {
                            img.setRGB(x, y, Color.WHITE.getRGB());
                        }
                    }
                }

                File file = new File(resUrl);
                if (!file.exists())
                {
                    File dir = file.getParentFile();
                    if (!dir.exists())
                    {
                        dir.mkdirs();
                    }
                    try
                    {
                        file.createNewFile();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                ImageIO.write(img, "jpg", file);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //todo 判断是否为白色的方法
    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue()>600) {
            return 1;
        }
        return 0;
    }

    public static void picDeal(String url,String resUrl){

        //定义一个临界阈值
        int threshold = 360;
        try {
            BufferedImage img = ImageIO.read(new File(url));
            int width = img.getWidth();
            int height = img.getHeight();
            for(int i = 1;i < width;i++){
                for (int x = 0; x < width; x++){
                    for (int y = 0; y < height; y++){
                        Color color = new Color(img.getRGB(x, y));
                        System.out.println("red:"+color.getRed()+" | green:"+color.getGreen()+" | blue:"+color.getBlue());
                        int num = color.getRed()+color.getGreen()+color.getBlue();
                        if(num >= threshold){
                            img.setRGB(x, y, Color.WHITE.getRGB());
                        }
                    }
                }
            }
            for(int i = 1;i<width;i++){
                Color color1 = new Color(img.getRGB(i, 1));
                int num1 = color1.getRed()+color1.getGreen()+color1.getBlue();
                for (int x = 0; x < width; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        Color color = new Color(img.getRGB(x, y));

                        int num = color.getRed()+color.getGreen()+color.getBlue();
                        if(num==num1){
                            img.setRGB(x, y, Color.BLACK.getRGB());
                        }else{
                            img.setRGB(x, y, Color.WHITE.getRGB());
                        }
                    }
                }
            }
            File file = new File(resUrl);
            if (!file.exists())
            {
                File dir = file.getParentFile();
                if (!dir.exists())
                {
                    dir.mkdirs();
                }
                try
                {
                    file.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            ImageIO.write(img, "jpg", file);
        }catch (Exception e){
            System.out.println("e = "+e);
        }
    }


    public static void cuttingImg(String imgUrl){
        try{
            File newfile=new File(imgUrl);
            BufferedImage bufferedimage=ImageIO.read(newfile);
            int width = bufferedimage.getWidth();
            int height = bufferedimage.getHeight();
            if (width > 187) {
                bufferedimage=cropImage(bufferedimage,(int) ((width - 187) / 2),0,(int) (width - (width-187) / 2),(int) (height));
                if (height > 88) {
                    bufferedimage=cropImage(bufferedimage,0,(int) ((height - 88) / 2),187,(int) (height - (height - 88) / 2));
                }
            }else{
                if (height > 88) {
                    bufferedimage=cropImage(bufferedimage,0,(int) ((height - 88) / 2),(int) (width),(int) (height - (height - 88) / 2));
                }
            }
            ImageIO.write(bufferedimage, "jpg", new File(imgUrl));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static BufferedImage cropImage(BufferedImage bufferedImage, int startX, int startY, int endX, int endY) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (startX == -1) {
            startX = 0;
        }
        if (startY == -1) {
            startY = 0;
        }
        if (endX == -1) {
            endX = width - 1;
        }
        if (endY == -1) {
            endY = height - 1;
        }
        BufferedImage result = new BufferedImage(endX - startX, endY - startY, 4);
        for (int x = startX; x < endX; ++x) {
            for (int y = startY; y < endY; ++y) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x - startX, y - startY, rgb);
            }
        }
        return result;
    }
}
