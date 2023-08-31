import { CISType, CaptureMode, Codeline, ConfirmationType, DocumentType, Extraction, FaceDetection, FeedbackLevel, FileSize, IDCheckioOrientation, IDCheckioParamsBuilder, IntegrityCheck, Language, OnlineConfig, ScanBothSides } from "react-idcheckio"

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
.setAdjustCrop(true)
.setConfirmAbort(false)
.build()

export const paramsIDOnline = new IDCheckioParamsBuilder()
.setDocType(DocumentType.ID)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setIntegrityCheck(new IntegrityCheck({ readEmrtd: true, docLiveness: true }))
.setOnlineConfig(new OnlineConfig({ isReferenceDocument: true }))
.build()

export const paramsIDAnalyze = new IDCheckioParamsBuilder()
.setDocType(DocumentType.ID)
.setSideOneExtraction(new Extraction(Codeline.ANY, FaceDetection.ENABLED))
.setSideTwoExtraction(new Extraction(Codeline.ANY, FaceDetection.ENABLED))
.setMaxPictureFilesize(FileSize.TWO_MEGA_BYTES)
.build()

export const paramsLiveness = new IDCheckioParamsBuilder()
.setDocType(DocumentType.LIVENESS)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setConfirmAbort(true)
.build()

export const paramsFrenchHealthCard = new IDCheckioParamsBuilder()
.setDocType(DocumentType.FRENCH_HEALTH_CARD)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.build()

export const paramsSelfie = new IDCheckioParamsBuilder()
.setDocType(DocumentType.SELFIE)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.build()

export const paramsAddressProof = new IDCheckioParamsBuilder()
.setDocType(DocumentType.A4)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setUseHd(true)
.setOnlineConfig(new OnlineConfig({ cisType: CISType.ADDRESS_PROOF }))
.build()

export const paramsVehicleRegistration = new IDCheckioParamsBuilder()
.setDocType(DocumentType.VEHICLE_REGISTRATION)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setSideOneExtraction(new Extraction(Codeline.VALID, FaceDetection.DISABLED))
.build()

export const paramsIban = new IDCheckioParamsBuilder()
.setDocType(DocumentType.PHOTO)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setCaptureMode(CaptureMode.PROMPT)
.setOnlineConfig(new OnlineConfig({ cisType: CISType.IBAN }))
.build()

export const paramsAttachment = new IDCheckioParamsBuilder()
.setDocType(DocumentType.PHOTO)
.setConfirmationType(ConfirmationType.DATA_OR_PICTURE)
.setOrientation(IDCheckioOrientation.PORTRAIT)
.setUseHd(true)
.setAdjustCrop(true)
.setOnlineConfig(new OnlineConfig({ cisType: CISType.OTHER }))
.build()

export class ParamsListItem {
    constructor({name, params, isOnline, isUpload = false}={}){
        this.name = name
        this.params = params
        this.isOnline = isOnline
        this.isUpload = isUpload
    }
}

export const paramsList = [
    new ParamsListItem({name: "ID Offline", params: paramsIDOffline, isOnline: false}),
    new ParamsListItem({name: "ID Online", params: paramsIDOnline, isOnline: true}),
    new ParamsListItem({name: "Liveness Online", params: paramsLiveness, isOnline: true}),
    new ParamsListItem({name: "French health card Online", params: paramsFrenchHealthCard, isOnline: true}),
    new ParamsListItem({name: "Selfie Online", params: paramsSelfie, isOnline: true}),
    new ParamsListItem({name: "Address proof Online", params: paramsAddressProof, isOnline: true}),
    new ParamsListItem({name: "Vehicle registration Online", params: paramsVehicleRegistration, isOnline: true}),
    new ParamsListItem({name: "Iban Online", params: paramsIban, isOnline: true}),
    new ParamsListItem({name: "ID Analyze", params: paramsIDAnalyze, isOnline: true, isUpload: true}),
    new ParamsListItem({name: "Attachment", params: paramsAttachment, isOnline: true})
]
