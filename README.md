# Basic Image Picker

A Simple Android Library to record and pick audio.

### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```Kotlin
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. Add the dependency

```Kotlin
dependencies {
    implementation 'com.github.abdullahalshafi:BasicAudioPicker:1.0.0'
}
```

### Usage

#### Record

```kotlin
  AudioUtilHelper.create(this, audioLauncher) {
    recordAudio()
    start()
}
```

#### Pick Audio

```kotlin
 AudioUtilHelper.create(this, audioLauncher) {
    pickAudio()
    start()
}
```

#### Audio Result

```kotlin
private val audioLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {

            val basicAudioData: BasicAudioData =
                it.data!!.getSerializableExtra(BasicAudioData::class.java.simpleName) as BasicAudioData

            //do stuffs with the image object
            Log.d("AUDIO_DATA", "name: ${basicAudioData.name} path: ${basicAudioData.path}")

        } else if (it.resultCode == Activity.RESULT_CANCELED) {
            //handle your own situation
        }
    }
```


