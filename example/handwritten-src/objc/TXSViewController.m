#import "TXSItemList.h"
#import "TXSSortItems.h"
#import "TXSTextboxListenerImpl.h"
#import "TXSViewController.h"
#import "TXSTextboxListener.h"

#import <QuartzCore/QuartzCore.h>

@interface TXSViewController ()

@property (nonatomic) IBOutlet UITextView *textView;

@property (nonatomic) IBOutlet UIButton *button;

@end

@implementation TXSViewController {
    TXSSortItems* _sortItemInterface;
    id <TXSTextboxListener> _textboxListener;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Set border for the TextArea
    [[self.textView layer] setBorderColor:[[UIColor grayColor] CGColor]];
    [[self.textView layer] setBorderWidth:2.3];
    [[self.textView layer] setCornerRadius:5];

    // Dismiss keyboard when not used
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]
                                   initWithTarget:self
                                   action:@selector(dismissKeyboard)];
    [self.view addGestureRecognizer:tap];

    // Create the Objective-C TXSTextboxListener
    _textboxListener = [[TXSTextboxListenerImpl alloc] initWithUITextView:self.textView];
    _sortItemInterface = [TXSSortItems createWithListener:_textboxListener];
}

- (IBAction)sort:(id)sender
{
    [self sortWithOrder:TXSSortOrderAscending];
}

- (IBAction)reverseSort:(id)sender
{
    [self sortWithOrder:TXSSortOrderDescending];
}

- (IBAction)randomSort:(id)sender
{
    [self sortWithOrder:TXSSortOrderRandom];
}

- (void)sortWithOrder:(TXSSortOrder)order
{
    NSString *str = self.textView.text;
    NSArray *strList = [str componentsSeparatedByString:@"\n"];
    TXSItemList *list = [[TXSItemList alloc] initWithItems:strList];
    [_sortItemInterface sort:order items:list];
}

- (void)dismissKeyboard
{
    [self.textView resignFirstResponder];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
