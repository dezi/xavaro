instaface.createFrame = function()
{
    var ic = instaface;

    ic.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    ic.topdiv.style.overflow = "hidden";

    ic.contentdiv = WebLibSimple.createDiv(0, 0, 0, 0, "contentdiv", ic.topdiv);
    ic.contentdiv.style.overflow = "hidden";

    ic.contentscoll = WebLibSimple.createDivHeight(0, 0, 0, null, "contentscoll", ic.contentdiv);
    WebLibSimple.setBGColor(ic.contentscoll, "#dddddddd");
    ic.contentscoll.scrollVertical = true;
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

instaface.postDivs = [];

instaface.displayPost = function(plat, post)
{
    var ic = instaface;

    var user = WebLibSocial.getPostUserid(plat, post);
    var date = WebLibSocial.getPostDate(plat, post);
    var name = WebLibSocial.getPostName(plat, post);
    var text = WebLibSocial.getPostText(plat, post);
    var imgs = WebLibSocial.getPostImgs(plat, post);

    var postdiv = WebLibSimple.createAnyAppend("div", ic.contentscoll);
    postdiv.style.position  = "relative";
    postdiv.style.margin  = "16px";
    postdiv.style.border = "1px solid black";

    var leftdif = WebLibSimple.createDivWidth(0, 0, 80, 0, "leftdif", postdiv);
    WebLibSimple.setBGColor(leftdif, "#cccccccc");
    leftdif.style.overflow = "hidden";

    var picn = WebLibSimple.createAnyAppend("img", leftdif);
    picn.style.width = "100%";
    picn.style.height = "auto";
    picn.src = WebLibSocial.getPlatformIcon(plat);

    var uicn = WebLibSimple.createAnyAppend("img", leftdif);
    uicn.style.width = "100%";
    uicn.style.height = "auto";
    uicn.src = WebLibSocial.getUserIcon(plat, user);

    var rightdif = WebLibSimple.createAnyAppend("div", postdiv);
    WebLibSimple.setBGColor(rightdif, "#aaaaaaaa");
    rightdif.style.overflow = "hidden";
    rightdif.style.marginLeft = "80px";

    var titlediv = WebLibSimple.createAnyAppend("div", rightdif);
    WebLibSimple.setBGColor(titlediv, "#88888888");
    titlediv.style.height = "80px";
    titlediv.style.margin = "16px";
    titlediv.style.padding = "8px";

    var namediv = WebLibSimple.createAnyAppend("div", titlediv);
    namediv.innerHTML = "Von" + " " + name + " – " + WebLibSimple.getNiceDate(date);

    var textdiv = WebLibSimple.createAnyAppend("div", titlediv);
    textdiv.innerHTML = text;

    var imgsdiv = WebLibSimple.createAnyAppend("div", rightdif);
    imgsdiv.style.margin = "16px";
    imgsdiv.style.overflow = "hidden";

    for (var inx = 0; inx < imgs.length; inx++)
    {
        var imgtag = WebLibSimple.createAnyAppend("img", imgsdiv);
        imgtag.src = imgs[ inx ].src ? imgs[ inx ].src : imgs[ inx ].url;
    }
}

instaface.createConts = function()
{
    for (var inx = 0; inx < 20; inx++)
    {
        instaface.retrieveBestPost();
    }
}

instaface.retrieveBestPost = function()
{
    //
    // Loop over all feeds and find the most recent
    // and suitable post.
    //

    var ic = instaface;

    var candipost = null;
    var candifeed = null;
    var candifinx = 0;
    var candipinx = 0;
    var candidate = 0;

    for (var finx = 0; finx < ic.feeds.length; finx++)
    {
        var feed = ic.feeds[ finx ];
        var data = ic.feedsdata[ finx ];

        for (var pinx = 0; pinx < data.length; pinx++)
        {
            var post = data[ pinx ];

            var postid = WebLibSocial.getPostId(feed.plat, post);
            var postdate = WebLibSocial.getPostDate(feed.plat, post);

            if (postdate > candidate)
            {
                //
                // Read in complete post and inspect.
                //

                var post = JSON.parse(WebAppSocial.getPost(feed.plat, postid));

                var suitable = WebLibSocial.getPostSuitable(feed.plat, post);

                if (suitable)
                {
                    candifinx = finx;
                    candifeed = data;
                    candipinx = pinx;
                    candipost = post;
                    candidate = postdate;

                    //
                    // Continue with next feed.
                    //

                    break;
                }

                //
                // Remove post from feed.
                //

                data.splice(pinx--, 1);
            }
        }
    }

    if (candipost)
    {
        //
        // Remove post from feed data.
        //

        candifeed.splice(candipinx, 1);

        instaface.displayPost(ic.feeds[ candifinx ].plat, candipost);
    }
}

instaface.createDebug = function()
{
    var ic = instaface;

    ic.contentdiv.style.height = "50%";

    ic.debugdiv = WebLibSimple.createDiv(0, "50%", 0, 0, "debugdiv", ic.topdiv);
    ic.debugdiv.style.overflow = "hidden";
    ic.debugscroll = WebLibSimple.createDivHeight(0, 0, 0, null, "debugscroll", ic.debugdiv);
    ic.debugscroll.scrollVertical = true;

    ic.debugpre = WebLibSimple.createAnyAppend("pre", ic.debugscroll);

    ic.debugpre.innerHTML += "===============>platforms\n";
    ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.platforms)) + "\n";

    ic.debugpre.innerHTML += "===============>feeds\n";
    ic.debugpre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feeds)) + "\n";
}

instaface.createFrame();
instaface.createFeeds();
instaface.createConts();

//instaface.createDebug();

