
[![](https://jitpack.io/v/uizaio/uiza-android-player-sdk.svg)](https://jitpack.io/#uizaio/uiza-android-player-sdk)

[![Play Store](https://www.google.com/photos/about/static/images/badge_google_play_36dp.svg)](https://play.google.com/store/apps/details?id=com.uiza.sampleplayer)

## Welcome to UIZA Android Player SDK

Simple Streaming at scale.

Uiza is the complete toolkit for building a powerful video streaming application with unlimited scalability. We design Uiza so simple that you only need a few lines of codes to start streaming, but sophisticated enough for you to build complex products on top of it.

Read [CHANGELOG here](https://github.com/uizaio/uiza-android-sdk/blob/master/CHANGELOG.md).

## Importing the Library
**Step 1. Add the `JitPack` repository to your `build.gradle` file**

```xml
    allprojects {
          repositories {
             maven { url 'https://jitpack.io' }
          }
    }
```

**Step 2. Add the dependency**

```xml
    implementation 'com.github.uizaio:uiza-android-player-sdk:1.1.x'
```

For __Android Support Compat__ (Support until January 2021)

```
    implementation 'com.github.uizaio:uiza-android-player-sdk:1.0.x'
```

Get latest release number [HERE](https://github.com/uizaio/uiza-android-player-sdk/releases).


- Additionally, if you want to use the Chromecast feature, add the following dependencies to your project:

```xml
        // for ChromeCast
        implementation 'androidx.mediarouter:mediarouter:1.0.0'
        implementation 'com.google.android.gms:play-services-cast-framework:18.1.0'
```
if you use `android support compat`

```xml
        // for ChromeCast
        implementation 'com.android.support:mediarouter-v7:28.0.0'
        implementation 'com.google.android.gms:play-services-cast-framework:16.2.0' // from 17.x support androidx only
```

- If advertising support should be enabled, also add the following dependencies to your project:

```xml
        // for IMA Ads
        implementation 'com.google.android.exoplayer:extension-ima:2.10.8'
        implementation 'com.google.android.gms:play-services-ads:19.0.1' // from 18.x support androidx only
```

**Note:**
- The version of the ExoPlayer Extension IMA must match the version of the ExoPlayer library being used.
- If you are using both ChromeCast and IMA Ads dependencies, we recommend using dependency `com.google.android.gms:play-services-cast-framework:$version` with `version >= 18.1.0` or `version=16.2.0` (support compat) to avoid dependency version conflicts


Check [example here](https://github.com/uizaio/uiza-android-sdk/blob/master/sampleplayer/build.gradle).

**Turn on Java 8 support**

If not enabled already, you need to turn on Java 8 support in all `build.gradle` files depending on ExoPlayer, by adding the following to the `android` section:

```gradle
compileOptions {
  targetCompatibility JavaVersion.VERSION_1_8
}
```
> Node, Inside v1:
>
> - Use [androidx](https://developer.android.com/jetpack/androidx)
> - Use [Timber](https://github.com/JakeWharton/timber) for logger

## Init SDK

1. Init UZPlayer

     ```java
     public class App extends MultiDexApplication {
            @Override
            public void onCreate() {
                super.onCreate();
                UZPlayer.init(this);
            }
     }
     ```
3. If you want show log, install any `Tree` instances you want in the `onCreate` of your application class

```java
	if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
```

### Manifest

```xml
    <application
      android:name=".App"
    >
```

## How to play the video?:
**XML**

```xml
    <com.uiza.sdk.view.UZVideoView
      android:id="@id/uiza_video"
      android:layout_width="match_parent"
      android:layout_height="wrap_content" />
```
**JAVA**

```java
Create java file MainActivity:

    public class MainActivity extends AppCompatActivity implements UZCallback {
       ...
    }
```

Manifest

```xml
    <activity
      android:name=".MainActivity "
      android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode" />
```

In your `activity` or `fragment`

- Play with entity:

    ```java
    uzVideo = (UZVideoView) findViewById(R.id.uiza_video);
    uzVideo.setUZCallback(this);
    uzVideo.play(UZPlayback playback);
    // or 
    UZPlayer.setCurrentPlayback(UZPlayback playback);
    uzVideo.play();
    // or playlist
    uzVideo.play(List<UZPlayback> playlist);
    ```

Don't forget to add in activity life cycle event:

```java
    @Override
    public void onDestroy() {
        uzVideo.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        uzVideo.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        uzVideo.onPause();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uzVideo.onActivityResult(resultCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
```

## How to apply live viewers for UZPlayer

Please see [uiza-android-api-sdk](https://github.com/uizaio/uiza-android-api-sdk/blob/master/README.md) and see [sample](https://github.com/uizaio/uiza-android-player-sdk/blob/ef52e8a58bafc9398a560ff431c5b0054b61cd0f/sampleplayer/src/main/java/com/uiza/sampleplayer/PipPlayerActivity.java#L173)

```java
    private void getLiveViewsTimer(UZPlayback playback, boolean firstRun) {
        handler.postDelayed(() -> {
            UZApi.getLiveViewers(playback.getLinkPlay(), res -> {
                uzVideo.setLiveViewers(res.getViews());
            }, Timber::e, () -> {
                getLiveViewsTimer(playback, false);
            });
        }, firstRun ? 0 : 5000);
    }
```

## How to customize your skin?
Only 3 steps, you can customize everything about player skin.

**Step 1:**
Create layout ***uzplayer_skin_custom.xml*** like [THIS](https://github.com/uizaio/uiza-android-sdk/blob/master/sampleplayer/src/main/res/layout/uzplayer_skin_custom.xml):

Please note *`app:controller_layout_id="@layout/uz_controller_custom_layout"`*

**Step 2:**
Create layout ***uz_controller_custom_layout.xml*** like [THIS](https://github.com/uizaio/uiza-android-sdk/blob/master/sampleplayer/src/main/res/layout/uz_controller_custom_layout.xml):
- In this xml file, you can edit anything you like: position, color, drawable resouces...
- You can add more view (TextView, Button, ImageView...).
- You can remove any component which you dont like.
- Please note: Don't change any view `id`s  if you are using it.

**Step 3:**
On function `onCreate()` of `Activity`, put this code:

```java
    UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_custom);
```
Ex:
```java
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
		UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_custom);
        super.onCreate(savedState);
    }
```
**Note:** If you are using Chromecast, please use UZPlayer.setCasty(Activity activity) on function onCreate() of Activity
```java
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        UZPlayer.setCasty(this);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_custom);
        super.onCreate(savedState);
    }
```
Ex: findView from your custom layout:

```java
    TextView tvSample = uzVideo.findViewById(R.id.tv_sample);
```

That's enough! This code above will change the player's skin quickly. You can build and run your app now.

But if you wanna change the player's skin when the player is playing, please you this function:

```java
    uzVideo.changeSkin(R.layout.uzplayer_skin_custom);
```

***Note:***
- You should not change the id of the view.
Ex: `android:id="@id/player_view"`
Do not change `android:id="@id/player_view_0"` or `android:id="@+id/player_view_0"` ...

## Picture In Picture (PIP)

1. You can use `UZDragView`, review [`PlayerActivity`](https://github.com/uizaio/uiza-android-player-sdk/blob/master/sampleplayer/src/main/java/com/uiza/sampleplayer/PlayerActivity.java)

2. From `Android Nougat` (Android SDK >= 24) Google supported `PIP`. To implement,
in `AndroidManifest.xml` add `android:supportsPictureInPicture="true"` inside `Your Activity` and review [`PIPPlayerActivity`](https://github.com/uizaio/uiza-android-player-sdk/blob/master/sampleplayer/src/main/java/com/uiza/sampleplayer/PipPlayerActivity.java).

## R8 / ProGuard

___Do not support R8___

```xml
    buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        useProguard true
        proguardFiles = [
            getDefaultProguardFile('proguard-android.txt'),
            'proguard-rules.pro'
        ]
    }
    }
```

## For contributors

 Uiza Checkstyle configuration is based on the Google coding conventions from Google Java Style
 that can be found at [here](https://google.github.io/styleguide/javaguide.html).

 Your code must be followed the rules that defined in our [`uiza_style.xml` rules](https://github.com/uizaio/uiza-android-player-sdk/tree/master/configs/codestyle/uiza_style.xml)

 You can setting the rules after import project to Android Studio follow below steps:

 1. **File** > **Settings** > **Editor** > **Code Style**
 2. Right on the `Scheme`, select the setting icon > **Import Scheme** > **Intellij IDEA code style XML**
 3. Select the `uiza_style.xml` file path
 4. Click **Apply** > **OK**, then ready to go

 For apply check style, install [CheckStyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea), then

 1. **File** > **Settings** > **Other Settings** > **Checkstyle**
 2. In Configuration file, select the **`+`** icon
 3. Check `Use local checkstyle file` & select path to `uiza_check.xml` file
 4. Select **OK** & you're ready to go

 To run checkstyle for project

 1. Right click on project
 2. Select **Analyze** > **Inspect Code**


## Reference
[API Reference](https://uizaio.github.io/uiza-android-player-sdk/)

## Supported devices

Support all devices which have ***Android 5.0 (API level 21) above.***
For a given use case, we aim to support UizaSDK on all Android devices that satisfy the minimum version requirement.

**Note:** Some Android emulators do not properly implement components of Android’s media stack, and as a result do not support UizaSDK. This is an issue with the emulator, not with UizaSDK. Android’s official emulator (“Virtual Devices” in Android Studio) supports UizaSDK provided the system image has an API level of at least 23. System images with earlier API levels do not support UizaSDK. The level of support provided by third party emulators varies. Issues running UizaSDK on third party emulators should be reported to the developer of the emulator rather than to the UizaSDK team. Where possible, we recommend testing media applications on physical devices rather than emulators.

## Error message
Check this [class](https://github.com/uizaio/uiza-android-sdk/blob/master/uzplayer/src/main/java/com/uiza/sdk/exceptions/ErrorConstant.java) you can know error code and error message when use UZPlayer.

## Support

If you've found an error in this sample, please file an [issue ](https://github.com/uizaio/uiza-android-player-sdk/issues)

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub. Please feel free to contact me anytime: developer@uiza.io for more details.

Address: _33 Ubi Avenue 3 #08- 13, Vertex Tower B, Singapore 408868_
Email: _developer@uiza.io_
Website: _[uiza.io](https://uiza.io/)_

## License

UizaSDK is released under the BSD license. See  [LICENSE](https://github.com/uizaio/uiza-android-player-sdk/blob/master/LICENSE)  for details.
