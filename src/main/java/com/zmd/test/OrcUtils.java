/**   
 * Copyright © 2020 zmd. All rights reserved.
 * 
 * 功能描述：
 * @Package: com.zmd.reptile 
 * @author: Lin   
 * @date: 2020年6月18日 上午9:57:30 
 */
package com.zmd.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Copyright: Copyright (c) 2020 zmd
 * 
 * @ClassName: OrcUtils.java
 * @Description: 该类的功能描述
 *
 * @version: v1.0.0
 * @author: Lin
 * @date: 2020年6月18日 上午9:57:30 
 *
 */
public class OrcUtils {

	static class ImageFileFilter implements FileFilter {
		private String postfix = ".jpg";

		public ImageFileFilter(String postfix) {
			if(!postfix.startsWith("."))
				postfix = "." + postfix;

			this.postfix = postfix;
		}

		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(postfix);
		}
	}
	// 存放比对图片与代表数字的Map
    private static Map<BufferedImage, String> trainMap = null;
	
	/**
	 * 
	* @Title: getValidateCode
	* @Description:识别调用
	* @param @param file
	* @param @return
	* @param @throws Exception    参数
	* @return String    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午10:34:21
	 */
	public static String getValidateCode(File file) throws Exception {
		
        // 装载图片
        BufferedImage image = ImageIO.read(file);
        //移除白色背景
        removeInterference(image);
        // 拆分图片
        List<BufferedImage> digitImageList = splitImage(image);
        //装在图片
        loadTrainData();

        // 循环每一位数字图进行比对
        StringBuilder sb = new StringBuilder();
        for (BufferedImage digitImage : digitImageList) {
        	 sb.append(getSingleCharOcr(digitImage, trainMap));
        }

		String path = OrcUtils.class.getResource("/").getPath().replace("test-classes", "resultCode");
		File dir = new File(path);
		if(!dir.exists() && dir.mkdir());

        ImageIO.write(image, "jpg", new File(OrcUtils.class.getResource("/").getPath().replace("test-classes", "resultCode"), sb.toString() + ".jpg"));
        
        return sb.toString();
    }
	
	/**
	 * 
	* @Title: getSingleCharOcr
	* @Description: 验证码解析
	* @param @param img
	* @param @param map
	* @param @return    参数
	* @return String    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 下午4:24:36
	 */
	public static String getSingleCharOcr(BufferedImage img,Map<BufferedImage, String> map) {
		String result = "#";
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			if (Math.abs(bi.getWidth()-width) > 2)
				continue;
			int widthmin = width < bi.getWidth() ? width : bi.getWidth();
			int heightmin = height < bi.getHeight() ? height : bi.getHeight();
			Label1: for (int x = 0; x < widthmin; ++x) {
				for (int y = 0; y < heightmin; ++y) {
					if (isBlack(img.getRGB(x, y)) != isBlack(bi.getRGB(x, y))) {
						count++;
						if (count >= min)
							break Label1;
					}
				}
			}
			if (count < min) {
				min = count;
				result = map.get(bi);
			}
		}
		return result;
	}
	
	/**
	 * 
	* @Title: loadTrainData
	* @Description: 装在对比的图片
	* @param @return
	* @param @throws IOException    参数
	* @return Map<BufferedImage,String>    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午10:42:12
	 */
	private static Map<BufferedImage, String> loadTrainData() throws IOException {

	        if (trainMap == null) {
	            Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
	            String path = OrcUtils.class.getResource("/").getPath().replace("test-classes", "splitCode");
	            File dir = new File(path);
	            File[] files = dir.listFiles();
	            for (File file : files) {
	                map.put(ImageIO.read(file), file.getName().charAt(0) + "");
	            }
	            trainMap = map;
	        }
	        return trainMap;
	    }
	
	/**
	 * 
	* @Title: generateStdDigitImgage
	* @Description: 图片分割，并生成模板库
	* @param @throws Exception    参数
	* @return void    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午10:34:02
	 */
	public static void generateStdDigitImgage() throws Exception {
        File dir = new File(OrcUtils.class.getResource("/").getPath().replace("test-classes", "checkCode"));
        File[] files = dir.listFiles(new ImageFileFilter("jpg"));
		String path = OrcUtils.class.getResource("/").getPath().replace("test-classes", "splitCode1");
		File dir2 = new File(path);
		if(!dir2.exists() && dir2.mkdir());

		int counter = 0;
        for (File file : files) {
            BufferedImage image = ImageIO.read(file);
            removeInterference(image);
			List<BufferedImage> list = new ArrayList<BufferedImage>();
            List<BufferedImage> digitImageList = splitImage(image);
			/*List<BufferedImage> digitImageList2 = splitImage2(image);
			list.addAll(digitImageList);
			list.addAll(digitImageList2);*/
            for (int i = 0; i < digitImageList.size(); i++) {
                BufferedImage bi = digitImageList.get(i);
                ImageIO.write(bi, "jpg", new File(OrcUtils.class.getResource("/").getPath().replace("test-classes", "splitCode1"), counter++ + ".jpg"));
                //ImageIO.write(bi, "jpg", new File(OrcUtils.class.getResource("/").getPath().replace("test-classes", "compareCode"), counter++ + ".jpg"));
            }
        }
        System.out.println("生成供比对的图片完毕，请到目录中手工识别并重命名图片，并删除其它无关图片！");
    }

	/**
	 * 
	* @Title: removeInterference
	* @Description: 去除图像干扰像素（非必须操作，只是可以提高精度而已）。
	* @param @param image
	* @param @return
	* @param @throws Exception    参数
	* @return BufferedImage    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午9:58:56
	 */
	public static BufferedImage removeInterference(BufferedImage img)  throws Exception {  
        /*int width = image.getWidth();  
        int height = image.getHeight();  
        for (int x = 0; x < width; ++x) {  
            for (int y = 0; y < height; ++y) {  
                if (isFontColor(image.getRGB(x, y))) {
                    // 如果当前像素是字体色，则检查周边是否都为白色，如都是则删除本像素。
                    int roundWhiteCount = 0;
                    if(isWhiteColor(image, x+1, y+1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x+1, y-1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x-1, y+1))
                        roundWhiteCount++;
                    if(isWhiteColor(image, x-1, y-1))
                        roundWhiteCount++;
                    if(roundWhiteCount == 4) {
                        image.setRGB(x, y, Color.WHITE.getRGB());  
                    }
                } 
            }  
        }  
        return image;  */
		//1.去处干扰线
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 1; x < width - 1; ++x) {
			for (int y = 1; y < height - 1; ++y) {
				//对点color[i][j],如果color[i+1][j],color[i-1][j],color[i][j+1],color[i][j-1]都是纯黑或者纯白色的，就认为color[i][j]是干扰，将color[i][j]置为白色。
            	if (getColorBright(img.getRGB(x, y)) < 70) {
					if (isBlackOrWhite(img.getRGB(x - 1, y))
							+ isBlackOrWhite(img.getRGB(x + 1, y))
							+ isBlackOrWhite(img.getRGB(x, y - 1))
							+ isBlackOrWhite(img.getRGB(x, y + 1)) == 4) {
						img.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
            	//去噪处理
            	if (img.getRGB(x, y) == Color.BLACK.getRGB()) {
                    if(isNoisePoint(img, x, y))
                        img.setRGB(x, y, Color.WHITE.getRGB());
                } 
            	
            }
        }
        
        //将所有字符转为黑色，便于识别
        for (int x = 1; x < width - 1; ++x) {
			for (int y = 1; y < height - 1; ++y) {
				if (getColorBright(img.getRGB(x, y)) < 600) {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
        }
        
        img = img.getSubimage(1, 1, img.getWidth() - 1, img.getHeight() - 1);
        return img;
	}
	
	/**
	 * 
	* @Title: isNoisePoint
	* @Description: 图片去噪点
	* @param @param img
	* @param @param x
	* @param @param y
	* @param @return    参数
	* @return boolean    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 下午3:39:11
	 */
	private static boolean isNoisePoint(BufferedImage img, int x, int y) {

        return img.getRGB(x, y-1) == Color.WHITE.getRGB() 
                && img.getRGB(x, y+1) == Color.WHITE.getRGB()
                && img.getRGB(x+1, y) == Color.WHITE.getRGB()
                && img.getRGB(x-1, y) == Color.WHITE.getRGB()
                && img.getRGB(x+1, y+1) == Color.WHITE.getRGB()
                && img.getRGB(x+1, y-1) == Color.WHITE.getRGB()
                && img.getRGB(x-1, y-1) == Color.WHITE.getRGB()
                && img.getRGB(x-1, y+1) == Color.WHITE.getRGB();
    }
	
	 /**
	  * 
	 * @Title: isWhiteColor
	 * @Description: 取得指定位置的颜色是否为白色，如果超出边界，返回true;本方法是从removeInterference方法中摘取出来的。单独调用本方法无意义。
	 * @param @param image
	 * @param @param x
	 * @param @param y
	 * @param @return
	 * @param @throws Exception    参数
	 * @return boolean    返回类型
	 * @throws
	 *
	 * @author: Lin
	 * @date: 2020年6月18日 上午9:58:06
	  */
	 /*private static boolean isWhiteColor(BufferedImage image, int x, int y) throws Exception {
        if(x < 0 || y < 0) return true;
        if(x >= image.getWidth() || y >= image.getHeight()) return true;

        Color color = new Color(image.getRGB(x, y));
        
        return color.equals(Color.WHITE)?true:false;
	 }*/
	 
	/**
	 * 
	* @Title: isFontColor
	* @Description: 判断字体的颜色含义：正常可以用rgb三种颜色加起来表示，字与非字应该有显示的区别，找出来。
	* @param @param colorInt
	* @param @return    参数
	* @return boolean    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午10:05:34
	 */
	public static boolean isFontColor(int colorInt) {
        Color color = new Color(colorInt);

        return color.getRed() + color.getGreen() + color.getBlue() == 340;
    }
    
    public static int isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {
			return 1;
		}
		return 0;
	}

	public static int isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
			return 1;
		}
		return 0;
	}

	/**
	 * 
	* @Title: getColorBright
	* @Description: 获取RGB色号值
	* @param @param colorInt
	* @param @return    参数
	* @return int    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 下午3:37:41
	 */
	public static int getColorBright(int colorInt) {
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();

	}

	/**
	 * 
	* @Title: isBlackOrWhite
	* @Description: getColorBright(colorInt) < 80认为是黑色；getColorBright(colorInt) > 700认为是白色
	* @param @param colorInt
	* @param @return    参数
	* @return int    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 下午3:37:16
	 */
	public static int isBlackOrWhite(int colorInt) {
		if (getColorBright(colorInt) < 80 || getColorBright(colorInt) > 700) {
			return 1;
		}
		return 0;
	}
	 
	/**
	 * 
	* @Title: splitImage
	* @Description: 判断拆分验证码的标准：就是定义验证码中包含的各数字的x、y坐标值，及它们的宽度（width）、高度（height）
	* @param @param image
	* @param @return
	* @param @throws Exception    参数
	* @return List<BufferedImage>    返回类型
	* @throws
	*
	* @author: Lin
	* @date: 2020年6月18日 上午10:02:20
	 */
    private static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
        final int DIGIT_WIDTH = 14;
        final int DIGIT_HEIGHT = 23;
        List<BufferedImage> digitImageList = new ArrayList<BufferedImage>();
        digitImageList.add(img.getSubimage(10, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(img.getSubimage(24, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(img.getSubimage(38, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
        digitImageList.add(img.getSubimage(52, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(66, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(80, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
        return digitImageList;
    	/*List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth();
		int height = img.getHeight();
		List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (isBlack(img.getRGB(x, y)) == 1) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size(); i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			if (length > 18) {
				subImgs.add(removeBlank(img.getSubimage(i - length, 0,length / 2, height)));
				subImgs.add(removeBlank(img.getSubimage(i - length / 2, 0,length / 2, height)));
			} else if (length > 5) {
				subImgs.add(removeBlank(img.getSubimage(i - length, 0, length, height)));
			}
		}

		return subImgs;*/
    }

	private static List<BufferedImage> splitImage2(BufferedImage img) throws Exception {
		final int DIGIT_WIDTH = 15;
		final int DIGIT_HEIGHT = 23;
		List<BufferedImage> digitImageList = new ArrayList<BufferedImage>();
		digitImageList.add(img.getSubimage(15, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(25, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(35, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(45, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(55, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		digitImageList.add(img.getSubimage(65, 2, DIGIT_WIDTH, DIGIT_HEIGHT));
		return digitImageList;
    	/*List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth();
		int height = img.getHeight();
		List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (isBlack(img.getRGB(x, y)) == 1) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size(); i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			if (length > 18) {
				subImgs.add(removeBlank(img.getSubimage(i - length, 0,length / 2, height)));
				subImgs.add(removeBlank(img.getSubimage(i - length / 2, 0,length / 2, height)));
			} else if (length > 5) {
				subImgs.add(removeBlank(img.getSubimage(i - length, 0, length, height)));
			}
		}

		return subImgs;*/
	}
    
    public static BufferedImage removeBlank(BufferedImage img) throws Exception {
		int width = img.getWidth();
		int height = img.getHeight();
		int start = 0;
		int end = 0;
		Label1: for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (isBlack(img.getRGB(x, y)) == 1) {
					start = y;
					break Label1;
				}
			}
		}
		Label2: for (int y = height - 1; y >= 0; --y) {
			for (int x = 0; x < width; ++x) {
				if (isBlack(img.getRGB(x, y)) == 1) {
					end = y;
					break Label2;
				}
			}
		}
		return img.getSubimage(0, start, width, end - start + 1);
	}
}
