#import "TXSTextboxListener.h"
#import <Foundation/Foundation.h>

@interface TXSTextboxListenerImpl : NSObject <TXSTextboxListener>

- (id)initWithUITextView:(UITextView *)textView;

@end
