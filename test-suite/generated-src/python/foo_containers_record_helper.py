# AUTOGENERATED FILE - DO NOT MODIFY!
# This file generated by Djinni from foo_containers.djinni

from djinni.support import MultiSet # default imported in all files
from djinni.exception import CPyException # default imported in all files
from djinni.pycffi_marshal import CPyBinary, CPyBoxedI32, CPyDate, CPyObject, CPyObject, CPyObjectProxy, CPyPrimitive, CPyRecord, CPyString

from dh__list_binary import ListBinaryHelper
from dh__list_date import ListDateHelper
from dh__list_int32_t import ListInt32THelper
from dh__list_list_string import ListListStringHelper
from dh__list_optional_binary import ListOptionalBinaryHelper
from dh__list_record_foo_some_other_record import ListRecordFooSomeOtherRecordHelper
from dh__list_string import ListStringHelper
from dh__map_boxed_int32_t_set_string import MapBoxedInt32TSetStringHelper
from dh__map_boxed_int32_t_set_string import MapBoxedInt32TSetStringProxy
from dh__map_int8_t_list_date import MapInt8TListDateHelper
from dh__map_int8_t_list_date import MapInt8TListDateProxy
from dh__map_int8_t_set_string import MapInt8TSetStringHelper
from dh__map_int8_t_set_string import MapInt8TSetStringProxy
from dh__map_optional_string_optional_string import MapOptionalStringOptionalStringHelper
from dh__map_optional_string_optional_string import MapOptionalStringOptionalStringProxy
from dh__map_string_int32_t import MapStringInt32THelper
from dh__map_string_int32_t import MapStringInt32TProxy
from dh__map_string_string import MapStringStringHelper
from dh__map_string_string import MapStringStringProxy
from dh__set_optional_string import SetOptionalStringHelper
from dh__set_optional_string import SetOptionalStringProxy
from dh__set_string import SetStringHelper
from dh__set_string import SetStringProxy
from foo_some_other_record import FooSomeOtherRecord
from foo_some_other_record_helper import FooSomeOtherRecordHelper
from PyCFFIlib_cffi import ffi, lib

from djinni import exception # this forces run of __init__.py which gives cpp option to call back into py to create exception

from foo_containers_record import FooContainersRecord

class FooContainersRecordHelper:
    @staticmethod
    def release(c_ptr):
        assert c_ptr in c_data_set
        c_data_set.remove(ffi.cast("void*", c_ptr))

    @ffi.callback("struct DjinniOptionalObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f1(cself):
        try:
            return CPyObject.fromPyOpt(ListInt32THelper.c_data_set, CPyRecord.toPy(None, cself).optional_list_int)
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f2(cself):
        try:
            _ret = CPyObject.fromPy(ListInt32THelper.c_data_set, CPyRecord.toPy(None, cself).list_int)
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f3(cself):
        try:
            _ret = CPyObject.fromPy(ListBinaryHelper.c_data_set, CPyRecord.toPy(None, cself).list_binary)
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f4(cself):
        try:
            _ret = CPyObject.fromPy(ListOptionalBinaryHelper.c_data_set, CPyRecord.toPy(None, cself).list_optional_binary)
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f5(cself):
        try:
            _ret = CPyObject.fromPy(ListListStringHelper.c_data_set, CPyRecord.toPy(None, cself).list_list_string)
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f6(cself):
        try:
            _ret = CPyObject.fromPy(ListRecordFooSomeOtherRecordHelper.c_data_set, CPyRecord.toPy(None, cself).list_record)
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniOptionalObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f7(cself):
        try:
            return CPyObjectProxy.fromPyOpt(MapStringInt32THelper.c_data_set, MapStringInt32TProxy(CPyRecord.toPy(None, cself).optional_map_string_int))
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f8(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapStringInt32THelper.c_data_set, MapStringInt32TProxy(CPyRecord.toPy(None, cself).map_string_int))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f9(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapStringStringHelper.c_data_set, MapStringStringProxy(CPyRecord.toPy(None, cself).map_string_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f10(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapOptionalStringOptionalStringHelper.c_data_set, MapOptionalStringOptionalStringProxy(CPyRecord.toPy(None, cself).map_optional_string_optional_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f11(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapInt8TListDateHelper.c_data_set, MapInt8TListDateProxy(CPyRecord.toPy(None, cself).map_int_list_date))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniOptionalObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f12(cself):
        try:
            return CPyObjectProxy.fromPyOpt(SetStringHelper.c_data_set, SetStringProxy(CPyRecord.toPy(None, cself).optional_set_string))
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f13(cself):
        try:
            _ret = CPyObjectProxy.fromPy(SetStringHelper.c_data_set, SetStringProxy(CPyRecord.toPy(None, cself).set_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f14(cself):
        try:
            _ret = CPyObjectProxy.fromPy(SetOptionalStringHelper.c_data_set, SetOptionalStringProxy(CPyRecord.toPy(None, cself).set_optional_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f15(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapInt8TSetStringHelper.c_data_set, MapInt8TSetStringProxy(CPyRecord.toPy(None, cself).map_int_set_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniObjectHandle *(struct DjinniRecordHandle *)")
    def get_foo_containers_record_f16(cself):
        try:
            _ret = CPyObjectProxy.fromPy(MapBoxedInt32TSetStringHelper.c_data_set, MapBoxedInt32TSetStringProxy(CPyRecord.toPy(None, cself).map_optional_int_set_string))
            assert _ret != ffi.NULL
            return _ret
        except Exception as _djinni_py_e:
            CPyException.setExceptionFromPy(_djinni_py_e)
            return ffi.NULL

    @ffi.callback("struct DjinniRecordHandle *(struct DjinniOptionalObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniOptionalObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniOptionalObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *,struct DjinniObjectHandle *)")
    def python_create_foo_containers_record(optional_list_int,list_int,list_binary,list_optional_binary,list_list_string,list_record,optional_map_string_int,map_string_int,map_string_string,map_optional_string_optional_string,map_int_list_date,optional_set_string,set_string,set_optional_string,map_int_set_string,map_optional_int_set_string):
        py_rec = FooContainersRecord(
            CPyObject.toPyOpt(ListInt32THelper.c_data_set, optional_list_int),
            CPyObject.toPy(ListInt32THelper.c_data_set, list_int),
            CPyObject.toPy(ListBinaryHelper.c_data_set, list_binary),
            CPyObject.toPy(ListOptionalBinaryHelper.c_data_set, list_optional_binary),
            CPyObject.toPy(ListListStringHelper.c_data_set, list_list_string),
            CPyObject.toPy(ListRecordFooSomeOtherRecordHelper.c_data_set, list_record),
            CPyObjectProxy.toPyObjOpt(MapStringInt32THelper.c_data_set, optional_map_string_int),
            CPyObjectProxy.toPyObj(MapStringInt32THelper.c_data_set, map_string_int),
            CPyObjectProxy.toPyObj(MapStringStringHelper.c_data_set, map_string_string),
            CPyObjectProxy.toPyObj(MapOptionalStringOptionalStringHelper.c_data_set, map_optional_string_optional_string),
            CPyObjectProxy.toPyObj(MapInt8TListDateHelper.c_data_set, map_int_list_date),
            CPyObjectProxy.toPyObjOpt(SetStringHelper.c_data_set, optional_set_string),
            CPyObjectProxy.toPyObj(SetStringHelper.c_data_set, set_string),
            CPyObjectProxy.toPyObj(SetOptionalStringHelper.c_data_set, set_optional_string),
            CPyObjectProxy.toPyObj(MapInt8TSetStringHelper.c_data_set, map_int_set_string),
            CPyObjectProxy.toPyObj(MapBoxedInt32TSetStringHelper.c_data_set, map_optional_int_set_string))
        return CPyRecord.fromPy(FooContainersRecord.c_data_set, py_rec) #to do: can be optional?

    @ffi.callback("void (struct DjinniRecordHandle *)")
    def __delete(dh):
        assert dh in FooContainersRecord.c_data_set
        FooContainersRecord.c_data_set.remove(dh)

    @staticmethod
    def _add_callbacks():
        lib.foo_containers_record_add_callback_get_foo_containers_record_f8(FooContainersRecordHelper.get_foo_containers_record_f8)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f9(FooContainersRecordHelper.get_foo_containers_record_f9)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f10(FooContainersRecordHelper.get_foo_containers_record_f10)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f2(FooContainersRecordHelper.get_foo_containers_record_f2)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f11(FooContainersRecordHelper.get_foo_containers_record_f11)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f3(FooContainersRecordHelper.get_foo_containers_record_f3)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f12(FooContainersRecordHelper.get_foo_containers_record_f12)
        lib.foo_containers_record_add_callback_python_create_foo_containers_record(FooContainersRecordHelper.python_create_foo_containers_record)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f4(FooContainersRecordHelper.get_foo_containers_record_f4)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f13(FooContainersRecordHelper.get_foo_containers_record_f13)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f5(FooContainersRecordHelper.get_foo_containers_record_f5)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f14(FooContainersRecordHelper.get_foo_containers_record_f14)
        lib.foo_containers_record_add_callback___delete(FooContainersRecordHelper.__delete)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f6(FooContainersRecordHelper.get_foo_containers_record_f6)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f15(FooContainersRecordHelper.get_foo_containers_record_f15)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f7(FooContainersRecordHelper.get_foo_containers_record_f7)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f16(FooContainersRecordHelper.get_foo_containers_record_f16)
        lib.foo_containers_record_add_callback_get_foo_containers_record_f1(FooContainersRecordHelper.get_foo_containers_record_f1)

FooContainersRecordHelper._add_callbacks()

