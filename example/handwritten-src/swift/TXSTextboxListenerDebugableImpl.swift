//
//  TXSTextboxListenerImpl.swift
//  TextSort
//
//  Created by Bruno Coelho on 10/03/2017.
//  Copyright © 2017 Dropbox, Inc. All rights reserved.
//

import UIKit

@objc class TXSTextboxListenerDebugableImpl : NSObject, TXSTextboxListener {
    private var textView_: UITextView
    
    @objc(initWithUITextView:)
    init (textView: UITextView) {
        textView_ = textView
    }
    
    @objc func update(_ items: TXSItemList) {
        let string = items.items.joined(separator: "\n")
        print("TXSTextboxListenerDebugableImpl -> update \n\(string)")
        textView_.text = string
    }
}
