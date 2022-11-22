/**
 * @format
 */
import React, {
    Component
} from 'react';
import IdcheckioModule from './IdcheckioModule';
import {Picker} from '@react-native-picker/picker';
import * as Dictionnary from './Dictionnary';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    Button,
    TextInput
} from 'react-native';
import { launchImageLibrary } from 'react-native-image-picker';


const demoToken = "YOUR_ACTIVATION_TOKEN"

class Resultat extends Component {

    constructor(props) {
        super(props);
        this.state = {
            sdkActivated: false,
            sdkResult: null,
            isIps: false,
            folderUid: "",
            selectedValue: 0
        };
    }

    setSelectedValue(value){
        this.setState({
            isIps: value == 10,
            selectedValue: value
        })
    }

    setFolderUid(value){
        this.setState({
            folderUid: value
        })
    }

    response_server(results) {
        this.setState({
            sdkResult: results
        });
    }

    capture() {
        let selectedParams = Dictionnary.paramsList[this.state.selectedValue]
        //Retrieve the online context from last session, it will be used as parameter for the next session
        let onlineContext
        if(this.state.sdkResult != null && this.state.sdkResult.onlineContext != null) {
            onlineContext = this.state.sdkResult.onlineContext
        } else {
            onlineContext = null
        }
        if(selectedParams.isUpload){
            // Analyze mode
            this.analyze(selectedParams.params, onlineContext)
        } else {
            // Capture mode
            if (selectedParams.isOnline) {
                IdcheckioModule.startOnline(selectedParams.params, onlineContext)
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
            } else {
                IdcheckioModule.start(selectedParams.params)
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
        }
    }

    startIps() {
        IdcheckioModule.startIps(this.state.folderUid)
        .then(data => {
            console.log(data);
            let results = JSON.parse(data)
            this.response_server(results);
            console.log("Session IPS Ok !")
        },
        cause => {
            this.showError(cause);
        })
        .catch(err => {
            console.log(err);
        });
    }

    showError(error) {
        console.log(error);
        var errorMsg = JSON.parse(error.message.replace("\n", "\\n"));
        console.log("cause: " + errorMsg.cause);
        console.log("details: " + errorMsg.details);
        console.log("message: " + errorMsg.message);
        console.log("subCause: " + errorMsg.subCause);
        alert(errorMsg.message);
    }

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

    preload() {
        IdcheckioModule.preload(true);
    }

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

    render() {
        if(!this.state.sdkActivated){
          this.preload();
        }
        return (
            <View style = {styles.container} >
                <Text style = {styles.activationText}>{(this.state.sdkActivated)? "SDK activated! 🎉" : "SDK not activated"}</Text>
                <View style={styles.elementContainer} key='Choose your configuration'>
                    <Text style={styles.picker}> Choose your configuration </Text>
                    <Picker
                        selectedValue={this.state.selectedValue}
                        onValueChange={(v) => this.setSelectedValue(v)}
                        mode="dropdown">
                        <Picker.Item label={Dictionnary.paramsList[0].name} value='0'/>
                        <Picker.Item label={Dictionnary.paramsList[1].name} value='1'/>
                        <Picker.Item label={Dictionnary.paramsList[2].name} value='2'/>
                        <Picker.Item label={Dictionnary.paramsList[3].name} value='3'/>
                        <Picker.Item label={Dictionnary.paramsList[4].name} value='4'/>
                        <Picker.Item label={Dictionnary.paramsList[5].name} value='5'/>
                        <Picker.Item label={Dictionnary.paramsList[6].name} value='6'/>
                        <Picker.Item label={Dictionnary.paramsList[7].name} value='7'/>
                        <Picker.Item label={Dictionnary.paramsList[8].name} value='8'/>
                        <Picker.Item label={Dictionnary.paramsList[9].name} value='9'/>
                        <Picker.Item label='Start Ips' value='10'/>
                    </Picker>
                </View>
                <TextInput style={styles.input} display= {(this.state.isIps)? 'flex' : 'none'} onChangeText={(text) => this.setFolderUid(text)}/>
                <Button disabled = {this.state.sdkActivated} onPress = {() => {this.activate()}}
                    title = {(this.state.sdkActivated)? "SDK already activated" : "Activate SDK"}
                />
                <Button disabled = {!this.state.sdkActivated} onPress = {(this.state.isIps)? () => {this.startIps()} : () => {this.capture()}}
                    title = {(this.state.sdkActivated)? ((this.state.isIps)? "Start ips session" : "Capture Document") : "SDK not activated"}
                />
                <Text style = {styles.welcome}>
                    {(this.state.sdkResult != null)?
                        ((this.state.sdkResult.document != null && this.state.sdkResult.document.type == "IdentityDocument")?
                            "Howdy".concat(' ', this.state.sdkResult.document.fields.firstNames.value.split(' ')[0]).concat(' ', this.state.sdkResult.document.fields.lastNames.value).concat('', "! 🤓")
                            : "Capture OK 👍")
                         : "Please first scan an ID"}
                </Text>
            </View>
        );
    }
}

export default class ReactIdcheckioClient extends Component {
    render() {
        return (<Resultat />);
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    picker: {
        fontSize: 18,
        textAlign: 'center'
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
    input: {
        borderColor: "gray",
        width: "60%",
        borderWidth: 1,
        borderRadius: 10,
        padding: 10,
    },
    activationText: {
        fontSize: 24,
        marginBottom: 40
    }
});

AppRegistry.registerComponent('ReactIdcheckioClient', () => ReactIdcheckioClient);
