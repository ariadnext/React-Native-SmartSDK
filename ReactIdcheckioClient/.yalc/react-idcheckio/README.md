# IdcheckioSdk - ReactNative module

## Prerequisites

Before started, please make sure you've installed :
- **npm** - `brew install node`, `brew install watchman`
- **react-native** - `npm install -g react-native-cli`, `npm install -g react`, `npm install -g react-native`
- **yalc** - `npm install -g yalc`

## Getting started

#### 1) Publish your local copy of react-idcheckio

- From your terminal, go to the `react-idcheckio` folder
```shell
$ cd PATH_TO_MODULE_FOLDER/react-idcheckio
```

- Publish the react-idcheckio module locally:
```shell
  $ yalc publish
  react-idcheckio@1.0.0-dbcb9ede published in store.
```

#### 2) Add react-idcheckio to your project

- From your terminal, go to your project's folder :
```shell
  $ cd YOUR_PROJECT_FOLDER
```

- Add your local copy of `react-idcheckio` to your project's modules:
```shell
$ yalc add react-idcheckio
Package react-idcheckio@1.0.0-dbcb9ede added ==> /Users/mlegendre/devel/ariadnext/Autre/IdcheckioClient/node_modules/react-idcheckio.
Don\'t forget you may need to run yarn after adding packages with yalc to install/update dependencies/bin scripts.
```

- Install the dependency:
```shell
  $ yarn install
```

## Platform specific configuration
 
#### Android

1. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
```groovy
implementation 'androidx.multidex:multidex:2.0.1'
```
- In the `android.defaultConfig` block, add the line `multiDexEnabled true`
- In the `android` block, add the following lines :
```groovy
packagingOptions {
    pickFirst '**/armeabi-v7a/libc++_shared.so'
    pickFirst '**/x86/libc++_shared.so'
    pickFirst '**/arm64-v8a/libc++_shared.so'
    pickFirst '**/x86_64/libc++_shared.so'
    pickFirst '**/x86/libjsc.so'
    pickFirst '**/armeabi-v7a/libjsc.so'
    pickFirst 'META-INF/DEPENDENCIES'
    pickFirst 'META-INF/notice.txt'
    pickFirst 'META-INF/license.txt'
}
```

2. Add a new maven repository to retrieve the sdk from our nexus. If you have in your main `build.gradle` an `allprojects{}` block containing your repositories, add the following lines in it else add them in the plugin's `build.gradle`  :
```groovy
maven {
    credentials {
        username = "$YOUR_USERNAME"
        password = "$YOUR_PASSWORD"
    }
    url "https://repoman.rennes.ariadnext.com/content/repositories/com.ariadnext.idcheckio/"
}
```

3. Put the licence file in  `android/app/src/main/assets/`
- ⚠️  &nbsp; Don't forget to change your `signingConfig` with the certificate you give us to create the licence. &nbsp;⚠️

## Usage

1. Create a file `IdcheckioModule.js` and copy the following lines :
```javascript
'use strict';
import { NativeModules } from 'react-native';
module.exports = NativeModules.IdcheckioModule;
```

2. Open your `index.js` or any other files where you want to use the IdcheckioModule and add `import IdcheckioModule from './IdcheckioModule';` to the imports at the top of the file.

3.  Before initializing the sdk, you have to ask for permissions (`CAMERA` and `RECORD_AUDIO`). To do so, you can use the following method :
```javascript
async requestPermissions() {
    try {
        const granted = await PermissionsAndroid.requestMultiple(
            [PermissionsAndroid.PERMISSIONS.CAMERA, PermissionsAndroid.PERMISSIONS.RECORD_AUDIO]
        )
        if (granted[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED &&
            granted[PermissionsAndroid.PERMISSIONS.RECORD_AUDIO] === PermissionsAndroid.RESULTS.GRANTED) {} else {
            console.log("Permission denied")
        }
    } catch (err) {
        console.warn(err);
    }
}
```

4. Before doing any call to the sdk, you can use the `preload()` method. It will accelerate the future call the to the capture process. You won't receive any callback when calling this method.
```javascript
async preload() {
    await IdcheckioModule.preload(true);
}
```

5. Before capturing any document, you need to activate the licence. To do so, you have to use the `activate()` method.
```javascript
async activate() {
    try {
        await IdcheckioModule.activate("license", true, false);
        console.log("Activated");
    } catch (error) {
        console.log(error);
    }
}
```

6. To start the capture of a document, you have to call the start method with your wanted parameters. You will receive the result in a string that can be parse into a json object.
```javascript
  export var paramsId = {'DocumentType': 'ID',
    'Orientation': 'LANDSCAPE',
    'ConfirmType': 'DATA_OR_PICTURE',
    'UseHd': 'true',
    'ScanBothSides': 'ENABLED',
    'Side1Extraction': {
      'DataRequirement': 'DECODED',
      'FaceDetection': 'ENABLED'
    }
  };

  async start() {
      try {
          var {
              idcheckioResult
          } = await IdcheckioModule.start(Dictionnary.paramsId);
          results = JSON.parse(idcheckioResult);
          this.response_server(results);
      } catch (e) {
          console.log(e);
      }
  }
```

7. To start an online capture of a document, you have the method the `startOnline()` method. You will receive the result in a string that can be parse into a json object.
```javascript
  export var paramsLiveness = {'DocumentType': 'LIVENESS',
    'Orientation': 'PORTRAIT'
  };

  async startOnline() {
      var cisContext = {
          'referenceDocUid': results.documentUid,
          'referenceTaskUid': results.taskUid,
          'folderUid': results.folderUid
      };
      try {
          var {
              idcheckioResult
          } = await IdcheckioModule.startOnline(Dictionnary.paramsLiveness, "license", cisContext, false);
          results = JSON.parse(idcheckioResult);
          this.response_server(results);
      } catch (e) {
          console.log(e);
      }
  }
```

- ✅  &nbsp; To learn more informations on those methods and theirs parameters. Please refer to the official IDCheck.io sdk documentation.
