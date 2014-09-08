#include "sort_items_impl.hpp"
#include <algorithm>

namespace textsort {

std::shared_ptr<SortItems> SortItems::create_with_listener(const std::shared_ptr<TextboxListener>& listener) {
    return std::make_shared<SortItemsImpl>(listener);
}

SortItemsImpl::SortItemsImpl (const std::shared_ptr<TextboxListener> & listener) {
    this->m_listener = listener;
}

void SortItemsImpl::sort (const ItemList & items) {
    auto list = items.items;
    std::sort(list.begin(), list.end());

    // Pass result to client interface
    this->m_listener->update(ItemList(list));
}

}
