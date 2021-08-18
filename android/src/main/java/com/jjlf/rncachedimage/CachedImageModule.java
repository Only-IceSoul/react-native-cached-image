// ReactNativeCachedImageModule.java

package com.jjlf.rncachedimage;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.UIManagerModule;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import static com.jjlf.rncachedimage.CachedImageView.*;

public class CachedImageModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public CachedImageModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ImageModule";
    }

    @ReactMethod
    public void getImage(ReadableMap data, final Callback callback){
        int w = ModUtil.getInt(data,"width",-1);
        int h = ModUtil.getInt(data,"height",-1);
        String mode = ModUtil.getString(data,"resizeMode",RESIZE_MODE_CONTAIN);
        boolean skipMemoryCache = ModUtil.getBoolean(data,"skipMemoryCache",false);
        DiskCacheStrategy diskCacheStrategy = getDiskCacheStrategy( ModUtil.getString(data,"diskCacheStrategy",DISK_CACHE_STRATEGY_AUTOMATIC));
        String uri =  ModUtil.getString(data,"uri",null);
        ReadableMap headers =  ModUtil.getMap(data,"headers",null);
        String prior =  ModUtil.getString(data,"priority",PRIORITY_NORMAL);
        Priority priority = Objects.equals(prior, PRIORITY_LOW) ? Priority.LOW : (Objects.equals(prior, PRIORITY_HIGH) ? Priority.HIGH : Priority.NORMAL);
        boolean resize = w > 0 && h > 0;

        final float quality = (float) ModUtil.getDouble(data,"quality",1.0);
        final String format =  ModUtil.getString(data,"format","png");

        RequestOptions options = new RequestOptions()
                .priority(priority)
                .diskCacheStrategy(diskCacheStrategy)
                .skipMemoryCache(skipMemoryCache);

        if(resize){
            options = Objects.equals(mode, RESIZE_MODE_COVER) ? options.centerCrop().override(w,h) : options.fitCenter().override(w,h);
        }

        RequestBuilder<Bitmap> manager = Glide.with(reactContext).asBitmap()
                .apply(options)
                .listener(
                new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        WritableMap mapFailed =  Arguments.createMap();
                        mapFailed.putString("error", e != null ? e.getMessage() : "");
                        mapFailed.putInt("width", -1);
                        mapFailed.putInt("height", -1);
                        mapFailed.putString("image", null);
                        callback.invoke(mapFailed);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        WritableMap mapSuccess =  Arguments.createMap();
                        mapSuccess.putString("error",  null );
                        mapSuccess.putInt("width",resource.getWidth());
                        mapSuccess.putInt("height",resource.getHeight());
                        try {
                            ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
                            int q = (int)(100f * quality);
                            if (format.equals("png")){
                                resource.compress(Bitmap.CompressFormat.PNG, q, bytesStream);
                            }else{
                                resource.compress(Bitmap.CompressFormat.JPEG, q, bytesStream);
                            }
                            byte[] bytes = bytesStream.toByteArray();
                            String str = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
                            mapSuccess.putString("image", str);
                        }catch (Exception ignored){
                            mapSuccess.putString("image", null);
                        }

                        callback.invoke(mapSuccess);
                        return false;
                    }
                }
        );

        if(headers != null){
            ReadableMapKeySetIterator iterator = headers.keySetIterator();
            LazyHeaders.Builder hh = new LazyHeaders.Builder();
            while (iterator.hasNextKey()){
                String key = iterator.nextKey();
                String value = headers.getString(key);
                hh.addHeader(key, value != null ? value : "");
            }
            manager = manager.load(new GlideUrl(uri,hh.build()));
        }else{
            manager = manager.load(uri);
        }
        manager.into(new CustomTarget<Bitmap>(){
            @Override
            public void onResourceReady(@NonNull  Bitmap resource, @Nullable Transition<? super Bitmap> transition) { }
            @Override
            public void onLoadCleared(@Nullable  Drawable placeholder) { }
        });

    }

    @ReactMethod
    public void requestImage(int tag,String format,float quality, Promise promise){
        try{
            CachedImageView view = getImageView(tag);
            if (view != null) {
                Bitmap.CompressFormat f = format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
                int q = (int)(quality * 100f);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Drawable d = view.getDrawable();
                if(ModUtil.toBitmap(d).compress(f,q,bytes)){
                    promise.resolve(android.util.Base64.encodeToString(bytes.toByteArray(), android.util.Base64.DEFAULT));
                }else{
                    throw new Error("failed to compress format "+ format);
                }

            } else {
                throw new Error("Expecting a CachedImageView, got: null" );
            }
        }catch (Exception e){
            promise.reject(e);
        }
    }
    @ReactMethod
    public void clear(int tag){
        final CachedImageView v =  getImageView(tag);
       if(v != null){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(reactContext).clear(v);
                }
            });
        }
    }
    @ReactMethod
    public void clearMemoryCache(final Promise promise){
       new Handler(Looper.getMainLooper()).post(new Runnable() {
           @Override
           public void run() {
               try {
                   Glide.get(reactContext).clearMemory();
                   promise.resolve(true);
               }catch (Exception e){
                   promise.reject(e);
               }
           }
        });
    }

    private CachedImageView getImageView(int tag) {
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        View view = uiManager.resolveView(tag);
        if (view.getClass() == CachedImageView.class) {
            return (CachedImageView) view;
        }
        return null;
    }

    private DiskCacheStrategy getDiskCacheStrategy(String strategy)  {

        if (strategy.equals(DISK_CACHE_STRATEGY_NONE)) { return DiskCacheStrategy.NONE; }
        else if (strategy.equals(DISK_CACHE_STRATEGY_ALL)) { return DiskCacheStrategy.ALL; }
        else if (strategy.equals(DISK_CACHE_STRATEGY_DATA)) { return DiskCacheStrategy.DATA; }
        else if (strategy.equals(DISK_CACHE_STRATEGY_RESOURCE)) { return DiskCacheStrategy.RESOURCE; }
        else {
            return DiskCacheStrategy.AUTOMATIC;
        }

    }
}
