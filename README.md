# BHome
Boomerang Home is part of a series of plugins designed to let you pick the functionality you want from the Boomerang suite of plugins.

# Version 1.4 

* Fixed players not spawning at X + 0.5 and Z + 0.5 of the target location.<br />
* Added config.yml option 'spawn_height' to enable specifying height above block player should spawn at target location.<br />
* Added updating timestamp of player's home yml file on player join in order to record last join. This enables easily identifying old player files.<br />
* Added subcommands to bhome: 'show' and 'tp' with tab-completion to allow ops to list and teleport to offline player homes.<br />
* Updated 'sethome' command so that if no home exists for the calling player and no arguments are given, a defaullt home 'home' is created.<br />
* Updated 'home' command so that if a calling player has only one home, that home is select and teleported to by default.<br />
* Fixed problem with locations not calculating right having to do with offsets from negative coords.<br />
