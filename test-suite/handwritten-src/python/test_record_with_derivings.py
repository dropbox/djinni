from __future__ import absolute_import, division, print_function, unicode_literals

from record_with_derivings import RecordWithDerivings
from record_with_nested_derivings import RecordWithNestedDerivings

from datetime import datetime

record1 = RecordWithDerivings(1, 2, 3, 4, 5.0, 6.0, datetime.fromtimestamp(7), "String8")
record1A = RecordWithDerivings(1, 2, 3, 4, 5.0, 6.0, datetime.fromtimestamp(7), "String8")
record2 = RecordWithDerivings(1, 2, 3, 4, 5.0, 6.0, datetime.fromtimestamp(7), "String888")
record3 = RecordWithDerivings(111, 2, 3, 4, 5.0, 6.0, datetime.fromtimestamp(7), "String8")

def test_record_ord():
    assert not record1 < record1A
    assert not record1A < record1
    assert not record1 > record1A
    assert not record1A > record1

    assert record1 < record2
    assert record2 > record1
    assert record1 < record3
    assert record3 > record1
    assert record2 < record3
    assert record3 > record2

    assert not record1 > record2
    assert not record2 < record1
    assert not record1 > record3
    assert not record3 < record1
    assert not record2 > record3
    assert not record3 < record2

def test_record_eq():
    assert record1 == record1A
    assert record1A == record1
    assert not record1 == record2
    assert not record2 == record1
    assert not record1 == record3
    assert not record3 == record1
    assert not record2 == record3
    assert not record3 == record2

    assert record1.__hash__() == record1A.__hash__()
    assert record1.__hash__() != record2.__hash__()
    assert record1.__hash__() != record3.__hash__()
    assert record2.__hash__() != record3.__hash__()

nested_record1 = RecordWithNestedDerivings(1, record1)
nested_record1A = RecordWithNestedDerivings(1, record1A)
nested_record2 = RecordWithNestedDerivings(1, record2)

def test_nested_record_ord():
    assert not nested_record1 > nested_record1A
    assert not nested_record1 < nested_record1A
    assert not nested_record1A > nested_record1
    assert not nested_record1A < nested_record1
    assert nested_record1 < nested_record2
    assert nested_record2 > nested_record1

def test_nested_record_eq():
    assert nested_record1 == nested_record1A
    assert nested_record1 != nested_record2
    assert nested_record2 != nested_record1
