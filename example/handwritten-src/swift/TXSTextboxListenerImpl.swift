//
//  TXSTextboxListenerImpl.swift
//  TextSort
//
//  Created by Bruno Coelho on 10/03/2017.
//  Copyright © 2017 Dropbox, Inc. All rights reserved.
//

import UIKit

@objc class TXSTextboxListenerImpl : NSObject, TXSTextboxListener {
    private var textView_: UITextView
    
    @objc(initWithUITextView:)
    init (textView: UITextView) {
        textView_ = textView
    }
    
    @objc func update(_ items: TXSItemList) {
        textView_.text = items.items.joined(separator: "\n")
    }
}
