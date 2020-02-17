export var paramsId = {'DocumentType': 'ID',
  'Orientation': 'PORTRAIT',
  'ConfirmType': 'DATA_OR_PICTURE',
  'UseHd': 'false',
  'ScanBothSides': 'ENABLED',
  'Side1Extraction': {
    'DataRequirement': 'DECODED',
    'FaceDetection': 'ENABLED'
  }
};

export var paramsIdOnline = {'DocumentType': 'ID',
  'Orientation': 'LANDSCAPE',
  'ConfirmType': 'DATA_OR_PICTURE',
  'UseHd': 'false',
  'ScanBothSides': 'ENABLED',
  'Side1Extraction': {
    'DataRequirement': 'DECODED',
    'FaceDetection': 'ENABLED'
  }
};

export var paramsLiveness = {'DocumentType': 'LIVENESS',
  'Orientation': 'PORTRAIT',
  'ExtraParams': {
    'ActivationUrl': 'https://api.ariadnext.com/activation/rest/v0/activate',
    'FalconWSS': 'wss://falcon-demo.ariadnext.com/wss-falcon'
  }
};
