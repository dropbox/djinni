#include "return_one.hpp"
#include "return_two.hpp"

namespace testsuite {

class ReturnOneTwo : public ReturnOne, public ReturnTwo {
public:
    static std::shared_ptr<ReturnOneTwo> shared_instance() {
        static auto instance = std::make_shared<ReturnOneTwo>();
        return instance;
    }

    int8_t return_one() override { return 1; }
    int8_t return_two() override { return 2; }
};

std::shared_ptr<ReturnOne> ReturnOne::get_instance() {
    return ReturnOneTwo::shared_instance();
}

std::shared_ptr<ReturnTwo> ReturnTwo::get_instance() {
    return ReturnOneTwo::shared_instance();
}

}  // namespace testsuite
