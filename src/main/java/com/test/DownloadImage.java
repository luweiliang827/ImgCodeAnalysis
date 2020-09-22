package com.test;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * created by luweiliang on 2020/9/21
 */
public class DownloadImage {

    public static void main(String[] args){
        try {
            download();
        }catch (Exception e){
            System.out.println("e="+e);
        }

    }

    public static void download() throws Exception {
        String urlString = "https://investorservice.cfmmc.com/veriCode.do?t="+System.currentTimeMillis();
        String savePath = "D:/img/";
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5*1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;

        // 输出的文件流
        File sf=new File(savePath);
        if(!sf.exists()){
            sf.mkdirs();
        }

        OutputStream os = new FileOutputStream(sf.getPath()+"\\"+"test.jpg");
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }


    /*public void dowanLoadPictureVerificationCode() throws IOException{
        driver.get("https://my.1hai.cn/Login/?url=http://www.1hai.cn/");
        WebElement ele = driver.findElement(By.xpath(".//img[@id='quick_imgCaptcha']"));
        ele.click();
        Utils.waitABit(2000);
        File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);  // 读取截图
        // 得到页面元素
        org.openqa.selenium.Point point= ele.getLocation();
        // 得到长、宽
        int eleWidth= ele.getSize().getWidth();
        int eleHeight= ele.getSize().getHeight();

        BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
        // copy 把图片放对应的生成目录下
        File screenshotLocation = new File("E:/Vame/img/test.jpg");
        FileUtils.copyFile(screenshot, screenshotLocation);
    }*/
}
