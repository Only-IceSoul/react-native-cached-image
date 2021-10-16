//
//  CustomTarget.swift
//  react-native-cached-image
//
//  Created by Juan J LF on 8/23/21.
//

import Foundation
import UIKit
import JJGuiso

class CustomTarget: ViewTarget {
    
    
    var mCallBack :RCTResponseSenderBlock
    var mUri = ""
    var mFormat = "png"
    var mQuality: CGFloat = 1
    init(uri:String ,_ format:String,_ quality:CGFloat, callback :@escaping RCTResponseSenderBlock) {
        mCallBack = callback
        mUri = uri
        mFormat = format
        mQuality = quality
    }
    
    
    public func onFallback() {
      
    }
    
    //error, placeholder, fallback
    public func onHolder(_ image: UIImage?) {
       
    }
 
    
    public func onResourceReady(_ gif: AnimatedLayer) {
   
        mCallBack([["width": -1,
                    "height": -1,
                    "image": nil,
                    "error": "Expected a image , got AnimatedLayer"]])
                    removeFromList()
    }
    
    public func onResourceReady(_ img: UIImage) {

        if let re = mFormat == "png" ? img.pngData() : img.jpegData(compressionQuality: mQuality){
            mCallBack([["width": img.cgImage?.width ?? 0,
                        "height": img.cgImage?.height ?? 0,
                        "image": re.base64EncodedString(),
                        "error": nil ]])
            
        }else{
            mCallBack([["width": img.cgImage?.width ?? 0,
                        "height": img.cgImage?.height ?? 0,
                        "image": nil,
                        "error": "failed to compress with Format \(mFormat)" ]])
        }
       
     
        removeFromList()
    }
    

 public func onLoadFailed(_ error:String) {
    mCallBack([["width": -1,
                "height": -1,
                "image": nil,
                "error": error]])
                removeFromList()
    }
  
    
    private var mRequest: GuisoRequest?
    public func setRequest(_ tag:GuisoRequest?) {
        mRequest = tag
    }
    public func getRequest() -> GuisoRequest?{
        return mRequest
    }
   

    public func getContentMode() -> UIView.ContentMode {
        return UIView.ContentMode.scaleAspectFit
    }
    
    public func removeFromList(){
        JJSCachedImage.CustomTargetList[mUri] = nil
    }
}
