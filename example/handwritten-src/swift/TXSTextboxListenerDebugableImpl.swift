import UIKit

final class TXSTextboxListenerDebugableImpl : NSObject, TXSTextboxListener {
    private var textView: UITextView
    
    @available(*, unavailable)
    override init() {
        fatalError("Unsupported")
    }
    
    @objc(initWithUITextView:)
    init(textView: UITextView) {
        self.textView = textView
    }
    
    func update(_ items: TXSItemList) {
        let string = items.items.joined(separator: "\n")
        print("TXSTextboxListenerDebugableImpl -> update \n\(string)")
        textView.text = string
    }
}
