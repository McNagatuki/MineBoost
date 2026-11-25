#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f gradle/wrapper/gradle-wrapper.jar || ! -f gradlew ]]; then
  tmp=$(mktemp -d)
  trap 'rm -rf "$tmp"' EXIT
  curl --fail --location --retry 2 'https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.0/forge-1.20.1-47.4.0-mdk.zip' -o "$tmp/mdk.zip"
  unzip -q "$tmp/mdk.zip" 'gradlew' 'gradlew.bat' 'gradle/wrapper/*' -d "$tmp/mdk"
  cp "$tmp/mdk/gradlew" "$tmp/mdk/gradlew.bat" .
  mkdir -p gradle/wrapper
  cp "$tmp/mdk/gradle/wrapper/"* gradle/wrapper/
fi
chmod +x gradlew
./gradlew build "$@"
