// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from example.djinni

#import "TXSSortOrderTranslator+Private.h"
#import <Foundation/Foundation.h>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@implementation TXSSortOrderTranslator

+ (TXSSortOrder)cppSortOrderToObjcSortOrder:(::textsort::sort_order)sortOrder
{
    return static_cast<TXSSortOrder>(sortOrder);
}

+ (::textsort::sort_order)objcSortOrderToCppSortOrder:(TXSSortOrder)sortOrder
{
    return static_cast<enum ::textsort::sort_order>(sortOrder);
}

@end
