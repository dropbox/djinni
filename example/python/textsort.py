from __future__ import print_function
import sys

from sort_order import SortOrder
from sort_items import SortItems
from item_list import ItemList
from textbox_listener import TextboxListener

print("Running Djinni Python example.")
print("Python version: ", sys.version_info)

test_item_list = ['a', 'z', 'b']
active_items = ItemList(test_item_list)

class MyListener(TextboxListener):
    def update(self, items):
        global active_items
        active_items = items

listener = MyListener()
si = SortItems.create_with_listener(listener)

print("original items: ", active_items.items)
assert active_items.items == test_item_list

si.sort(SortOrder.Ascending, active_items)

print("sorted items: ", active_items.items)
assert active_items.items == sorted(test_item_list)
