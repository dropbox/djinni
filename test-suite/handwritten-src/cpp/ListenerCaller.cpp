#include "listener_caller.hpp"

#include "first_listener.hpp"
#include "second_listener.hpp"

namespace testsuite {

class ListenerCallerImpl : public ListenerCaller {
public:
    ListenerCallerImpl(const std::shared_ptr<FirstListener> &firstListener, const std::shared_ptr<SecondListener> &secondListener)
    : firstListener(firstListener), secondListener(secondListener)
    {}

    void callFirst() override {
        firstListener->first();
    }

    void callSecond() override {
        secondListener->second();
    }

private:
    std::shared_ptr<FirstListener> firstListener;
    std::shared_ptr<SecondListener> secondListener;
};

std::shared_ptr<ListenerCaller> ListenerCaller::init(
    const std::shared_ptr<FirstListener> &firstListener,
    const std::shared_ptr<SecondListener> &secondListener) {

    return std::make_shared<ListenerCallerImpl>(firstListener, secondListener);
}

} // namespace testsuite
