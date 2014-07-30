package org.cyberlis.pyloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import sun.rmi.log.ReliableLog.LogFile;

/**
 * Java plugin to initialize python plugin loader and provide it with a little moral boost.
 * @author masteroftime
 * @author lahwran
 * @author cyberlis
 *
 */
public class PythonLoader extends JavaPlugin {

	protected PluginManager pm;
    public void onDisable() {}
    public void onEnable() {}

    /**
     * Initialize and load up the plugin loader.
     */
    @Override
    public void onLoad() {
        //check if jython.jar exists if not try to download
        if(!new File("lib/jython.jar").exists()) {
            getServer().getLogger().log(Level.SEVERE, "Could not find lib/jython.jar! I will try to automatically download it for you.");
            try {
                URL website = new URL("http://rccraft.ru/dwn/jython.jar");
                URLConnection connection = website.openConnection();
                connection.connect();

                //create lib folder if it doesn't exist
                File lib = new File("lib");
                if(!lib.exists()) {
                    lib.mkdir();
                }

                //delete temporary _dl file if it already exists
                File dl_file = new File("lib/jython.jar_dl");
                if(dl_file.exists()) {
                    dl_file.delete();
                }

                InputStream in = connection.getInputStream();
                FileOutputStream out = new FileOutputStream(dl_file);

                long total = connection.getContentLengthLong();
                long progress = 0;
                byte[] buffer = new byte[1024];
                int read;
                long start = System.currentTimeMillis();

                while((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    progress += read;
                    if(System.currentTimeMillis() - start > 2000) {
                        System.out.println("Downloading Jython: " + (progress*100/total) + " %");
                        start = System.currentTimeMillis();
                    }
                }

                out.close();
                in.close();

                dl_file.renameTo(new File("lib/jython.jar"));
                getServer().getLogger().log(Level.INFO, "Download successful!");
                getServer().getLogger().log(Level.INFO, "YOU MUST RESTART YOUR SERVER NOW!");
                return;
            } catch (IOException e) {
                getServer().getLogger().log(Level.SEVERE, "Error while downloading jython.jar, loading of python plugins will fail! Please download jython from http://dev.bukkit.org/media/files/647/602/jython.jar and place it in the lib folder");
                e.printStackTrace();
            }
            
            
        }

        //System.out.println("PythonLoader: initializing");
        // This must occur as early as possible, and only once.
        pm = Bukkit.getServer().getPluginManager();
        boolean needsload = true;

        String errorstr = "cannot ensure that the python loader class is not loaded twice!";
        Map<Pattern, PluginLoader> fileAssociations = ReflectionHelper.getFileAssociations(pm, errorstr);

        if (fileAssociations != null) {
            PluginLoader loader = fileAssociations.get(PythonPluginLoader.fileFilters[0]);
            if (loader != null) // already loaded
                needsload = false;
        }

        if (needsload) {
            //System.out.println("PythonLoader: loading into bukkit");
            pm.registerInterface(PythonPluginLoader.class);
            //pm.loadPlugins(this.getFile().getParentFile()); //TODO Check weather this reloads java plugins which were already loaded

            for (File file : this.getFile().getParentFile().listFiles()) {
                for (Pattern filter : PythonPluginLoader.fileFilters) {
                    Matcher match = filter.matcher(file.getName());
                    if (match.find()) {
                        try {
                            pm.loadPlugin(file);
                        } catch (InvalidPluginException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvalidDescriptionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (UnknownDependencyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    
    public final boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
      if (cmd.getName().equalsIgnoreCase("pploader")) {
        if (args.length < 1) {
          return false;
        }
        String action = args[0];

        if ((!action.equalsIgnoreCase("load")) && (!action.equalsIgnoreCase("unload")) && (!action.equalsIgnoreCase("reload"))) {
          getServer().getLogger().severe("Invalid action specified.");
          return false;
        }

        if(!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player && sender.isOp())){
          getServer().getLogger().severe("You do not have the permission to do this.");
          return true;
        }

        if (args.length == 1) {
          getServer().getLogger().severe("You must specify plugin name or filename");
          return true;
        }

          try
          {
            if (action.equalsIgnoreCase("unload")){
            	String plName = args[1];
        		unloadPlugin(plName);
            }
            else if (action.equalsIgnoreCase("load")){
            	String fileName = args[1];
            	loadPlugin(fileName);
            }
            else if (action.equalsIgnoreCase("reload") && args.length == 3){
            	String plName = args[1];
            	String fileName = args[2];
            	reloadPlugin(plName, fileName);
            } else {
            	getServer().getLogger().severe("You must specify plugin name or filename");
            }
          }
          catch (Exception e) {
        	  e.printStackTrace();
            getServer().getLogger().severe("Exception while perfoming action "+action+" "+e.getMessage());
          }

        return true;
      }

      return false;
    }

    private boolean unloadPlugin(String pluginName)
      throws Exception
    {
      PluginManager manager = getServer().getPluginManager();
      SimplePluginManager spmanager = (SimplePluginManager)manager;

      if (spmanager != null) {
        Field pluginsField = spmanager.getClass().getDeclaredField("plugins");
        pluginsField.setAccessible(true);
        List plugins = (List)pluginsField.get(spmanager);

        Field lookupNamesField = spmanager.getClass().getDeclaredField("lookupNames");
        lookupNamesField.setAccessible(true);
        Map lookupNames = (Map)lookupNamesField.get(spmanager);

        Field commandMapField = spmanager.getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        SimpleCommandMap commandMap = (SimpleCommandMap)commandMapField.get(spmanager);

        Field knownCommandsField = null;
        Map knownCommands = null;

        if (commandMap != null) {
          knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
          knownCommandsField.setAccessible(true);
          knownCommands = (Map)knownCommandsField.get(commandMap);
        }
        Iterator it;
        for (Plugin plugin: manager.getPlugins())
          if (plugin.getDescription().getName().equalsIgnoreCase(pluginName)) {
            manager.disablePlugin(plugin);

            if ((plugins != null) && (plugins.contains(plugin))) {
              plugins.remove(plugin);
            }

            if ((lookupNames != null) && (lookupNames.containsKey(pluginName))) {
              lookupNames.remove(pluginName);
            }

            if (commandMap != null)
              for (it = knownCommands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();

                if ((entry.getValue() instanceof PluginCommand)) {
                  PluginCommand command = (PluginCommand)entry.getValue();

                  if (command.getPlugin() == plugin) {
                    command.unregister(commandMap);
                    it.remove();
                  }
                }
              }
          }
      }
      else
      {
        getServer().getLogger().warning(pluginName + " is already unloaded.");
        return true;
      }
      
      getServer().getLogger().info("Unloaded " + pluginName + " successfully!");

      return true;
    }

    private boolean loadPlugin(String pluginName)
    {
      try
      {
    	  
    	  File file = new File("plugins", pluginName);
          for (Pattern filter : PythonPluginLoader.fileFilters) {
              Matcher match = filter.matcher(file.getName());
              if (match.find()) {
                  try {
                	  Plugin plugin = pm.loadPlugin(file);
                      pm.enablePlugin(plugin);
                  } catch (InvalidPluginException e) {

                      e.printStackTrace();
                  } catch (InvalidDescriptionException e) {

                      e.printStackTrace();
                  } catch (UnknownDependencyException e) {

                      e.printStackTrace();
                  }
              }
          }
   
      } catch (Exception e) {
    	  e.printStackTrace();
    	  getServer().getLogger().severe("Error loading " + pluginName + ", this plugin must be reloaded by restarting the server.");
    	  return false;
      }

      getServer().getLogger().info("Loaded " + pluginName + " successfully!");
      return true;
    }

    private boolean reloadPlugin(String pluginName, String fileName)
      throws Exception
    {
      boolean unload = unloadPlugin(pluginName);
      boolean load = loadPlugin(fileName);

      if ((unload) && (load)) {
    	  getServer().getLogger().info("Reloaded " + pluginName + " successfully!");
      }
      else {
    	  getServer().getLogger().severe("Error reloading " + pluginName + ".");

        return false;
      }

      return true;
    }

}
