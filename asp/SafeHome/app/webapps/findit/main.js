findit.readConfigLists = function()
{
    //
    // Get locale string from Android system.
    //

    var localelists = "lists." + WebAppUtility.getLocale();

    //
    // Load default lists as initial set.
    //

    findit.configLists = JSON.parse(WebAppRequest.loadSync("lists.json"));

    //
    // Load locale lists if present.
    //

    for (var inx in findit.configLists.locales)
    {
        if (findit.configLists.locales[ inx ] == localelists)
        {
            //
            // We have found our locale list. Read it into memory.
            //

            var localedata = JSON.parse(WebAppRequest.loadSync(localelists + ".json"));

            //
            // Merge entries into base list.
            //

            for (var key in localedata) findit.configLists[ key ] = localedata[ key ];
        }
    }
}

findit.createLaunchItems = function()
{
    //
    // Shortcuts to config lists.
    //

    var gamesListKeys  = findit.configLists[ "list.index.keys"  ];
    var gamesListVals  = findit.configLists[ "list.index.vals"  ];
    var gamesListIcons = findit.configLists[ "list.index.icons" ];

    //
    // Empty list of launch items.
    //

    findit.lauchItems = [];

    //
    // Create a launch item for each different game.
    //

    for (var inx in gamesListKeys)
    {
        //
        // Derive required data.
        //

        var gameKey   = gamesListKeys [ inx ];
        var gameIcon  = gamesListIcons[ inx ];

        //
        // Translate the game key into locale language name.
        //

        var gameLabel = WebLibStrings.getTransTrans("list.index.keys", gameKey);

        //
        // Configure launch item.
        //

        var config = {};

        config.icon = gameIcon;
        config.label = gameLabel;

        //
        // Create launch item and push to list.
        //

        findit.lauchItems.push = WebLibLaunch.createLaunchItem(config);
    }
}

WebLibLaunch.createFrame();

findit.readConfigLists();
findit.createLaunchItems();
