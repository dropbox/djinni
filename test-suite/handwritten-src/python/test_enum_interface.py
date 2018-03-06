# Example code written by a python developer to access cpp implementation of Foo
# This file should be hand-written by a python developer
from foo_enum_interface import FooEnumInterface
from color import Color

def get_set(foo, colorInt, colorString):
    color_enum = Color(colorInt)
    assert color_enum == colorInt and color_enum.value == colorInt and isinstance(color_enum.value, int) and \
        color_enum.name == colorString and isinstance(color_enum.name, str), "test_color failed construction"

    foo.set_enum(color_enum)
    got_enum = foo.get_enum()
    assert got_enum == color_enum and got_enum == colorInt and isinstance(got_enum, Color) and\
     isinstance(color_enum, Color), "test_color get/set failed"

    # It should work for ints too, to allow duck typing
    foo.set_enum(colorInt)
    got_enum = foo.get_enum()
    assert got_enum == color_enum and got_enum == colorInt and isinstance(got_enum, Color) and \
           isinstance(color_enum, Color), "test_color get/set int failed"

def get_set_optional(foo, colorInt, colorString):
    color_enum = Color(colorInt)
    assert color_enum.value == colorInt and \
        color_enum.name == colorString

    foo.set_optional_enum(color_enum)
    assert foo.get_optional_enum() == color_enum

def test_color():
    Red, Orange, Yellow, Green, Blue, Indigo, Violet = range(7)
    foo = FooEnumInterface.create()

    get_set(foo, Red, "Red")
    get_set(foo, Green, "Green")
    get_set(foo, Violet, "Violet")

    # Test default enum __hash__ function works as expected
    color_set = set()

    color_green_1 = Color(Green)
    color_green_2 = Color(Green)
    color_set.add(color_green_1)
    color_set.add(color_green_2)
    assert len(color_set) == 1, "test_color failed"

    color_set.add(Color(Red))
    assert len(color_set) == 2, "test_color failed"
    get_set_optional(foo, Red, "Red")
    get_set_optional(foo, Green, "Green")
    get_set_optional(foo, Violet, "Violet")

    foo.set_optional_enum(None)
    assert foo.get_optional_enum() is None

