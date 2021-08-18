// main index.js

import { requireNativeComponent } from 'react-native';
import ImageModule from './src/ImageModule'

export const Image = requireNativeComponent('Image',null)
export const Controller = ImageModule