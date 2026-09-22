# Repro: `expo-router` declares an `expo-source` export condition for a `src/` tree it does not publish

`jest-expo`'s presets always append `expo-source` to `customExportConditions`
(`jest-expo/config/getPlatformPreset.js`). Under that condition every `expo-router` subpath
resolves to `./src/...`, which is not in the published tarball, so no `expo-router` specifier
resolves under `jest-expo`.

Verified on `expo-router@58.0.5` and `58.0.6`.

## Run

```sh
npm install
bash repro.sh
```

## Expected

Each specifier resolves under both condition sets.

## Actual

```
--- files allowlist (no "src") ---
link android !android/.gitignore !android/build/** local-maven-repo assets build internal vendor ...

--- node_modules/expo-router/src present? ---
ls: cannot access 'node_modules/expo-router/src': No such file or directory

--- expo-router WITHOUT conditions ---
.../node_modules/expo-router/build/index.js
--- expo-router WITH jest-expo's conditions ---
Error: Cannot find module '.../node_modules/expo-router/src/index.tsx'

--- expo-router/react-navigation WITHOUT conditions ---
.../node_modules/expo-router/build/react-navigation/index.js
--- expo-router/react-navigation WITH jest-expo's conditions ---
Error: Cannot find module '.../node_modules/expo-router/src/react-navigation/index.ts'

--- expo-router/build/link/Link WITHOUT conditions ---
.../node_modules/expo-router/build/link/Link.js
--- expo-router/build/link/Link WITH jest-expo's conditions ---
Error: Cannot find module '.../node_modules/expo-router/src/link/Link.ts'
```

`expo-doctor` reports 20/20 on this project.

Note the last one: the `"./build/*"` entry maps to `./src/*.ts`, but the real source is
`src/link/Link.tsx`. 597 of expo-router's source files are `.tsx`, so that glob would still
miss them even if `src/` were published.
