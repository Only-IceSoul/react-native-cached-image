import { findNodeHandle, NativeModules } from 'react-native';

const Image = NativeModules.ImageModule

export default ImageModule = {
    
      clearMemoryCache:() => {
        return new Promise((resolve, reject) => {
            PhotoKit.clearMemoryCache().then(r=>{
                resolve(r)
            }).catch(e => {
                reject(e)
            })
           
       })
      },
      requestImage: (ref,format,quality) =>{
        return new Promise((resolve, reject) => {
            let f = format ? format : 'png'
            let q = quality ? quality : 1
            Image.requestImage(findNodeHandle(ref),f,q).then(r=>{
                resolve(r)
            }).catch(e => {
                reject(e)
            })
           
       })
      } ,
      clearImage: (ref)=>{
          if(ref){
            Image.clear(findNodeHandle(ref))
          }
      },
      get:(data,cb)=>{
          Image.getImage(data,cb)
      }
}