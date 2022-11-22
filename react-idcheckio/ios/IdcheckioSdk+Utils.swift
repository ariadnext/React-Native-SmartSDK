//
//  IdcheckioSdk+Utils.swift
//
//  Created by Matthieu Legendre on 10/09/2019.
//

import Foundation
import IDCheckIOSDK

public class IdcheckioObjcUtil: NSObject {
    @objc public static func resultToJSON(_ result: IdcheckioResult) -> String {
        if let jsonData = try? JSONEncoder().encode(result) {
            return String(data: jsonData, encoding: .utf8) ?? ""
        } else {
            return "{}"
        }
    }
    
    @objc public static func getErrorJson(_ error: NSError) -> String {
        if let jsonData = try? JSONEncoder().encode(error as! IdcheckioError) {
            return String(data: jsonData, encoding: .utf8) ?? ""
        } else {
            return "{\"message\":\"Internal error.\",\"details\":\"INTERNAL_ERROR\",\"cause\":\"INTERNAL_ERROR\"}"
        }
    }
    
    @objc public static func missingFolderUid() -> String {
        let error = IdcheckioError.customerError(details: "MISSING_FOLDER_UID", message: "The ips folderUid is mandatory to start an ips session.", subcause: nil)
        if let jsonData = try? JSONEncoder().encode(error) {
            return String(data: jsonData, encoding: .utf8) ?? ""
        } else {
            return "{\"message\":\"Internal error.\",\"details\":\"INTERNAL_ERROR\",\"cause\":\"INTERNAL_ERROR\"}"
        }
    }
}
