import UIKit

@objc class TXSTextboxResetListenerImpl : NSObject, TXSTextboxResetListener {
    private var textView_: UITextView
    
    @objc(initWithUITextView:)
    init (textView: UITextView) {
        textView_ = textView
    }
    
    func reset(_ text: String) {
        textView_.text = text
    }
}
