# WatermelonDS Wide (WideMelon widescreen 3D)

Personal build of WatermelonDS with the WideMelon widescreen 3D view, aimed at the AYN Thor's
"Internal: Top, External: Bottom" dual-screen preset (e.g. Pokémon Black 2 / White 2).

- The top screen's **3D** is rendered wider (even widths 256–768); 2D, menus and the touchscreen stay
  native 256×192 in the middle.
- The wide top screen is shown on whichever display shows the DS top screen; the bottom screen stays
  4:3 and touch mapping is unchanged.
- Renderers: **OpenGL** and **Compute**. Software and **Vulkan** (incl. FastPath) always run at native
  256; the setting is hidden for them.
- Built-in filters and RetroArch shader presets (OpenGL/Compute) are width-aware.
- App ID `com.sapphire.watermelonds.wide` (`.dev` suffix on debug builds), name "WatermelonDS Wide":
  installs next to stock WatermelonDS and other melonDS builds.

## Using it

Settings → Video → Renderer **OpenGL** or **Compute** → **Widescreen 3D view (WideMelon)**:

| Value | Aspect | Notes |
|-------|--------|-------|
| Off (256) | 4:3 | Stock behaviour |
| 320 | 5:3 | widemelon-android "Performance" preset width |
| **342** | **16:9** | Exact fit for the Thor's 1920×1080 top display |
| 384 | 2:1 | widemelon-android default / "Balanced" / "Quality" width |
| 448 – 768 | 7:3 – 4:1 | 512 is widemelon-android's "Max wide" width |

Like renderer and internal resolution, it can't be changed in-game: change it from the main settings
and (re)start the game. GPU cost grows with width × internal resolution.

Expected limitations (game logic, not bugs): objects outside the original field of view may pop in at
the screen edges (B2W2 overworld), and scenes that are 2D or that the game renders at 4:3 (battles,
menus) stay 4:3 with black side bars. RetroAchievements hardcore mode may reject this modified build.

## Building from a clean clone (macOS)

Toolchain (versions from `buildSrc/src/main/kotlin/AppConfig.kt` and `app/build.gradle.kts`):
JDK 21, Android SDK 36, NDK `28.0.13004108`, CMake `3.22.1`, Rust stable with the Android targets
(librashader is built from source). Gradle 9.5 comes with the wrapper.

```sh
brew install openjdk@21 rustup
brew install --cask android-commandlinetools
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:/opt/homebrew/opt/rustup/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
rustup default stable
yes | sdkmanager --sdk_root=$ANDROID_HOME --licenses
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-36" "build-tools;36.0.0" "ndk;28.0.13004108" "cmake;3.22.1"

git clone --recurse-submodules -b widescreen https://github.com/cngjd/WatermelonDS.git
cd WatermelonDS
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleGitHubProdRelease -PmelonAbis=arm64-v8a
```

`-PmelonAbis=arm64-v8a` only builds the ABI the Thor needs (omit it to build all three). Use the **release**
variant for playing: without a keystore in `local.properties` it is signed with the debug key. The debug variant
compiles the native emulator unoptimized and is much slower.
The APK is written to `app/build/outputs/apk/gitHubProd/release/app-gitHub-prod-release.apk`:

```sh
adb install -r app/build/outputs/apk/gitHubProd/release/app-gitHub-prod-release.apk
```

The build regenerates the Vulkan SPIR-V headers in `melonDS-android-lib` (`regenerateVulkanSpirv`,
same as stock WatermelonDS), which can leave those generated files modified in the submodule.

## Staying up to date

The `widemelon-sync` GitHub workflow (daily, or manually from the Actions tab) rebases the `widescreen` branch
onto the latest SapphireRhodonite/WatermelonDS `Develop`, and the `lib-widescreen` branch (melonDS-android-lib
WideMelon commits, stored in this same repository) onto the lib commit upstream pins. It then builds the release APK
signed with the fixed key from the `WIDE_KEYSTORE`/`WIDE_KEYSTORE_PASSWORD` secrets, runs the unit tests, pushes
both branches and publishes a GitHub release. On a conflict or a failure nothing is pushed and an issue is opened.
It also opens an issue when manijose1919/widemelon-android has commits newer than
`.github/widemelon-android-reviewed`. The same rebase can be run locally with `scripts/widemelon-sync.sh`.

## What changed (for maintainers)

Native core (`melonDS-android-lib`, branch `widescreen`): `WideMelon.h` (width + projection helpers),
`GPU3D.cpp` (X projection in `SubmitVertex`/`BoxTest`), OpenGL and Compute renderers (wide framebuffer,
X mapping, centered display capture), GL compositor (top-screen wings, bottom side columns black).

App: width setting → `RendererConfiguration.effectiveWidescreenViewWidth` → JNI → `MelonInstance`
(`WideMelon::SetWidth`, forced to 256 for Software/Vulkan, renderer recreated on change);
`ScreenTextureUVs` + `DSRenderer` / `ExternalLayoutRender` / `ExternalScreenRender` (wide top rect,
cropped bottom); width-aware built-in filters; width-aware `OpenGlRetroArchFilter`; thumbnails cropped
to native 256. Save and savestate formats are unchanged.

## Credits and license

GPL-3.0-or-later, like everything it is built from:

- [melonDS](https://github.com/melonDS-emu/melonDS) by Arisotura and contributors
- [melonDS Android](https://github.com/rafaelvcaetano/melonDS-android) by rafaelvcaetano
- [WatermelonDS](https://github.com/SapphireRhodonite/WatermelonDS) by SapphireRhodonite and contributors
- [WideMelon](https://github.com/pruefsumme/widemelon) by pruefsumme: the widescreen projection/compositing approach
- [widemelon-android](https://github.com/manijose1919/widemelon-android) by manijose1919: the Android port this is adapted from
