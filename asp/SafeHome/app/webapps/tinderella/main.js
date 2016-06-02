tinderella.config =
{
    "iconsize" : 120
}

tinderella.createFrame = function()
{
    var ti = tinderella;

    ti.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setBGColor(ti.topdiv, "#dddddd");
    ti.topdiv.style.overflow = "hidden";

    ti.theight = ti.config.iconsize + 8 + 8

    ti.titlediv = WebLibSimple.createDivHeight(0, 0, 0, ti.theight, "titlediv", ti.topdiv);
    WebLibSimple.setBGColor(ti.titlediv, "#ff7755cc");
    ti.titlediv.style.overflow = "hidden";

    ti.titlescroll = WebLibSimple.createDivWidth(0, 0, null, 0, "titlescroll", ti.titlediv);
    ti.titlescroll.scrollHorizontal = true;

    ti.contentdiv = WebLibSimple.createDiv(0, 0, 0, 0, "contentdiv", ti.topdiv);
    ti.contentdiv.style.overflow = "hidden";

    ti.contentscroll = WebLibSimple.createDivHeight(0, 0, 0, 0, "contentscroll", ti.contentdiv);
    ti.contentscroll.style.bottom = null;
    ti.contentscroll.style.height = null;
    ti.contentscroll.scrollVertical = true;

    ti.titlediv.style.top = 0 + "px";
    ti.contentdiv.style.top = ti.theight + "px";
}

tinderella.onClickLike = function(target, ctarget)
{
    WebAppUtility.makeClick();

    if (! (target && target.rec)) return;

    var match = WebAppSocial.getTinderLikePass("like", target.rec[ "_id" ]);
    console.log("tinderella.onClickLike: " + match);

    WebLibSimple.detachElement(target.rec.recdiv);

    tinderella.contentscroll.style.top = "0px";
}

tinderella.onClickPass = function(target, ctarget)
{
    WebAppUtility.makeClick();

    if (! (target && target.rec)) return;

    var match = WebAppSocial.getTinderLikePass("pass", target.rec[ "_id" ]);
    console.log("tinderella.onClickPass: " + match);

    WebLibSimple.detachElement(target.rec.recdiv);

    tinderella.contentscroll.style.top = "0px";
}

tinderella.onClickMore = function(target, ctarget)
{
    WebAppUtility.makeClick();

    WebLibSimple.detachElement(target);

    tinderella.createRecs();
}

tinderella.onTitleImageClick = function(target, ctarget)
{
    WebAppUtility.makeClick();
}

tinderella.createMatch = function(match)
{
    var ti = tinderella;

    if (! (match.person && match.person.photos && match.person.photos.length)) return;

    var photo = match.person.photos[ 0 ];

    var pdiv = WebLibSimple.createDivWidHei(
                        (ti.titlewid + 4), 4,
                        ti.config.iconsize + 8, ti.config.iconsize + 8,
                        null, ti.titlescroll);

    pdiv.onTouchClick = ti.onTitleImageClick;

    var picn = WebLibSimple.createAnyAppend("img", pdiv);

    picn.style.position = "absolute";
    picn.style.top = "4px";
    picn.style.left = "4px";
    picn.style.width = ti.config.iconsize + "px";
    picn.style.height = ti.config.iconsize + "px";
    picn.src = photo.url;

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

    ti.titlewid += ti.config.iconsize + 4 + 4;
}

tinderella.createUpdates = function()
{
    var ti = tinderella;

    var lastdate = new Date().toISOString();
    lastdate = null;

    ti.updates = JSON.parse(WebAppSocial.getTinderUpdates(lastdate));

    if ((! ti.updates) && ! ti.updates.matches) return;

    ti.titlewid = 0;

    for (var inx = 0; inx < ti.updates.matches.length; inx++)
    {
        ti.createMatch(ti.updates.matches[ inx ]);
    }

    ti.titlewid += 8;

    ti.titlescroll.style.width = ti.titlewid + "px";

    var prediv = WebLibSimple.createAnyAppend("pre", ti.contentscroll);
    prediv.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(ti.updates));
}

tinderella.createRecs = function()
{
    var ti = tinderella;

    ti.recs = JSON.parse(WebAppSocial.getTinderRecommendations());

    if (! ti.recs) return;

    var outoflikes = false;

    for (var inx = 0; inx < ti.recs.length; inx++)
    {
        var rec = ti.recs[ inx ];

        var recdiv = WebLibSimple.createAnyAppend("div", ti.contentscroll);
        WebLibSimple.setBGColor(recdiv, "#ffffff");
        recdiv.style.margin = "16px";
        recdiv.style.border = "2px solid grey";
        recdiv.style.padding = "16px";
        recdiv.style.borderRadius = "20px";

        var bdt = new Date(rec.birth_date).getFullYear();
        var age = new Date().getFullYear() - bdt;

        var onl = (new Date().getTime() - new Date(rec.ping_time).getTime());
        onl = Math.floor(onl / (1000 * 60));

        var onltag = "online";

        if (onl > ( 1 * 60)) onltag = "gerade aktiv";
        if (onl > ( 4 * 60)) onltag = "vorhin aktiv";
        if (onl > (12 * 60)) onltag = "heute aktiv";
        if (onl > (24 * 60)) onltag = "gestern aktiv";
        if (onl > (48 * 60)) onltag = "vorgestern aktiv";
        if (onl > (96 * 60)) onltag = "diese woche";
        if (onl > (7 * 24 * 60)) onltag = "nicht aktiv";

        var distance = Math.floor(rec.distance_mi / 1.6);

        var namediv = WebLibSimple.createAnyAppend("center", recdiv);
        WebLibSimple.setFontSpecs(namediv, 36, "bold");

        if (rec.name == "Tinder Team")
        {
            namediv.innerHTML = rec.name;
        }
        else
        {
            namediv.innerHTML = rec.name + " (" + age + ") – " + distance + " km – " + onltag;
        }

        if (rec.teaser && rec.teaser.string)
        {
            var teaserdiv = WebLibSimple.createAnyAppend("center", recdiv);
            WebLibSimple.setFontSpecs(teaserdiv, 20, "italic");
            teaserdiv.innerHTML = rec.teaser.string;
        }

        var biodiv = WebLibSimple.createAnyAppend("center", recdiv);
        WebLibSimple.setFontSpecs(biodiv, 20, "bold");
        biodiv.innerHTML = rec.bio;

        var imgsdiv = WebLibSimple.createAnyAppend("center", recdiv);
        imgsdiv.style.marginTop = "16px";

        for (var pinx = 0; pinx < rec.photos.length; pinx++)
        {
            var photo = rec.photos[ pinx ];

            var imgdiv = WebLibSimple.createAnyAppend("div", imgsdiv);
            imgdiv.style.display = "inline-block";
            imgdiv.style.margin = "0px";
            imgdiv.style.paddingLeft = "4px";
            imgdiv.style.paddingLRight = "4px";
            imgdiv.style.width = "328px";
            imgdiv.style.height = "320px";

            var imgtag = WebLibSimple.createAnyAppend("img", imgdiv);
            imgtag.style.width = "100%";
            imgtag.style.height = "100%";
            imgtag.src = photo.url;
        }

        if (rec.name == "Tinder Team")
        {
            //
            // Out of likes today.
            //

            outoflikes = true;

            var prediv = WebLibSimple.createAnyAppend("pre", ti.contentscroll);
            prediv.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(rec));
            
            break;
        }

        var butdiv = WebLibSimple.createAnyAppend("center", recdiv);
        WebLibSimple.setFontSpecs(butdiv, 20, "bold");

        var passdiv = WebLibSimple.createAnyAppend("div", butdiv);
        WebLibSimple.setBGColor(passdiv, "#66afff");
        passdiv.style.display = "inline-block";
        passdiv.style.border = "2px solid #0a80ff";
        passdiv.style.borderRadius = "16px";
        passdiv.style.padding = "16px";
        passdiv.style.margin = "16px";
        passdiv.style.marginBottom = "0px";
        passdiv.style.width = "200px";
        passdiv.innerHTML = "Gefällt mir nicht";

        passdiv.onTouchClick = tinderella.onClickPass;
        passdiv.rec = rec;

        var likediv = WebLibSimple.createAnyAppend("div", butdiv);
        WebLibSimple.setBGColor(likediv, "#66afff");
        likediv.style.display = "inline-block";
        likediv.style.border = "2px solid #0a80ff";
        likediv.style.borderRadius = "16px";
        likediv.style.padding = "16px";
        likediv.style.margin = "16px";
        likediv.style.marginBottom = "0px";
        likediv.style.width = "200px";
        likediv.innerHTML = "Gefällt mir gut";

        likediv.onTouchClick = tinderella.onClickLike;
        likediv.rec = rec;

        rec.recdiv = recdiv;
    }

    if (! outoflikes)
    {
        var morediv = WebLibSimple.createAnyAppend("center", ti.contentscroll);
        WebLibSimple.setFontSpecs(morediv, 20, "bold");

        var morebut = WebLibSimple.createAnyAppend("div", morediv);
        WebLibSimple.setBGColor(morebut, "#66afff");
        morebut.style.display = "inline-block";
        morebut.style.border = "2px solid #0a80ff";
        morebut.style.borderRadius = "16px";
        morebut.style.padding = "16px";
        morebut.style.margin = "16px";
        morebut.style.width = "200px";
        morebut.innerHTML = "Weitere...";

        morebut.onTouchClick = tinderella.onClickMore;
    }
}

tinderella.createFrame();
tinderella.createUpdates();
//tinderella.createRecs();

