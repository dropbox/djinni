//
//  TXSTextboxListenerImpl.swift
//  TextSort
//
//  Created by Michal Kowalczyk on 24/04/16.
//  Copyright © 2016 Dropbox, Inc. All rights reserved.
//

import UIKit

@objc class TXSTextboxListenerImpl : NSObject, TXSTextboxListener {
    private var textView_: UITextView
    
    @objc(initWithUITextView:)
    init (textView: UITextView) {
        textView_ = textView
    }

    @objc func update(items: TXSItemList) {
        textView_.text = items.items.joinWithSeparator("\n")
    }
}