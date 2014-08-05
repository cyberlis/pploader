/*
Copyright 2014 Lisovik Denis (Лисовик Денис) ckyberlis@gmail.com
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
*/
package org.cyberlis.pyloader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Special listener to handle events that were registered with PythonHooks.
 *
 */
public class PythonListener implements Listener {

    /**
     * handlers registered that this listener needs to handle
     */
    HashMap<Class<? extends Event>, Set<PythonEventHandler>> handlers = new HashMap<Class<? extends Event>, Set<PythonEventHandler>>();


    void fireEvent(Event e, PythonEventHandler handler) {
        handler.handler.__call__(Py.java2py(e));
    }

    public  void addHandler(PyObject handler, Class<? extends Event> type, EventPriority priority) {
        Set<PythonEventHandler> set = this.handlers.get(type);
        PythonEventHandler pythonHandler = new PythonEventHandler(handler, type, priority);
        if(set == null) {
            set = new HashSet<PythonEventHandler>();
            handlers.put(type, set);
        }

        set.add(pythonHandler);
    }
}
