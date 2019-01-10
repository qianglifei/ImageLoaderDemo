package com.example.administrator.imageloaderdemo;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/***
 *
 * 单独抽象一个类，做图片的压缩
 */
public class ImageResizer {
    public ImageResizer() {

    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,int reqWidth,int reqHeight){
        //通过BitmapFactory.Options 来缩放图片， 通过inSampleSize 即采样率
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //解码bitmap时可以只返回其高、宽和Mime类型，而不必为其申请内存，从而节省了内存空间。
        options.inJustDecodeBounds = true;
        //开始加载照片
        BitmapFactory.decodeResource(res,resId,options);

        //imSampleSize
        options.inSampleSize = calculateInSample(options,reqWidth,reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res,resId,options);
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd,int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fd,null,options);

        //Calculate inSampleSize
        options.inSampleSize = calculateInSample(options,reqWidth,reqHeight);
        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    private int calculateInSample(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0){
            return 1;
        }

        //Raw width and height of image

        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth){
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
