PPLoader - Python Plugin Loader
====================

The python plugin loader is a pluginloader for bukkit to load python plugins
via jython. 


Using the plugin loader
-----------------------

Building
********


1. Create project in Eclipse
2. Import source code
3. Export to jar and don't forget MANIFEST.MF


Running
*******

1. Put pploader.jar in your bukkit/plugins/ dir
2. Put jython.jar in your bukkit/lib/ dir
3. [Re-]Start bukkit

Using plugins
*************

1. Stick the plugin.pyp or .zip or .py.dir in your bukkit/plugins/ dir
2. [Re-]Start bukkit



API Details
===========

The api contains quite a few places where you can do things multiple ways. This
section documents these.

Plugin files
------------

Your plugin may go in:

- A zip whos name ends in either .py.zip or .pyp
- A directory whose name ends in .py.dir or \_py_dir (for windows users)
- A python file (obviously, named .py)

Zips with the .pyp extension are recommended if you release any plugins. When
you use a zip, your must specify your own metadata; it will not allow guessed
metadata.

When using a dir or a zip, your zip or dir must contain a main python file and
optionally a plugin.yml containing metadata (see the following section). Your
python main file normally should be named either plugin.py or main.py.
plugin.py should generally be used when you are using the class api and main.py
when using the decorator api. Under some conditions you may want to change the
name of your main file (such as, other plugins needing to be able to import
it). This is not recommended but is possible with the main field in the
metadata.

When using a single .py file in plugins, your single .py is your main python
file. You cannot have a separate plugin.yml - if you want to have any special
metadata, you will need a directory or zip plugin.

Plugin metadata
---------------

Plugins require metadata. The absolute minimum metadata is a name and a version.
The location of your main file/class is also required, if you don't like
defaults. The 'main' field of plugin metadata has special behavior:

- if the main is set in plugin.yml, it searches for the value set in main as
   the main file before searching for the default file names. see "Main files".
- the main is used to search for a main class before searching the default
   class name.

There are three places you can put this metadata. In order of quality:

- plugin.yml
- your main python file
- your plugin filename

plugin.yml is the best as you are able to set all metadata fields that exist
in bukkit, and should be used for all plugins that you release. plugin.yml is
used in all java plugins (as it is the only option for java plugins). as such,
opening up java plugin jars is a good way to learn what can go in it. Here is
an example of plugin.yml:

    name: SamplePlugin
    main: SampleClass
    version: 0.1-dev
    commands:
        samplecommand:
            description: send a sample message
            usage: /<command>

The plugin filename is automatically used if no plugin.yml is found. The
extension is removed from the filename and used as the "name" field.
The version field is set to "dev" (as this case should only occur when first
creating a plugin). the main field is set to a default value that has no
effect.

The plugin main python file can be used if (and only if) you do not have a
plugin.yml file, so that you can override the defaults set by the plugin
filename case. It is recommended that you set these values at the top of your
main python file. None of these values are required. These are the values you
can set:

    __plugin_name__ = "SamplePlugin"
    __plugin_version__ = "0.1-dev"
    __plugin_mainclass__ = "SampleClass"
    __plugin_website__ = "http://example.com/sampleplugin"

note that plugin_mainclass can only be used to set the main class; it
cannot be used to set the main python file, as it must be contained in the
main python file. if you want to change the main python file, you must have a
plugin.yml.

Summary of fields:

- "main" - name of main python file or name of main class
- "name" - name of plugin to show in /plugins list and such. used to name the
   config directory. for this reason it must not equal the full name of the
   plugin file.
- "version" - version of plugin. shown in errors, and other plugins can access it
- "website" - mainly for people reading the code

Class (bukkit standard) API
---------------------------

To write a plugin with this api is almost identical to writing one in java, so
much so that you can safely use the documentation on how to write a java
plugin; simply translate it into python. the java2py tool may even work on
existing java plugins (though no promises).

If your plugin is a single file it must contain metada and main class.
Main class have to be extended from PythonPlugin class. (You don't have to 
import it, because it is auto imported on plugin startup by scripts/preload.py 
in pploader.jar). Your main class must have onEnable and onDisable method.

plugin.py
*********

    __plugin_name__ = "SamplePlugin"
    __plugin_version__ = "0.1-dev"
    __plugin_mainclass__ = "SampleClass"
    __plugin_website__ = "http://example.com/sampleplugin"
    
    
    class SampleClass(PythonPlugin):
        def onEnable(self):            
            print "sample plugin enabled"
        
        def onDisable(self):
            print "sample plugin disabled"
            

Event Handlers (Обработчики событий)
-----------------------------------

Event Handlers is the main part of any plugin. Вся логика 
обычно именно здесь. Создание Event Handlers немного отличается от стандартного 
java api. As usual you must create Listener class, but extend it from 
PythonListener class. (You don't have to import it, because it is auto imported 
on plugin startup by scripts/preload.py in pploader.jar). Methods of Listener
class which will be event handlers must be decorated with EventHandler. You 
must pass EventType and EventPriority to EventHandler decorator. 
Example: 

plugin.yml
**********

    name: SamplePlugin
    main: SampleClass
    version: 0.1-dev
    commands:
        samplecommand:
            description: send a sample message
            usage: /<command>

plugin.py
*********

    from org.bukkit.event import EventPriority
    from org.bukkit.event.server import ServerCommandEvent

    class SimpleListener(PythonListener):
        @EventHandler(ServerCommandEvent, EventPriority.NORMAL)
        def onServerCommand(self, event):
            print event.getCommand()  
            
    class SampleClass(PythonPlugin):

        def onEnable(self):
            pm = self.getServer().getPluginManager()
            self.listener = SimpleListener()
            pm.registerEvents(self.listener, self)
            print "sample plugin enabled"

        def onDisable(self):
            print "sample plugin disabled"

        def onCommand(self, sender, command, label, args):
            return False

        def onServerCommand(self, event):
            print event.getCommand()
      
    print "sample plugin main file run"
