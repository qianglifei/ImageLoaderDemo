package com.example.administrator.imageloaderdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ImageLoader {
    private static final String TAG = "ImageLoader";
    public static final int MESSAGE_POST_RESULT = 1;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final  int CORE_POOR_SIZE = CPU_COUNT + 1;

    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;

    //private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int IO_BUFFER_SIZE = 1024 * 8;
    private static final int DISK_BUFFER_INDEX = 0;
    private static final int TAG_KEY_URI = 1;

    private boolean mIsDiskLruCacheCreated = false;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger();
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable,"ImageLoader#" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(CORE_POOR_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE,
                    TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),sThreadFactory);

    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoaderResult result = (LoaderResult) msg.obj;

            ImageView imageView = result.mImageView;
            String uri = result.uri;
            if (uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else {
                Log.w(TAG, "set image bitmap ,but url has change,ignored!");
            }
        }
    };

    private Context mContext;
    private ImageResizer mImageResizer = new ImageResizer();
    private LruCache<String,Bitmap> mMemoryChche;
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context){
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        mMemoryChche = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };

        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if (!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }

        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * build a new instance of ImageLoader
     * @param context
     * @return a new instance of ImageLoader
     */
    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemCache(key) == null){
            mMemoryChche.put(key,bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryChche.get(key);
    }

    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight){
        imageView.setTag(TAG_KEY_URI,uri);
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri,reqWidth,reqHeight);
                if (bitmap != null){
                    LoaderResult result = new LoaderResult(imageView,uri,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };

        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    private Bitmap loadBitmap(String uri,int reqWidth,int reqHeight){
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if (bitmap != null){
            Log.i(TAG, "loadBitmapFromMemCache: " + uri);
            return bitmap;
        }
    }

    private Bitmap loadBitmapFromMemCache(String uri) {
    }




    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSize() * statFs.getAvailableBlocks();
    }

    private File getDiskCacheDir(Context mContext, String uniqueName) {
        boolean externalStorageAvailable = Environment.
                getExternalStorageState().
                equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable){
            cachePath = mContext.getExternalCacheDir().getPath();
        }else {
            cachePath = mContext.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }


    private static class LoaderResult{
        public ImageView mImageView;
        public String uri;
        public Bitmap bitmap;
        public LoaderResult(ImageView imageView,String uri,Bitmap bitmap){
            mImageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }
}
