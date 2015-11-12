from __future__ import absolute_import, division, print_function, unicode_literals
from threading import Lock
import re

class MultiSet:
    def __init__(self):
        self._dict = dict()
        self._lock = Lock()

    def add(self, item):
        with self._lock:
            if item not in self._dict:
                self._dict[item] = 1
            else:
                self._dict[item] += 1

    def remove(self, item):
        with self._lock:
            assert item in self._dict
            self._dict[item] -= 1
            if self._dict[item] == 0:
                del self._dict[item]

    def __len__(self):
        with self._lock:
            return len(self._dict)
    def __contains__(self, item):
        with self._lock:
            return item in self._dict

# Handwritten utility functions, currently used in both client (for testing)
# and in to-be-generated code ( should eventually be split)

def decoded_utf_8(s):
    if isinstance(s, bytes):
        return s.decode('utf-8')
    return s

def encoded_utf_8(s):
    if isinstance(s, bytes):
        return s
    return s.encode('utf-8')

def clean_header_for(s, to_ignore):
    lines = s.split('\n')

    cleans = []
    for line in lines:
        if to_ignore not in line:
            cleans.append(line + "\n")
    return cleans

def clean_headers_for(headers, to_ignore):
    cleaned_headers = []
    for header in headers:
        f = open(header, "r")
        f_h = f.read()
        f.close()
        cleaned_headers.extend(clean_header_for(f_h, to_ignore))

    return cleaned_headers

# match forward declarations of DjinniStructs like: struct DjinniStructureName;
# don't match anything that containts a struct DjnniStructName but is not a forward declaration
pattern = re.compile("struct Djinni[a-zA-Z0-9]*[^\s)];")
def sort_by_import_order(lines):
    first_lines = ""
    last_lines = ""
    for line in lines:
        if pattern.search(line):
            first_lines += line
        else:
            last_lines += line

    return first_lines + last_lines
