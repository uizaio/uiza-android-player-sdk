
## Welcome to UIZA Android SDK

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
    defaultConfig {
        multiDexEnabled  true
    }
    dependencies {
        // for playing VOD, LIVE video
        implementation 'com.uiza.uiza-android-sdk:uzplayer:[latest-release-number]'
        // for broadcasting / live streaming
        implementation 'com.uiza.uiza-android-sdk:uzbroadcast:[latest-release-number]'
    }
```

Get latest release number [HERE](https://github.com/uizaio/uiza-android-sdk/releases).

If you are using `uiza-android-sdk` (Version 1.0.0 and above), you will need to import dependencies:

```xml
    // for playing VOD, LIVE video
    implementation 'com.uiza.uiza-android-sdk:uzplayer:1.x.x'
    implementation 'com.google.android.exoplayer:exoplayer:2.10.8'
    implementation 'com.google.android.exoplayer:exoplayer-dash:2.10.8'
    implementation 'com.google.android.exoplayer:exoplayer-ui:2.10.8'
```

- Additionally, if you want to use the Chromecast feature, add the following dependencies to your project:

```xml
        // for ChromeCast
        implementation 'androidx.mediarouter:mediarouter:1.0.0'
        implementation 'com.google.android.gms:play-services-cast-framework:18.1.0'
```

- If advertising support should be enabled, also add the following dependencies to your project:

```xml
        // for IMA Ads
        implementation 'com.google.android.exoplayer:extension-ima:2.10.8'
        implementation 'com.google.android.gms:play-services-ads:19.0.0'
```

**Note:**
- The version of the ExoPlayer Extension IMA must match the version of the ExoPlayer library being used.
- If you are using both ChromeCast and IMA Ads dependencies, we recommend using dependency 'com.google.android.gms:play-services-cast-framework:$version' with version >= 18.0.0 to avoid dependency version conflicts


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

1. appId : generate [HERE](http://id.com.uiza.io/register).
2. apiUrl : default is `api.com.uiza.sh`

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
    uzVideo.play("UZPlaybackInfo");
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

## How to customize your skin?
Only 3 steps, you can customize everything about player skin.

**Step 1:**
Create layout ***uiza_controller_skin_custom_main.xml*** like [THIS](https://github.com/uizaio/uiza-android-sdk/blob/master/sampleplayer/src/main/res/layout/uiza_controller_skin_custom_main.xml):

Please note *`app:controller_layout_id="@layout/uiza_controller_skin_custom_detail"`*

**Step 2:**
Create layout ***uiza_controller_skin_custom_detail.xml*** like [THIS](https://github.com/uizaio/uiza-android-sdk/blob/master/sampleplayer/src/main/res/layout/uiza_controller_skin_custom_detail.xml):
- In this xml file, you can edit anything you like: position, color, drawable resouces...
- You can add more view (TextView, Button, ImageView...).
- You can remove any component which you dont like.
- Please note: Don't change any view `id`s  if you are using it.

**Step 3:**
On function `onCreate()` of `Activity`, put this code:

```java
    UZPlayer.setCurrentPlayerId(R.layout.uiza_controller_skin_custom_main);
```
Ex:
```java
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
		UZPlayer.setCurrentPlayerId(R.layout.uiza_controller_skin_custom_main);
        super.onCreate(savedState);
    }
```
**Note:** If you are using Chromecast, please use UZPlayer.setCasty(Activity activity) on function onCreate() of Activity
```java
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        UZPlayer.setCasty(this);
        UZPlayer.setCurrentPlayerId(R.layout.uiza_controller_skin_custom_main);
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
    uzVideo.changeSkin(R.layout.uiza_controller_skin_custom_main);
```

This sample help you know how to customize player's skin, please refer to  [THIS](https://github.com/uizaio/uiza-android-sdk/tree/master/sampleplayer/src/main/java/com/uiza/sampleplayer/customskin)

***Note:***
- You should not change the id of the view.
Ex: `android:id="@id/player_view"`
Do not change `android:id="@id/player_view_0"` or `android:id="@+id/player_view_0"` ...

## How to broadcast with UizaSDK?:
It's very easy, plz follow these steps below to implement:

XML:

```xml
    <com.uiza.sdk.view.UZBroadCastView
      android:id="@+id/uz_broadcast"
      android:layout_width="match_parent"
      android:layout_height="match_parent" />
```

In class [`LivePortraitActivity`](https://github.com/uizaio/uiza-android-sdk/blob/master/samplebroadcast/src/main/java/com/uiza/samplebroadcast/UZBroadCastActivity.java):
```java
    public class UZBroadCastActivity extends AppCompatActivity implements UZBroadCastListener {
        // ...
    }
```
In `onCreate()`:

```java
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    uzBroadCast = (UZBroadCastView) findViewById(R.id.uz_broadcast);
    uzBroadCast.setUzLivestreamCallback(this);
```

In `onResume()`:

```java
    @Override
    protected void onResume() {
        uzBroadCast.onResume();
        super.onResume();
    }
```
Start a `portrait` livestream:

```java
    if (uzBroadCast.prepareStream()) {
        uzBroadCast.startStream(liveStreamUrl);
    }
```

To stream in landscape mode, use `uzBroadCast.prepareStream(true)` instead.

Start a livestream and save to MP4 file:

```java
	uzBroadCast.setProfile(ProfileVideoEncoder.P720);
    if (uzBroadCast.prepareStream()) {
        uzBroadCast.startStream("streamUrl");
    }
```

Stop streaming (It auto saves mp4 file in your gallery if you start a livestream with option save local file)

```java
    uzBroadCast.stopStream();
```

Switch camera:

```java
    uzBroadCast.switchCamera();
```
Allows streaming again after back from background:

```java
    uzBroadCast.setBackgroundAllowedDuration(YOUR_ALLOW_TIME); // default time is 2 minutes
```

This sample help you know how to use all Uiza SDK for livestream, please refer to  [THIS](https://github.com/uizaio/uiza-android-sdk/tree/master/samplebroadcast)

## For contributors

 Uiza Checkstyle configuration is based on the Google coding conventions from Google Java Style
 that can be found at [here](https://google.github.io/styleguide/javaguide.html).

 Your code must be followed the rules that defined in our [`uiza_style.xml` rules](https://github.com/uizaio/uiza-android-sdk/tree/master/configs/codestyle/uiza_style.xml)

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


## Docs
[Docs](https://uizaio.github.io/uiza-android-sdk/)

## Supported devices

Support all devices which have ***Android 5.0 (API level 21) above.***
For a given use case, we aim to support UizaSDK on all Android devices that satisfy the minimum version requirement.

**Note:** Some Android emulators do not properly implement components of Android’s media stack, and as a result do not support UizaSDK. This is an issue with the emulator, not with UizaSDK. Android’s official emulator (“Virtual Devices” in Android Studio) supports UizaSDK provided the system image has an API level of at least 23. System images with earlier API levels do not support UizaSDK. The level of support provided by third party emulators varies. Issues running UizaSDK on third party emulators should be reported to the developer of the emulator rather than to the UizaSDK team. Where possible, we recommend testing media applications on physical devices rather than emulators.

## Error message
Check this [class](https://github.com/uizaio/uiza-android-sdk/blob/master/uzplayer/src/main/java/com/uiza/sdk/exceptions/UZException.java) you can know error code and error message when use UizaSDK.

## Support

If you've found an error in this sample, please file an [issue ](https://github.com/uizaio/uiza-android-sdk/issues)

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub. Please feel free to contact me anytime: developer@com.uiza.io for more details.

Address: _33 Ubi Avenue 3 #08- 13, Vertex Tower B, Singapore 408868_
Email: _developer@com.uiza.io_
Website: _[com.uiza.io](http://com.uiza.io/)_

## License

UizaSDK is released under the BSD license. See  [LICENSE](https://github.com/uizaio/uiza-android-sdk/blob/master/LICENSE)  for details.


