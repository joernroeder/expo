/// <reference types="expo/types" />
import { Pressable, type ViewProps } from 'react-native';

declare const props: ViewProps;

// Any component typed as ViewProps that forwards its props to a Pressable.
export const Broken = () => <Pressable {...props} />;
