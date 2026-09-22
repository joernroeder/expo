# Repro: `expo/types/react-native-web.d.ts` redeclares `PressableProps.style`, breaking `ViewProps` → `Pressable`

`expo`'s `declare module 'react-native'` block redeclares `PressableProps.style` as
`StyleProp<ViewStyle> | ((state) => StyleProp<ViewStyle>)`. Interface merging makes that
declaration win over RN 0.88's own `____ViewStyleProp_Internal`. `ViewProps.style` is not
augmented, so `View` and `Pressable` no longer agree and any `ViewProps` value spread into a
`Pressable` fails to typecheck.

## Run

```sh
npm install
npx tsc --noEmit                        # TS2322 at repro.tsx
npx tsc --noEmit --skipLibCheck false    # TS2430 inside react-native's own d.ts
```

## Expected

Both clean.

## Actual

```
repro.tsx(7,30): error TS2322: Type '{ className?: string | undefined; ... }' is not assignable
  to type 'Omit<PressableProps, "ref">'.
  Types of property 'style' are incompatible.
    Type '____ViewStyleProp_Internal | undefined' is not assignable to type
      'StyleProp<ViewStyle> | ((state: PressableStateCallbackType) => StyleProp<ViewStyle>) | undefined'.
      ...
          Types of property 'backgroundImage' are incompatible.
            Type 'string | readonly BackgroundImageValue[] | undefined' is not assignable to type 'string | undefined'.
```

With `--skipLibCheck false` TypeScript names the real cause:

```
node_modules/react-native/types_generated/Libraries/Components/Pressable/Pressable.d.ts(141,18):
  error TS2430: Interface 'PressableProps' incorrectly extends ...
node_modules/react-native/types_generated/Libraries/StyleSheet/StyleSheet.d.ts(109,18):
  error TS2430: Interface 'ViewStyle' incorrectly extends ...
node_modules/react-native/types_generated/Libraries/StyleSheet/StyleSheet.d.ts(126,18):
  error TS2430: Interface 'TextStyle' incorrectly extends ...
node_modules/react-native/types_generated/Libraries/Text/TextProps.d.ts(175,18):
  error TS2430: Interface 'TextProps' incorrectly extends ...
```

`expo/tsconfig.base` sets `skipLibCheck: true`, which hides all four.

## Confirming expo is the source

Delete the `/// <reference types="expo/types" />` line from `repro.tsx` and `tsc --noEmit`
passes. Or delete only lines 255-257 of `node_modules/expo/types/react-native-web.d.ts`
(the `style?:` member of the `PressableProps` augmentation) and it also passes.
