# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals
import pytest
# Example code written by a python developer to access cpp implementation of Foo
# This file should be hand-written by a python developer

from foo_primitives import FooPrimitives
from djinni.support import decoded_utf_8
import datetime
import sys
PYTHON3 = sys.version_info[0] >= 3

from PyCFFIlib_cffi import lib

# testing climits: http://www.cplusplus.com/reference/climits/
def test_int8():
    foo = FooPrimitives.create()

    min_i8 = -128
    foo.set_int8(min_i8)
    assert min_i8 == foo.get_int8(), "test_ints failed"
    max_i8 = 127
    foo.set_int8(max_i8)
    assert max_i8 == foo.get_int8(), "test_ints failed"

    pytest.raises(OverflowError, foo.set_int8, min_i8 - 1)
    pytest.raises(OverflowError, foo.set_int8, max_i8 + 1)

# testing climits: http://www.cplusplus.com/reference/climits/
def test_int16():
    foo = FooPrimitives.create()

    min_i16 = -32768
    foo.set_int16(min_i16)
    assert min_i16 == foo.get_int16(), "test_ints failed"
    max_i16 = 32767
    foo.set_int16(max_i16)
    assert max_i16 == foo.get_int16(), "test_ints failed"

    pytest.raises(OverflowError, foo.set_int16, min_i16 - 1)
    pytest.raises(OverflowError, foo.set_int16, max_i16 + 1)

def test_int32():
    foo = FooPrimitives.create()

    min_i32 = -2147483648
    foo.set_int32(min_i32)
    assert min_i32 == foo.get_int32(), "test_ints failed"
    max_i32 = 2147483647
    foo.set_int32(max_i32)
    assert max_i32 == foo.get_int32(), "test_ints failed"

    pytest.raises(OverflowError, foo.set_int32, min_i32 - 1)
    pytest.raises(OverflowError, foo.set_int32, max_i32 + 1)

def test_int64():
    foo = FooPrimitives.create()

    min_i64 = -9223372036854775808
    foo.set_int64(min_i64)
    assert min_i64 == foo.get_int64(), "test_ints failed"
    max_i64 = 9223372036854775807
    foo.set_int64(max_i64)
    assert max_i64 == foo.get_int64(), "test_ints failed"

    pytest.raises(OverflowError, foo.set_int64, min_i64 - 1)
    pytest.raises(OverflowError, foo.set_int64, max_i64 + 1)

def test_float():
    foo = FooPrimitives.create()

    min_f32 = lib.min_f32_t()
    foo.set_float(min_f32)
    assert min_f32 == foo.get_float(), "test_float failed"
    max_f32 = lib.max_f32_t()
    foo.set_float(max_f32)
    assert max_f32 == foo.get_float(), "test_float failed"


def test_double():
    foo = FooPrimitives.create()

    min_f64 = lib.min_f64_t()
    foo.set_double(min_f64)
    assert min_f64 == foo.get_double(), "test_double failed"
    max_f64 = lib.max_f64_t()
    foo.set_double(max_f64)
    assert max_f64 == foo.get_double(), "test_double failed"


def test_binary():
    foo = FooPrimitives.create()
    b = b''
    foo.set_binary(b)
    bGet = foo.get_binary()
    assert b == bGet, "test_binary failed"
    assert type(bGet) is bytes

    b = b'123g'
    foo.set_binary(b)
    bGet = foo.get_binary()
    assert b == bGet, "test_binary failed"
    assert type(bGet) is bytes

def test_bool():
    foo = FooPrimitives.create()
    b = True
    foo.set_bool(b)
    assert b == foo.get_bool(), "test_bool failed"

    b = False
    foo.set_bool(b)
    assert b == foo.get_bool(), "test_bool failed"

def test_date():
    epoch = datetime.datetime.utcfromtimestamp(0)

    foo = FooPrimitives.create()
    d = datetime.datetime(2007,4,17,1,2,3)
    foo.set_date(d)
    assert d == foo.get_date(), "test_date failed"

    for i in range(100): # more reliable
        d = datetime.datetime.now()
        d = d.replace(microsecond = (d.microsecond // 1000) * 1000)  # our precision is millisecond level

        foo.set_date(d)
        assert d == foo.get_date(), "test_date failed"


# Can set: unicode strings (python 2 and 3), bytes utf-8 encoded (python 3)
# Will get: utf-8 encoded strings, and utf-8 encoded bytes respectively
DECODEUtf8 = 1
def test_strings():
    foo = FooPrimitives.create()
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
        foo.set_string(sSet)

        sSetUnicode = sSet
        if decode:
            sSetUnicode = decoded_utf_8(sSet)
        sGetUnicode = foo.get_string()

        # print ("client SetPrs=", sSetUnicode, ".", len(sSetUnicode), List(sSetUnicode) )
        # print ("client GetPrs=", sGetUnicode, ".", len(sGetUnicode), List(sGetUnicode))
        assert sSetUnicode == sGetUnicode


