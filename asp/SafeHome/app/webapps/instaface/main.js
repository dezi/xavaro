instaface.config =
{
    "iconsize" : 120
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

    ic.retrieveBestPost();
    ic.adjustImageSizes()

    ic.updaterTimeout = setTimeout(instaface.updateConts, 5000);
}

instaface.createFrame = function()
{
    var ic = instaface;

    ic.mode = WebAppSocial.getMode();

    ic.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    ic.topdiv.style.overflow = "hidden";

    ic.theight = ic.config.iconsize + 8 + 8

    ic.titlediv = WebLibSimple.createDivHeight(0, 0, 0, ic.theight, "titlediv", ic.topdiv);
    WebLibSimple.setBGColor(ic.titlediv, "#ff7755cc");
    ic.titlediv.style.overflow = "hidden";

    ic.titlescroll = WebLibSimple.createDivWidth(0, 0, null, 0, "titlescroll", ic.titlediv);
    ic.titlescroll.scrollHorizontal = true;

    ic.contentdiv = WebLibSimple.createDiv(0, 0, 0, 0, "contentdiv", ic.topdiv);
    ic.contentdiv.style.overflow = "hidden";

    ic.contentscroll = WebLibSimple.createDivHeight(0, 0, 0, 0, "contentscroll", ic.contentdiv);

    addEventListener("resize", instaface.onWindowResize);
    instaface.onWindowResize();
}

instaface.adjustMode = function()
{
    var ic = instaface;

    if (ic.mode == "normal")
    {
        if (ic.updaterTimeout)
        {
            clearTimeout(ic.updaterTimeout);
            ic.updaterTimeout = null;
        }

        ic.titlediv.style.display = "block";
        ic.contentdiv.style.top = ic.theight + "px";
    }

    if (ic.mode == "news")
    {
        if (! ic.updaterTimeout)
        {
            ic.updaterTimeout = setTimeout(instaface.updateConts, 5000);
        }

        ic.titlediv.style.display = "none";
        ic.contentdiv.style.top = "0px";
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

    if (selector == ic.activeselector)
    {
        if (selector.activated)
        {
            WebLibSimple.setBGColor(selector.pdiv, "#00000000");
            selector.activated = false;
            ic.activeselector = null;
        }
    }
    else
    {
        if (ic.activeselector)
        {
            WebLibSimple.setBGColor(ic.activeselector.pdiv, "#00000000");
            ic.activeselector.activated = false;
        }

        WebLibSimple.setBGColor(selector.pdiv, "#ffff0000");
        selector.activated = true;

        ic.activeselector = selector;
    }

    ic.createConts();
}

instaface.createFeeds = function()
{
    var ic = instaface;

    //
    // Get all platforms the user is present on.
    //

    ic.platforms = JSON.parse(WebAppSocial.getPlatforms());

    ic.icontitles = {};
    ic.iconsorter = [];
    ic.usertype = {};

    //
    // Build a list with all feeds of all platforms
    // and read in all recent feed data.
    //

    ic.feeds = [];
    ic.feedsdata = [];
    ic.feedsdinx = [];

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

            if ((ic.platforms.length > 1) && (! ic.icontitles[ platform.plat ]))
            {
                var picn = WebLibSimple.createAnyAppend("img");
                picn.src = WebLibSocial.getPlatformIcon(platform.plat);

                ic.icontitles[ platform.plat ] = picn;
                ic.iconsorter.push("1|" + platform.plat);
            }

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
                }

                ic.feeds.push(ownerfeed);
                ic.feedsdata.push(JSON.parse(WebAppSocial.getFeed(platform.plat, ownerfeed.id)));
                ic.feedsdinx.push(0);
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
            ic.feedsdinx.push(0);
        }
    }

    if (ic.iconsorter.length == 0)
    {
        ic.titlediv.style.display = "none";
        ic.contentdiv.style.top = "0px";
    }
    else
    {
        ic.iconsorter.sort();

        ic.contentselectors = [];

        var titlewid = 0;

        for (var inx = 0; inx < ic.iconsorter.length; inx++)
        {
            var stag = ic.iconsorter[ inx ];
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

            var selector = {};

            selector.stag = stag;
            selector.pdiv = pdiv;
            selector.picn = picn;
            selector.activated = false;

            ic.contentselectors.push(selector);

            titlewid += ic.config.iconsize + 4 + 4;
        }

        titlewid += 8;

        ic.titlescroll.style.width = titlewid + "px";
    }
}

instaface.adjustImageSizes = function()
{
    var ic = instaface;

    if (ic.postDivs)
    {
        for (var inx = 0; inx < ic.postDivs.length; inx++)
        {
            var postdiv = ic.postDivs[ inx ];

            if (postdiv.myimgtag && postdiv.myimage)
            {
                var headdiv = postdiv.myheaddiv;
                var imgsdiv = postdiv.myimgsdiv;
                var imgtag  = postdiv.myimgtag;
                var image   = postdiv.myimage;

                var useheight = (postdiv.clientHeight - headdiv.clientHeight);

                if (useheight > 0)
                {
                    //
                    // We have a predeterninated height which we must use.
                    // To make nice images, we zoom into it a little bit.
                    //

                    imgsdiv.style.height = useheight + "px";
                }
                else
                {
                    if (Math.abs(image.width - imgsdiv.clientWidth) < 100)
                    {
                        imgsdiv.style.height = Math.floor(image.height * imgsdiv.clientWidth / image.width) + "px";
                    }
                    else
                    {
                        imgsdiv.style.height = image.height + "px";
                    }
                }

                var zoom = 20;
                var wid = zoom + imgsdiv.offsetWidth;
                var hei = zoom + imgsdiv.offsetHeight;

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

                console.log("===============> useheight:" + useheight);

                imgtag.width  = imgwid;
                imgtag.height = imghei;

                imgtag.style.left   = Math.floor((imgsdiv.offsetWidth  - imgwid) / 2) + "px";
                imgtag.style.top    = Math.floor((imgsdiv.offsetHeight - imghei) / 2) + "px";
                imgtag.style.right  = Math.floor((imgwid - imgsdiv.offsetWidth ) / 2) + "px";
                imgtag.style.bottom = Math.floor((imghei - imgsdiv.offsetHeight) / 2) + "px";
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

    var postdiv;

    if (ic.mode == "normal")
    {
        var paddiv = WebLibSimple.createAnyAppend("div", ic.contentscroll);
        paddiv.style.margin = "48px";
        paddiv.style.padding = "16px";
        paddiv.style.border = "4px solid grey";
        paddiv.style.backgroundColor = "#dddddd";

        postdiv = WebLibSimple.createAnyAppend("div", paddiv);
        postdiv.style.position = "relative";
    }

    if (ic.mode == "news")
    {
        postdiv = WebLibSimple.createDiv(0, 0, 0, 0, null, ic.contentscroll);
        postdiv.style.margin = "8px";
    }

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
    WebLibSimple.setBGColor(sepadiv, "#dddddd");
    sepadiv.style.clear = "both";
    sepadiv.style.height = "2px";

    var textdiv = WebLibSimple.createAnyAppend("div", headdiv);
    WebLibSimple.setFontSpecs(textdiv, 20, "normal");
    textdiv.style.overflow = "hidden";
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
    var ic = instaface;

    ic.mode = ic.isFullScreen() ? "normal" : "news";

    console.log("onWindowResize:" + ic.mode);

    ic.adjustMode();

    ic.adjustImageSizes();
}

instaface.displayPostNormal = function(plat, post)
{
    var ic = instaface;

    var pfid = WebLibSocial.getPostUserid(plat, post);
    var pfna = WebLibSocial.getPostUsername(plat, post);
    var date = WebLibSocial.getPostDate(plat, post);
    var name = WebLibSocial.getPostName(plat, post);
    var text = WebLibSocial.getPostText(plat, post);
    var imgs = WebLibSocial.getPostImgs(plat, post);

    var postdiv = WebLibSimple.createAnyAppend("div", ic.contentscroll);
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
    uicn.src = WebLibSocial.getUserIcon(plat, pfid, pfna);

    var rightdif = WebLibSimple.createAnyAppend("div", postdiv);
    WebLibSimple.setBGColor(rightdif, "#aaaaaaaa");
    rightdif.style.position = "relative";
    rightdif.style.overflow = "hidden";
    rightdif.style.marginLeft = "80px";

    var titlediv = WebLibSimple.createAnyAppend("div", rightdif);
    WebLibSimple.setBGColor(titlediv, "#88888888");
    titlediv.style.margin = "16px";
    titlediv.style.padding = "8px";

    var namediv = WebLibSimple.createAnyAppend("div", titlediv);
    namediv.innerHTML = "Von" + " " + name + " – " + WebLibSimple.getNiceDate(date);

    var textdiv = WebLibSimple.createAnyAppend("div", titlediv);
    textdiv.innerHTML = text;

    var imgsdiv = WebLibSimple.createAnyAppend("div", rightdif);
    imgsdiv.style.position = "relative";
    imgsdiv.style.margin = "16px";
    imgsdiv.style.overflow = "hidden";

    for (var inx = 0; inx < imgs.length; inx++)
    {
        var imgtag = WebLibSimple.createAnyAppend("img", imgsdiv);
        imgtag.src = imgs[ inx ].src ? imgs[ inx ].src : imgs[ inx ].url;

        if ((imgs[ inx ].width > imgsdiv.clientWidth) ||
            ((imgsdiv.clientWidth - imgs[ inx ].width) < 100))
        {
            var width = imgsdiv.clientWidth;
            var height = Math.floor(imgs[ inx ].height * width / imgs[ inx ].width);

            imgtag.style.width  = width  + "px";
            imgtag.style.height = height + "px";
        }
        else
        {
            imgtag.style.width  = imgs[ inx ].width  + "px";
            imgtag.style.height = imgs[ inx ].height + "px";
        }
    }
}

instaface.createConts = function()
{
    var ic = instaface;

    //
    // Nuke current content if any.
    //

    ic.contentscroll.innerHTML = "";
    ic.contentscroll.style.top = "0px";

    if (ic.mode == "normal")
    {
         ic.contentscroll.style.bottom = null;
         ic.contentscroll.style.height = null;
         ic.contentscroll.scrollVertical = true;
    }

    if (ic.mode == "news")
    {
        ic.contentscroll.style.bottom = "0px";
        ic.contentscroll.style.height = null;
        ic.contentscroll.scrollVertical = false;
    }

    //
    // Reset feed indices.
    //

    for (var inx = 0; inx < ic.feedsdinx.length; inx++)
    {
        ic.feedsdinx[ inx ] = 0;
    }

    //
    // Collect some posts.
    //

    ic.postDivs = [];

    var max = (ic.mode == "normal") ? 20 : 1;

    for (var inx = 0; inx < max; inx++)
    {
        ic.retrieveBestPost();
    }

    ic.adjustImageSizes()
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
    var candidinx = 0;
    var candifinx = 0;
    var candidate = 0;

    var stag = ic.activeselector ? ic.activeselector.stag : false;

    for (var finx = 0; finx < ic.feeds.length; finx++)
    {
        var feed = ic.feeds[ finx ];
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

        instaface.displayPostCompact(ic.feeds[ candifinx ].plat, candipost);
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

