## Installation and Setup
Once the app is created:

**1.** Open `build.gradle (module: app)` and add the following to your `dependencies {}`.

```java
//SDK dependency
compile 'com.digital2go:sdk:1.0.4'

//External dependencies
compile 'com.android.support:multidex:1.0.1'
compile 'com.google.android.gms:play-services:9.0.2'
compile 'com.google.android.gms:play-services-maps:9.0.2'
compile 'com.google.maps.android:android-maps-utils:0.4+'
compile 'com.google.android.gms:play-services-location:9.0.2'
compile 'com.kontaktio:sdk:3.0.0'
compile 'com.squareup.okhttp3:okhttp:3.0.1'
compile 'com.squareup.retrofit:retrofit:1.9.0'
compile 'com.google.code.gson:gson:2.8.1'

//Push Notifications
compile 'com.google.firebase:firebase-messaging:9.0.2'
compile 'com.amazonaws:aws-android-sdk-sns:2.2.11'
```

**2.** Enable the multidex at your `defaultConfig{}` like this

```java
defaultConfig {
	....
	multiDexEnabled true
	...
}
```

**3.** Add the following to your **strings.xml** file at`/app/src/main/res/values/strings.xml` 


```xml
<string name="kontakt_io_api_key">YOUR KONTAKTIO API KEY</string>
<string name="aws_key">YOUR AWS KEY</string>
<string name="aws_secret">YOUR AWS SECRET</string>
<string name="app_name">YOUR APP NAME WITHOUT SPACES</string>
<string name="d2go_app_name">YOUR D2GO APP USERNAME</string>
<string name="d2go_app_pass">YOUR D2GO APP PASSWORD</string>
```

```java
//if you want to override the default endpoint
<string name="d2go_api_endpoint">YOUR ENDPOINT</string>
```

> **note**: the “app_name” value should stay free of blank spaces, also

**4.** Customize your **AndroidManifest.xml**:

**Adding permissions:**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

> **note**: if the API is at least 22 you need to request some permissions at runtime [check](https://developer.android.com/training/permissions/requesting.html)

**Setting Meta-Data**

```xml
<meta-data 
	android:name="app_name" 
	android:value="@string/app_name" />

<meta-data 
	android:name="d2go_app_username" 
	android:value="@string/d2go_app_name" />

<meta-data 
	android:name="d2go_app_password" 
	android:value="@string/d2go_app_pass" />

<meta-data 
	android:name="kontakt.io.API_KEY"
	android:value="@string/kontakt_io_api_key" />

<meta-data 
	android:name="aws_key" 
	android:value="@string/aws_key" />

<meta-data 
	android:name="aws_secret" 
	android:value="@string/aws_secret" />

<meta-data 
	android:name="com.google.android.gms.version" 
	android:value="@integer/google_play_services_version" />
```
