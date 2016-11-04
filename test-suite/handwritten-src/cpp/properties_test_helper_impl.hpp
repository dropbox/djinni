//
//  properties_test_helper_impl.hpp
//  DjinniObjcTest
//
//  Created by Samuel Hall on 03/11/2016.
//  Copyright © 2016 Dropbox, Inc. All rights reserved.
//

#ifndef properties_test_helper_impl_hpp
#define properties_test_helper_impl_hpp

#include <string>

#include "properties_test_helper.hpp"

namespace testsuite {
    
class PropertiesTestHelperImpl : public PropertiesTestHelper {
    
private:
    
    int32_t m_item;
    std::string m_test_string;
    
public:
    
    int32_t get_item();
    void set_item(int32_t new_item);
    
    std::string get_test_string();
    void set_test_string(std::string new_test_string);
    
    int32_t int_method() {
        return 42;
    }
    
    std::string string_method(const std::string & value) {
        return value;
    }
};
    
}

#endif /* properties_test_helper_impl_hpp */
