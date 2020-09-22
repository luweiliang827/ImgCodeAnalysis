package com.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * created by luweiliang on 2020/9/10
 */
public class DeletePassPoint {
    public static BufferedImage deletepoints(BufferedImage img){
        //todo 为白色的像素点rgb值
//        int[] whitePointNum={-257,-1,-770,-1027,-514};
        int width = img.getWidth();
        int height = img.getHeight();
        double subWidth = (double) width;

        ColorModel cm = img.getColorModel();
        BufferedImage imgNew=new BufferedImage(cm,cm.createCompatibleWritableRaster(img.getWidth(),img.getHeight()),cm.isAlphaPremultiplied(), null);

        for (int x = (int) (1 + 0 * subWidth); x < (0 + 1) * subWidth && x < width - 1; ++x) {
            for (int y = 1; y < height-1; ++y) {

                int leftRgb=img.getRGB(x-1,y);
                int rightRgb=img.getRGB(x+1,y);
                int buttonRgb=img.getRGB(x,y-1);
                int headRgb=img.getRGB(x,y+1);


                int judgy=0;
                //todo 对自己本身像素点周边进行检测
                if (isWhite(leftRgb)==0){
                    judgy++;
                }
                if (isWhite(rightRgb)==0){
                    judgy++;
                }

                if (isWhite(buttonRgb)==0){
                    judgy++;
                }

                if (isWhite(headRgb)==0){
                    judgy++;
                }



                //todo 当判定上下左右其中3个方位为空时，此点算离散点
                if (judgy>=3){
//                    imgNew.setRGB(x, y, Color.BLACK.getRGB());
                    imgNew.setRGB(x, y, Color.WHITE.getRGB());
                }else{
//                    imgNew.setRGB(x, y, Color.WHITE.getRGB());
                    imgNew.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        return imgNew;
    }

    //todo 判断是否为白色的方法  爬虫 验证码处理
    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue()>600) {
            return 1;
        }
        return 0;
    }

}
