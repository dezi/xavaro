instaface.createFrame = function()
{
    var ic = instaface;

    ic.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    ic.topdiv.style.overflow = "hidden";

    ic.contentdiv = WebLibSimple.createDiv(0, 0, 0, "50%", "contentdiv", ic.topdiv);
    ic.contentdiv.style.overflow = "hidden";
    ic.contentscoll = WebLibSimple.createDivHeight(0, 0, 0, null, "contentscoll", ic.contentdiv);
    ic.contentscoll.scrollVertical = true;

    WebLibSimple.setBGColor(ic.contentscoll, "#dddddddd");
    ic.contentscoll.innerHTML = "bla";
}

instaface.createFeeds = function()
{
    var ic = instaface;

    //
    // Get all platforms the user is present on.
    //

    ic.platforms = JSON.parse(WebAppSocial.getPlatforms());

    //
    // Build a list with all feeds of all platforms
    // and read in all recent feed data.
    //

    ic.feeds = [];
    ic.feedsdata = [];

    for (var inx = 0; inx < ic.platforms.length; inx++)
    {
        var platform = ic.platforms[ inx ];

        if (platform.type == "owner")
        {
            //
            // The type of this is the owner, means the
            // person which is the platform account holder.
            // Get all his configured feeds for this platform.
            //

            var ownerfeeds = JSON.parse(WebAppSocial.getUserFeeds(platform.plat));

            for (var fnz = 0; fnz < ownerfeeds.length; fnz++)
            {
                var ownerfeed = ownerfeeds[ fnz ];

                ic.feeds.push(ownerfeed);
                ic.feedsdata.push(JSON.parse(WebAppSocial.getFeed(platform.plat, ownerfeed.id)));
            }
        }
        else
        {
            //
            // Platform type is either friend or like.
            // Append the platform entry as an single feed.
            //

            ic.feeds.push(platform);
            ic.feedsdata.push(JSON.parse(WebAppSocial.getFeed(platform.plat, platform.id)));
        }
    }
}

instaface.createDebug = function()
{
    var ic = instaface;

    ic.debugdiv = WebLibSimple.createDiv(0, "50%", 0, 0, "debugdiv", ic.topdiv);
    ic.debugdiv.style.overflow = "hidden";
    ic.debugscroll = WebLibSimple.createDivHeight(0, 0, 0, null, "debugscroll", ic.debugdiv);
    ic.debugscroll.scrollVertical = true;

    ic.debugpre = WebLibSimple.createAnyAppend("pre", ic.debugscroll);

    ic.debugpre.innerHTML += "===============>platforms\n";
    ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.platforms)) + "\n";

    ic.debugpre.innerHTML += "===============>feeds\n";
    ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feeds)) + "\n";

    /*
    ic.feed = JSON.parse(WebAppSocial.getFeed(ic.platforms[ 0 ].plat, ic.platforms[ 0 ].id));

    if (ic.feed.length > 0)
    {
        var postid = ic.feed[ 0 ].id_str ? ic.feed[ 0 ].id_str : ic.feed[ 0 ].id;

        var platform = ic.platforms[ 0 ].plat;
        ic.post = JSON.parse(WebAppSocial.getPost(platform, postid));

        ic.debugpre.innerHTML += "===============>post\n";
        ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.post)) + "\n";
    }

    ic.debugpre.innerHTML += "===============>feed\n";
    ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feed)) + "\n";
    */
}

instaface.createFrame();
instaface.createFeeds();
instaface.createDebug();

