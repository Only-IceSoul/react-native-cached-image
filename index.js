// main index.js

import NativeImage from './src/NativeCachedImage'
import NativeDrawableImage from './src/NativeDrawableImage'
import ImageModule from './src/ImageModule'
import { Platform } from 'react-native'

export const CachedImage = NativeImage
export const DrawableImage = Platform.OS === 'ios' ? NativeImage  : NativeDrawableImage
export const Controller = ImageModule