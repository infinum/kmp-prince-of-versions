//
//  KotlinException.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation

func isKotlin<T>(_ error: Error, _ type: T.Type) -> Bool {
    (error as NSError).kotlinException is T
}
