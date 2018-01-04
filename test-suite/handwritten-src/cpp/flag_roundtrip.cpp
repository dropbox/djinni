#include "flag_roundtrip.hpp"

using namespace testsuite;

access_flags FlagRoundtrip::roundtrip_access(access_flags flag) {
  return flag;
}

empty_flags FlagRoundtrip::roundtrip_empty(empty_flags flag) {
  return flag;
}

std::experimental::optional<access_flags> FlagRoundtrip::roundtrip_access_boxed(std::experimental::optional<access_flags> flag) {
  return flag;
}

std::experimental::optional<empty_flags> FlagRoundtrip::roundtrip_empty_boxed(std::experimental::optional<empty_flags> flag) {
  return flag;
}
