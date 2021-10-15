// main index.js

import { requireNativeComponent } from 'react-native';
import ImageModule from './src/ImageModule'

export const CachedImage = requireNativeComponent('Image',null)
export const Controller = ImageModule