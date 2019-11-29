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
}
