//
//  new.swift
//  DoubleConversion
//
//  Created by Juan J LF on 8/23/21.
//

import Foundation
import UIKit
import JJGuiso

@objc(Image)
class Image: RCTViewManager {

    private let RESIZE_MODE_CONTAIN = "contain"
    private let RESIZE_MODE_COVER = "cover"
    private let PRIORITY_LOW = "low"
    private let PRIORITY_NORMAL = "normal"
    private let PRIORITY_HIGH = "high"
    private let DISK_CACHE_STRATEGY_ALL = "all"
    private let DISK_CACHE_STRATEGY_NONE = "none"
    private let DISK_CACHE_STRATEGY_AUTOMATIC = "automatic"
    private let DISK_CACHE_STRATEGY_DATA = "data"
    private let DISK_CACHE_STRATEGY_RESOURCE = "resource"
    
    static var CustomTargetList = [String:CustomTarget]()
    
    override func view() -> UIView! {
    
       return ImageView()
     }
    override static func requiresMainQueueSetup() -> Bool {
      return false
    }
    
    @objc func requestImage(_ tag:NSNumber,format:String,quality:CGFloat, resolve: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock){
      
         self.bridge?.uiManager?.addUIBlock { (_, views) in
            if let v = views?[tag] as? ImageView{
                
                if let re = format == "png" ? v.image?.pngData() : v.image?.jpegData(compressionQuality: quality){
                    resolve(re.base64EncodedString())
                    
                }else{
                    rejecter("Image", "failed to compress with Format \(format)", nil)
                }
               
               
            }else{
                rejecter("Image", "Expecting a CachedImageView , got: null", nil)
            }
        }
      
    }
    
    @objc func clear(_ tag:NSNumber){
         self.bridge?.uiManager?.addUIBlock { (_, views) in
            if let v = views?[tag] as? ImageView{
                Guiso.clear(target: v)
            }
        }
    }
    
    @objc func clearMemoryCache(_ resolve: RCTPromiseResolveBlock, rejecter:RCTPromiseRejectBlock){
        Guiso.cleanMemoryCache()
        resolve(true)
    }
    
    @objc func getImage(_ data: [String:Any]?, callback:@escaping RCTResponseSenderBlock){
        
        if data != nil{
            let w = data!["width"] as? Int ?? -1
            let h = data!["height"] as? Int ?? -1
            let mode = data!["resizeMode"] as? String  ?? RESIZE_MODE_CONTAIN
            let skipMemoryCache = data!["skipMemoryCache"] as? Bool ?? false
            let diskCacheStrategy = data!["diskCacheStrategy"] as? String ?? DISK_CACHE_STRATEGY_AUTOMATIC
            let headers = data!["headers"] as? [String:String]
            let prio = data!["priority"] as? String ?? PRIORITY_NORMAL
            let uri = data!["uri"] as? String

            let priority :Guiso.Priority = prio == PRIORITY_LOW ? .low : prio == PRIORITY_HIGH ? .high: .normal
            
            let resize = w > 0 && h > 0
            
            let quality = data!["quality"] as? CGFloat ?? 1
            let format = data!["format"] as? String ?? "png"
            
            var options = GuisoOptions().skipMemoryCache(skipMemoryCache)
                .priority(priority)
                .diskCacheStrategy(getDiskCacheStrategy(diskCacheStrategy))
            
            if headers != nil {
                options = options.header(GuisoHeader(headers!))
            }
            if resize {
                options = mode == RESIZE_MODE_COVER ? options.centerCrop().override(w,h)
                    : options.fitCenter().override(w,h)
            }
            
            if(uri != nil){
                if uri!.count <= 0 {
                    callback([["width": -1,
                               "height": -1,
                               "image": nil,
                               "error": "uri is nil or empty " ]])
                }else{
                    
                    let customTarget = CustomTarget(uri: uri!,format,quality,callback: callback)
                    Image.CustomTargetList[uri!] = customTarget
                    Guiso.load(model: uri!).apply(options)
                        .into(customTarget)
                }
                
                
            }else{
                callback([["width": -1,
                           "height": -1,
                           "image": nil,
                           "error": "uri is nil or empty " ]])
            }
            
           
            
        }else{
            callback([["width": -1,
                       "height": -1,
                       "image": nil,
                       "error": "data is null" ]])
        }
    }
    
    private func getDiskCacheStrategy(_ strategy:String) -> Guiso.DiskCacheStrategy{
        var s:Guiso.DiskCacheStrategy = .automatic
        switch strategy {
        case DISK_CACHE_STRATEGY_NONE:
            s = .none
        case DISK_CACHE_STRATEGY_ALL:
            s = .all
        case DISK_CACHE_STRATEGY_DATA:
            s = .data
        case DISK_CACHE_STRATEGY_RESOURCE:
            s = .resource
        default:
            s = .automatic
        }
        
        return s
    }
    
}
