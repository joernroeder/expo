#!/usr/bin/env bash
# Resolution probe. `expo-source` is the condition jest-expo appends in
# jest-expo/config/getPlatformPreset.js.

echo "--- files allowlist (no \"src\") ---"
node -p "require('expo-router/package.json').files.join(' ')"

echo
echo "--- node_modules/expo-router/src present? ---"
ls node_modules/expo-router/src 2>&1 | head -1

for spec in expo-router expo-router/react-navigation expo-router/build/link/Link; do
  echo
  echo "--- $spec WITHOUT conditions ---"
  node -e "console.log(require.resolve('$spec'))" 2>&1 | head -2
  echo "--- $spec WITH jest-expo's conditions ---"
  node --conditions=react-native --conditions=expo-source \
    -e "console.log(require.resolve('$spec'))" 2>&1 | grep -E "Cannot find module|expo-router" | head -2
done
