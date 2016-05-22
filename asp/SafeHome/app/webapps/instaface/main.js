instaface.config =
{
    "iconsize" : 120
}

instaface.animateTitle = function()
{
    var ic = instaface;

    ic.animateTimeout = null;

    if (ic.mode == "normal")
    {
        if ((ic.titlediv.offsetTop + 16) < 0)
        {
            ic.titlediv.style.top = (ic.titlediv.offsetTop + 16) + "px";
            ic.contentdiv.style.top = (ic.contentdiv.offsetTop + 16) + "px";
            ic.animateTimeout = setTimeout(instaface.animateTitle, 40)
        }
        else
        {
            ic.titlediv.style.top = "0px";
            ic.contentdiv.style.top = ic.theight + "px";
        }
    }

    if (ic.mode == "news")
    {
        if ((ic.contentdiv.offsetTop - 32) > 0)
        {
            ic.titlediv.style.top = (ic.titlediv.offsetTop - 32) + "px";
            ic.contentdiv.style.top = (ic.contentdiv.offsetTop - 32) + "px";
            ic.animateTimeout = setTimeout(instaface.animateTitle, 40)
        }
        else
        {
            ic.titlediv.style.top = -ic.theight + "px";
            ic.contentdiv.style.top = "0px";
        }
    }

    ic.adjustPostDivs();
}

instaface.updateConts = function()
{
    var ic = instaface;

    ic.updaterTimeout = null;

    //
    // Nuke last content.
    //

    ic.postDivs = [];

    ic.contentscroll.innerHTML = null;
    ic.contentscroll.style.top = "0px";

    if (! ic.retrieveBestPost(false))
    {
        //
        // Restart content.
        //

        for (var inx = 0; inx < ic.feedsdinx.length; inx++)
        {
            ic.feedsused[ inx ] = 0;
            ic.feedsdinx[ inx ] = 0;
        }

        //
        // Retrieve at least the latest post even if old.
        //

        ic.retrieveBestPost(true);
    }

    ic.adjustPostDivs();

    ic.updaterTimeout = setTimeout(instaface.updateConts, 5000);
}

instaface.createFrame = function()
{
    var ic = instaface;

    ic.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setBGColor(ic.topdiv, "#dddddd");
    ic.topdiv.style.overflow = "hidden";

    ic.mode = ic.isFullScreen() ? "normal" : "news";

    ic.theight = ic.config.iconsize + 8 + 8

    ic.titlediv = WebLibSimple.createDivHeight(0, 0, 0, ic.theight, "titlediv", ic.topdiv);
    WebLibSimple.setBGColor(ic.titlediv, "#ff7755cc");
    ic.titlediv.style.overflow = "hidden";

    ic.titlescroll = WebLibSimple.createDivWidth(0, 0, null, 0, "titlescroll", ic.titlediv);
    ic.titlescroll.scrollHorizontal = true;

    ic.contentdiv = WebLibSimple.createDiv(0, 0, 0, 0, "contentdiv", ic.topdiv);
    ic.contentdiv.style.overflow = "hidden";

    ic.contentscroll = WebLibSimple.createDivHeight(0, 0, 0, 0, "contentscroll", ic.contentdiv);

    if (ic.mode == "normal")
    {
        ic.titlediv.style.top = 0 + "px";
        ic.contentdiv.style.top = ic.theight + "px";
    }
    else
    {
        ic.titlediv.style.top = -ic.theight + "px";
        ic.contentdiv.style.top = 0 + "px";
    }

    addEventListener("resize", instaface.onWindowResize);
}

instaface.adjustMode = function()
{
    var ic = instaface;

    ic.mode = ic.isFullScreen() ? "normal" : "news";

    if (ic.mode == "normal")
    {
        if (ic.updaterTimeout)
        {
            clearTimeout(ic.updaterTimeout);
            ic.updaterTimeout = null;
        }

        if ((ic.contentdiv.offsetTop == 0) && ! ic.animateTimeout && (ic.iconsorter.length > 0))
        {
            ic.animateTimeout = setTimeout(instaface.animateTitle, 1500);
        }
    }

    if (ic.mode == "news")
    {
        if (! ic.updaterTimeout)
        {
            ic.updaterTimeout = setTimeout(instaface.updateConts, 5000);
        }

        if ((ic.titlediv.offsetTop == 0) && ! ic.animateTimeout)
        {
            //
            // Remove all posts leaving only one.
            //

            ic.contentscroll.style.top = "0px";

            while (ic.postDivs.length > 1)
            {
                var postdiv = ic.postDivs.pop();
                var padddiv = postdiv.mypadddiv;

                WebLibSimple.detachElement(padddiv);
            }

            ic.animateTimeout = setTimeout(instaface.animateTitle, 0);
        }
    }
}

instaface.getSelector = function(target)
{
    var ic = instaface;

    for (var inx = 0; inx < ic.contentselectors.length; inx++)
    {
        if (target == ic.contentselectors[ inx ].pdiv)
        {
            return ic.contentselectors[ inx ];
        }
    }

    return null;
}

instaface.onTitleImageClick = function(ctarget, target)
{
    WebAppUtility.makeClick();

    var ic = instaface;

    var selector = ic.getSelector(ctarget);
    if (selector == null) return;

    if (selector == ic.activeselector) return;

    if (ic.activeselector)
    {
        WebLibSimple.setBGColor(ic.activeselector.pdiv, "#00000000");
        ic.activeselector.activated = false;
    }

    WebLibSimple.setBGColor(selector.pdiv, "#ffff0000");
    selector.activated = true;

    ic.activeselector = selector;

    ic.markFeedsAsRead();
    ic.createConts();
}

instaface.updateNewsCounts = function()
{
    var ic = instaface;

    var total = 0;

    for (var inx = 0; inx < ic.contentselectors.length; inx++)
    {
        var selector = ic.contentselectors[ inx ];

        var styp = selector.styp;
        var skey = selector.skey;
        var pcnt = selector.pcnt;

        var newscount = (styp == "1") ? ic.platnews[ skey ] : ic.usernews[ skey ];
        if (styp != "1") total += newscount;

        pcnt.style.display = newscount ? "block" : "none";
        pcnt.innerHTML = newscount;
    }

    WebAppSocial.postTotalNewsCount(total);
}

instaface.createFeeds = function()
{
    var ic = instaface;

    ic.createFeedsTimeout = null;

    //
    // Get all platforms the user is present on.
    //

    ic.titlescroll.innerHTML = null;
    ic.contentselectors = [];

    ic.feednews = JSON.parse(WebAppSocial.getFeedNews());
    ic.platforms = JSON.parse(WebAppSocial.getPlatforms());

    ic.icontitles = {};
    ic.iconsorter = [];

    ic.usertype = {};
    ic.usernews = {};
    ic.platnews = {};

    //
    // Build a list with all feeds of all platforms
    // and read in all recent feed data.
    //

    ic.feeds = [];
    ic.feedsdata = [];
    ic.feedsnews = [];
    ic.feedsdinx = [];
    ic.feedsused = [];

    for (var inx = 0; inx < ic.platforms.length; inx++)
    {
        var platform = ic.platforms[ inx ];
        ic.platnews[ platform.plat ] = 0;

        if (platform.type == "owner")
        {
            //
            // The type of this is the owner, means the
            // person which is the platform account holder.
            // Get all his configured feeds for this platform.
            //

            /*
            if ((ic.platforms.length > 1) && (! ic.icontitles[ platform.plat ]))
            {
                var picn = WebLibSimple.createAnyAppend("img");
                picn.src = WebLibSocial.getPlatformIcon(platform.plat);

                ic.icontitles[ platform.plat ] = picn;
                ic.iconsorter.push("1|" + platform.plat);
            }
            */

            var ownerfeeds = JSON.parse(WebAppSocial.getUserFeeds(platform.plat));

            for (var fnz = 0; fnz < ownerfeeds.length; fnz++)
            {
                var ownerfeed = ownerfeeds[ fnz ];
                var ownername = ownerfeed.name.toLowerCase();

                if (! ic.icontitles[ ownername ])
                {
                    var sort = (ownerfeed.type == "owner") ? "2" : "3";

                    var picn = WebLibSimple.createAnyAppend("img");
                    picn.src = WebLibSocial.getUserIcon(platform.plat, ownerfeed.id);

                    ic.icontitles[ ownername ] = picn;
                    ic.iconsorter.push(sort + "|" + ownername);

                    ic.usertype[ ownername ] = ownerfeed.type;
                    ic.usernews[ ownername ] = 0;
                }

                var newstag = platform.plat + ".count." + ownerfeed.id;
                var newscount = ic.feednews[ newstag ] ? ic.feednews[ newstag ] : 0;

                ic.usernews[ ownername ] += newscount;
                ic.platnews[ platform.plat ] += newscount;

                ic.feeds.push(ownerfeed);
                ic.feedsdata.push(JSON.parse(WebAppSocial.getFeed(platform.plat, ownerfeed.id)));
                ic.feedsnews.push(newscount);
                ic.feedsdinx.push(0);
                ic.feedsused.push(0);
            }
        }
        else
        {
            //
            // Platform type is either friend or like.
            // Append the platform entry as an single feed.
            //

            var feedname = platform.name.toLowerCase();

            if (! ic.usertype[ feedname ])
            {
                ic.usertype[ feedname ] = platform.type;
                ic.usernews[ feedname ] = 0;
            }

            var newstag = platform.plat + ".count." + platform.id;
            var newscount = ic.feednews[ newstag ] ? ic.feednews[ newstag ] : 0;

            ic.usernews[ ownername ] += newscount;
            ic.platnews[ platform.plat ] += newscount;

            ic.feeds.push(platform);
            ic.feedsdata.push(JSON.parse(WebAppSocial.getFeed(platform.plat, platform.id)));
            ic.feedsnews.push(newscount);
            ic.feedsdinx.push(0);
            ic.feedsused.push(0);
        }
    }

    //
    // Build title list user icons.
    //

    if (ic.iconsorter.length == 0)
    {
        ic.titlediv.style.top = -ic.theight + "px";
        ic.contentdiv.style.top = 0 + "px";
    }
    else
    {
        ic.iconsorter.sort();

        var titlewid = 0;

        for (var inx = 0; inx < ic.iconsorter.length; inx++)
        {
            var stag = ic.iconsorter[ inx ];
            var styp = stag.substring(0, 1);
            var skey = stag.substring(2);
            var picn = ic.icontitles[ skey ];

            var pdiv = WebLibSimple.createDivWidHei(
                                (titlewid + 4), 4,
                                ic.config.iconsize + 8, ic.config.iconsize + 8,
                                null, ic.titlescroll);

            pdiv.onTouchClick = ic.onTitleImageClick;
            pdiv.appendChild(picn);

            picn.style.position = "absolute";
            picn.style.top = "4px";
            picn.style.left = "4px";
            picn.style.width = ic.config.iconsize + "px";
            picn.style.height = ic.config.iconsize + "px";

            var pcnt = WebLibSimple.createAnyAppend("div", pdiv);
            WebLibSimple.setFontSpecs(pcnt, 20, "bold");
            WebLibSimple.setBGColor(pcnt, "#44cc44");
            pcnt.style.display = "none";
            pcnt.style.position = "absolute";
            pcnt.style.textAlign = "center";
            pcnt.style.bottom = "12px";
            pcnt.style.right = "12px";
            pcnt.style.height = "24px";
            pcnt.style.minWidth = "24px";
            pcnt.style.padding = "6px";
            pcnt.style.borderRadius = "20px";

            var selector = {};

            selector.styp = styp;
            selector.stag = stag;
            selector.skey = skey;
            selector.pdiv = pdiv;
            selector.picn = picn;
            selector.pcnt = pcnt;
            selector.activated = false;

            ic.contentselectors.push(selector);

            titlewid += ic.config.iconsize + 4 + 4;
        }

        titlewid += 8;

        ic.titlescroll.style.width = titlewid + "px";

        ic.updateNewsCounts();

        ic.createFeedsTimeout = setTimeout(ic.createFeeds, 600 * 1000);
    }
}

instaface.adjustPostDivs = function(startinx)
{
    if (! startinx) startinx = 0;

    var ic = instaface;

    if (ic.postDivs)
    {
        for (var inx = startinx; inx < ic.postDivs.length; inx++)
        {
            var postdiv = ic.postDivs[ inx ];
            var padddiv = postdiv.mypadddiv;
            var sepadiv = postdiv.mysepadiv;
            var headdiv = postdiv.myheaddiv;
            var imgsdiv = postdiv.myimgsdiv;

            if (ic.mode == "normal")
            {
                ic.contentscroll.style.bottom = null;
                ic.contentscroll.style.height = null;
                ic.contentscroll.scrollVertical = true;

                padddiv.style.position = "relative";
                padddiv.style.margin = "48px";
                padddiv.style.padding = "24px";
                padddiv.style.border = "2px solid #cccccc";
                padddiv.style.borderRadius = "16px";

                padddiv.style.left = null;
                padddiv.style.top = null;
                padddiv.style.right = null;
                padddiv.style.bottom = null;

                postdiv.style.position = "relative";
                postdiv.style.margin = "0px";

                WebLibSimple.setBGColor(padddiv, "#ffffff");
                WebLibSimple.setBGColor(sepadiv, "#444444");
            }

            if (ic.mode == "news")
            {
                ic.contentscroll.style.bottom = "0px";
                ic.contentscroll.style.height = null;
                ic.contentscroll.scrollVertical = false;

                padddiv.style.position = "absolute";
                padddiv.style.margin = "0px";
                padddiv.style.padding = "0px";
                padddiv.style.border = "0px solid grey";
                padddiv.style.borderRadius = "0px";

                padddiv.style.left = "0px";
                padddiv.style.top = "0px";
                padddiv.style.right = "0px";
                padddiv.style.bottom = "0px";

                postdiv.style.position = "absolute";
                postdiv.style.margin = "8px";

                WebLibSimple.setBGColor(padddiv, "#ffffff");
                WebLibSimple.setBGColor(sepadiv, "#dddddd");
            }

            if (postdiv.myimgtag && postdiv.myimage)
            {
                var imgtag = postdiv.myimgtag;
                var image  = postdiv.myimage;

                if (ic.mode == "news")
                {
                    //
                    // We have a predeterminated height which we must use.
                    //

                    var useheight = (postdiv.clientHeight - headdiv.clientHeight);
                    imgsdiv.style.height = useheight + "px";
                }
                else
                {
                    //
                    // We can just fit in the image.
                    //

                    imgsdiv.style.height = Math.floor(image.height * imgsdiv.clientWidth / image.width) + "px";
                }

                var wid = imgsdiv.clientWidth;
                var hei = imgsdiv.clientHeight;

                var scalex = wid / image.width;
                var scaley = hei / image.height;

                var imgwid;
                var imghei;

                if (scalex > scaley)
                {
                    imgwid = Math.floor(image.width  * scalex);
                    imghei = Math.floor(image.height * scalex);
                }
                else
                {
                    imgwid = Math.floor(image.width  * scaley);
                    imghei = Math.floor(image.height * scaley);
                }

                imgtag.width  = imgwid;
                imgtag.height = imghei;

                imgtag.style.left   = Math.floor((imgsdiv.clientWidth  - imgwid) / 2) + "px";
                imgtag.style.top    = Math.floor((imgsdiv.clientHeight - imghei) / 2) + "px";
                imgtag.style.right  = Math.floor((imgwid - imgsdiv.clientWidth ) / 2) + "px";
                imgtag.style.bottom = Math.floor((imghei - imgsdiv.clientHeight) / 2) + "px";
            }
        }
    }
}

instaface.displayPostCompact = function(plat, post)
{
    var ic = instaface;

    var pfid = WebLibSocial.getPostUserid(plat, post);
    var pfna = WebLibSocial.getPostUsername(plat, post);
    var date = WebLibSocial.getPostDate(plat, post);
    var name = WebLibSocial.getPostName(plat, post);
    var text = WebLibSocial.getPostText(plat, post);
    var imgs = WebLibSocial.getPostImgs(plat, post);
    var type = (name != null) ? ic.usertype[ name.toLowerCase() ] : null;

    var padddiv = WebLibSimple.createDiv(0, 0, 0, 0, null, ic.contentscroll);
    var postdiv = WebLibSimple.createDiv(0, 0, 0, 0, null, padddiv);
    var headdiv = WebLibSimple.createAnyAppend("div", postdiv);

    var uicn = WebLibSimple.createAnyAppend("img", headdiv);
    uicn.style.width = "auto";
    uicn.style.height = "80px";
    uicn.style.float = "left";
    uicn.style.marginRight = "16px";
    uicn.src = WebLibSocial.getUserIcon(plat, pfid, pfna);

    var infodiv = WebLibSimple.createAnyAppend("div", headdiv);

    var spacediv = WebLibSimple.createAnyAppend("div", infodiv);

    var namediv = WebLibSimple.createAnyAppend("div", infodiv);
    WebLibSimple.setFontSpecs(namediv, 24, "bold");
    namediv.style.overflow = "hidden";
    namediv.style.whiteSpace = "nowrap";
    namediv.style.textOverflow = "ellipsis";
    namediv.innerHTML = name;

    var timediv = WebLibSimple.createAnyAppend("div", infodiv);
    WebLibSimple.setFontSpecs(timediv, 18, "normal");
    timediv.innerHTML = WebLibSimple.getNiceDate(date);

    var platdiv = WebLibSimple.createAnyAppend("div", infodiv);
    WebLibSimple.setFontSpecs(platdiv, 18, "bold", "#888888");

    platdiv.innerHTML = WebLibSocial.getPlatformName(plat)
                      + " – "
                      + WebLibStrings.getTransTrans("social.type.keys", type);

    spacediv.style.height = (80 - infodiv.scrollHeight) + "px";

    var sepadiv = WebLibSimple.createAnyAppend("div", headdiv);
    sepadiv.style.clear = "both";
    sepadiv.style.marginTop = "4px";
    sepadiv.style.marginBottom = "2px";
    sepadiv.style.height = "2px";

    var textdiv = WebLibSimple.createAnyAppend("div", headdiv);
    WebLibSimple.setFontSpecs(textdiv, 20, "normal");
    textdiv.style.overflow = "hidden";
    textdiv.style.paddingTop = "4px";
    textdiv.style.paddingBottom = "4px";
    textdiv.innerHTML = text;
    WebLibSimple.ellipsizeTextBox(textdiv, 80);

    var imgsdiv = WebLibSimple.createAnyAppend("div", postdiv);
    imgsdiv.style.position = "relative";
    imgsdiv.style.overflow = "hidden";

    if (imgs && imgs.length)
    {
        var image = imgs[ 0 ];

        var imgtag = WebLibSimple.createAnyAppend("img", imgsdiv);
        imgtag.src = image.src ? image.src : image.url;
        imgtag.style.position = "absolute";

        postdiv.myimgtag  = imgtag;
        postdiv.myimage   = image;
    }

    postdiv.mypadddiv = padddiv;
    postdiv.mysepadiv = sepadiv;
    postdiv.myheaddiv = headdiv;
    postdiv.myimgsdiv = imgsdiv;

    instaface.postDivs.push(postdiv);
}

instaface.isFullScreen = function()
{
    return (WebAppUtility.getDeviceWidth()  == instaface.topdiv.clientWidth) ||
           (WebAppUtility.getDeviceHeight() == instaface.topdiv.clientHeight);
}

instaface.onWindowResize = function()
{
    instaface.adjustMode();
    instaface.adjustPostDivs();

    console.log("onWindowResize:" + instaface.mode);
}

instaface.createConts = function()
{
    var ic = instaface;

    //
    // Nuke current content if any.
    //

    ic.contentscroll.innerHTML = "";
    ic.contentscroll.style.top = "0px";

    //
    // Reset feed indices.
    //

    for (var inx = 0; inx < ic.feedsdinx.length; inx++)
    {
        ic.feedsused[ inx ] = 0;
        ic.feedsdinx[ inx ] = 0;
    }

    //
    // Collect one post upfront.
    //

    ic.postDivs = [];

    if (ic.deferredTimeout)
    {
        clearTimeout(ic.deferredTimeout);
        ic.deferredTimeout = null;
    }

    if (ic.retrieveBestPost(true))
    {
        ic.adjustMode();
        ic.adjustPostDivs()

        if (ic.mode == "normal")
        {
            //
            // Retrieve more posts in deferred mode.
            //

            ic.deferredTodo = 19;
            ic.deferredTimeout = setTimeout(instaface.retrieveDeferred, 100);
        }
    }
}

instaface.retrieveDeferred = function()
{
    var ic = instaface;

    ic.deferredTimeout = null;

    if (ic.deferredTodo > 0)
    {
        ic.deferredTodo--;

        if (ic.retrieveBestPost(true))
        {
            ic.adjustPostDivs(ic.postDivs.length - 1);

            if (ic.deferredTodo > 0)
            {
                ic.deferredTimeout = setTimeout(instaface.retrieveDeferred, 100);
            }
        }
    }
}

instaface.markFeedsAsRead = function()
{
    var ic = instaface;

    if (! ic.activeselector) return;

    var stag = ic.activeselector.stag;
    var mode = stag.substring(0, 1);
    var valu = stag.substring(2);

    for (var finx = 0; finx < ic.feeds.length; finx++)
    {
        var feed = ic.feeds[ finx ];

        if ((mode == "1") && (feed.plat != valu)) continue;
        if ((mode == "2") && (feed.name.toLowerCase() != valu.toLowerCase())) continue;
        if ((mode == "3") && (feed.name.toLowerCase() != valu.toLowerCase())) continue;

        var ownername = feed.name.toLowerCase();
        var newstag = feed.plat + ".count." + feed.id;

        //
        // Reset feed news count.
        //

        var newscount = ic.feednews[ newstag ] ? ic.feednews[ newstag ] : 0;
        if (newscount == 0) continue;

        ic.feednews[ newstag ] = 0;

        ic.usernews[ ownername ] -= newscount;
        ic.platnews[ feed.plat ] -= newscount;

        WebAppSocial.resetFeedNews(feed.plat, feed.id);
     }

    ic.updateNewsCounts();
}

instaface.retrieveBestPost = function(anypost)
{
    //
    // Loop over all feeds and find the most recent
    // and suitable post.
    //

    var ic = instaface;

    var candipost = null;
    var candifeed = null;
    var candidinx = 0;
    var candifinx = 0;
    var candidate = 0;

    var stag = ic.activeselector ? ic.activeselector.stag : false;

    for (var finx = 0; finx < ic.feeds.length; finx++)
    {
        var feed = ic.feeds[ finx ];

        if ((ic.feedsused[ finx ] >= ic.feedsnews[ finx ]) && (ic.mode == "news") && ! anypost)
        {
            //
            // All new posts have been displayed.
            //

            continue;
        }

        var data = ic.feedsdata[ finx ];
        var dinx = ic.feedsdinx[ finx ];

        if (stag)
        {
            var mode = stag.substring(0, 1);
            var valu = stag.substring(2);

            if (mode == "1")
            {
                //
                // Platform selector does not match.
                //

               if (feed.plat != valu) continue;
            }

            if ((mode == "2") || (mode == "3"))
            {
                //
                // User name selector does not match.
                //

                if (feed.name.toLowerCase() != valu.toLowerCase()) continue;
            }
        }

        while (dinx < data.length)
        {
            var post = data[ dinx++ ];

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
                    candidinx = dinx;
                    candifeed = data;
                    candipost = post;
                    candidate = postdate;

                    //
                    // Continue with next feed.
                    //

                    break;
                }
                else
                {
                    //
                    // Remove post from feed for better performance.
                    //

                    data.splice(--dinx, 1);
                }
            }
        }
    }

    if (candipost)
    {
        //
        // Move winning feed ahead.
        //

        ic.feedsdinx[ candifinx ] = candidinx;
        ic.feedsused[ candifinx ]++;

        instaface.displayPostCompact(ic.feeds[ candifinx ].plat, candipost);
    }

    return candipost;
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

