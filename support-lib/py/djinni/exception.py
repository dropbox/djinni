from PyCFFIlib_cffi import ffi, lib
from djinni.pycffi_marshal import CPyString
from djinni.support import MultiSet

# Allows python developer to differentiate exceptions coming via djinni code
class DjinniException(RuntimeError):
    def __init__(self, error_message):
        self.args = error_message

class ExceptionHelper:
    # keeping the callback for exceptions alive, and cleaning it up at the end of a process
    # holding c_data_set for exception handles (handles to python exception passed into c/cpp)
    c_data_set = MultiSet()

    @staticmethod
    @ffi.callback("struct DjinniPythonExceptionHandle * (struct DjinniString *)")
    def create_py_from_cpp_exception(ds_error_message):
        e = DjinniException(CPyString.toPy(ds_error_message))
        c_ptr = ffi.new_handle(e)
        ExceptionHelper.c_data_set.add(c_ptr)
        return c_ptr

    @staticmethod
    @ffi.callback("void(struct DjinniPythonExceptionHandle * )")
    def exception_delete(c_ptr):
        assert c_ptr in ExceptionHelper.c_data_set
        ExceptionHelper.c_data_set.remove(c_ptr)

class CPyException:
    @staticmethod
    def toPyCheckAndRaise(ret_val):
        c_ptr = lib.djinni_from_python_check_and_clear_exception()
        if c_ptr == ffi.NULL: # no exception was thrown
            return

        assert c_ptr in ExceptionHelper.c_data_set
        try:
            # if exception was thrown, return value must be null
            exception = ffi.from_handle(ffi.cast("void * ", c_ptr))
            assert ret_val == ffi.NULL
            raise exception
        finally:
            ExceptionHelper.c_data_set.remove(c_ptr)
    @staticmethod
    def setExceptionFromPy(py_e):
        bare_c_ptr = ffi.new_handle(py_e)
        ExceptionHelper.c_data_set.add(bare_c_ptr)
        lib.djinni_create_and_set_cpp_from_py_exception(bare_c_ptr)

lib._djinni_add_callback_create_py_from_cpp_exception(ExceptionHelper.create_py_from_cpp_exception)
lib._djinni_add_callback_exception___delete(ExceptionHelper.exception_delete)
