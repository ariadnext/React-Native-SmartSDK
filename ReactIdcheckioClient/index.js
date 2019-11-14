/**
 * @format
 */
import React, {
    Component
} from 'react';
import IdcheckioModule from './IdcheckioModule';
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

var results;

class Resultat extends Component {

    constructor(props) {
        super(props);
        this.state = {
            last_name: "unknown",
            first_name: "unknown",
            recto_uri: ""
        };
    }

    response_server(results) {
        this.setState({
            last_name: results.documents.fields.lastNames.value,
            first_name: "",
            recto_uri: ""
        });
    }

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

    async start() {
        try {
            var {
                idcheckioResult
            } = await IdcheckioModule.start(Dictionnary.paramsId);
            console.log(idcheckioResult);
            results = JSON.parse(idcheckioResult);
            console.log(results);
            this.response_server(results);
        } catch (e) {
            console.log(e);
        }
    }

    async startOnline() {
        try {
            var {
                idcheckioResult
            } = await IdcheckioModule.startOnline(Dictionnary.paramsIdOnline, "license", {}, false);
            results = JSON.parse(idcheckioResult);
            this.response_server(results);
        } catch (e) {
            console.log(e);
        }
    }

    async startLiveness() {
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

    async activate() {
        try {
            await IdcheckioModule.activate("license", true, false);
            console.log("Activated");
        } catch (error) {
            console.log(error);
        }
    }

    async preload() {
        await IdcheckioModule.preload(true);
    }

    render() {
        this.preload();
        if (Platform.OS === 'android') {
            this.requestPermissions();
        }
        return ( <View style = {
                styles.container
            } >
            <Text style = {
                styles.welcome
            } > Hello, {
                this.state.last_name
            } {
                this.state.first_name
            }! </Text>
            <Button onPress = {
                () => {
                    this.activate();
                }
            }
            title = "Activate" />
            <Button onPress = {
                () => {
                    this.start();
                }
            }
            title = "Start" />
            <Button onPress = {
                () => {
                    this.startOnline();
                }
            }
            title = "StartOnline" />
            <Button onPress = {
                () => {
                    this.startLiveness();
                }
            }
            title = "StartLiveness" />
            </View>
        );
    }
}

export default class ReactIdcheckioClient extends Component {
    render() {
        return ( <
            Resultat / >
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

AppRegistry.registerComponent('ReactIdcheckioClient', () => ReactIdcheckioClient);
