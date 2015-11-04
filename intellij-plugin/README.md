Djinni intellij plugin
======================

This plugin adds support for some basic navigation, code-completion and error highlighting in .djinni files
to the [jetbrains][1] family of IDE's.

### Development

Setup an environment to develop plugins for the intellij platform. There is a [section on how to do that][2]
in the [IntelliJPlatform SDK DevGuide][3]. But instead of creating an entirely new plugin as part of that with "File | New | Module"
you choose "File | New | Module from Existing Sources" and select the djinni.iml file right beside this readme.
Another good starting point for information about intellij plugin development is the JetBrains blog category [Plugin development][4].


### Deployment

This plugin will be available in the jetbrains 3rd party plugin repository at some point.
Until then, or for custom builds, the plugin can be deployed as follows:
1. Set up the dev environment as described above.
2. Navigate to the djinni plugin in the Project Navigation view.
3. Right click on the djinni plugin module there.
4. Choose "Prepare Plugin Module 'djinni' For Deployment". Choose a place and name for the .jar file that results from this.
5. Install this plugin into a jetbrains IDE from the plugins tab in the preferences by clicking "Install plugin from disk..."


[1]: http://www.jetbrains.com
[2]: http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html
[3]: http://www.jetbrains.org/intellij/sdk/docs/
[4]: http://blog.jetbrains.com/idea/category/plugin-development/
