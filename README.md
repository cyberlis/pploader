PPLoader - Python Plugin Loader
====================
Forked from https://github.com/masteroftime/Python-Plugin-Loader

Python plugin loader это загрузчик плагинов для bukkit, который позволяет
загружать плагины написанные на jython. 


Использование загрузчика
-----------------------

Компилирование
********


1. Создаем в Eclipse проект
2. Импортируем исходники
3. Экспортируем в jar файл


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
- В python файле (как обычно заканчивается на .py)

Zipы лучше всего использовать с расширением .pyp Когда вы используете zip,
необходимо объявить metadata;

При использовании каталога или zip-файла, они должны содержать главный python 
файл, который должен называться plugin.py, и необязательный (но желательный)
файл plugin.yml содержащий metadata.

При использовании одиночного python файла .py в каталоге plugins, 
каждый .py является главным файлом плагина.В этом случае у каждого плагина
не может быть отдельного plugin.yml для metadata.

Metadata плагинов
---------------

Плагинам необходима metadata. Самый минимум metadata это имя плагина, версия 
и главный класс.

Существует три места где может храниться metadata. В порядке качества:

- plugin.yml
- главный python файл
- имя главного python файла

plugin.yml самый лучший способ для указания metada. plugin.yml используется
во все текущих плагинах на java. Только тут можно указать всю возможную 
metadata. Тут можно указать команды права и прочее.
Пример plugin.yml:

    name: SamplePlugin
    main: SampleClass
    version: 0.1-dev
    commands:
        samplecommand:
            description: send a sample message
            usage: /<command>

Имя фала для получения metadata используется если не найден plugin.yml.
Расширение главного файла убирается и имя файла используется для поля 'name'.
Поле 'version' ставится равным 'dev'.
Поле 'main' становится равным 'Plugin'

Главный python файл может быть использован для задания metada, тогда и только
тогда, когда нет plugin.yml и нужно изменить значения по умолчанию 
установленные из имени файла. Рекомендуется устанавливать эти значения в начале
файла. Поля не являются обязательными. Пример:

    __plugin_name__ = "SamplePlugin"
    __plugin_version__ = "0.1-dev"
    __plugin_mainclass__ = "SampleClass"
    __plugin_website__ = "http://example.com/sampleplugin"

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
org.cyberlis.pyloader.PythonPlugin. Так же он должен содержать методы onEnable
и onDisable. Пример:

plugin.py
*********
    __plugin_name__ = "SamplePlugin"
    __plugin_version__ = "0.1-dev"
    __plugin_mainclass__ = "SampleClass"
    __plugin_website__ = "http://example.com/sampleplugin"
    
    from org.cyberlis.pyloader import PythonPlugin
    
    class SampleClass(PythonPlugin):
        def onEnable(self):            
            print "sample plugin enabled"
        
        def onDisable(self):
            print "sample plugin disabled"
            
Event Handlers (Обработчики событий)
-----------------------------------

Самая главная часть любого плагина. Вся логика обычно именно здесь.
Создание Event Handlers немного отличается от стандартного java api
Как и обычно необходимо создать Listener класс, но наследовать его надо от
org.cyberlis.pyloader.PythonListener. После вызвать метод addHandler 
экзэмпляра класса PythonListener и передать ему функции обработчика события,
событие которое мы будем обрабатывать и его приоритет. Пример: 

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
    from org.cyberlis.pyloader import PythonListener

    class SampleClass(PythonPlugin):
        def __init__(self):
            print "sample plugin main class instantiated"

        def onEnable(self):
            pm = self.getServer().getPluginManager()
            self.listener = PythonListener()
            self.listener.addHandler(self.onServerCommand, ServerCommandEvent, EventPriority.NORMAL)
            pm.registerEvents(self.listener, self)

            print "sample plugin enabled"

        def onDisable(self):
            print "sample plugin disabled"

        def onCommand(self, sender, command, label, args):
            msg = "sample plugin command"
            print sender.getName()
            sender.sendMessage(msg)
            return True

        def onServerCommand(self, event):
            print event.getCommand()
    print "sample plugin main file run"
