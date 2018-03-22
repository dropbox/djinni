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
    bool m_read_only_bool = true;
    
public:
    std::string other_method(const std::string & argument) override;

    int32_t get_item() const override;
    void set_item(int32_t new_item) override;
    
    std::string get_test_string() const override;
    void set_test_string(const std::string & new_test_string) override;
    
    std::vector<int32_t> get_test_list() const override;
    void set_test_list(const std::vector<int32_t> & new_test_list) override;

    bool get_read_only_bool() const override;
};
    
}

#endif /* properties_test_helper_impl_hpp */
