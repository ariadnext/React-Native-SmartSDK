/** @format */

import React, { Component } from 'react';
import SmartsdkModule from './SmartsdkModule';
import * as Dictionnary from './Dictionnary';
import {
  AppRegistry,
  Platform,
  StyleSheet,
  Text,
  View,
  Image,
  Button,
  PermissionsAndroid
} from 'react-native';

class Resultat extends Component {
  constructor(props) {
    super(props);
    this.state = {last_name: "unknown", first_name: "unknown", recto_uri : ""};
  }

  response_server(results){
    this.setState({
      last_name: results.mapDocument.IDENTITY_DOCUMENT.fields.LAST_NAME,
      first_name: results.mapDocument.IDENTITY_DOCUMENT.fields.FIRST_NAMES,
      recto_uri: results["mapImageSource"]["IMAGES_RECTO"]["imageUri"]
    });
  }

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

  async capture(){
    try{
      var{
        axtSdkResult
      } = await SmartsdkModule.capture(Dictionnary.params);
      var results = JSON.parse(axtSdkResult);
      this.response_server(results);
    }  catch (e){
      console.log(e);
    }
  }

  async init(){
    try {
      await SmartsdkModule.initSmartSdk();
      console.log("Activated");
    } catch (e){
      console.error(e);
    }
  }

  render() {
    if (Platform.OS === 'android') {
      this.requestPermissions();
    } else {
      this.init();
    }
    return (
      <View style={styles.container}>
        <Text style={styles.welcome} >Hello, {this.state.last_name} {this.state.first_name} !</Text>
        <Button onPress={() => { this.capture(); }} title="Capture"/>
      </View>
    );
  }
}

export default class ReactSmartsdkClient extends Component {
  render() {
    return (
      <Resultat/>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('ReactSmartsdkClient', () => ReactSmartsdkClient);
