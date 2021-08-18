package com.jjlf.rncachedimage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

class ModUtil {

    static float clamp(float v){
        return v > 1 ? 1 : (v < 0 ? 0 : v);
    }
    static ReadableMap getMap(ReadableMap m, String name, ReadableMap optional){
        if(m != null){

            try{
                ReadableMap v = m.getMap(name);
                if(v != null){
                    return v;
                }
            }catch (Exception ignored){
                return optional;
            }
        }
        return optional;
    }
    static String getString(ReadableMap m, String name, String optional){
        if(m != null){
            try{
                String v = m.getString(name);
                if(v != null){
                    return v;
                }
            }catch (Exception ignored){
                return optional;
            }

        }
        return optional;
    }
    static double getDouble(ReadableMap m, String name, double optional){
        if(m != null){
            try{
                return m.getDouble(name);
            }catch (Exception ignored){
                return optional;
            }
        }
        return optional;
    }
    static int getInt(ReadableMap m, String name, int optional){
        if(m != null){
            try{
                return m.getInt(name);
            }catch (Exception ignored){
                return optional;
            }
        }
        return optional;
    }
    static boolean getBoolean(ReadableMap m, String name, boolean optional){
        if(m != null){
            try{
                return m.getBoolean(name);
            }catch (Exception ignored){
                return optional;
            }

        }
        return optional;
    }
    static ReadableArray getArray(ReadableMap m, String name, ReadableArray optional){
        if(m != null){
            try{
                ReadableArray v = m.getArray(name);
                if(v != null){
                    return v;
                }
            }catch (Exception ignored){
                return optional;
            }
        }
        return optional;
    }

    static int[] toIntArray(ReadableArray colors){
        int[] list = new int [colors.size()];
        for(int i = 0 ;   i < colors.size() ; i++){
            list[i] = colors.getInt(i);
        }
        return list;
    }
    static float[] toFloatArray(ReadableArray colors){
        float[] list = new float [colors.size()];
        for(int i = 0 ;   i < colors.size() ; i++){
            list[i] = (float) colors.getDouble(i);
        }
        return list;
    }

    static Bitmap toBitmap(Drawable drawable){
        if(drawable == null){
            return null;
        }
        if (drawable.getClass() == BitmapDrawable.class) {
              return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width,height,  Bitmap.Config.ARGB_8888);
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }
}
