PPLoader - Python Plugin Loader
====================

The python plugin loader is a pluginloader for bukkit to load python plugins
via jython. 


Using the plugin loader
-----------------------

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

Zips with the .pyp extension are recommended if you release any plugins. When
you use a zip, your must specify your own metadata; it will not allow guessed
metadata.

When using a dir or a zip, your zip or dir must contain a main python file and
a plugin.yml containing metadata (see the following section). Your
python main file normally should be named either plugin.py or main.py.
plugin.py should generally be used when you are using the class api and main.py

Plugin metadata
---------------

Plugins require metadata. The absolute minimum metadata is a name version and main class.
The 'main' field of plugin metadata has special behavior:

- the main is used to search for a main class before searching the default
   class name.

plugin.yml is able to set all metadata fields that exist
in bukkit, and should be used for all plugins that you release. plugin.yml is
used in all java plugins (as it is the only option for java plugins). as such,
opening up java plugin jars is a good way to learn what can go in it. 
Or you can read about it here http://wiki.bukkit.org/Plugin_YAML
Here isan example of plugin.yml:

    name: SamplePlugin
    main: SampleClass
    version: 0.1-dev
    commands:
        samplecommand:
            description: send a sample message
            usage: /<command>

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

Main class have to be extended from PythonPlugin class. (You don't have to 
import it, because it is auto imported on plugin startup by scripts/preload.py 
in pploader.jar). Your main class must have onEnable and onDisable method.

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

    class SampleClass(PythonPlugin):
        def onEnable(self):            
            print "sample plugin enabled"
        
        def onDisable(self):
            print "sample plugin disabled"
            

Event Handlers
-----------------------------------

Event Handlers is the main part of any plugin. Вся логика 
обычно именно здесь. Создание Event Handlers немного отличается от стандартного 
java api. As usual you must create Listener class, but extend it from 
PythonListener class. (You don't have to import it, because it is auto imported 
on plugin startup by scripts/preload.py in pploader.jar). Methods of Listener
class which will be event handlers must be decorated with PythonEventHandler. 
You must pass EventType and EventPriority to PythonEventHandler decorator. 
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
    from org.bukkit.event.player import PlayerJoinEvent

    class SimpleListener(PythonListener):

        @PythonEventHandler(PlayerJoinEvent, EventPriority.NORMAL)
        def onPlayerJoin(self, event):
            event.getPlayer().sendMessage('Wellcome to rccraft server')
            
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
