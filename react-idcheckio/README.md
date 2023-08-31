# IdcheckioSdk - ReactNative module

## Prerequisites

Before started, please make sure you've installed :
- **npm** - `brew install node`, `npm install -g yarn`
- **react-native** - `npm install -g react`, `npm install -g react-native`
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
  react-idcheckio@5.4.2-dbcb9ede published in store.
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

#### iOS

1. In your project folder, go to your ios directory and open the Podfile :
 - Change the minimum version to at least '10.0'
 - Add the following lines before the target :
```
source 'https://github.com/CocoaPods/Specs.git'
source 'https://git-externe.rennes.ariadnext.com/idcheckio/axt-podspecs.git'
```
 - Add `use_frameworks!` before the `use_native_modules!`

2. Retrieve the sdk using `pod install --repo-update`
- ⚠️⚠️  &nbsp; You will need to have a .netrc file configurated with our credentials. Check the official documentation for more informations. &nbsp;⚠️⚠️

3. In your project, open the `*.plist` file and the two following entries :
- "Privacy - Camera Usage Description" : "Camera is being used to scan documents"

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

## Usage

1. Create a file `IdcheckioModule.js` and copy the following lines :
```javascript
'use strict';
import { NativeModules } from 'react-native';
module.exports = NativeModules.IdcheckioModule;
```

2. Open your `index.js` or any other files where you want to use the IdcheckioModule and add `import IdcheckioModule from './IdcheckioModule';` to the imports at the top of the file.

3. (Optional) Before starting, the sdk will ask for permissions (`CAMERA`). But you can if you want ask them by youself before using the following method :
```javascript
async requestPermissions() {
    try {
        const granted = await PermissionsAndroid.requestMultiple(
            [PermissionsAndroid.PERMISSIONS.CAMERA]
        )
        if (granted[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED) {} else {
            console.log("Permission denied")
        }
    } catch (err) {
        console.warn(err);
    }
}
```

4. Before doing any call to the sdk, you can use the `preload()` method. It will accelerate the future call the to the capture process. You won't receive any callback when calling this method.
```javascript
preload() {
    IdcheckioModule.preload(true);
}
```

5. Before capturing any document, you need to activate the sdk. To do so, add your activation token on the javascript side and use it to call the `activate()` method.
```javascript
activate() {
    IdcheckioModule.activate(demoToken, true)
    .then(data => {
        this.setState({sdkActivated: true})
        console.log("Activated");
    },
    cause => {
        this.showError(cause);
    })
    .catch(err => {
        console.log(err);
    });
}
```

6. To start the capture of a document, you have to call the start method with your wanted parameters. Check the `Dictionnary.js` file to find some recommandations on the parameters. You will then receive the result in a string that can be parse into a json object.
```javascript
export const paramsIDOffline = new IDCheckioParamsBuilder()
.setDocType(DocumentType.ID)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setUseHd(false)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setScanBothSides(ScanBothSides.ENABLED)
.setSideOneExtraction(new Extraction(Codeline.VALID, FaceDetection.ENABLED))
.setSideTwoExtraction(new Extraction(Codeline.REJECT, FaceDetection.DISABLED))
.setLanguage(Language.fr)
.setManualButtonTimer(10)
.setMaxPictureFilesize(FileSize.TWO_MEGA_BYTES)
.setFeedbackLevel(FeedbackLevel.ALL)
.setAdjustCrop(false)
.setConfirmAbort(false)
.build()

IdcheckioModule.start(Dictionnary.paramsIDOffline)
.then(data => {
    results = JSON.parse(data);
    this.response_server(results);
},
cause => {
    this.showError(cause);
})
.catch(err => {
    console.log(err);
});
```

7. To start an online capture of a document, you have the method the `startOnline()` method. You will receive the result in a string that can be parse into a json object.
```javascript
export const paramsLiveness = new IDCheckioParamsBuilder()
.setDocType(DocumentType.LIVENESS)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setConfirmAbort(true)
.build()

IdcheckioModule.startOnline(paramsLiveness, onlineContext)
.then(data => {
    console.log(data);
    let results = JSON.parse(data)
    this.response_server(results);
},
cause => {
    this.showError(cause);
})
.catch(err => {
    console.log(err);
});
```

8. To just analyze a document, you have the method the `analyze()` method. You will receive the result in a string that can be parse into a json object.
```javascript
analyze(params, onlineContext){
    const options = {
        title: 'Select Document',
        storageOptions: {
            skipBackup: true,
            path: 'images',
        },
    };

    launchImageLibrary(options, (response) => {
        if (response.didCancel) {
            console.log('User cancelled image picker');
        } else if (response.error) {
            console.log('ImagePicker Error: ', response.error);
        } else if (response.customButton) {
            console.log('User tapped custom button: ', response.customButton);
        } else {
            const source = { uri: response.assets[0].uri };
            // You can also display the image using data:
            // const source = { uri: 'data:image/jpeg;base64,' + response.data };
            IdcheckioModule.analyze(params, source.uri, null, true, onlineContext)
            .then(data => {
                console.log(data);
                let results = JSON.parse(data)
                this.response_server(results);
            },
            cause => {
                this.showError(cause);
            })
            .catch(err => {
                console.log(err);
            });
        }
    });
}
```

9. To handle error, you can parse the message of the error and use this object to do some error handling. To know a bit more about the error system in the sdk, look at `4.2.10. IdcheckioError` in the developer's guide.
```javascript
showError(error) {
    console.log(error);
    var errorMsg = JSON.parse(error.message.replace("\n", "\\n"));
    console.log("cause: " + errorMsg.cause);
    console.log("details: " + errorMsg.details);
    console.log("message: " + errorMsg.message);
    console.log("subCause: " + errorMsg.subCause);
    alert(errorMsg.message);        
}
```

## Theming

If you want to change the colors of the sdk to match your theme, it's possible !
You need to first create an IDCheckTheme() object with your own colors and then add it in the builder in the theme parameter.
```javascript
new IDCheckTheme({primaryColor: "#FF0000B7", foregroundColor: "#FFFFFFFF", backgroundColor: "#FFFFFFFF", borderColor: "#FF0000B7"})
```


- ✅  &nbsp; To learn more informations on those methods and theirs parameters. Please refer to the official IDCheck.io sdk documentation.
