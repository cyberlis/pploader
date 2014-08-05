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
package org.cyberlis.dataloaders;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Boolean;

/**
 * Used on initialization of a plugin, because I now have three different kinds
 * of plugin files, and it's about time to abstract them.
 *
 */
public abstract class PluginDataFile {

    public Boolean closed = true;  // Whether the data file is closed or not.

    /**
     * Close up shop.
     * @throws IOException thrown if closing fails
     */
    public void close() throws IOException {
        // This is an optionally overridable method
    }

    /**
     * Get a stream for a file inside the datafile.
     * @param filename name to get
     * @return stream or null if file does not exist
     * @throws IOException thrown if opening fails
     */
    public abstract InputStream getStream(String filename) throws IOException;

    /**
     * @return whether to add the file for this PluginDataFile to the pythonpath
     */
    public abstract boolean shouldAddPathEntry();

    /**
     * @return true if an exception should be thrown for missing solid metadata
     */
    public abstract boolean getNeedsSolidMeta();

    /**
     * Use this to reload any files.
     * @author gdude2002
     * @throws IOException thrown if reloading fails
     */
    public void reload() throws IOException {
        // This is an optionally overridable method.
    }
}
