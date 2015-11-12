# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

# Example code written by a python developer to access cpp implementation of Foo
# This file should be hand-written by a python developer


from foo_interface import FooInterface
from djinni.support import decoded_utf_8
import sys
PYTHON3 = sys.version_info[0] >= 3

def test_ints():
    foo = FooInterface.create()
    iSet = 8
    foo.set_private_int32(iSet)
    assert iSet == foo.get_private_int32(), "TestInts failed"

    iSet = 18
    foo.set_private_int32(foo.int32_inverse(iSet))
    assert -iSet == foo.get_private_int32(), "TestInts failed"

    #foo.__del__()
    #assert 0 == 1
    print ("TestInts succeeded")

def test_interface_as_return_value():
    foo = FooInterface.create()
    foo_primitives = foo.get_foo_primitives()

    # Test a foo_primitives specific feature (impl of bools) works
    b = True
    foo_primitives.set_bool(b)
    assert b == foo_primitives.get_bool(), "test_bool failed"

    b = False
    foo_primitives.set_bool(b)
    assert b == foo_primitives.get_bool(), "test_bool failed"


# Can set: unicode strings (python 2 and 3), bytes utf-8 encoded (python 3)
# Will get: utf-8 encoded strings, and utf-8 encoded bytes respectively
DECODEUtf8 = 1
def test_strings():
    foo = FooInterface.create()
    strs = dict([
            # PYTHON 2 and 3 unicode strings
            (u"", not DECODEUtf8),
            (u"my\0text", not DECODEUtf8),
            (u"the best text", not DECODEUtf8),
            (u"my \b friend", not DECODEUtf8),
            #"Non-ASCII / 非 ASCII 字符"
            (u"Non-ASCII / \xe9\x9d\x9e ASCII \xe5\xad\x97\xe7\xac\xa6", not DECODEUtf8),
            (u"Non-ASCII / \u975e ASCII \u5b57\u7b26", not DECODEUtf8)
        ])
    if PYTHON3:
        strs.update({
            chr(40960) + u'abcd' + chr(1972) + u"\0\bhi": not DECODEUtf8, #unicode string
            bytes(chr(40960) + u'abcd' + chr(1972) + u"\0\bhi", 'utf-8'): DECODEUtf8 # bytes utf-8 encoded
            })
    else:
        strs.update({
            unichr(40960) + u'abcd' + unichr(1972) + u"\0\bhi": not DECODEUtf8, #unicode string for python 2
            })

    for sSet, decode in strs.items():
        foo.set_private_string(sSet)

        sSetUnicode = sSet
        if decode:
            sSetUnicode = decoded_utf_8(sSet)
        sGetUnicode = foo.get_private_string()

        # print ("client SetPrs=", sSetUnicode, ".", len(sSetUnicode), List(sSetUnicode) )
        # print ("client GetPrs=", sGetUnicode, ".", len(sGetUnicode), List(sGetUnicode))
        assert sSetUnicode == sGetUnicode

    print ("TestStrings succeeded")

def test_abc_direct_instantiate():
    try:
        f = FooInterface()
        assert False, "Instantiated abstract base class"
    except:
        pass

def test_abc_subclass_instantiate():
    class FooInterfaceSub(FooInterface):
        pass
    try:
        dummy = FooInterfaceSub()
        assert False, "Instantiated abstract base class"
    except TypeError:
        pass

def test_abc_missing_method_instantiate():
    class FooInterfaceSub(FooInterface):
        def int32_inverse(self, x):
            pass
        def set_private_int32(self, private_int):
            pass
        def get_private_int32(self):
            pass
        def set_private_string(self, private_string):
            pass
        #def get_private_string(self):
        #    pass
        def get_set_strings(self, ps1, ps2):
            pass
        def get_foo_primitives(self):
            pass
    try:
        dummy = FooInterfaceSub()
        assert False, "Instantiated abstract base class"
    except TypeError:
        pass

def test_abc_successful_instantiate():
    class FooInterfaceSub(FooInterface):
        def int32_inverse(self, x):
            pass
        def set_private_int32(self, private_int):
            pass
        def get_private_int32(self):
            pass
        def set_private_string(self, private_string):
            pass
        def get_private_string(self):
            pass
        def get_set_strings(self, ps1, ps2):
            pass
        def get_foo_primitives(self):
            pass
    dummy = FooInterfaceSub()
