# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals
import pytest
import datetime
import sys
import gc
from djinni.exception import DjinniException

from foo_listener_impl import FooListenerImpl
from foo_receiver import FooReceiver
from foo_listener import FooListenerHelper
from foo_some_other_record import FooSomeOtherRecord

# add foo listener impl here with variable set inside
class FooListenerImpl_SimpleTest:
    def __init__(self):
        self._local_false = True
        self._local_true = False
    def on_string_change(self, prs):
        self._prs = prs
        self._local_false = False
        self._local_true = True
        return self._prs
    def get_private_int(self):
        return self._pri
    def get_private_int_optional(self):
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
    def on_changes_record_returned(self, n1, n2):
        print ("Not to be used")
    def cause_py_exception(self, s):
        print ("Not to be used")
    def cause_zero_division_error(self):
        print ("Not to be used")

class FooListenerImpl_FullTest:
    def on_string_change(self, pri, prs):
        print ("Not to be used")
    def get_private_int(self):
        print ("Not to be used")
    def get_private_int_optional(self):
        print ("Not to be used")
    def cause_py_exception(self, s):
        print ("raising exception in cause_py_exception")
        raise Exception(s)
    def cause_zero_division_error(self):
        raise ZeroDivisionError
    def on_changes_string_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_prs
    def on_changes_string_optional_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_prs
    def on_changes_date_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_prdate
    def on_changes_binary_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_prbin
    def on_changes_int_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_pri
    def on_changes_int_optional_returned(self, i, f, s, binar, b, d):
        self._local_pri = i
        self._local_prf = f
        self._local_prs = s
        self._local_prbin = binar
        self._local_prbool = b
        self._local_prdate = d
        return self._local_pri
    def on_changes_record_returned(self, n1, n2):
        return FooSomeOtherRecord(n1,n2)

def test_simple_callback_indirect():
    fr = FooReceiver.create()
    fl = FooListenerImpl()
    fr.add_listener(fl)

    # indirect check via returned value from receiver
    s = "Hello world!"
    assert fr.set_private_string(s) == s, "test_simple_callback_indirect failed"
    fl = None
    fr = None
    gc.collect()

def test_simple_callback_direct():
    fr = FooReceiver.create()
    fl = FooListenerImpl_SimpleTest()
    fr.add_listener(fl)

    s = ""
    assert fr.set_private_string(s) == s

    # direct check via peek inside listener
    assert fl._local_true == True and fl._local_false == False, "test_simple_callback_direct failed"
    fl = None
    fr = None
    gc.collect()

def fr_fl(fr, fl):
    i32 = 32 # could use limit number, and add other integer
    f32 = 32.0
    s = "string"
    binar = b'binary'
    b = True
    d = datetime.datetime(2007,4,17,1,2,3)

    assert fr.cause_changes_string_returned(i32, f32, s, binar, b, d) == s, "test_full_callback failed"
    assert fr.cause_changes_binary_returned(i32, f32, s, binar, b, d) == binar, "test_full_callback failed"
    assert fr.cause_changes_date_returned(i32, f32, s, binar, b, d) == d, "test_full_callback failed"
    assert fr.cause_changes_int_returned(i32, f32, s, binar, b, d) == i32, "test_full_callback failed"

    # non none optional
    assert fr.cause_changes_string_optional_returned(i32, f32, s, binar, b, d) == s, "test_full_callback failed"
    assert fr.cause_changes_int_optional_returned(i32, f32, s, binar, b, d) == i32, "test_full_callback failed"

    # direct check via peek inside listener
    assert fl._local_pri == i32 and \
           fl._local_prf == f32 and \
           fl._local_prs == s and \
           fl._local_prbin == binar and \
           fl._local_prbool == b and \
           fl._local_prdate == d, "test_full_callback failed"

    # none optionals
    assert fr.cause_changes_string_optional_returned(i32, f32, None, binar, b, d) is None, "test_full_callback failed"
    assert fr.cause_changes_string_optional_returned(None, f32, None, binar, b, d) is None, "test_full_callback failed"
    assert fr.cause_changes_int_optional_returned(None, f32, s, binar, b, d) is None, "test_full_callback failed"
    assert fr.cause_changes_int_optional_returned(None, f32, None, binar, b, d) is None, "test_full_callback failed"

def test_full_callback():
    fr = FooReceiver.create()
    fl = FooListenerImpl_FullTest()
    fl_opt = FooListenerImpl_FullTest()
    fr.add_listener(fl)
    fr_fl(fr,fl)

    fr.add_optional_listener(fl_opt)
    fr_fl(fr,fl_opt)

    fr.add_optional_listener(None)
    assert fr.get_optional_listener() is None, "test_full_callback failed"

    fl = None
    gc.collect()
    fr = None
    gc.collect()
    assert 0 == len(FooListenerHelper.c_data_set), "test_full_callback failed"

def test_exceptions():
    fr = FooReceiver.create()
    exception_arg = "cpp_impl_error!"

    # pytest.raises(RuntimeError(exception_arg), fr.cause_cpp_exception, exception_arg) # tried to simplify like this, did not work yet

    # CAUSE CUSTOM EXCEPTION in CPP IMPLEMENTATION
    try:
        fr.cause_cpp_exception(exception_arg)
    except DjinniException as e:
        # Note: fully checking exception equality requires type check as well; not sure if needed for our purposes
        assert e.args == DjinniException(exception_arg).args

    fl = FooListenerImpl_FullTest()
    fr.add_listener(fl)

    # CAUSE CUSTOM EXCEPTION in PY IMPLEMENTATION
    exception_arg = "py_impl_error!"
    try:
        fr.cause_py_exception(exception_arg)
    except Exception as e:
        # Note: fully checking exception equality requires type check as well; not sure if needed for our purposes
        assert e.args == Exception(exception_arg).args

    # CAUSE REGULAR PY EXCEPTION IN PY
    pytest.raises(ZeroDivisionError, fr.cause_zero_division_error)

    from djinni.exception import ExceptionHelper
    assert len(ExceptionHelper.c_data_set) == 0

