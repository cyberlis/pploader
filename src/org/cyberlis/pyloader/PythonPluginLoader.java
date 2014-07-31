/**
 *
 */
package org.cyberlis.pyloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.cyberlis.dataloaders.PluginDataFile;
import org.cyberlis.dataloaders.PluginPythonDirectory;
import org.cyberlis.dataloaders.PluginPythonZip;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * A jython plugin loader. depends on JavaPluginLoader and SimplePluginManager.
 *
 * @author masteroftime
 * @author lahwran
 * @author cyberlis
 */
public class PythonPluginLoader implements PluginLoader {

    private final Server server;

    /**
     * Filter - matches all of the following, for the regex illiterate:
     * <pre>
     * plugin_py_dir
     * plugin.py.dir
     * plugin.py.zip
     * plugin.pyp
     * </pre>
     */
    public static final Pattern[] fileFilters = new Pattern[] {
            Pattern.compile("^(.*)\\.py\\.dir$"),
            Pattern.compile("^(.*)_py_dir$"),
            Pattern.compile("^(.*)\\.py\\.zip$"),
            Pattern.compile("^(.*)\\.pyp$"),
        };

    private HashSet<String> loadedplugins = new HashSet<String>();

    /**
     * @param server server to initialize with
     */
    public PythonPluginLoader(Server server) {
        this.server = server;
    }

    public Plugin loadPlugin(File file) throws InvalidPluginException/*, UnknownDependencyException*/ {
        return loadPlugin(file, false);
    }

    public Plugin loadPlugin(File file, boolean ignoreSoftDependencies)
            throws InvalidPluginException/*, InvalidDescriptionException, UnknownDependencyException*/ {

        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(String.format("%s does not exist",
                    file.getPath())));
        }

        PluginDataFile data = null;

        if (file.getName().endsWith("dir")) {
            if (!file.isDirectory())
                throw new InvalidPluginException(new Exception("python directories cannot be normal files! try .py or .py.zip instead."));
            data = new PluginPythonDirectory(file);
        } else if (file.getName().endsWith("zip") || file.getName().endsWith("pyp")) {
            if (file.isDirectory())
                throw new InvalidPluginException(new Exception("python zips cannot be directories! try .py.dir instead."));
            data = new PluginPythonZip(file);
        } else {
            throw new InvalidPluginException(new Exception("filename '"+file.getName()+"' does not end in py, dir, zip, or pyp! did you add a regex without altering loadPlugin()?"));
        }

        try {
            return loadPlugin(file, ignoreSoftDependencies, data);
        } finally {
            try {
                data.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Plugin loadPlugin(File file, boolean ignoreSoftDependencies, PluginDataFile data) throws InvalidPluginException/*, InvalidDescriptionException, UnknownDependencyException*/ {
        System.out.println("[PPLoader] Loading Plugin " + file.getName());
        PythonPlugin result = null;
        PluginDescriptionFile description = null;
        try {
            InputStream stream = data.getStream("plugin.yml");
            if (stream == null){
                throw new InvalidPluginException(new Exception("You must include plugin.yml!"));
            }
            description = new PluginDescriptionFile(stream);
            if (stream != null)
                stream.close();
        } catch (IOException ex) {
            throw new InvalidPluginException(ex);
        } catch (YAMLException ex) {
            throw new InvalidPluginException(ex);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidPluginException(ex);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());

        if (dataFolder.getAbsolutePath().equals(file.getAbsolutePath())) {
            throw new InvalidPluginException(new Exception(String.format("Projected datafolder: '%s' for %s is the same file as the plugin itself (%s)",
                    dataFolder,
                    description.getName(),
                    file)));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(new Exception(String.format("Projected datafolder: '%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getName(),
                    file)));
        }

        List<String> depend;

        try {
            depend = description.getDepend();
            if (depend == null) {
                depend = new ArrayList<>();
            }
        } catch (ClassCastException ex) {
            throw new InvalidPluginException(ex);
        }

        for (String pluginName : depend) {
            if (!isPluginLoaded(pluginName)) {
                throw new UnknownDependencyException(pluginName);
            }
        }
        PySystemState state = new PySystemState();
        PyList pythonpath = state.path;
        PyString filepath = new PyString(file.getAbsolutePath());
    	pythonpath.append(filepath);


        String mainfile = "plugin.py";
        InputStream instream = null;
        try {
            instream = data.getStream(mainfile);
            if (instream == null) {
                mainfile = "main.py";
                instream = data.getStream(mainfile);
            }
        } catch (IOException e) {
            throw new InvalidPluginException(e);
        }

        if (instream == null) {
            throw new InvalidPluginException(new FileNotFoundException("Can not find plugin.py or main.py"));
        }
        try {
            PyDictionary table = new PyDictionary();
            PythonInterpreter interp = new PythonInterpreter(table, state);
            
            String[] pre_plugin_scripts = {"preload.py"};
            String[] post_plugin_scripts = {"postload.py"};
            
            // Run scripts designed to be run before plugin creation
            for (String script : pre_plugin_scripts) {
	            InputStream metastream = this.getClass().getClassLoader().getResourceAsStream("scripts/"+script);
	            interp.execfile(metastream);
	            metastream.close();
            }

            interp.execfile(instream);

            instream.close();

            String mainclass = description.getMain();
            PyObject pyClass = interp.get(mainclass);
            if (pyClass == null)
                pyClass = interp.get("Plugin");
                if(pyClass == null){
                    throw new InvalidPluginException(new Exception("Can not find Mainclass."));
                }
            else
                result = (PythonPlugin) pyClass.__call__().__tojava__(PythonPlugin.class);
            
            interp.set("PYPLUGIN", result);
            
            result.interp = interp;
            
            // Run scripts designed to be run after plugin creation
            for (String script : post_plugin_scripts) {
	            InputStream metastream = this.getClass().getClassLoader().getResourceAsStream("scripts/"+script);
	            interp.execfile(metastream);
	            metastream.close();
            }
            
            result.initialize(this, server, description, dataFolder, file);
            result.setDataFile(data);
            
        } catch (Throwable t) {
            throw new InvalidPluginException(t);
        }

        if (!loadedplugins.contains(description.getName()))
            loadedplugins.add(description.getName());
        return result;
    }

    private boolean isPluginLoaded(String name) {
        if (loadedplugins.contains(name))
            return true;
        if (ReflectionHelper.isJavaPluginLoaded(server.getPluginManager(), name))
            return true;
        return false;
    }

    public Pattern[] getPluginFileFilters() {
        return fileFilters;
    }

    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof PythonPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (plugin.isEnabled()) {
            PythonPlugin pyPlugin = (PythonPlugin) plugin;

            try {
                pyPlugin.setEnabled(false);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE,
                        "Error occurred while disabling " + plugin.getDescription().getFullName()
                                + " (Is it up to date?): " + ex.getMessage(),
                        ex);
            }

            server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

            String pluginName = pyPlugin.getDescription().getName();
            if (loadedplugins.contains(pluginName))
                loadedplugins.remove(pluginName);
        }
    }

    public void enablePlugin(Plugin plugin) {
        if (!(plugin instanceof PythonPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (!plugin.isEnabled()) {
            PythonPlugin pyPlugin = (PythonPlugin) plugin;

            String pluginName = pyPlugin.getDescription().getName();

            if (!loadedplugins.contains(pluginName))
                loadedplugins.add(pluginName);

            try {
                pyPlugin.setEnabled(true);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE,
                        "Error occurred while enabling " + plugin.getDescription().getFullName()
                                + " (Is it up to date?): " + ex.getMessage(),
                        ex);
            }

            // Perhaps abort here, rather than continue going, but as it stands,
            // an abort is not possible the way it's currently written
            server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        }
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(
            Listener listener, Plugin plugin) {
        boolean useTimings = server.getPluginManager().useTimings();
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<Class<? extends Event>, Set<RegisteredListener>>();
        PythonListener pyListener = (PythonListener)listener;

        for(Map.Entry<Class<? extends Event>, Set<PythonEventHandler>> entry : pyListener.handlers.entrySet()) {
            Set<RegisteredListener> eventSet = new HashSet<RegisteredListener>();

            for(final PythonEventHandler handler : entry.getValue()) {
                EventExecutor executor = new EventExecutor() {

                    @Override
                    public void execute(Listener listener, Event event) throws EventException {
                        ((PythonListener)listener).fireEvent(event, handler);
                    }
                };
                if(useTimings) {
                    eventSet.add(new TimedRegisteredListener(pyListener, executor, handler.priority, plugin, false));
                }
                else {
                    eventSet.add(new RegisteredListener(pyListener, executor, handler.priority, plugin, false));
                }
            }
            ret.put(entry.getKey(), eventSet);
        }
        return ret;
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file)
            throws InvalidDescriptionException {
        Validate.notNull(file, "File cannot be null");

        InputStream stream = null;
        PluginDataFile data = null;

        if (file.getName().endsWith("dir")) {
            if (!file.isDirectory())
                throw new InvalidDescriptionException(new InvalidPluginException(new Exception("python directories cannot be normal files! .pyp or .py.zip instead.")));
            data = new PluginPythonDirectory(file);
        } else if (file.getName().endsWith("zip") || file.getName().endsWith("pyp")) {
            if (file.isDirectory())
                throw new InvalidDescriptionException(new InvalidPluginException(new Exception("python zips cannot be directories! try .py.dir instead.")));
            try {
                data = new PluginPythonZip(file);
            } catch (InvalidPluginException ex) {
                throw new InvalidDescriptionException(ex);
            }
        } else {
            throw new InvalidDescriptionException(new InvalidPluginException(new Exception("filename '"+file.getName()+"' does not end in dir, zip, or pyp! did you add a regex without altering loadPlugin()?")));
        }

        try {
            stream = data.getStream("plugin.yml");

            if(stream == null) {
                //TODO Does this cause serious problems with plugins which have no plugin.yml file?
                throw new InvalidDescriptionException(new InvalidPluginException(new FileNotFoundException("Plugin does not contain plugin.yml")));
            }

            return new PluginDescriptionFile(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {}
            }
        }
        return null;
    }
}
