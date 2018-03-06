# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals
import datetime
import gc
import pytest
import sys
sys.path.append("/Users/iuliatamas/djinni/test-suite/generated-src/python")
# print (sys.path)

from foo_containers_interface import FooContainersInterface
from foo_containers_record import FooContainersRecord
from foo_some_other_record import FooSomeOtherRecord

def set_get(ci, optional_list_int, list_int, list_binary, list_optional_binary, list_list_string,
    list_record, optional_map_string_int, map_string_int, map_string_string, map_optional_string_optional_string, map_int_list_date,
    optional_set_string, set_string, set_optional_string, map_int_set_string, map_optional_int_set_string):

    set_rec = FooContainersRecord(optional_list_int, list_int, list_binary, list_binary, list_list_string, \
     list_record, optional_map_string_int, map_string_int, map_string_string, map_string_string, map_int_list_date, \
     optional_set_string, set_string, set_string, map_int_set_string, map_optional_int_set_string)

    ci.set_containers_record(set_rec)
    get_rec = ci.get_containers_record()
    eq(set_rec, get_rec)

def set_get_optional(ci, optional_list_int, list_int, list_binary, list_optional_binary, list_list_string,
    list_record, optional_map_string_int, map_string_int, map_string_string, map_optional_string_optional_string, map_int_list_date,
    optional_set_string, set_string, set_optional_string, map_int_set_string, map_optional_int_set_string):

    set_rec = FooContainersRecord(optional_list_int, list_int, list_binary, list_binary, list_list_string, \
     list_record, optional_map_string_int, map_string_int, map_string_string, map_string_string, map_int_list_date, \
     optional_set_string, set_string, set_string, map_int_set_string, map_optional_int_set_string)

    ci.set_optional_containers_record(set_rec)
    get_rec = ci.get_optional_containers_record()
    eq(set_rec, get_rec)

# TODO: add some informative messafe to assert fail
def eq(set_rec, get_rec):
    if set_rec.optional_list_int is None:
        assert get_rec.optional_list_int is None
    else:
        assert set_rec.optional_list_int == get_rec.optional_list_int

    if set_rec.optional_map_string_int is None:
        assert get_rec.optional_map_string_int is None
    else:
        assert set_rec.optional_map_string_int == get_rec.optional_map_string_int

    if set_rec.optional_set_string is None:
        assert get_rec.optional_set_string is None
    else:
        assert set_rec.optional_set_string == get_rec.optional_set_string

    assert set_rec.list_int == get_rec.list_int and \
        set_rec.list_binary == get_rec.list_binary and \
        set_rec.map_optional_string_optional_string == get_rec.map_optional_string_optional_string and \
        set_rec.list_list_string == get_rec.list_list_string and \
        set_rec.map_string_int == get_rec.map_string_int and \
        set_rec.map_string_string == get_rec.map_string_string and \
        set_rec.map_int_list_date == get_rec.map_int_list_date and \
        set_rec.set_string == get_rec.set_string and \
        set_rec.set_optional_string == get_rec.set_optional_string and \
        set_rec.map_int_set_string == get_rec.map_int_set_string and \
        set_rec.map_optional_int_set_string == get_rec.map_optional_int_set_string and \
        set_rec.list_optional_binary == get_rec.list_optional_binary #and \
        # set_rec.set_record == get_rec.set_record

    for i, s_r in enumerate(set_rec.list_record):
        assert s_r.__dict__ == get_rec.list_record[i].__dict__

def test_set_get_record():
    ci = FooContainersInterface.create()

    optional_list_int = None
    list_int = list([5,6,7,8,9])
    list_int_2 = list_int # possible to use same list as argument twice, as we use multiset
    list_binary = list([b'some_binary', b'some_other_binary'])
    list_string1 = list(['hi', '', 'bye'])
    list_string2 = list(['hi2', '', 'bye2'])
    list_list_string = list([list_string1, list_string2])
    number1 = 10
    number2 = -10
    some_record_1 = FooSomeOtherRecord(number1, number2)
    some_record_2 = FooSomeOtherRecord(number1 * 2, number2 * 2)
    list_record = list([some_record_1, some_record_2])
    set_record = set([some_record_1, some_record_1])
    optional_map_string_int = None
    map_string_int = dict([('josh', 4139), ('anna', 4127), ('jack', 4098)])
    map_string_string = dict([('josh', 'val1'), ('anna', 'val2'), ('jack', 'val3')])
    list_date1 = list([datetime.datetime(2007,4,17,1,2,3)])
    list_date2 = list([datetime.datetime(2007,4,17,1,2,4)])
    map_int_list_date = dict([(18, list_date1), (5, list_date2)])
    optional_set_string = None
    set_string = set(['hi', 'bye'])
    map_int_set_string = dict([(18, set_string)])
    map_optional_int_set_string = dict([(18, set_string), (None, set_string)])
    list_optional_binary = list([b'some_binary', None])
    map_optional_string_optional_string = dict([('anna', None), (None, None)])
    set_optional_string = set(['hi', None])
    # set_record = set(list[some_record_1, some_record_2])

    # Passing optional list as argument
    ci.set_optional_list_int(list_int)
    assert ci.get_optional_list_int() == list_int
    ci.set_optional_list_int(None)
    assert ci.get_optional_list_int() is None

    # Passing optional map as argument
    ci.set_optional_map_string_int(map_string_int)
    assert ci.get_optional_map_string_int() == map_string_int
    ci.set_optional_map_string_int(None)
    assert ci.get_optional_map_string_int() is None

    # Passing non-Nones for the optional arguments
    set_get(ci, list_int_2, list_int, list_binary, list_binary, list_list_string, \
     list_record, map_string_int, map_string_int, map_string_string, map_string_string, map_int_list_date, \
       set_string, set_string, set_string, map_int_set_string, map_optional_int_set_string)

    set_get(ci, list(), list(), list(), list(), list(list()), list(), dict(), dict(), dict(), dict(), dict(), set(), set(), set(), dict(), dict())

    # Passing Nones for the optional arguments
    set_get(ci, optional_list_int, list_int, list_binary, list_optional_binary, list_list_string, \
     list_record, optional_map_string_int, map_string_int, map_string_string, map_optional_string_optional_string, map_int_list_date, \
      optional_set_string, set_string, set_optional_string, map_int_set_string, map_optional_int_set_string)

    # Passing non-None for optional record argument
    set_get_optional(ci, list(), list(), list(), list(), list(list()), list(), dict(), dict(), dict(), dict(), dict(), set(), set(), set(), dict(), dict())
    set_get_optional(ci, optional_list_int, list_int, list_binary, list_optional_binary, list_list_string, \
     list_record, optional_map_string_int, map_string_int, map_string_string, map_optional_string_optional_string, map_int_list_date, \
      optional_set_string, set_string, set_optional_string, map_int_set_string, map_optional_int_set_string)

    # Passing None for optional record argument
    ci.set_optional_containers_record(None)
    get_rec = ci.get_optional_containers_record()
    assert get_rec is None

    print ("Forcing garbage collection")
    set_rec = None
    get_rec = None
    some_record_2 = None
    some_record_1 = None
    list_record = None
    # set_record = None
    list_string1 = None
    list_string2 = None
    list_list_string = None

    list_date1 = None
    list_date2 = None
    map_int_list_date = None
    gc.collect()

    FooContainersRecord.check_c_data_set_empty()
    FooSomeOtherRecord.check_c_data_set_empty()

    # print ("GOT MAP ", get_rec._map_string_int)
    # TODO: more tests: ex: map_binary_list_record

    # print ("got ", get_rec)
    # print ("_list_list_string", get_rec._list_list_string)
    # print ("_list_optional_binary", get_rec._list_optional_binary)
    # print ("_map_opt_str_opt_str", set_rec._map_optional_string_optional_string, get_rec._map_optional_string_optional_string, map_optional_string_optional_string)
    # print ("_list_record", get_rec._list_record)

def test_set_get_list():
    ci = FooContainersInterface.create()

    list_binary = list([b'binary_test', b''])

    ci.set_list_binary(list_binary)
    assert ci.get_list_binary() == list_binary
    # print ("_list_binary", ci.get_list_binary())

'''
def test_set_get_list():
    # TODO: good closer to complete test, for when time allows
    ci = FooContainersInterface.create()

    list_int = list([5,6,7,8,9])
    list_binary = list([b'some_binary', b'some_other_binary'])

    list_string1 = list(['hi', '', 'bye'])
    list_string2 = list(['hi2', '', 'bye2'])
    list_list_string = list([list_string1, list_string2])

    number1 = 10
    number2 = -10
    some_record_1 = FooSomeOtherRecord(number1, number2)
    some_record_2 = FooSomeOtherRecord(number1 * 2, number2 * 2)
    list_record = list([some_record_1, some_record_2])

    ci.set_list_int(list_int)
    assert ci.get_list_int == list_int

    ci.set_list_binary(list_binary)
    assert ci.get_list_binary == list_binary

    ci.set_list_string(list_string)
    assert ci.get_list_string == list_string

    ci.set_list_record(list_record)
    get_list_record = ci.get_list_record
    for i, s_r in enumerate(list_record):
        assert s_r.__dict__ == get_list_record[i].__dict__

'''

'''
    set_list_int(lb: list<int>);
    get_list_int(): list<int>;
'''
