#pragma once

#include <boost/optional.hpp>

using boost::optional;

// A common functional way of representing error conditions. The left value is
// typically an error and the right value is a correct ("right") value.
template<typename LeftType, typename RightType>
struct either {
public:
    either(LeftType left) : mLeft(std::move(left)) {}
    either(RightType right) : mRight(std::move(right)) {}

    bool isLeft() const throw() { return static_cast<bool>(mLeft); }
    bool isRight() const throw() { return static_cast<bool>(mRight); }

    LeftType left() const { return *mLeft; }
    RightType right() const { return *mRight; }

private:
    const optional<LeftType> mLeft;
    const optional<RightType> mRight;
};
