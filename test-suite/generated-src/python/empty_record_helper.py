# AUTOGENERATED FILE - DO NOT MODIFY!
# This file generated by Djinni from test.djinni

from djinni.support import MultiSet # default imported in all files
from djinni.exception import CPyException # default imported in all files
from djinni.pycffi_marshal import CPyRecord
from PyCFFIlib_cffi import ffi, lib

from djinni import exception # this forces run of __init__.py which gives cpp option to call back into py to create exception

from empty_record import EmptyRecord

class EmptyRecordHelper:
    @staticmethod
    def release(c_ptr):
        assert c_ptr in c_data_set
        c_data_set.remove(ffi.cast("void*", c_ptr))

    @ffi.callback("struct DjinniRecordHandle *()")
    def python_create_empty_record():
        py_rec = EmptyRecord(
        )
        return CPyRecord.fromPy(EmptyRecord.c_data_set, py_rec) #to do: can be optional?

    @ffi.callback("void (struct DjinniRecordHandle *)")
    def __delete(dh):
        assert dh in EmptyRecord.c_data_set
        EmptyRecord.c_data_set.remove(dh)

    @staticmethod
    def _add_callbacks():
        lib.empty_record_add_callback___delete(EmptyRecordHelper.__delete)
        lib.empty_record_add_callback_python_create_empty_record(EmptyRecordHelper.python_create_empty_record)

EmptyRecordHelper._add_callbacks()

