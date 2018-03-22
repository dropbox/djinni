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

    std::string PropertiesTestHelperImpl::other_method(const std::string & argument) {
        return argument;
    }

    int32_t PropertiesTestHelperImpl::get_item() const {
        return m_item;
    }

    void PropertiesTestHelperImpl::set_item(int32_t new_item)  {
        m_item = new_item;
    }
    
    std::string PropertiesTestHelperImpl::get_test_string() const {
        return m_test_string;
    }
    
    void PropertiesTestHelperImpl::set_test_string(const std::string & new_test_string) {
        m_test_string = new_test_string;
    }
    
    std::vector<int32_t> PropertiesTestHelperImpl::get_test_list() const {
        return m_test_list;
    }
    
    void PropertiesTestHelperImpl::set_test_list(const std::vector<int32_t> & new_test_list) {
        m_test_list = new_test_list;
    }

    bool PropertiesTestHelperImpl::get_read_only_bool() const {
        return m_read_only_bool;
    }
}
