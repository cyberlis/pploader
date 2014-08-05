"""
Copyright 2014 Lisovik Denis  ckyberlis@gmail.com
This file is part of PPLoader.
PPLoader is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PPLoader is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PPLoader.  If not, see <http://www.gnu.org/licenses/>
"""

import sys
from org.bukkit.event import EventPriority
import org.bukkit as bukkit
from java.util.logging import Level

import org.cyberlis.pyloader.PythonPlugin as PythonPlugin
import org.cyberlis.pyloader.PythonListener as _PythonListener

def PythonEventHandler(event, priority=EventPriority.NORMAL):
    """Adds to PythonListener class methods fields _handlerType and 
        _handlePriority    
    """
    def first_wrapper(method):
        def second_wrapper(*args, **kwargs):
            method(*args, **kwargs)
        second_wrapper._handlerType = event
        second_wrapper._handlePriority = priority
        return second_wrapper
    return first_wrapper

class PythonListener(_PythonListener):
    """ Event listener class. Modified __init__ for auto adding handlers
        from local methods which was decorated by EventHandler
    """
    def __init__(self, *args, **kwargs):
        for m in dir(self):
            if hasattr(getattr(self, m), '_handlerType') and hasattr(getattr(self, m), '_handlePriority'):
                self.addHandler(getattr(self, m), getattr(self, m)._handlerType, getattr(self, m)._handlePriority)

class PyStdoutRedirect(object):
    def write(self, txt):
        if txt.endswith("\n"):
            sys.__stdout__.write(txt[:-1])
            sys.__stdout__.flush()
        else:
            sys.__stdout__.write(txt)

sys.stdout = PyStdoutRedirect()

server = bukkit.Bukkit.getServer()

class Log(object):
    prefix = ""
    logger = server.getLogger()

    @staticmethod
    def info(*text):
        Log.logger.log(Level.INFO,Log.prefix+" ".join(map(unicode,text)))

    @staticmethod
    def severe(*text):
        Log.logger.log(Level.SEVERE,Log.prefix+" ".join(map(unicode,text)))

    @staticmethod
    def msg(player,*text):
        player.sendMessage(Log.prefix+" ".join(map(unicode,text)))

log = Log
