# World of Goo 2 Tool

A mod loader for World of Goo 2 which allows you to publish and install custom mods in the .goo2mod format.

Make sure you have Java 17 or above installed.

## Usage

Download Goo2Tool.jar from [Releases](https://github.com/Darxoon/Goo2Tool/releases/) and run it. Follow along with the setup it asks of you. After that, just drag and drop .goo2mod files into the application window, save and lauch the game!

Note that the **MacOS Steam version** is currently completely unsupported and **MacOS support in general** is currently experimental. Windows and Linux are fully supported though.

To create your own goo2mods, either use the "Package level as .goo2mod" option in the "File" menu or refer to [the Goo2mod v2.2 spec](goo2mod-spec-v2.2.md) for creating your own.

## FistyLoader support

If you are on the Windows/Proton Steam version, you can install FistyLoader directly from within the application. It is also possible for mods to use FistyLoader via the [FistyLoader Spec Extension](goo2mod-fistyloader.md) (which is also handled by default by the "Package level as .goo2mod" feature btw).

## Example mods

If you are interested in example mods for Goo2Tool, check out "Flavor Extractor" by me (Darxoon) and "Cloud Upload" by MeraVera also in [Releases](https://github.com/Darxoon/Goo2Tool/releases/)!

Note that Flavor Extractor requires FistyLoader (which might also make it a good example of how to make your own mod with custom gooballs!)

## Building

Make sure you have the JDK 17+ and Maven installed.

Open the Goo2Tool folder in the command line and run `mvn install`. This should install all necessary dependencies and compile the program into `Goo2Tool.jar` in the root of the project  folder.
