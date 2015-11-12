# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals
import pytest
import gc
import sys
sys.path.append("/Users/iuliatamas/djinni/test-suite/generated-src/python")
# print (sys.path)

from foo_client_interface import FooClientInterface
from foo_client_returned_record import FooClientReturnedRecord
from foo_some_other_record import FooSomeOtherRecord
from foo_extensible_record import FooExtensibleRecord

# TODO rename this test to something more suggestive

def test_client_returned_record():
    ci = FooClientInterface.create()

    number1 = 10
    number2 = -10
    some_record = FooSomeOtherRecord(number1, number2)

    record_id = 28
    content = "Hello World"
    set_rec = FooClientReturnedRecord(record_id, content, some_record)

    ci.set_record(set_rec)
    get_rec = ci.get_record()

    assert set_rec.record_id == record_id and \
           set_rec.content == content and \
           set_rec.some_record.__dict__ == some_record.__dict__

    print ("Forcing garbage collection in test_client_interface_py")
    set_rec = None
    get_rec = None
    some_record = None
    gc.collect()

    FooClientReturnedRecord.check_c_data_set_empty()
    FooSomeOtherRecord.check_c_data_set_empty()

def eq_neq(rec_1a, rec_1b, rec_1c, rec_less_1, rec_greater_1):
    assert rec_1a == rec_1b and rec_1b == rec_1c and \
            rec_1a != rec_less_1 and rec_less_1 != rec_1a and \
            rec_1a != rec_greater_1 and rec_greater_1 != rec_1a, "test_client_returned_record_derivings failed"

def lt_gt(rec_less_1, rec_1a):
    assert rec_less_1.__lt__(rec_1a) and \
             rec_1a.__gt__(rec_less_1), "test_client_returned_record_derivings failed"

def test_client_returned_record_derivings():
    number1 = 10
    number2 = 20
    some_record_1a = FooSomeOtherRecord(number1, number2)
    some_record_1b = FooSomeOtherRecord(number1, number2)
    some_record_1c = some_record_1a

    some_record_less_1 = FooSomeOtherRecord(number1 -2, number2)
    some_record_less_2 = FooSomeOtherRecord(number1 , number2 -2)
    some_record_greater_1 = FooSomeOtherRecord(number1 +2, number2)
    some_record_greater_2 = FooSomeOtherRecord(number1 +2, number2 -2)

    eq_neq(some_record_1a, some_record_1b, some_record_1c, some_record_less_1, some_record_greater_1)
    lt_gt(some_record_less_1, some_record_1a)
    lt_gt(some_record_less_2, some_record_1a)

    pytest.raises(AssertionError, lt_gt, some_record_greater_2, some_record_1a)

    record_id = 28
    content = "Hello World"
    rec_1a = FooClientReturnedRecord(record_id, content, some_record_1a)
    rec_1b = FooClientReturnedRecord(record_id, content, some_record_1b)
    rec_1c = rec_1a

    rec_less_1 = FooClientReturnedRecord(record_id - 2, content, some_record_1a)
    rec_less_2 = FooClientReturnedRecord(record_id, "A " + content, some_record_1a)
    rec_less_3 = FooClientReturnedRecord(record_id, content, some_record_less_1)
    rec_greater_1 = FooClientReturnedRecord(record_id + 2, content, some_record_1b)

    eq_neq(rec_1a, rec_1b, rec_1c, rec_less_1, rec_greater_1)
    lt_gt(rec_less_1, rec_1a)
    lt_gt(rec_less_2, rec_1a)
    lt_gt(rec_less_3, rec_1a)

def test_extensible_record():
    ci = FooClientInterface.create()

    number1 = 10
    number2 = 1
    string1 = "hello"
    string2 = "does not matter"
    set_rec = FooExtensibleRecord(number1, string1)
    set_rec.number2 = number2                    # to confirm it does not matter we are sending extended rather than base record
    set_rec.string2 = string2    # to confirm it does not matter we are sending extended rather than base record

    ci.set_extensible_record(set_rec)
    got_rec = ci.get_extensible_record()

    assert ci.get_extensible_record_number2() == number1 * 2, "test_extensible_record failed"
    assert ci.get_extensible_record_string2() == string1 + string1, "test_extensible_record failed"

    assert set_rec.number1 == got_rec.number1 and\
            set_rec.string1 == got_rec.string1 and\
            set_rec.number2 == number2 and \
            set_rec.string2 == string2, "test_extensible_record failed"

    # Check that we can call method specific to extended record
    assert isinstance(set_rec, FooExtensibleRecord)
    assert isinstance(got_rec, FooExtensibleRecord)
    assert set_rec.ret5() == 5, "test_extensible_record failed"
    assert got_rec.ret5() == 5, "test_extensible_record failed"


'''
    # more things I could potentially test

    some_record_greater_2 = FooSomeOtherRecord(number1, number2 +2)
    rec_greater_2 = FooClientReturnedRecord(record_id, "z " + content, some_record_1b)
    rec_greater_3 = FooClientReturnedRecord(record_id, content, some_record_greater_1)

    # get_record_with_fields(record_id: i64, utf8string: string): foo_client_returned_record;
    # return_str(): string;

'''
