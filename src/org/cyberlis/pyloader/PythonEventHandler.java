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

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.python.core.PyObject;

/**
 * Class to wrap python functions so they can be used to handle events
 *
 */
public class PythonEventHandler {
    /**
     * Python function to call
     */
    final PyObject handler;

    /**
     * Event type this handler is listening for
     */
    final Class<? extends Event> type;

    /**
     * Priority to register the handler at
     */
    final EventPriority priority;

    /**
     * Whether we've registered yet
     */
    boolean currentlyRegistered = false;

    /**
     * @param handler Python function to call
     * @param type Event type this handler is listening for
     * @param priority Priority to register the handler at
     */
    public PythonEventHandler(PyObject handler, Class<? extends Event> type, EventPriority priority) {
        if(handler.isCallable())
        {
            this.handler = handler;
        }
        else 
        {
            throw new IllegalArgumentException("Tried to register event handler with an invalid type " + handler.getClass().getName());
        }
        this.type = type;
        this.priority = priority;
    }
}
