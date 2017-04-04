#include <algorithm>
#include <random>

#include "item_list.hpp"
#include "sort_order.hpp"

#include "sort_items_impl.hpp"

namespace textsort {

std::shared_ptr<SortItems> SortItems::create_with_listener(const std::shared_ptr<TextboxListener>& listener) {
    return std::make_shared<SortItemsImpl>(listener);
}

SortItemsImpl::SortItemsImpl (const std::shared_ptr<TextboxListener> & listener) {
    this->m_listener = listener;
}

void SortItemsImpl::sort(sort_order order, const ItemList & items) {
    auto lines = items.items;
    switch (order) {
        case sort_order::ASCENDING: {
            std::sort(lines.begin(), lines.end(), std::less<std::string>());
            break;
        }
        case sort_order::DESCENDING: {
            std::sort(lines.begin(), lines.end(), std::greater<std::string>());
            break;
        }
        case sort_order::RANDOM: {
            std::shuffle(lines.begin(), lines.end(), std::default_random_engine{});
            break;
        }
    }

    // Pass result to client interface
    this->m_listener->update(ItemList(lines));
}

ItemList SortItems::run_sort(const ItemList & items) {
    auto lines = items.items;
    std::sort(lines.begin(), lines.end(), std::less<std::string>());
    return ItemList(lines);
}

}
