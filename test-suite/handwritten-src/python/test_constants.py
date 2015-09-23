# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

from foo_constants import FooConstants
from foo_constants_interface import FooConstantsInterface

def test_record_consts():
    assert FooConstants.BOOL_CONSTANT == True
    assert FooConstants.I8_CONSTANT == 1
    assert FooConstants.I16_CONSTANT == 2
    assert FooConstants.I32_CONSTANT == 3
    assert FooConstants.I64_CONSTANT == 4
    assert FooConstants.F32_CONSTANT == 5.0
    assert FooConstants.F64_CONSTANT == 6.0
    assert FooConstants.STRING_CONSTANT == "string-constant"
    assert FooConstants.OPTIONAL_INTEGER_CONSTANT == 1

    assert FooConstants.OBJECT_CONSTANT.some_integer == FooConstants.I32_CONSTANT
    assert FooConstants.OBJECT_CONSTANT.some_string == FooConstants.STRING_CONSTANT

    assert FooConstants.SOME_RECORD.number1 == 28
    assert FooConstants.SOME_RECORD.number2 == FooConstants.I16_CONSTANT

def test_interface_consts():
    assert FooConstantsInterface.BOOL_CONSTANT == True
    assert FooConstantsInterface.I8_CONSTANT == 1
    assert FooConstantsInterface.I16_CONSTANT == 2
    assert FooConstantsInterface.I32_CONSTANT == 3
    assert FooConstantsInterface.I64_CONSTANT == 4
    assert FooConstantsInterface.F32_CONSTANT == 5.0
    assert FooConstantsInterface.F64_CONSTANT == 6.0
    assert FooConstantsInterface.STRING_CONSTANT == "another-string-constant"
    assert FooConstantsInterface.OPTIONAL_INTEGER_CONSTANT == 1

    assert FooConstantsInterface.OBJECT_CONSTANT.some_integer == FooConstantsInterface.I32_CONSTANT
    assert FooConstantsInterface.OBJECT_CONSTANT.some_string == FooConstantsInterface.STRING_CONSTANT
