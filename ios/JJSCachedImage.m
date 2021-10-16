//
//  Image.m
//  react-native-cached-image
//
//  Created by Juan J LF on 8/23/21.
//


#import <Foundation/Foundation.h>
#import "React/RCTViewManager.h"
#import <React/RCTBridgeModule.h>


@interface


RCT_EXTERN_MODULE(JJSCachedImage,RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(translateZ, NSNumber)

RCT_EXPORT_VIEW_PROPERTY(source, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(scaleType, NSString)

RCT_EXPORT_VIEW_PROPERTY(onLoadStart, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadSuccess, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadEnd, RCTDirectEventBlock)

RCT_EXTERN_METHOD(requestImage:(nonnull NSNumber)tag format:(NSString)format quality:(CGFloat)quality resolve:(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(clear:(nonnull NSNumber)tag)

RCT_EXTERN_METHOD(clearMemoryCache:
(RCTPromiseResolveBlock)resolve
rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getImage:(NSDictionary)data callback:(RCTResponseSenderBlock)callback)

@end
