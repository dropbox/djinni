# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

class FooListenerImpl:
    def on_string_change(self, prs):
        print ("FooListenerImpl.py: on_string_change prs", prs)
        self._prs = prs
        return self._prs
    def get_private_int(self):
        return self._pri
    def on_changes_string_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def on_changes_string_optional_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def on_changes_date_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def on_changes_binary_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def on_changes_int_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def on_changes_int_optional_returned(self, i, f, s, binar, b, d):
        print ("Not to be used")
    def delete_fl_in_fl(self):
        print ("Not to be used")
    def cause_py_exception(self, s):
        print ("Not to be used")
    def cause_zero_division_error(self):
        print ("Not to be used")
    def on_changes_record_returned(self, n1, n2):
        print ("Not to be used")

