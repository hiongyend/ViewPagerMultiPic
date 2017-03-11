package com.android.androidframework.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Author KINCAI
 * .
 * description TODO
 * .
 * Time 2016-12-17 18:56
 */
public class BitmapCompressUtils {
    static {
        System.loadLibrary("jpeg");// libjpeg
        System.loadLibrary("imagerar");// 我们自己的库
    }

    /**
     * 本地方法 JNI处理图片
     * LibJpeg库压缩图片
     *
     * @param bitmap
     *            bitmap
     * @param width
     *            宽度
     * @param height
     *            高度
     * @param quality
     *            图片质量 100表示不变 越小就压缩越严重
     * @param fileName
     *            文件路径的byte数组
     * @param optimize
     *            是否采用哈弗曼表数据计算
     * @return "0"失败, "1"成功
     */
    public static native String compressBitmap(Bitmap bitmap, int width,
                                               int height, int quality, byte[] fileName, boolean optimize);

    public static native String test();
    public static String s(){
        return "dd";
    }

    /**
     * 尺寸和质量压缩 没有oom处理
     *
     * @param bitmap
     *            bitmap
     * @param filePath
     *            文件路径
     */
    public static void compressBitmap(Bitmap bitmap, String filePath) {
        // 最大图片大小 150k
        int maxSize = 150;
        // 根据设定的最大分辨率获取压缩比例
        int ratio = getRatioSize(bitmap.getWidth(),
                bitmap.getHeight());

        int afterWidth = bitmap.getWidth() / ratio;
        int afterHeight = bitmap.getHeight() / ratio;
        // 根据比例压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(afterWidth, afterHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, afterWidth, afterHeight);
        canvas.drawBitmap(bitmap, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            // 重置baos
            baos.reset();
            options -= 10;
            result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }

        Log.e("ImageUtils", "save image " + options);

        // 保存图片 true表示使用哈夫曼算法
        saveBitmap(result, options, filePath, true);

        if (result != null && !result.isRecycled()) {
            result.recycle();
            result = null;
        }
    }

    /**
     * 尺寸和质量压缩 没有oom处理
     *
     * @param compressFilepath
     *            原始文件路径
     * @param filePath
     *            目标文件路径
     */
    public static void compressBitmap(String compressFilepath, String filePath) {
        // 最大图片大小 150KB
        int maxSize = 160;
        // 根据地址获取bitmap
        Bitmap result = getBitmapFromFile(compressFilepath);
        if(result == null)  return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int quality = 100;
        result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            // 重置baos即清空baos
            baos.reset();
            quality -= 6;
            result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        // 保存图片 true表示使用哈夫曼算法
        saveBitmap(result, quality, filePath, true);
        // 释放Bitmap
        if (!result.isRecycled()) {
            result.recycle();
        }

    }

    /**
     * 通过文件路径读获取Bitmap防止OOM以及解决图片旋转问题
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapFromFile(String filePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        BitmapFactory.decodeFile(filePath, newOpts);
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 获取尺寸压缩倍数
        newOpts.inSampleSize = getRatioSize(w, h);
        newOpts.inJustDecodeBounds = false;// 读取所有内容
        newOpts.inDither = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        newOpts.inTempStorage = new byte[32 * 1024];
        Bitmap bitmap = null;
        File file = new File(filePath);
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fs != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
                        newOpts);
                // 旋转图片
                int photoDegree = readPictureDegree(filePath);
                if (photoDegree != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(photoDegree);
                    // 创建新的图片
                    bitmap = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                    bitmap.getHeight(), matrix, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 读取旋转角度
     *
     * @param path
     *            文件路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 计算缩放比
     *
     * @param bitWidth
     *            图片宽度
     * @param bitHeight
     *            图片高度
     * @return 比例
     */
    public static int getRatioSize(int bitWidth, int bitHeight) {
        // 图片最大分辨率
        int imageHeight = 1288;
        int imageWidth = 966;
        // 缩放比
        int ratio = 1;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth > bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitHeight / imageHeight;
        }
        // 最小比率为1
        if (ratio <= 0)
            ratio = 1;
        return ratio;
    }

    /**
     * 经过java层压缩后再通过libjpeg库压缩
     *
     * @param bitmap
     *            bitmap
     * @param quality
     *            图片质量 100表示不变 越小就压缩越严重
     * @param fileName
     *            文件路径的byte数组
     * @param optimize
     *            是否采用哈弗曼表数据计算
     */
    private static void saveBitmap(Bitmap bitmap, int quality, String fileName,
                                   boolean optimize) {
        String code = compressBitmap(bitmap, bitmap.getWidth(),
                bitmap.getHeight(), quality, fileName.getBytes(), optimize);
        Log.e("ImageUtils", "code " + code);
    }
}
