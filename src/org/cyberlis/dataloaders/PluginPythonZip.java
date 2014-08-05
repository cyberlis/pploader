/*
Copyright 2014 Lisovik Denis (Лисовик Денис)  ckyberlis@gmail.com
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.Override;
import java.lang.String;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.plugin.InvalidPluginException;

/**
 *
 */
public class PluginPythonZip extends PluginDataFile {

    /**
     * Zipfile we belong to
     */
    public ZipFile zip;

    /**
     * Absolute path of the zipfile, for reloading purposes.
     * @author gdude2002
     */
    public String filepath = null;

    /**
     * @param file Zipfile we belong to
     * @throws InvalidPluginException thrown if there is an error opening zip
     */
    public PluginPythonZip(File file) throws InvalidPluginException {
        filepath = file.getAbsolutePath();  // Store the path of the file
        try {
            this.reload();
        }
        catch (IOException e) {
            throw new InvalidPluginException(e);
        }
    }

    /**
     * @throws IOException thrown if there is an error opening zip
     */

    @Override
    public void reload() throws IOException {
        if (closed) {
            File fh = new File(filepath);
            zip = new ZipFile(fh);
            closed = false;
        }
    }

    public void close() throws IOException {
        zip.close();
        closed = true;
    }

    @Override
    public InputStream getStream(String filename) throws IOException {
        ZipEntry entry = zip.getEntry(filename);
        if (entry == null)
            return null;
        return zip.getInputStream(entry);
    }

    @Override
    public boolean shouldAddPathEntry() {
        return true;
    }

    @Override
    public boolean getNeedsSolidMeta() {
        return true;
    }
}
