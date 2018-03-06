# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

from foo_extensible_record_base import FooExtensibleRecordBase

class FooExtensibleRecord(FooExtensibleRecordBase):
    def ret5(self): # simple extension to a record
        return 5

