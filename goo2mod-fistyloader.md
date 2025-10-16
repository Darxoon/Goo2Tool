# Goo2mod Specification FistyLoader v1.1 Extension

[FistyLoader](https://github.com/Darxoon/FistyLoader/) is a runtime code modification to the Steam release of World of Goo 2 that allows for features such as custom goo balls.

Goo2Tool implements the FistyLoader Extension for [the Goo2mod v2.2 spec](goo2mod-spec-v2.2.md). It targets FistyLoader v1.1 (which added Steam and Level Editor support) currently. The format will be detailed in the following.

> Note: Goo2Tool's "package level as .goo2mod" feature already does most of this automatically for you if all you need to export is a level and the custom resources it depends on.
> 
> However, in case you need to do manual edits to the resulting goo2mod or create anything more complex than this, it is still a good idea to properly learn the goo2mod format.

## Overview

If your goo2mod wishes to use FistyLoader, for example to make use of custom goo balls, you can enable this feature by adding the following item to your addin.xml's `dependencies` property:

```XML
<depends min-version="1.1">FistyLoader</depends>
```

With this added item, if a user wants to install your Goo2mod, their tool should automatically check for FistyLoader to be installed and of the correct version and if not, prompt the user to install it automatically.

Enabling this allows you to put the following file into the root of your goo2mod zip file:

* `balls.ini`: A file of the same file format as FistyLoader's `ballTable.ini` file, except that it exclusively contains the custom goo balls used and defined in your mod.

When installing your goo2mod, the tool should automatically append this file to the user's global ballTable.ini file while making sure to not include any duplicates and also while resolving any conflicts caused by several mods using the same typeEnum for different goo balls.

In case the just mentioned instructions cause any custom goo balls to be mapped to different typeEnums than defined in the goo2mod's balls.ini file, it is the tool's responsibility to go through all references of the original typeEnum and update them to the new value instead. 

## Using dependencies which use FistyLoader

If you add another addin to your `dependencies` field that uses FistyLoader, as long as you don't use any of the dependency's custom goo balls, you won't have to do anything else. The user's tool should just figure out the dependency's FistyLoader dependency on its own.

If you want to make use of the dependency's custom goo balls though, you will have to go through some extra steps in order to make the tool aware that you are using them, as defined in the following:

* You have to add FistyLoader as a dependency to your mod directly (see the example XML tag in [Overview](#overview)) in case you haven't already.

* Every goo ball that you use from the dependency has to also be defined in your mod's balls.ini file, even if the ball's resources aren't included in your goo2mod zip file.

    (Note: the typeEnum in your goo2mod does not necessarily have to be the same as in the mod in which the gooball is defined, as long as it's consistent with how it's used in your custom levels. The user's tool should be able to modify all references to the gooball to whichever typeEnum it ends up being allocated at in the end anyway.)

* You are also able to define goo balls from the dependency in your balls.ini file if you don't actually use them in your mod.

    (This is mainly so that you can just copy-paste the dependency's ball.ini file into yours verbatim.)

These requirements exist mainly because due to technical reasons, it can't always be guaranteed that the contents of the dependency are already loaded and known to the tool at the time of installing your mod.

If you don't follow these steps then your mod might still work under certain fragile conditions, although your mod will be able to break easily when it is mixed with other mods that also use FistyLoader. If you followed these steps then your mod should be still work no matter which circumstances it is in.
