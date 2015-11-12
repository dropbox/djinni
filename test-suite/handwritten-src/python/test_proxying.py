# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

from foo_receiver import FooReceiver
from foo_listener_bf import FooListenerBfHelper
from PyCFFIlib_cffi import ffi, lib
import gc

class FooListenerBfImpl:
    def delete_fl_in_fl(self):
        print ("Not to be used")
    def on_string_change(self, prs):
        print ("FooListenerImpl.py: on_string_change prs", prs)
        self._prs = prs
        return self._prs
    def get_string(self):
        return self._prs
    def set_listener_bf(self,fl):
        self._fl = fl
    def get_listener_bf(self):
        return self._fl
    def set_binary(self,b):
        print ("setting Binary in FooListenerBfImpl ", b)
        self._b = b
    def get_binary(self):
        return self._b
    def send_return(self,fl):
        return fl
    def create():
        # TODO: decide if we want to have this here or make checks in the helper.frompy for all
        # methods to exist as attributes on the class more lenient
        print ("I don't use it but the +p +c plus the check in fromPy for having all methods needs me to have this")

def fr_set_get(fr, fl, s):
    fr.add_listener_bf(fl)
    assert fr.set_private_bf_string(s) == s, "test_interface_back_forth failed"
    # assert fl._prs == s, "test_interface_back_forth failed"
    assert fr.get_listener_bf_string() == s, "test_interface_back_forth failed"

# back and forth via regular calls from python to cpp
def test_interface_back_forth():
    print ("start test len ", len(FooListenerBfHelper.c_data_set))
    fr = FooReceiver.create()
    fl = FooListenerBfImpl()            # python implementation of listener
    fl_cpp = fr.get_foo_listener_bf()   # cpp implementation of listener

    # both direct and indirect test for python impl of FooListenerBf
    fr_set_get(fr, fl, "Hello world!")

    # both direct and indirect test for cpp impl of FooListenerBf
    fr_set_get(fr, fl_cpp, "Goodbye world!")
    fr_set_get(fr, fl_cpp, "Goodbye world!")
    # send python implementation back and forth and see that it can still be used, and that no wrapper was added
    fl_1 = fr.send_return(fl)
    fl_2 = fr.send_return(fl_1)
    fr_set_get(fr, fl_2, "Hello")
    assert fl == fl_1 and fl_1 == fl_2, "test_interface_back_forth failed"

    # send cpp implementation back and forth and see that is can still be used, and handles hold same implementation
    fl_cpp_1 = fr.send_return(fl_cpp)
    fl_cpp_2 = fr.send_return(fl_cpp_1)
    fr_set_get(fr, fl_cpp_2, "Goodbye")
    assert lib.equal_handles_cw__foo_listener_bf(fl_cpp._cpp_impl, fl_cpp_1._cpp_impl) and \
            lib.equal_handles_cw__foo_listener_bf(fl_cpp_1._cpp_impl, fl_cpp_2._cpp_impl)

    fl = fl_1 = fl_2 = fl_cpp = fl_cpp_1 = fl_cpp_2 = None
    gc.collect()
    fr = None
    gc.collect()
    assert 0 == len(FooListenerBfHelper.c_data_set)

def fr_fl_set_get(fr, fl_in_fl, b):
    fr.set_listener_bf_in_listener_bf(fl_in_fl)
    fr.set_binary_in_listener_bf_in_listener_bf(b)
    assert b == fr.get_binary_in_listener_bf_in_listener_bf(), "test_interface_back_forth failed"

# back and forth via callbacks cpp to python
def test_interface_callback_back_forth():
    fr = FooReceiver.create()
    fl = FooListenerBfImpl()
    fr.add_listener_bf(fl)

    fl_in_fl = FooListenerBfImpl()
    b = b'Some Binary 11'
    fr_fl_set_get(fr, fl_in_fl, b) # listener 1 in python, listener 2 in python
    fl_in_fl_1 = fr.in_listener_bf_send_return(fl_in_fl)
    fl_in_fl_2 = fr.in_listener_bf_send_return(fl_in_fl_1)
    assert fl_in_fl == fl_in_fl_1 and fl_in_fl_1 == fl_in_fl_2, "test_interface_back_forth failed"
    fr_fl_set_get(fr, fl_in_fl_2, b) # listener 1 in python, listener 2 in python after back&forth

    fl_in_fl = fr.get_foo_listener_bf()
    b = b'Some Other Binary 12'
    fr_fl_set_get(fr, fl_in_fl, b) # listener 1 in python, listener 2 in cpp
    fl_in_fl_1 = fr.in_listener_bf_send_return(fl_in_fl)
    fl_in_fl_2 = fr.in_listener_bf_send_return(fl_in_fl_1)
    assert lib.equal_handles_cw__foo_listener_bf(fl_in_fl._cpp_impl, fl_in_fl_1._cpp_impl) and \
           lib.equal_handles_cw__foo_listener_bf(fl_in_fl_1._cpp_impl, fl_in_fl_2._cpp_impl)
    fr_fl_set_get(fr, fl_in_fl_2, b) # listener 1 in python, listener 2 in cpp after back&forth


    fl = fr.get_foo_listener_bf()
    fr.add_listener_bf(fl)

    fl_in_fl = FooListenerBfImpl()
    b = b'Some Binary 21'
    fr_fl_set_get(fr, fl_in_fl, b) # listener 1 in cpp, listener 2 in python
    fl_in_fl_1 = fr.in_listener_bf_send_return(fl_in_fl)
    fl_in_fl_2 = fr.in_listener_bf_send_return(fl_in_fl_1)
    assert fl_in_fl == fl_in_fl_1 and fl_in_fl_1 == fl_in_fl_2, "test_interface_back_forth failed"
    fr_fl_set_get(fr, fl_in_fl_2, b) # listener 1 in cpp, listener 2 in python after back&forth


    fl_in_fl = fr.get_foo_listener_bf()
    b = b'Some Other Binary 22'
    fr_fl_set_get(fr, fl_in_fl, b) # listener 1 in cpp, listener 2 in cpp
    fl_in_fl_1 = fr.in_listener_bf_send_return(fl_in_fl)
    fl_in_fl_2 = fr.in_listener_bf_send_return(fl_in_fl_1)
    assert lib.equal_handles_cw__foo_listener_bf(fl_in_fl._cpp_impl, fl_in_fl_1._cpp_impl) and \
           lib.equal_handles_cw__foo_listener_bf(fl_in_fl_1._cpp_impl, fl_in_fl_2._cpp_impl)
    fr_fl_set_get(fr, fl_in_fl_2, b) # listener 1 in cpp, listener 2 in cpp after back&forth

    fl = fl_in_fl = fl_in_fl_1 = fl_in_fl_2 = None
    gc.collect()
    fr = None
    gc.collect()
    assert 0 == len(FooListenerBfHelper.c_data_set)

