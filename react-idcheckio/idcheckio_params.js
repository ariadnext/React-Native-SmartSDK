export class IDCheckioParams {
    constructor({docType, orientation, confirmationType, useHd, integrityCheck, scanBothSides, sideOneExtraction, sideTwoExtraction, 
        language, manualButtonTimer, feedbackLevel, adjustCrop, maxPictureFilesize, token, confirmAbort, onlineConfig}={}){
        this.docType = docType
        this.orientation = orientation
        this.confirmationType = confirmationType
        this.useHd = useHd
        this.integrityCheck = integrityCheck
        this.scanBothSides = scanBothSides
        this.sideOneExtraction = sideOneExtraction
        this.sideTwoExtraction = sideTwoExtraction
        this.language = language
        this.manualButtonTimer = manualButtonTimer
        this.feedbackLevel = feedbackLevel
        this.adjustCrop = adjustCrop
        this.maxPictureFilesize = maxPictureFilesize
        this.token = token
        this.confirmAbort = confirmAbort
        this.onlineConfig = onlineConfig
    }
}

export class IDCheckioParamsBuilder {
    constructor() {}

    setDocType(docType){
        if(Object.values(DocumentType).includes(docType)){
            this.docType = docType
            return this
        } else {
            throw new Error("DocumentType value is incorrect")
        }
    }

    setOrientation(orientation){
        if(Object.values(IDCheckioOrientation).includes(orientation)){
            this.orientation = orientation
            return this
        } else {
            throw new Error("IDCheckioOrientation value is incorrect")
        }
    }

    setConfirmationType(confirmationType){
        if(Object.values(ConfirmationType).includes(confirmationType)){
            this.confirmationType = confirmationType
            return this
        } else {
            throw new Error("ConfirmationType value is incorrect")
        }
    }

    setUseHd(useHd){
        if(typeof useHd == "boolean"){
            this.useHd = useHd
            return this
        } else {
            throw new Error("useHd must be a boolean")
        }
    }

    setIntegrityCheck(integrityCheck){
        if(typeof integrityCheck == "object"){
            this.integrityCheck = integrityCheck
            return this
        } else {
            throw new Error("integrityCheck must be an IntegrityCheck")
        }
    }

    setScanBothSides(scanBothSides){
        if(Object.values(ScanBothSides).includes(scanBothSides)){
            this.scanBothSides = scanBothSides
            return this
        } else {
            throw new Error("ScanBothSides value is incorrect")
        }
    }

    setSideOneExtraction(sideOneExtraction){
        if(typeof sideOneExtraction == "object"){
            this.sideOneExtraction = sideOneExtraction
            return this
        } else {
            throw new Error("sideOneExtraction must be an Extraction ")
        }
    }

    setSideTwoExtraction(sideTwoExtraction){
        if(typeof sideTwoExtraction == "object"){
            this.sideTwoExtraction = sideTwoExtraction
            return this
        } else {
            throw new Error("sideTwoExtraction must be an Extraction ")
        }
    }

    setLanguage(language){
        if(Object.values(Language).includes(language)){
            this.language = language
            return this
        } else {
            throw new Error("Language value is incorrect")
        }
    }
    
    setManualButtonTimer(manualButtonTimer){
        if(typeof manualButtonTimer == "number"){       
            this.manualButtonTimer = manualButtonTimer
            return this
        } else {
            throw new Error("manualButtonTimer must be a number")
        }
    }

    setFeedbackLevel(feedbackLevel){
        if(Object.values(FeedbackLevel).includes(feedbackLevel)){
            this.feedbackLevel = feedbackLevel
            return this
        } else {
            throw new Error("FeedbackLevel value is incorrect")
        }
    }

    setAdjustCrop(adjustCrop){
        if(typeof adjustCrop == "boolean"){
            this.adjustCrop = adjustCrop
            return this
        } else {
            throw new Error("adjustCrop must be a boolean")
        }
    }

    setMaxPictureFilesize(maxPictureFilesize){
        if(Object.values(FileSize).includes(maxPictureFilesize)){
            this.maxPictureFilesize = maxPictureFilesize
            return this
        } else {
            throw new Error("FileSize value is incorrect")
        }
    }

    setToken(token){
        if(typeof token == "string"){
            this.token = token
            return this
        } else {
            throw new Error("token must be a string")
        }
    }

    setConfirmAbort(confirmAbort){
        if(typeof confirmAbort == "boolean"){
            this.confirmAbort = confirmAbort
            return this
        } else {
            throw new Error("confirmAbort must be a boolean")
        }
    }

    setOnlineConfig(onlineConfig){
        if(typeof onlineConfig == "object"){
            this.onlineConfig = onlineConfig
            return this
        } else {
            throw new Error("onlineConfig must be an Extraction ")
        }
    }

    build() {
        return new IDCheckioParams({ docType: this.docType, adjustCrop: this.adjustCrop, confirmAbort: this.confirmAbort, feedbackLevel: this.feedbackLevel, confirmationType: this.confirmationType,
            integrityCheck: this.integrityCheck, language: this.language, manualButtonTimer: this.manualButtonTimer, maxPictureFilesize: this.maxPictureFilesize, onlineConfig: this.onlineConfig,
            orientation: this.orientation, scanBothSides: this.scanBothSides, sideOneExtraction: this.sideOneExtraction, sideTwoExtraction: this.sideTwoExtraction, token: this.token, useHd: this.useHd
        })
    }
}
  
export class Extraction {
    constructor(codeline, faceDetection){
        if(Object.values(Codeline).includes(codeline)){
            this.codeline = codeline
        } else {
            throw new Error("Codeline value is incorrect")
        }
        if(Object.values(FaceDetection).includes(faceDetection)){
            this.faceDetection = faceDetection
        } else {
            throw new Error("FaceDetection value is incorrect")
        }
    }
}

export class IntegrityCheck {
    constructor(readEmrtd){
        if(typeof readEmrtd == "boolean"){
            this.readEmrtd = readEmrtd
        } else {
            throw new Error("readEmrtd must be a boolean")
        }
    }
}

export class OnlineConfig {
    constructor({
        isReferenceDocument = false,
        checkType = CheckType.CHECK_FULL,
        cisType = null,
        folderUid = null,
        biometricConsent = null,
        enableManualAnalysis = false
    }={}){
        this.isReferenceDocument = isReferenceDocument
        this.checkType = checkType
        this.cisType = cisType
        this.folderUid = folderUid
        this.biometricConsent = biometricConsent
        this.enableManualAnalysis = enableManualAnalysis
    }
}
  
/*
 * Enumeration
 */
export const DocumentType = {
    DISABLED:"DISABLED",
    ID:"ID",
    LIVENESS:"LIVENESS",
    A4:"A4",
    FRENCH_HEALTH_CARD:"FRENCH_HEALTH_CARD",
    BANK_CHECK:"BANK_CHECK",
    OLD_DL_FR:"OLD_DL_FR",
    PHOTO:"PHOTO",
    VEHICLE_REGISTRATION:"VEHICLE_REGISTRATION",
    SELFIE:"SELFIE"
}

export const Environment = {
    DEMO:"DEMO",
    PROD:"PROD"
}
  
export const IDCheckioOrientation = {
    PORTRAIT:"PORTRAIT",
    LANDSCAPE:"LANDSCAPE"
}
  
export const ConfirmationType = {
    DATA_OR_PICTURE:"DATA_OR_PICTURE",
    CROPPED_PICTURE:"CROPPED_PICTURE",
    NONE:"NONE"
}
  
export const ScanBothSides = {
    ENABLED:"ENABLED",
    FORCED:"FORCED",
    DISABLED:"DISABLED"
}
  
export const Codeline = {
    DISABLED:"DISABLED",
    ANY:"ANY",
    DECODED:"DECODED",
    VALID:"VALID",
    REJECT:"REJECT"
}
  
export const FaceDetection = {
    ENABLED:"ENABLED",
    DISABLED:"DISABLED"
}

export const CheckType = { 
    CHECK_FULL:"CHECK_FULL", 
    CHECK_FAST:"CHECK_FAST"
}

export const Language = { 
    fr:"fr", 
    en:"en", 
    pl:"pl", 
    es:"es", 
    ro:"ro", 
    cs:"cs", 
    pt:"pt" 
}

export const FeedbackLevel = { 
    ALL:"ALL", 
    GUIDELINE:"GUIDELINE", 
    ERROR:"ERROR" 
}

export const FileSize = { 
    ONE_MEGA_BYTE:"ONE_MEGA_BYTE", 
    TWO_MEGA_BYTES:"TWO_MEGA_BYTES", 
    THREE_MEGA_BYTES:"THREE_MEGA_BYTES", 
    FOUR_MEGA_BYTES:"FOUR_MEGA_BYTES", 
    FIVE_MEGA_BYTES:"FIVE_MEGA_BYTES", 
    SIX_MEGA_BYTES:"SIX_MEGA_BYTES", 
    SEVEN_MEGA_BYTES:"SEVEN_MEGA_BYTES", 
    HEIGHT_MEGA_BYTES:"HEIGHT_MEGA_BYTES" 
}

export const CISType = { 
    ID:"ID", 
    IBAN:"IBAN", 
    CHEQUE:"CHEQUE", 
    TAX_SHEET:"TAX_SHEET", 
    PAY_SLIP:"PAY_SLIP", 
    ADDRESS_PROOF:"ADDRESS_PROOF", 
    CREDIT_CARD:"CREDIT_CARD", 
    PORTRAIT:"PORTRAIT", 
    LEGAL_ENTITY:"LEGAL_ENTITY", 
    CAR_REGISTRATION:"CAR_REGISTRATION", 
    LIVENESS:"LIVENESS", 
    OTHER:"OTHER" 
}