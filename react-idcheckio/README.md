# IdcheckioSdk - ReactNative module

## Prerequisites

Before started, please make sure you've installed :
- **npm** - `brew install node`, `brew install watchman`
- **react-native** - `npm install -g react-native-cli`

## Getting started

#### 1) Link npm to your local copy of react-idcheckio

- From your terminal, go to the `react-idcheckio` folder
```shell
$ cd PATH_TO_MODULE_FOLDER/react-idcheckio
```

- Link the react-idcheckio module to your local npm:
```shell
  $ npm link
  npm WARN react-idcheckio@1.0.0 requires a peer of react@16.9.0 but none is installed. You must install peer dependencies yourself.
  npm WARN react-idcheckio@1.0.0 requires a peer of react-native@>=0.60.0 but none is installed. You must install peer dependencies yourself.
  npm WARN react-idcheckio@1.0.0 No repository field.

  up to date in 0.624s
  found 0 vulnerabilities

  /usr/local/lib/node_modules/react-idcheckio -> PATH_TO_MODULE_FOLDER/react-idcheckio
```

  > The last line means that your local `npm` now knows from where to bind any asked link to the `react-idcheckio` module on your machine.

  If you encounter warnings message telling yout that you _must install peer dependencies yourself_, run the following commands to install the dependency manually :
```shell
  $ npm install -g react

  $ npm install -g react-native
```

  Do not forget to run latest versions of `npm` and `node` :  
```shell
  $ npm install -g npm@latest

  $ npm install -g node@latest
```

#### 2) Add react-idcheckio to your project

- From your terminal, go to your project's folder :
```shell
  $ cd YOUR_PROJECT_FOLDER
```
- ⚠️  &nbsp; If you did not init your project yet, please first init it using the command: &nbsp;⚠️
```shell
  $ npm install --save
```
  > This command will import all your dependencies.
  >
  > Please **make sure to run this command with the --save options**. This will save natives dependencies for the react-idcheckio.

- Link your local copy of `react-idcheckio` to your project's modules:
```shell
$ npm link react-idcheckio
YOUR_PROJECT_FOLDER/node_modules/react-idcheckio -> /usr/local/lib/node_modules/react-idcheckio -> PATH_TO_MODULE_FOLDER/react-idcheckio
```

  > This will add a symbolicated link of react-idcheckio in your project's node_modules' folder (cf. terminal feedback of the command)

<!--
  Note: If you still encounter an error for `react-idcheckio` package not found, your can directly link the library module with it's relative path:
```shell
  npm install ../react-idcheckio --save
```

- Open you package.json and check for the following line in the `devDependencies` block :
```
"react-idcheckio": ">=1.0.0" // or "../react-idcheckio" if you installed it with a relative path reference
```

- Finally, install the module as dependencies of your project:
```shell
  $ react-native link react-idcheckio                       
  info Linking "react-idcheckio" Android dependency
  info Android module "react-idcheckio" has been successfully linked
```
-->

  > Note: If you don't have `react-native` installed on your environment, you can still add the react-idcheckio module to your iOS/Android projects manually :
  > Just check the **Platform's specific configuration** section

## Platform specific configuration

  > ℹ️ &nbsp; Some steps of this section might have already been done through the command `$ react-native link react-idcheckio`. In that case, just check if the step is already done, and if not, do it 👍

#### Android

1. Append the following lines to `android/settings.gradle`:
```groovy
include ":react-idcheckio"
project(":react-idcheckio").projectDir = new File(rootProject.projectDir, "../node_modules/react-idcheckio/android")
```
2. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
```groovy
implementation project(':react-idcheckio')
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

3. Add a new maven repository to retrieve the sdk from our nexus. If you have in your main `build.gradle` an `allprojects{}` block containing your repositories, add the following lines in it else add them in the plugin's `build.gradle`  :
```groovy
maven {
    credentials {
        username = "$YOUR_USERNAME"
        password = "$YOUR_PASSWORD"
    }
    url "https://repoman.rennes.ariadnext.com/content/repositories/com.ariadnext.idcheckio/"
}
```
4. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `new IdcheckioPackage()` to the list returned by the `getPackages()` method

5. Put the licence file in  `android/app/src/main/assets/`
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
