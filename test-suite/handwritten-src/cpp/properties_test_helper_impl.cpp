//
//  properties_test_helper_impl.cpp
//  DjinniObjcTest
//
//  Created by Samuel Hall on 03/11/2016.
//  Copyright © 2016 Dropbox, Inc. All rights reserved.
//

#include "properties_test_helper_impl.hpp"

namespace testsuite {
    
    std::shared_ptr<PropertiesTestHelper> PropertiesTestHelper::create_new() {
        return std::make_shared<PropertiesTestHelperImpl>();
    }
    
    int32_t PropertiesTestHelperImpl::get_item() {
        return m_item;
    }

    void PropertiesTestHelperImpl::set_item(int32_t new_item)  {
        m_item = new_item;
    }
    
    std::string PropertiesTestHelperImpl::get_test_string() {
        return m_test_string;
    }
    
    void PropertiesTestHelperImpl::set_test_string(std::string new_test_string) {
        m_test_string = new_test_string;
    }
}
