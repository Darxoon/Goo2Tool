# Goo2mod Specification v2.2

Goo2Tool implements the (unofficial) version 2.2 of the goo2mod spec, which will be detailed in the following.

> Note: Goo2Tool's "package level as .goo2mod" feature already does most of this automatically for you if all you need to export is a level and the custom resources it depends on.
> 
> However, in case you need to do manual edits to the resulting goo2mod or create anything more complex than this, it is still a good idea to properly learn the goo2mod format.

## Overview

A .goo2mod file is essentially just a zip file renamed to .goo2mod. It consists of two files and three directories in the root level. Note that the zip root may contain more than that, for example, due to [the FistyLoader spec extension](goo2mod-fistyloader.md). However, all items defined are are as follows:

* `addin.xml`: This is an XML file containing the most important information of the mod, such as its identifier (ID) and all of the metadata which gets displayed in the GUI. [Jump to details.](#addinxml)

* `translation.xml`: This is an XML file of the same format as "translation-local.xml" and "translation-tool-export.xml" of the original game. This should contain all custom strings that the mod uses, which will get merged into these two original files respectively.

* `compile/`: This folder contains all completely custom '.wog2' and '.xml' files of the mod, which may get analyzed and transformed as necessary by the tool in order to install the file. If a file of the name and path already existed in the game directory, it will simply be overwritten by this.

* `merge/`: This folder contains all '.wog2' and '.xml' files which this mod makes modifications to. Currently supported file types to merge are any .wog2 file and all resources.xml files. [Jump to details.](#merge)

* `override/`: This folder contains any non-code assets, such as images and sounds, which will be inserted into the game directory.

In all three of these folders, the file structure inside them mirrors exactly the file structure inside World of Goo 2's game directory. E.g.: in order to insert the file `Test.wog2` at the location `World of Goo 2/game/res/levels`, the file should be placed in this location inside the zip file: `compile/res/levels/Test.wog2`.

## addin.xml

This file contains the most important metadata of your mod. The specification for this is based on the original GooTool's [addin.xml spec](http://goofans.com/developers/addin-file-format/manifest-file), albeit modified to fit the needs of World of Goo 2.

The addin.xml file contains one root xml element, called addin, which must have the attribute `spec-version="2.2"`. The file can contain the following properties:

* `id` *(required)*: The globally-unique identifier of this addin. This must not change between different versions of your addin. It is used for the internal filename of the addin, for other addins to define dependencies, and to ensure previous versions of your addin are removed on upgrade. It is never displayed to the user. 

    The format of this value should ideally be like this: `authorname.ModName`, where 'authorname' is your name in all-lowercase and without spaces, and 'ModName' is the of your mod in PascalCase, although you may put whatever values you want in between them, e.g., like this: `username.mylevelpack.Level1`.

* `name` *(required)*: The name of the addin, as it is shown to the user in places like the mod list.

* `type` *(required)*: This is either the value `level` or `mod`. Both are identical except that addins of type level have a "levels" property (as described later).

* `version` *(required)*: This value contains up to 4 numbers separated by periods, e.g., `1.0.2`. It should identify each release/update you make of the mod, so whenever you are updating your mod, you should increment this number.

* `description`: Here you can add a tagline/subtitle to your mod or explain your mod in more detail. This is shown in the text box at the bottom of the mods list whenever the user clicks on your addin.

* `author` *(required)*: Your name/username.

* `dependencies`: This can contain a list of other addins that your addin depends on, e.g., because your addin uses resources that are included in the depended on mod. Whenever someone tries to install your mod and doesn't have all dependencies installed and enabled, Goo2Tool will tell them and force them to install the dependencies too.

    Each item in this list of dependencies should be an xml tag with the name `depends`, which can have the attributes `min-version` and `max-version`. The content of the tag is the ID of the addin you are depending on.

* `levels`: If your addin is of type `level`, this can be a list of levels that will all get copied into the user's profile and will show up in the Level Editor menu. Each level has to be an xml tag with the name `level` and has to contain the following properties (in the form of other xml elements, not attributes):

    * `filename` *(required)*: the name of your .wog2 file inside the compile/res/levels directory *without* the file extension
    * `thumbnail`: the thumbnail to be shown in the level editor menu. Has to be a jpg file with dimensions 640x480 pixels.
    
        The actual jpg file should be put in the 'override' directory (e.g. `override/res/thumbnails/[...].jpg`), and this property should reference it by its path starting with `res/`. Note that this file will only be copied into the thumbnail cache in your profile, not the game directory.

All in all, your addin.xml could, for example, look something like this:

```XML
<addin spec-version="2.2">
    <id>darxoon.TestMod</id>
    <name>Test Mod</name>
    <type>level</type>
    <version>1.0</version>
    <description>Just testing things</description>
    <author>Darxoon</author>
    
    <dependencies>
        <!-- For the custom ISH background or something -->
        <depends min-version="1.0">vera.CloudUpload</depends>
    </dependencies>
    
    <levels>
        <level>
            <filename>TestMod</filename>
            <thumbnail>res/thumbnails/TestMod.jpg</thumbnail>
        </level>
    </levels>
</addin>
```

## Merge

The only currently supported files to be put in the `merge/` folder are any .wog2 file and any resources.xml / _resources.xml / .resrc file, although more file types may come in the future.

### resources.xml

Resource merges work almost the same as the original game's resources.xml files. At the root, they contain an xml element named `ResourceManifest`. This element can contain any number of `Resources` elements, all of which have to have an `id` attribute and can contain any number of `SetDefaults`, `Image`, `Sound`, etc. tags.

Note that unlike the original game's resource.xml files, here, all `Resources` elements must start with a `SetDefaults`. This is to ensure that any resources that come after it do not get prefixed with some unexpected idprefix or path from another mod, which will probably break things.

In the rare case that you actually do not want to prefix resource ID's and paths with anything (such as in `res/editor/resources.xml` for example), you can leave both the idprefix and path attributes of the SetDefaults blank, like this: `<SetDefaults idprefix="" path="" />`

>Note:  at the time of writing this at least, you probably should not use any custom SetDefaults values that are not present in the vanilla version of the file, as Goo2Tool might not handle them correctly and cause your file to break.

### .wog2

Similarly to how files work, the content of your .wog2 merge file should also be a .wog2 file that with a few exceptions, mirrors the original file in structure, albeit only containing custom property values that should be added or overridden in the file.

To identify the type of merge, the root object always has to contain the property `"__type__": "jsonMerge"`.

Note that special care has to be applied when you are trying to modify a child object (`{ ... }` in JSON). The equivalent object in your merge file has to contain the property `"__propertyType__": "merge"`.

For example, if you want to patch the level "A Goo Filled Hill" to use the Autumn background and have inverted gravity, you would put this file at `merge/res/levels/C01_A_Goo_Filled_Hill.wog2`:

```JSON
{
    "__type__": "jsonMerge",
	"backgroundId":	"00dbdf7a-cc6a-4478-bca5-86a4404a4e5c",
    "gravity":	{
        "__propertyType__": "merge",
		"x":	0,
		"y":	-10
	}
}
```

Special care also has to be applied to arrays (`[ ... ]` in JSON). Instead of also creating an array in your merge file, you instead need to create a JSON object instead and give it the property `"__propertyType__": "array"`. To modify the array, you can use the following further properties:

* `"merge"`: The value of this property also has to be a JSON object. In this object, the left hand side of the properties always represents the index of an existing object (count starting from 0). The right hand side can, as usual, be a regular value or another object with `"__propertyType__"` `"merge"` or `"array"`.

    For example, to change the typeEnum of a gooball in an existing level, you could do this:
    
    ```JSONc
    ...
    "balls": {
        "__propertyType__": "array",
        "merge": {
            // ball instance at index 0 (aka the very first one in the array)
            "0": {
                // just like merging any other object, see "gravity" example above
                "__propertyType__": "merge",
                "typeEnum": 15,
            }
        }
    },
    ...
    ```

* `"append"`: This property takes an array of values are that just taken plainly (i.e. without accounting for `"__propertyType__"` etc) and appended to the array.

    For example, if you wanted to add your own custom material to `res/properties/materials.wog2`, you could something like this file at `merge/res/properties/materials.wog2`:
    
    ```JSONc
    {
        "__type__": "jsonMerge",
        "materials": {
            "__propertyType__": "array",
            // append takes an array of plain JSON values
            // notice how there is no __propertyType__ in there
            "append": [ {
                "name": "terrain_ballbuster",
                "friction": 0,
                "bounciness": 0,
                "canHost": false,
                "canStick": false,
                "stickForce": 0.000199999994947575,
                "detaching": false,
                "walkable": false,
                "destroyBalls": false,
                "despawnBalls": false,
                "destroyLiquid": false,
                "destroyLiquidProbability": 1,
                "destroyGeometry": false,
                "popBalls": true,
                "useMinimumFriction": true
            } ]
        }
    }
    ```
