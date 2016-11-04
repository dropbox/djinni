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
    std::vector<int32_t> m_test_list;
    
public:
    
    int32_t get_item();
    void set_item(int32_t new_item);
    
    std::string get_test_string();
    void set_test_string(std::string new_test_string);
    
    std::vector<int32_t> get_test_list();
    void set_test_list(std::vector<int32_t> new_test_list);
};
    
}

#endif /* properties_test_helper_impl_hpp */
