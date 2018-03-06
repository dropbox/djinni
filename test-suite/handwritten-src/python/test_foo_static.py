# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

from foo_static import FooStatic

import datetime

def test_static_counter():
    FooStatic.update_static_counter_to(100) # 100
    FooStatic.update_static_counter_by(1) # 101
    FooStatic.update_static_counter_by(2) # 103
    FooStatic.update_static_counter_by(3) # 106
    assert FooStatic.update_static_counter_by(4) == 110, "test_static_counter failed"
    assert FooStatic.get_static_counter() == 110, "test_static_counter failed"

def test_static_message():
    assert FooStatic.update_static_messg_to("hello world") == "hello world", "test_static_message failed"
    assert FooStatic.get_static_messg() == "hello world", "test_static_message failed"

    assert FooStatic.update_static_messg_to("") == "", "test_static_message failed"
    assert FooStatic.get_static_messg() == "", "test_static_message failed"

def test_static_date():
    val = datetime.datetime(2015, 9, 12)
    assert val == FooStatic.date_id(val), "test_static_date failed"

def test_static_opt_date():
    val = datetime.datetime(2015, 9, 12)
    assert val == FooStatic.opt_date_id(val), "test_static_opt_date failed"

    #assert FooStatic.opt_date_id(None) is None, "test_static_opt_date failed"
