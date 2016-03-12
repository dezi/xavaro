tvguide.descriptionConstants =
{

}

tvguide.createInfoBox = function()
{
    //
    // tvguide.description.titlebar
    //

    tvguide.description.titlebar = WebLibSimple.createAnyAppend("div", tvguide.description.content);
    tvguide.description.titlebar.style.textAlign = "center";

    WebLibSimple.setBGColor(tvguide.description.titlebar, "#ffffff");
    WebLibSimple.setFontSpecs(tvguide.description.titlebar, 30, "bold", "#000000");

    //
    // tvguide.description.subtitlebar
    //

    tvguide.description.subtitlebar = WebLibSimple.createAnyAppend("div", tvguide.description.content);
    tvguide.description.subtitlebar.style.textAlign = "center";
    tvguide.description.subtitlebar.style.fontStyle = "italic";

    WebLibSimple.setBGColor(tvguide.description.subtitlebar, "#ffffff");
    WebLibSimple.setFontSpecs(tvguide.description.subtitlebar, 20, "normal", "#000000");

    //
    // tvguide.description.pic
    //

    tvguide.description.pic = WebLibSimple.createAnyAppend("img", tvguide.description.content);
    tvguide.description.pic.style.borderRadius = "10px";
    tvguide.description.pic.style.cssFloat = "left";

    WebLibSimple.setBGColor(tvguide.description.pic, "#ffffff");

    //
    // tvguide.description.infoBox
    //

    tvguide.description.infoBox = WebLibSimple.createAnyAppend("div", tvguide.description.content);

    WebLibSimple.setBGColor(tvguide.description.infoBox, "#ffffff");
    WebLibSimple.setFontSpecs(tvguide.description.infoBox, 15, "normal", "#000000");
}

tvguide.createDescriptionSetup = function()
{
    tvguide.description = {};

    //
    // tvguide.description.topdiv
    //

    tvguide.description.topdiv = WebLibSimple.createDivWidth("100%", 0, "50%", 0, "description.topdiv", tvguide.content1);
    tvguide.description.position = 100;
    tvguide.description.topdiv.style.zIndex = "3";

    WebLibSimple.setBGColor(tvguide.description.topdiv, tvguide.constants.descriptionColor);

    //
    // tvguide.description.content
    //

    tvguide.description.content = WebLibSimple.createDiv(0, 0, 0, 0, "description.content", tvguide.description.topdiv);
    tvguide.description.content.style.margin = "10px";

    WebLibSimple.setBGColor(tvguide.description.content, "#ffffff");

    //
    // Info content
    //

    tvguide.createInfoBox();
}

tvguide.animateInfoIn = function()
{
    var actpos = tvguide.description.topdiv.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));
    tvguide.description.position = actpos;

    if (actpos > 50)
    {
        actpos = actpos - 10;
        tvguide.description.topdiv.style.left = actpos + "%";

        tvguide.animateInfoRunning = true;
        tvguide.animateInfo = setTimeout(tvguide.animateInfoIn, 40);
    }
    else
    {
        tvguide.animateInfoRunning = false;
    }
}

tvguide.animateInfoOut = function()
{
    var actpos = tvguide.description.topdiv.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));
    tvguide.description.position = actpos;

    if (actpos < 100)
    {
        actpos = actpos + 10;
        tvguide.description.topdiv.style.left = actpos + "%";

        tvguide.animateInfoRunning = true;
        tvguide.animateInfo = setTimeout(tvguide.animateInfoOut, 40);
    }
    else
    {
        tvguide.animateInfoRunning = false;
    }
}

tvguide.onEPGTouchClick = function(target, element)
{
    console.log("tvguide.onEPGTouchClick: element " + element.epg.title);

    tvguide.description.titlebar.innerHTML = element.epg.title;
    tvguide.description.infoBox.innerHTML  = JSON.stringify(element.epg);

    if (element.epg.subtitle)
    {
        tvguide.description.subtitlebar.innerHTML = element.epg.subtitle;
    }

    if (element.epg.img)
    {
        tvguide.description.pic.style.width = "100%";
        tvguide.description.pic.style.height = "auto";

//        tvguide.description.pic.style.width = "20%";
//        tvguide.description.pic.style.height = "50%";

        var imgSrc = element.epg.title;

        if (element.epg.imgname)
        {
            imgSrc = element.epg.imgname;
        }

        tvguide.description.pic.src = encodeURI("http://" + WebApp.manifest.appserver +
            "/pgminfo/tv/de/" + imgSrc + ".orig.jpg");
    }
    else
    {
        tvguide.description.pic.style.width = "0%";
        tvguide.description.pic.style.height = "0%";
    }

    if (tvguide.animateInfoRunning)
    {
        console.log("=========> STOP");

        clearTimeout(tvguide.animateInfo);
        tvguide.animateInfoRunning = false;
        tvguide.animateInfoOut();
    }
    else
    {
        if (! tvguide.animateInfoRunning && tvguide.description.position <= 100)
        {
            tvguide.animateInfoIn();
        }

        if (! tvguide.animateInfoRunning && tvguide.description.position >= 50)
        {
            tvguide.animateInfoOut();
        }
    }
}
