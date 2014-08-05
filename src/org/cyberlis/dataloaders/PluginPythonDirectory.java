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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class PluginPythonDirectory extends PluginDataFile {

    /**
     * directory we represent
     */
    private final File dir;

    /**
     * @param dir directory we represent
     */
    public PluginPythonDirectory(File dir) {
        this.dir = dir;
    }

    @Override
    public InputStream getStream(String filename) throws IOException {
        File f = new File(dir, filename);
        if (!f.exists())
            return null;
        return new FileInputStream(f);
    }

    @Override
    public boolean shouldAddPathEntry() {
        return true;
    }

    @Override
    public boolean getNeedsSolidMeta() {
        return false;
    }

}
