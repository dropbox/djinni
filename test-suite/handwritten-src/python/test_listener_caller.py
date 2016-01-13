# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function, unicode_literals

from listener_caller import ListenerCaller
from first_listener import FirstListener
from second_listener import SecondListener

class MultiListenerImpl(FirstListener, SecondListener):
    def __init__(self):
        self.first_called = False
        self.second_called = False

    def first(self):
        self.first_called = True

    def second(self):
        self.second_called = True

def test_listener_caller():
    listener = MultiListenerImpl()
    caller = ListenerCaller.init(listener, listener)

    assert not listener.first_called
    assert not listener.second_called

    caller.callFirst()

    assert listener.first_called
    assert not listener.second_called

    caller.callSecond()

    assert listener.first_called
    assert listener.second_called
