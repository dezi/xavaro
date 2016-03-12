tvguide.descriptionConstants = {}

tvguide.createTimeBox = function()
{
    //
    // tvguide.description.timeBox
    //

    tvguide.description.timeBox = WebLibSimple.createAnyAppend("div", tvguide.description.content);
    tvguide.description.timeBox.style.position = "relative";

    WebLibSimple.setBGColor(tvguide.description.timeBox, "#00ff00");
    WebLibSimple.setFontSpecs(tvguide.description.timeBox, 20, "normal", "#000000");

    //
    // tvguide.description.timeBoxDay
    //

    tvguide.description.timeBoxDay = WebLibSimple.createAnyAppend("span", tvguide.description.timeBox);
    tvguide.description.timeBoxDay.style.display = "inline-block";
    tvguide.description.timeBoxDay.style.width = "50%";
    tvguide.description.timeBoxDay.style.textAlign = "left";
    WebLibSimple.setBGColor(tvguide.description.timeBoxDay, "#ff00ff");

    //
    // tvguide.description.timeBoxTime
    //

    tvguide.description.timeBoxTime = WebLibSimple.createAnyAppend("span", tvguide.description.timeBox);
    tvguide.description.timeBoxTime.style.display = "inline-block";
    tvguide.description.timeBoxTime.style.width = "50%";
    tvguide.description.timeBoxTime.style.textAlign = "right";
    WebLibSimple.setBGColor(tvguide.description.timeBoxTime, "#ffff00");
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

//    WebLibSimple.setBGColor(tvguide.description.pic, "#00ff00");

    //
    // time boxes
    //

    tvguide.createTimeBox();

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
    tvguide.description.infoBox.innerHTML  = "Duration:" + Math.floor(WebLibSimple.getDuration(element.epg.start, element.epg.stop) / 1000 / 60)
        + " ---> " + JSON.stringify(element.epg);

    tvguide.description.timeBoxDay.innerHTML  = WebLibSimple.getNiceDay(element.epg.start) + ":";
    tvguide.description.timeBoxTime.innerHTML = WebLibSimple.getNiceTime(element.epg.start) + " - "
                                              + WebLibSimple.getNiceTime(element.epg.stop);

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
//        tvguide.description.pic.style.cssFloat = "left";

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
