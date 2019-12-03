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
            recto_uri: "https://ci4.googleusercontent.com/proxy/XZHTJJGv_mPA3me1ZXmGiVZFvgZz8p0NdOoR6g-6skj4iJq2loHKQUTZoxxSIZiyVW2YB43AB0-3Dztl18bhxayjUjSeosPFkEHNFf6xcvRLdCg0rp4UUVU_MNSZOmXAI8k9cUuLDQ=s0-d-e1-ft#https://fr.ariadnext.com/wp-content/uploads//2019/01/logo-ariadnext-rvb-baseline.png"
        };
    }

    response_server(results) {
        this.setState({
            last_name: results.document.fields.lastNames.value,
            first_name: results.document.fields.firstNames.value,
            recto_uri: results.images[0].cropped
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

    start() {
        IdcheckioModule.start(Dictionnary.paramsId)
        .then(data => {
            console.log(data);
            results = JSON.parse(data);
            console.log(results);
            this.response_server(results);
        },
        cause => {
            console.log(cause);
        })
        .catch(err => {
            console.log(err);
        });
    }

    startOnline() {
        IdcheckioModule.startOnline(Dictionnary.paramsIdOnline, "license", {}, false)
        .then(data => {
            results = JSON.parse(data);
            this.response_server(results);
        },
        cause => {
            console.log(cause);
        })
        .catch(err => {
            console.log(err);
        });
    }

    startLiveness() {
        var cisContext = {
            'referenceDocUid': results.documentUid,
            'referenceTaskUid': results.taskUid,
            'folderUid': results.folderUid
        };
        IdcheckioModule.startOnline(Dictionnary.paramsLiveness, "license", cisContext, false)
        .then(data => {
            console.log(data);
            results = JSON.parse(data);
        },
        cause => {
            console.log(cause);
        })
        .catch(err => {
            console.log(err);
        });
    }

    activate() {
        IdcheckioModule.activate("license", true, false)
        .then(data => {
            console.log("Activated");
        },
        cause => {
            console.log(cause);
        })
        .catch(err => {
            console.log(err);
        });
    }

    preload() {
        IdcheckioModule.preload(true);
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
            } ! </Text>
            <Image style={{width: 300, height: 200,resizeMode: 'contain', borderWidth: 1, borderColor: 'black'}} source={{uri: this.state.recto_uri}} />
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
