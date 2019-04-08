# AriadNEXT's SmartSDK - ReactNative module

## Prerequisites
Before started, please make sure you've installed :
- **npm** - `brew install node`
- **react-native** - `npm install -g react-native-cli`

## Getting started

> The SmartSDK's react module is provided locally, so you'll need to have it on your local machine, and *link* it to your local npm in ordrer to add it to your project.

#### 1) Link npm to your local copy of react-smartsdk
- From your terminal, go to the `react-smartsdk` folder
```shell
$ cd PATH_TO_MODULE_FOLDER/react-smartsdk
```

- Link the react-smartsdk module to your local npm:
```shell
$ npm link
npm WARN react-smartsdk@1.0.0 requires a peer of react-native@^0.41.2 but none is installed. You must install peer dependencies yourself.
npm WARN react-smartsdk@1.0.0 No repository field.
up to date in 1.305s
found 0 vulnerabilities
/usr/local/lib/node_modules/react-smartsdk -> PATH_TO_MODULE_FOLDER/react-smartsdk
```

  > The last line means that your local `npm` now knows from where to bind any asked link to the `react-smartsdk` module on your machine.

#### 2) Add react-smartsdk to your project
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
  > Please **make sure to run this command with the --save options**. This will save natives dependencies for the react-smartsdk.

- Link your local copy of `react-smartsdk` to your project's modules:
```shell
$ npm link react-smartsdk
YOUR_PROJECT_FOLDER/node_modules/react-smartsdk -> /usr/local/lib/node_modules/react-smartsdk -> PATH_TO_MODULE_FOLDER/react-smartsdk
```
  > This will add a symbolicated link of react-smartsdk in your project's node_modules' folder (cf. terminal feedback of the command)

- Finally, add the module as dependencies of your project:
```shell
$ react-native link react-smartsdk                              
rnpm-install info Linking react-smartsdk ios dependency
rnpm-install info Platform 'ios' module react-smartsdk has been successfully linked
rnpm-install info Linking react-smartsdk android dependency
rnpm-install info Platform 'android' module react-smartsdk has been successfully linked
```

  > Note: If you don't have `react-native` installed on your environment, you can still add the react-smartsdk module to your iOS/Android projects manually : <br/>
  > Just check the **Platform's specific configuration** section

## Platform's specific configuration

> ℹ️  &nbsp; Some steps of this section might have already been done threw the command `$ react-native link react-smartsdk`. In that case, just check if the step is already done, and if not, do it 👍

### iOS

#### ⚙️ &nbsp; React-SmartSDK Module's configuration

Add the native `SmartsdkKit.framework` in the **react-smartsdk module (not in your app project !)**.
  - From your Finder, add your `SmartsdkKit.framework` in the folder `PATH_TO_MODULE_FOLDER/react-smartsdk/ios/Frameworks`
  - *Note: Be sure to download the `SmartsdkKit.framework` compiled **without bitcode** for now.*

#### 📱 &nbsp; Application's configuration (your project)

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-smartsdk` and add `Smartsdk.xcodeproj`
3. In XCode, in the project navigator, select your **project**. Add `libSmartsdk.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Still in XCode, select your **app target** (not the project !). Add the framework search path corresponding to `SmartsdkKit.framework` in `Build Settings` ➜ `Search Paths` ➜ `Frameworks Search Paths`:
```
$(PROJECT_DIR)/../node_modules/react-smartsdk/ios/Frameworks
```
5. Add the `SmartsdkKit.framework` to your iOS app's target.
  - Via Xcode's project navigator, select your target, then go to `General` ➜ `Embedded Binaries` ➜ `+`, and select `SmartsdkKit.framework`
  - *If it's not visible in the list, select `Add Other...` and go find it on your disk.*
6. Disable bitcode from your target by setting `Build Settings` ➜ `Build Options` ➜ `Enable Bitcode` to **NO**
7. In your applications' `Info.plist` file, if not already present, add this two entries, with a text describing your needs :
  - `NSCameraUsageDescription`: Permission to use the camera to take photo/video.
  - `NSPhotoLibraryUsageDescription`: Permission to access the photo library when needed.
8. Add your SDK's license file in your app's source folder (containing your app's `Info.plist` and `AppDelegate{.h,.m,.swift}`):
  ```
  YOUR_PROJECT_FOLDER/ios/SOURCE_FOLDER/licence.axt
  ```
    - ⚠️  &nbsp; Please be sure to rename the license file "**licence.axt**" ⚠️
    - ✅ &nbsp; Don't forget to add the licence file to your app bundle (check it in project navigator: `APP_TARGET` ➜ `Build Phases` ➜ `Copy Bundle Resources` ➜ Add `licence.axt` if not present in the list ).
9. Run your project (`Cmd+R`), you're done ! 🎉

#### Android

1. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-smartsdk'
  	project(':react-smartsdk').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-smartsdk/android')
  	```
2. In `android/app/build.gradle`:
  - Insert the following lines inside the dependencies block :
  	```
      implementation project(':react-smartsdk')
      implementation "com.android.support:multidex:1.0.3"
  	```
  - Insert the following lines inside the repositories block :
      ```
      flatDir{
        dirs "$rootDir/../node_modules/react-smartsdk/android/libs"
      }
      ```
  - Insert the line `multiDexEnabled true` in the block android -> defaultConfig

3. Open up `android/app/src/main/java/[...]/MainApplication.java`
- Add `import com.reactlibrary.SmartsdkPackage;` to the imports at the top of the file
- Add `new SmartsdkPackage()` to the list returned by the `getPackages()` method
- Make your MainApplication class extends MultiDexApplication instead of Application

4. Add the line `tools:replace="android:name"` to your `AndroidManifest.xml` in the `<application>` block.

5. Put the aar in  `./node_modules/react-smartsdk/android/libs`

6. Rename the licence file to  `licence.axt` and put it in  `android/app/src/main/assets/`

## Usage

1. Create a file `SmartsdkModule.js` and copy the following lines :
    ```javascript
    'use strict';
    import { NativeModules } from 'react-native';
    module.exports = NativeModules.SmartsdkModule;
    ```
2. Open your `index.js` or any another files where you want to use the SmartsdkModule
  - Add `import SmartsdkModule from './SmartsdkModule';` to the imports at the top of the file

3. Before capturing any document, you need to initialize the sdk. To do so, you can use the method `initSmartSdk()` of the `SmartsdkModule` :
    ```javascript
    async init(){
      SmartsdkModule.initSmartSdk()
      .then(() => console.log("The sdk is activated."))
      .catch(() => console.error(e));
    }
    ```
4. To start the capture of a document, you have to call the capture method with a map of parameters. After the capture you receive a string that can be parse into a json object containing the capture results.
    ```javascript
    async capture(){
      try{
        var{
          axtSdkResult
        } = await SmartsdkModule.capture(Dictionnary.params);
        var results = JSON.parse(axtSdkResult);
        //Do whatever you want with the results
      } catch (e){
        console.log(e);
      }
    }
    ```
5. Parameters (To see the list of all `EXTRA_PARAMETERS`, check the developer documentation) :
  - For **Android** :
    ```javascript
    export var params = {'EXTRACT_DATA': 'true',
                          'USE_HD': 'false',
                          'DISPLAY_CAPTURE': 'true',
                          'USE_FRONT_CAMERA': 'false',
                          'SCAN_RECTO_VERSO': 'true',
                          'DOCUMENT_TYPE': 'ID',
                          'DATA_EXTRACTION_REQUIREMENT': 'MRZ_FOUND',
                          'EXTRA_PARAMETERS': {'AXT_ADJUST_CROP': 'true'}};
    ```
    
  - For **iOS** :
    ```javascript
    export var params = {'EXTRACT_DATA': 'YES',
                          'USE_HD': 'NO',
                          'DISPLAY_CAPTURE': 'YES',
                          'USE_FRONT_CAMERA': 'NO',
                          'SCAN_RECTO_VERSO': 'YES',
                          'DOCUMENT_TYPE': 'ID',
                          'DATA_EXTRACTION_REQUIREMENT': 'MRZ_FOUND',
                          'EXTRA_PARAMETERS': {'AXT_ADJUST_CROP': 'YES'}};
    ```
    
6. ⚠️  &nbsp; **ANDROID ONLY** Before initializing the sdk, you have to ask for permissions (`CAMERA` and `READ_PHONE_STATE`).
  To do so, you can use the following method :
  ```javascript
    async requestPermissions(){
      try{
        const granted = await PermissionsAndroid.requestMultiple(
          [PermissionsAndroid.PERMISSIONS.CAMERA, PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE]
        )
        if (granted[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED
          && granted[PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE] === PermissionsAndroid.RESULTS.GRANTED) {
          this.init();
        } else {
          console.log("Permission denied")
        }
      } catch(err) {
        console.warn(err);
      }
    }
  ```
