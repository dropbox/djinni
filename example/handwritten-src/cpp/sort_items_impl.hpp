#pragma once

#include "sort_items.hpp"
#include "textbox_listener.hpp"
#include "textbox_reset_listener.hpp"

namespace textsort {

class SortItemsImpl : public SortItems {

public:
    SortItemsImpl(const std::shared_ptr<TextboxListener> & listener, const std::shared_ptr<TextboxResetListener> & reset_listener);
    virtual void reset() override;
    virtual void sort(sort_order order, const ItemList & items) override;

private:
    std::shared_ptr<TextboxListener> m_listener;
    std::shared_ptr<TextboxResetListener> m_reset_listener;

};

}
