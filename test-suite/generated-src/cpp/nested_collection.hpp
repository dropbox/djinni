// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from nested_collection.djinni

#pragma once

#include <string>
#include <unordered_set>
#include <utility>
#include <vector>

namespace djinni_generated {

struct NestedCollection final
{
    std::vector<std::unordered_set<std::string>> set_list;

    NestedCollection(std::vector<std::unordered_set<std::string>> set_list)
    : set_list(std::move(set_list))
    {}
};

}  // namespace djinni_generated
