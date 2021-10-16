package com.jjlf.rncachedimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Objects;

public class CachedImage  extends View implements CachedInterface {
    static String EVENT_ON_LOAD_START = "onLoadStart";
    static String EVENT_ON_LOAD_END = "onLoadEnd";
    static String EVENT_ON_LOAD_ERROR = "onLoadError";
    static String EVENT_ON_LOAD_SUCCESS = "onLoadSuccess";
    static String RESIZE_MODE_CONTAIN = "contain";
    static String RESIZE_MODE_COVER = "cover";
    static String SCALE_TYPE_CONTAIN = "contain";
    static String SCALE_TYPE_COVER = "cover";
    static String DISK_CACHE_STRATEGY_AUTOMATIC = "automatic";
    static String DISK_CACHE_STRATEGY_NONE = "none";
    static String DISK_CACHE_STRATEGY_ALL = "all";
    static String DISK_CACHE_STRATEGY_DATA = "data";
    static String DISK_CACHE_STRATEGY_RESOURCE = "resource";
    static String PRIORITY_LOW = "low";
    static String PRIORITY_NORMAL = "normal";
    static String PRIORITY_HIGH = "high";

   private  Target<Bitmap> mTarget;
    private Bitmap mBitmapImage;
    private final Matrix mMatrix = new Matrix();
    private final RectF mRect = new RectF();
    private final RectF mBounds = new RectF();
    private String mAlign = "xMidYMid";
    private int mAspect = SVGViewBox.MOS_MEET;
    public CachedImage(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds.set(0f,0f,w,h);
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    public void setBackgroundColor(int color) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBitmapImage != null) {
            mRect.set(0f, 0f, mBitmapImage.getWidth(), mBitmapImage.getHeight());
            SVGViewBox.transform(mRect, mBounds, mAlign, mAspect, 1f, mMatrix);
            canvas.drawBitmap(mBitmapImage,mMatrix,null);
        }
    }

    protected float mTranslationZ = 0f;
    public void setTranslateZ(float v) {
        if(mTranslationZ != v) {
            mTranslationZ = v;
            setTranslationZ(mTranslationZ);
        }
    }

    public void setScaleType( String scaleType) {
        mAspect = Objects.equals(scaleType, "cover") ? SVGViewBox.MOS_SLICE : SVGViewBox.MOS_MEET;
        invalidate();
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmapImage;
    }

    @Override
    public void clear(){
        if(mTarget != null) Glide.with(getContext()).clear(mTarget);
    }

    public void setSrc(ReadableMap data){
        if(data != null) {
            int w = ModUtil.getInt(data,"width",-1);
            int h = ModUtil.getInt(data,"height",-1);
            String mode = ModUtil.getString(data,"resizeMode",RESIZE_MODE_CONTAIN);
            boolean skipMemoryCache = ModUtil.getBoolean(data,"skipMemoryCache",false);
            String diskCacheStrategy = ModUtil.getString(data,"diskCacheStrategy",DISK_CACHE_STRATEGY_AUTOMATIC);
            String uri =  ModUtil.getString(data,"uri",null);
            String placeholder = ModUtil.getString(data,"placeholder",null);
            ReadableMap headers =  ModUtil.getMap(data,"headers",null);
            String prior =  ModUtil.getString(data,"priority",PRIORITY_NORMAL);
            Priority priority = Objects.equals(prior, PRIORITY_LOW) ? Priority.LOW : (Objects.equals(prior, PRIORITY_HIGH) ? Priority.HIGH : Priority.NORMAL);
            boolean resize = w > 0 && h > 0;
            updateImage(uri,placeholder, skipMemoryCache,diskCacheStrategy, headers, priority,resize,w, h,mode);
        }else{
            clear();
        }
    }

    private  void updateImage(String url,String placeholder,Boolean cache,String diskCacheStrategy, ReadableMap headers,
                              Priority priority ,Boolean resize, int reqW, int reqH,String resizeMode){
        final WeakReference<ReactContext> reactContext = new WeakReference<>((ReactContext) getContext());

        RequestOptions options = getOptions(priority,cache,diskCacheStrategy,placeholder,resize,reqW,reqH,resizeMode);

        RequestBuilder<Bitmap> manager  =
                Glide.with(getContext())
                        .asBitmap()
                        .listener(
                                new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable  GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        WritableMap mapFailed =  Arguments.createMap();
                                        mapFailed.putString("error", e != null ? e.getMessage() : "");
                                        reactContext.get().getJSModule(RCTEventEmitter.class).receiveEvent(getId(), EVENT_ON_LOAD_ERROR,mapFailed);
                                        reactContext.get().getJSModule(RCTEventEmitter.class).receiveEvent(getId(), EVENT_ON_LOAD_END, Arguments.createMap());
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        WritableMap mapSuccess =  Arguments.createMap();
                                        mapSuccess.putInt("width",resource.getWidth());
                                        mapSuccess.putInt("height",resource.getHeight());
                                        reactContext.get().getJSModule(RCTEventEmitter.class).receiveEvent(getId(), EVENT_ON_LOAD_SUCCESS,mapSuccess);
                                        reactContext.get().getJSModule(RCTEventEmitter.class).receiveEvent(getId(), EVENT_ON_LOAD_END, Arguments.createMap());
                                        return false;
                                    }
                                }
                        );


        if (url != null && url.contains("base64,"))  {
            String s = url.split(",")[1];
            byte[] bytes = android.util.Base64.decode(s,android.util.Base64.DEFAULT);
            manager = manager.load(bytes);
        } else if (url != null && url.contains("static;") ){
            String s = url.split("c;")[1];

            if (s.contains("http") ) {
                manager =  manager.load(s);
            } else{
                int id = getContext().getResources().getIdentifier(s, "drawable", getContext().getPackageName());
                manager = manager.load(id);
            }

        }
        else{

            if(headers != null){
                ReadableMapKeySetIterator iterator = headers.keySetIterator();
                LazyHeaders.Builder h = new LazyHeaders.Builder();
                while (iterator.hasNextKey()){
                    String key = iterator.nextKey();
                    String value = headers.getString(key);
                    h.addHeader(key, value != null ? value : "");
                }
                manager = manager.load(new GlideUrl(url,h.build()));
            }else{
                manager = manager.load(url);
            }
        }

        reactContext.get().getJSModule(RCTEventEmitter.class).receiveEvent(getId(), EVENT_ON_LOAD_START, Arguments.createMap());
        mTarget = manager.apply(options)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mBitmapImage = resource;
                        invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        mBitmapImage = null;
                        setBackground(placeholder);
                        invalidate();
                    }
                });

    }

    public Target<Bitmap> getTarget(){
        return mTarget;
    }


    private  RequestOptions getOptions(Priority priority ,Boolean cache,String diskCacheStrategy, String placeholder,
                                       Boolean resize,int reqW, int reqH,String mode){

        DiskCacheStrategy ds = getDiskCacheStrategy(diskCacheStrategy);

        RequestOptions options = new RequestOptions()
                .skipMemoryCache(cache)
                .priority(priority)
                .diskCacheStrategy(ds);

        Bitmap image = load(placeholder);

        if(image != null){
            options = options.placeholder(new BitmapDrawable(getResources(), image));
        }
        return options;
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

    private Bitmap load(String model) {
        if (model == null || model.isEmpty()) return null;


        if (model.contains("base64,") ) {
            String s = model.split(",")[1];
            byte[] bytes = Base64.decode(s, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
        if(model.contains("static;")){
            String s = model.split("c;")[1];
            if(s.contains("http")) {
                try{
                    URL url = new URL(s);
                    return  BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }catch (Exception ignored ){
                    return null;
                }
            }


            int id = getContext().getResources().getIdentifier(s,"drawable", getContext().getPackageName());
            return ModUtil.toBitmap(ContextCompat.getDrawable(getContext(),id));
        }
        return null;

    }

}
