package com.jjlf.rncachedimage;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class CachedImageViewManager extends SimpleViewManager<CachedImageView> {

    
    static String EVENT_ON_LOAD_START = "onLoadStart";
    static String EVENT_ON_LOAD_END = "onLoadEnd";
    static String EVENT_ON_LOAD_ERROR = "onLoadError";
    static String EVENT_ON_LOAD_SUCCESS = "onLoadSuccess";
        
    
    @ReactProp(name = "translateZ",defaultFloat = 0f)
    public void setTranslateZ(CachedImageView view ,float v) {
        view.setTranslateZ(v);
    }

    @ReactProp(name = "source")
    public void source(CachedImageView view, ReadableMap data)  {
        view.setSrc(data);
    }

    @ReactProp(name = "scaleType")
    public void scaleType(CachedImageView view, String scaleType) {
        if (scaleType.equals(CachedImageView.SCALE_TYPE_COVER)) {
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }




    @Override
    public Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
        return  MapBuilder.<String,Object>builder()
                .put(EVENT_ON_LOAD_START, MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", EVENT_ON_LOAD_START)))
                .put(EVENT_ON_LOAD_END, MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", EVENT_ON_LOAD_END)))
                .put(EVENT_ON_LOAD_ERROR, MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", EVENT_ON_LOAD_ERROR)))
                .put(EVENT_ON_LOAD_SUCCESS, MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", EVENT_ON_LOAD_SUCCESS)))
                .build();
    }

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    protected CachedImageView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new CachedImageView(reactContext);
    }
}