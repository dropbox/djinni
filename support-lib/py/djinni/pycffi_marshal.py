# Part of support library for djinni4python, handwritten

from __future__ import absolute_import, division, print_function, unicode_literals
from PyCFFIlib_cffi import ffi, lib
from djinni.support import encoded_utf_8
import datetime
import math

class CPyPrimitive:
    @staticmethod
    def toPy(p):
        assert p is not None
        return p

    @staticmethod
    def fromPy(p):
        assert p is not None
        return p

class CPyBoxedBase:
    def __init__(self, data, delete_fn, cpyoptional):
        self._data = data
        self._delete_fn = delete_fn

    def __enter__(self):
        return self

    def __exit__(self, ty, value, traceback):
        self.__del__()

    def __del__(self):
        if self._data:
            self._delete_fn(self._data)
            self._data= None

    def get_djinni_boxed(self):
        return self._data
    def release_djinni_boxed(self): # renounce ownership of resource
        dopt = self._data
        self._data= None
        return dopt

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt, get_fn):
        if dopt == ffi.NULL:
            return None
        return get_fn(dopt)

    @staticmethod
    def toPyOpt(dopt, get_fn, cpytype):
        with cpytype(dopt) as py_opt:
            return cpytype.toPyOptWithoutTakingOwnership(py_opt._data)

    @staticmethod
    def fromPyOpt(pyopt, create_fn, cpyoptional):
        if pyopt is None:
            return cpyoptional(ffi.NULL)

        dopt = create_fn(pyopt)
        return cpyoptional(dopt)

class CPyBoxedI8(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_i8, CPyBoxedI8)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_i8_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_i8_data, CPyBoxedI8)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_i8, CPyBoxedI8)

class CPyBoxedI16(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_i16, CPyBoxedI1)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_i16_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_i16_data, CPyBoxedI16)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_i16, CPyBoxedI16)

class CPyBoxedI32(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_i32, CPyBoxedI32)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_i32_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_i32_data, CPyBoxedI32)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_i32, CPyBoxedI32)

class CPyBoxedI64(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_i64, CPyBoxedI64)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_i64_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_i64_data, CPyBoxedI64)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_i64, CPyBoxedI64)

class CPyBoxedF32(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_f32, CPyBoxedF32)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_f32_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_f32_data, CPyBoxedF32)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_f32, CPyBoxedF32)

class CPyBoxedF64(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_f64, CPyBoxedF64)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_f64_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_f64_data, CPyBoxedF64)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_f64, CPyBoxedF64)

class CPyBoxedBool(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_bool, CPyBoxedBool)

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, lib.get_djinni_boxed_bool_data)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, lib.get_djinni_boxed_bool_data, CPyBoxedBool)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, lib.create_djinni_boxed_bool, CPyBoxedBool)

class CPyBoxedDate(CPyBoxedBase):
    def __init__(self, data):
        CPyBoxedBase.__init__(self, data, lib.delete_djinni_boxed_date, CPyBoxedDate)

    @staticmethod
    def _get_fn(c_boxed):
        return CPyDate.toPy(lib.get_djinni_boxed_date_data(c_boxed))

    @staticmethod
    def _create_fn(py_val):
        return lib.create_djinni_boxed_date(CPyDate.fromPy(py_val))

    @staticmethod
    def toPyOptWithoutTakingOwnership(dopt):
        return CPyBoxedBase.toPyOptWithoutTakingOwnership(dopt, CPyBoxedDate._get_fn)
    @staticmethod
    def toPyOpt(dopt):
        return CPyBoxedBase.toPyOpt(dopt, CPyBoxedDate._get_fn, CPyBoxedDate)
    @staticmethod
    def fromPyOpt(pyopt):
         return CPyBoxedBase.fromPyOpt(pyopt, CPyBoxedDate._create_fn, CPyBoxedDate)

class CPyStringBinaryHelper:
    @staticmethod
    def toPyHelper(ds, cpytype):
      with cpytype(ds) as py_ds: # for easy memory release
            return cpytype.toPyWithoutTakingOwnership(ds)

    @staticmethod
    def toPy(ds, cpytype):
        assert (ds != ffi.NULL) # not sure if assert(ds) means the same thing because Python..
        return cpytype.toPyHelper(ds)

    @staticmethod
    def toPyOpt(ds, cpytype):
        if ds == ffi.NULL:
            return None
        return cpytype.toPyHelper(ds)

    @staticmethod
    def toPyOptWithoutTakingOwnership(ds, cpytype):
        if ds == ffi.NULL:
            return None
        return cpytype.toPyWithoutTakingOwnership(ds)

class CPyString: # Python RAII for C DjinniString
    def __init__(self, djinni_string):
        self._djinni_string = djinni_string
    def __enter__(self):
        return self
    def __exit__(self, ty, value, traceback):
        self.__del__()
    def __del__(self):
        if self._djinni_string:
            lib.delete_djinni_string(self._djinni_string)
            self._djinni_string = None

    def get_djinni_string(self):
        return self._djinni_string
    def release_djinni_string(self): # renounce ownership of resource
        ds = self._djinni_string
        self._djinni_string = None
        return ds

    @staticmethod
    def toPyOptWithoutTakingOwnership(ds):
        return CPyStringBinaryHelper.toPyOptWithoutTakingOwnership(ds, CPyString)

    @staticmethod
    def toPyWithoutTakingOwnership(ds): # to Py with no with block
        ss = lib.get_djinni_string_chars(ds) # maybe: py_ds.get...
        l = lib.get_djinni_string_len(ds)
        return b''.join(ss[0:l]).decode('utf-8')

    @staticmethod
    def toPyHelper(ds):
        return CPyStringBinaryHelper.toPyHelper(ds, CPyString)

    @staticmethod
    def toPy(ds):
        return CPyStringBinaryHelper.toPy(ds, CPyString)

    @staticmethod
    def toPyOpt(ds):
        return CPyStringBinaryHelper.toPyOpt(ds, CPyString)

    @staticmethod
    def fromPyHelper(ps):
        s = encoded_utf_8(ps)
        ds = lib.create_djinni_string(s, len(s))
        return CPyString(ds)

    @staticmethod
    def fromPy(ps):
        assert ps is not None
        return CPyString.fromPyHelper(ps)

    @staticmethod
    def fromPyOpt(ps):
        if ps is None:
            return CPyString(ffi.NULL)
        return CPyString.fromPyHelper(ps)

class CPyBinary: # Python RAII for C DjinniBinary
    def __init__(self, djinni_binary):
        self._djinni_binary = djinni_binary
    def __enter__(self):
        return self
    def __exit__(self, ty, value, traceback):
        self.__del__()
    def __del__(self):
        if self._djinni_binary:
            lib.delete_djinni_binary(self._djinni_binary)
            self._djinni_binary = None
    def get_djinni_binary(self):
        return self._djinni_binary

    def release_djinni_binary(self): # renounce ownership of resource
        db = self._djinni_binary
        self._djinni_binary = None
        return db

    @staticmethod
    def toPyOptWithoutTakingOwnership(pb):
        return CPyStringBinaryHelper.toPyOptWithoutTakingOwnership(pb, CPyBinary)

    @staticmethod
    def toPyWithoutTakingOwnership(pb): # toPy with no with block
        ss = lib.get_djinni_binary_uint8s(pb) # maybe: py_db.get...
        l = lib.get_djinni_binary_len(pb)
        return ffi.buffer(ss,l)[:]

    @staticmethod
    def toPyHelper(ds):
        return CPyStringBinaryHelper.toPyHelper(ds, CPyBinary)

    @staticmethod
    def toPy(ds):
        return CPyStringBinaryHelper.toPy(ds, CPyBinary)

    @staticmethod
    def toPyOpt(ds):
        return CPyStringBinaryHelper.toPyOpt(ds, CPyBinary)

    @staticmethod
    def fromPyHelper(pb):
        ffipb = ffi.new("uint8_t []", pb)
        db = lib.create_djinni_binary(ffipb, len(pb))
        return CPyBinary(db)

    @staticmethod
    def fromPy(pb):
        assert pb is not None
        return CPyBinary.fromPyHelper(pb)

    @staticmethod
    def fromPyOpt(pb):
        if pb is None:
            return CPyBinary(ffi.NULL)
        return CPyBinary.fromPyHelper(pb)

class CPyDate:
    epoch = datetime.datetime.utcfromtimestamp(0)

    @staticmethod
    def toPy(date):
        return CPyDate.epoch + datetime.timedelta(milliseconds = date)

    @staticmethod
    def fromPy(pb):
        return int((pb-CPyDate.epoch).total_seconds() * 1000) # to milliseconds

class CPyStructuredBase:
    @staticmethod
    def toPyHelper(c_data_set, c_ptr):
        aux = ffi.from_handle(ffi.cast("void * ",c_ptr))
        if c_data_set is not None: # aka, if we want to remove c_ptr
            assert c_ptr in c_data_set
            c_data_set.remove(c_ptr)
        return aux

    @staticmethod
    def toPy(c_data_set, c_ptr):
        assert c_ptr != ffi.NULL # not sure if assert(c_ptr) means the same thing because Python..
        return CPyStructuredBase.toPyHelper(c_data_set, c_ptr)

    @staticmethod
    def toPyOpt(c_data_set, c_ptr):
        if c_ptr == ffi.NULL:
            return None
        return CPyStructuredBase.toPyHelper(c_data_set, c_ptr)

    @staticmethod
    def fromPyHelper(c_data_set, py_obj):
        c_ptr = ffi.new_handle(py_obj)
        c_data_set.add(c_ptr)
        return c_ptr

    @staticmethod
    def fromPy(c_data_set, py_obj):
        assert py_obj is not None
        return CPyStructuredBase.fromPyHelper(c_data_set, py_obj)

    @staticmethod
    def fromPyOpt(c_data_set, py_obj):
        if py_obj is None:
            return ffi.NULL
        return CPyStructuredBase.fromPyHelper(c_data_set, py_obj)

class CPyRecord(CPyStructuredBase):
    pass

# all containers are passed to and from C as abstract objects (void* pointers with a name)
class CPyObject(CPyStructuredBase):
    pass

# maps and sets have proxies that hold the implementations and state of their iterators
class CPyObjectProxy(CPyStructuredBase):
    @staticmethod
    def toPyObj(c_data_set, c_ptr):
        aux = CPyStructuredBase.toPy(c_data_set, c_ptr)
        assert aux is not None
        return aux._py_obj

    @staticmethod
    def toPyObjOpt(c_data_set, c_ptr):
        aux = CPyStructuredBase.toPyOpt(c_data_set, c_ptr)
        if aux is not None:
            return aux._py_obj
        return None

    @staticmethod
    def toPyIter(c_ptr):
        assert c_ptr != ffi.NULL
        aux = ffi.from_handle(ffi.cast("void * ", c_ptr))
        assert aux is not None
        return aux._py_iter

    @staticmethod
    def toPyIterOpt(c_ptr):
        if c_ptr == ffi.NULL:
            return None

        return CPyObjectProxy.toPyIter(c_ptr)

    @staticmethod
    def fromPyHelper(c_data_set, py_obj_proxy):
        bare_c_ptr = ffi.new_handle(py_obj_proxy)
        c_data_set.add(bare_c_ptr)
        return bare_c_ptr

    @staticmethod
    def fromPy(c_data_set, py_obj_proxy):
        assert py_obj_proxy._py_obj is not None
        return CPyObjectProxy.fromPyHelper(c_data_set, py_obj_proxy)

    @staticmethod
    def fromPyOpt(c_data_set, py_obj_proxy):
        if py_obj_proxy._py_obj is None:
            return ffi.NULL
        return CPyObjectProxy.fromPyHelper(c_data_set, py_obj_proxy)

class CPyEnum:
    @staticmethod
    def fromPy(e):
        assert e is not None
        return int(e)

    @staticmethod
    def fromPyOpt(e):
        if e is None:
            return -1 # to signal null optional enum
        return int(e)

    @staticmethod
    def toPy(enum_class, e):
        assert e >= 0
        return enum_class(e)

    @staticmethod
    def toPyOpt(enum_class, e):
        if e == -1:
            return None
        return enum_class(e)
