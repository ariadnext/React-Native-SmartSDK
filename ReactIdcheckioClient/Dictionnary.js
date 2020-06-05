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
  },
  'ExtraParams': {
    'SdkEnvironment': 'DEMO'
  }
};

export var paramsLiveness = {'DocumentType': 'LIVENESS',
  'Orientation': 'PORTRAIT',
  'ExtraParams': {
    'SdkEnvironment': 'DEMO'
  }
};
