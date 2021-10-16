import React from "react"
import { NativeMethods } from "react-native"
import { Constructor } from "react-native"
import { ViewProps } from "react-native"

type CompressFormat = "jpeg" | "png"
type Priority = "low" | "normal" | "high"
type ResizeMode = "contain" | "cover"
type DiskCacheStrategy = "automatic" | "all" | "none" | "data" | "resource"
type ScaleType = "contain" | "cover"
type ImageError = {
    error?:string | null
}
type ImageSuccess = {
    width?:number,
    height?:number
}

type ImageResult = {
    image?:string
}
type EventImageError = {
    nativeEvent?:ImageError | null,
}

type EventImageSuccess = {
    nativeEvent?:ImageSuccess | null,
}

interface ImageCallBack extends ImageSuccess , ImageError , ImageResult {

}

interface SourceBase {
    uri?:string
    headers?:Object | null,
    priority?:Priority,
    width?:number | null,
    height?:number | null,
    resizeMode?: ResizeMode,
    skipMemoryCache?:boolean | null,
    diskCacheStrategy?:DiskCacheStrategy,
}

interface ImageSource extends SourceBase{
    asGif?:boolean | null,
    placeholder?:string | null,
}

interface ImageProps extends ViewProps {
   source: ImageSource | null
   scaleType?:ScaleType;
   translateZ?:number
   onLoadStart?:() => void | null;
   onLoadEnd?:() => void | null;
   onLoadError?:(event:EventImageError) => void | null;
   onLoadSuccess?:(event:EventImageSuccess) => void | null;
}



declare class ImageComponent extends React.Component<ImageProps> {}
declare const ImageBase: Constructor<NativeMethods> & typeof ImageComponent;

export class CachedImage extends ImageBase {}
export class DrawableImage extends ImageBase {}

export class Controller {


    static clearMemoryCache():Promise<boolean>
    static requestImage(ref:CachedImage | DrawableImage | undefined | null,format?:CompressFormat,quality?:number): Promise<string>
    static clear(ref: CachedImage | DrawableImage | undefined | null): void
    static get(data: SourceBase, cb:(result:ImageCallBack)=> void): void

}
