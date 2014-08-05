PPLoader - Python Plugin Loader
====================
Forked from https://github.com/masteroftime/Python-Plugin-Loader

PPLoader это загрузчик плагинов для bukkit, который позволяет загружать плагины 
написанные на jython. 


Использование загрузчика
-----------------------

Запуск
*******

1. Кладем pploader.jar в каталог bukkit/plugins/
2. Кладем jython.jar вкаталог bukkit/lib/
3. Запускаем сервер

Использование python плагинов
*************

1. Закидываем .pyp или .zip или .py.dir в каталог bukkit/plugins/
2. Запускаем сервак

API
===========

Файлы плагинов
------------

Файлы плагинов написанные на python могут лежать:

- В zip файле имя которого кончается на .py.zip или .pyp
- В каталоге имя которого заканчивается
на .py.dir или \_py_dir (для windows)

Zipы лучше всего использовать с расширением .pyp

При использовании каталога или zip-файла, они должны содержать главный python 
файл, который должен называться plugin.py, и обязательный
файл plugin.yml содержащий metadata.

Metadata плагинов
---------------

Плагинам необходима metadata. Самый минимум metadata это имя плагина, версия 
и главный класс.

plugin.yml используется во все текущих плагинах на java. Только тут можно 
указать всю возможную metadata. Тут можно указать команды права и прочее.
Пример plugin.yml:

    name: SamplePlugin
    main: SampleClass
    version: 0.1-dev
    commands:
        samplecommand:
            description: send a sample message
            usage: /<command>

Описание полей:

- "main" - имя главного класса.
- "name" - имя плагина, которое используется в bukkit. Например при вводе
    комынды /plugins
- "version" - версия плагина. Используется bukkit при отображении ошибок.
- "website" - сайт. для людей которые просматривают код


Class (bukkit standard) API
---------------------------------

Разработка плагинов с помощью Class API идентична, разработке на java.
Можно уверенно использовать документацию по разработки java плагинов просто
переводя код в python. Далее будут описаны исключения и особенности.

Главный класс
---------------

Главый класс должен быть унаследован от класса 
org.cyberlis.pyloader.PythonPlugin (Его не нужно импортировать. По умолчанию 
его импортирует scripts/preload.py). Так же он должен содержать методы onEnable
и onDisable. Пример:

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
            
Event Handlers (Обработчики событий)
-----------------------------------

Обработчики событий cамая главная часть любого плагина. Вся логика 
обычно именно здесь. Создание Event Handlers немного отличается от стандартного 
java api. Как и обычно необходимо создать Listener класс, но наследовать его 
надо от PythonListener (Его не нужно импортировать. По умолчанию его 
импортирует и немного модифицирует scripts/preload.py). Методы Listener класса,
котрые будут обрабатывать события, необходимо обернуть декоратором 
@PythonEventHandler, в который необходимо передать тип события и его приоритет. 
Пример: 

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
        @PythonEventHandler(ServerCommandEvent, EventPriority.NORMAL)
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

