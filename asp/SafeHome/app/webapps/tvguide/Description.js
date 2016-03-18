tvguide.descriptionConstants =
{
    titlebarFontSize  : 30,
    titlebarFontColor : "#000000",
    titlebarBGColor   : "#ffffff",
    titlebarTextAlign : "center",

    subtitlebarFontSize   : 20,
    subtitlebarFontColor  : "#000000",
    subtitlebarBGColor    : "#ffffff",
    subtitlebarTextAlign  : "center",
    subtitlebarFontStylen : "italic",

    containerBG     : "#ffffffff",
    containerRadius : "4px",
    boxPaddingTop   : "10px",

    plannedButtonTilte   : "remove planned recording",
    plannedButtonColor   : "#4553c1",
    playButtonTitle      : "play",
    playButtonColor      : "#58d533",
    recordingButtonTitle : "record",
    recordingButtonColor : "#ff7a7a",
}

tvguide.createTitleBar = function(title, subtitle)
{
    //
    // titlebar
    //

    var titlebar = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    titlebar.style.textAlign = tvguide.descriptionConstants.titlebarTextAlign;

    WebLibSimple.setBGColor(titlebar, tvguide.descriptionConstants.titlebarBGColor);

    WebLibSimple.setFontSpecs(titlebar, tvguide.descriptionConstants.titlebarFontSize,
        "bold", tvguide.descriptionConstants.titlebarFontColor);

    titlebar.innerHTML = title;

    if (subtitle)
    {
        //
        // subtitlebar
        //

        var subtitlebar = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
        subtitlebar.style.textAlign = tvguide.descriptionConstants.subtitlebarTextAlign;
        subtitlebar.style.fontStyle = tvguide.descriptionConstants.subtitlebarFontStylen;

        subtitlebar.innerHTML = subtitle;

        WebLibSimple.setBGColor(subtitlebar, tvguide.descriptionConstants.subtitlebarBGColor);
        WebLibSimple.setFontSpecs(subtitlebar, 20, "normal", tvguide.descriptionConstants.subtitlebarFontColor);
    }
}

tvguide.createPic = function(src, landscape)
{
    //
    // pic container
    //

    var PicContainer = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    PicContainer.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;
    PicContainer.style.position   = "relative";

    //
    // padd container
    //

    var PicPaddContainer = WebLibSimple.createAnyAppend("div", PicContainer);
    PicPaddContainer.style.paddingBottom = "4px";

    //
    // pic
    //

    var pic = WebLibSimple.createAnyAppend("img", PicPaddContainer);
    pic.style.width = "100%";
    pic.style.height = "auto";
    pic.style.borderRadius = "10px";
    pic.src = src;

    var partentWidth = tvguide.description.content.clientWidth;

    if (landscape)
    {
        PicPaddContainer.style.paddingRight = "0px";
    }
    else
    {
        PicPaddContainer.style.paddingRight  = "8px";

        PicContainer.style.float = "left";
        PicContainer.style.width = "40%";
    }
}

tvguide.createSpliteDiv = function(leftText, rightText, landscapePic)
{
    //
    // DurationBox
    //

    var box = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    box.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    if (! landscapePic)
    {
        box.style.marginLeft = "40%";
    }

    //
    // container div
    //

    var container = WebLibSimple.createAnyAppend("div", box);

    container.style.position = "relative";
    container.style.borderRadius = tvguide.descriptionConstants.containerRadius;

    WebLibSimple.setFontSpecs(container, 20, "normal", "#000000");
    WebLibSimple.setBGColor(container, tvguide.descriptionConstants.containerBG);

    //
    // left span
    //

    var leftSpan = WebLibSimple.createAnyAppend("span", container);

    leftSpan.style.display   = "inline-block";
    leftSpan.style.width     = "30%";
    leftSpan.style.textAlign = "left";

    leftSpan.innerHTML = leftText;

    //
    // right span
    //

    var rightSpan = WebLibSimple.createAnyAppend("span", container);

    rightSpan.style.display   = "inline-block";
    rightSpan.style.width     = "70%";
    rightSpan.style.textAlign = "right";

    rightSpan.innerHTML = rightText;
}

tvguide.createInfoBox = function(description)
{
    if (! description) return;

    //
    // infoBox
    //

    var infoBox = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    infoBox.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    //
    // container
    //

    var container = WebLibSimple.createAnyAppend("div", infoBox);
    container.style.borderRadius = tvguide.descriptionConstants.containerRadius;

    container.innerHTML = description;

    WebLibSimple.setBGColor(container, tvguide.descriptionConstants.containerBG);
    WebLibSimple.setFontSpecs(container, 20, "normal", "#000000");
}

tvguide.nukeWiki = function()
{
    // tvguide.description = null;

    tvguide.wikiFrame = null;
}

tvguide.openWiki = function()
{
    var wikifilm = tvguide.description.epg.wikifilm;
    console.log("--> wiki:" + wikifilm);

    tvguide.wikiFrame = WebLibSimple.createDiv(0, 0, 0, 0, "dimemrDiv", tvguide.topdiv);

    var dimmerDiv = tvguide.wikiFrame;
    dimmerDiv.style.zIndex = "100";
    WebLibSimple.setBGColor(dimmerDiv, "#66000000");

    //
    // wikiFrame
    //

    var border = 80;
    var wikiFrame = WebLibSimple.createDiv(border, border, border, border, "wikiDiv", dimmerDiv);

    WebLibSimple.setBGColor(wikiFrame, "#000000");

    //
    // iframe
    //

    var iframe = WebLibSimple.createAnyWidHei("iframe", 0, 0, "100%", "100%", "iframe", wikiFrame);
    iframe.style.border = "0px solid black";

    iframe.src = "https://de.wikipedia.org/wiki/" + wikifilm;
}

tvguide.createWiki = function(wikifilm)
{
    if (! wikifilm) return;

    tvguide.createButton("Wikipedia", "#d1d1d1", tvguide.openWiki);
}

tvguide.createDescriptionSetup = function()
{
    tvguide.description = {};

    //
    // tvguide.description.topdiv
    //

    tvguide.description.topdiv = WebLibSimple.createDivWidth("100%", 0, "50%", 0, "description.topdiv", tvguide.content1);

    tvguide.descriptionPosition = 100;

    tvguide.description.topdiv.style.zIndex = "3";

    WebLibSimple.setBGColor(tvguide.description.topdiv, tvguide.constants.descriptionColor);

    //
    // tvguide.description.content
    //

    tvguide.description.content = WebLibSimple.createDiv(0, 0, 0, 0, "description.content", tvguide.description.topdiv);
    tvguide.description.content.style.margin = "10px";

    WebLibSimple.setBGColor(tvguide.description.content, "#ffffff");

    //
    // Scroll
    //

    tvguide.descriptionScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "descriptionScroll", tvguide.description.content);
    tvguide.descriptionScroll.scrollVertical = true;
}

tvguide.addRecord = function()
{
    var epg = tvguide.description.epg;

    var preload  = tvguide.constants.recordPreload  * 1000 * 60;
    var postload = tvguide.constants.recordPostload * 1000 * 60;

    var start = new Date(epg.start).getTime()  - preload;
    var stop  = new Date(epg.stop ).getTime()  + postload;

    epg.date     = new Date(start);
    epg.datestop = new Date(stop);

    WebAppMedia.addRecording(JSON.stringify(epg));

    tvguide.createButtons();

    alert("record: " + epg.title);
}

tvguide.play = function()
{
    WebAppMedia.openPlayer(tvguide.playFile);
}

tvguide.removeRecording = function()
{
    alert("remove: " + tvguide.removeRec.title);
    WebAppMedia.removeRecording(JSON.stringify(tvguide.removeRec));

    tvguide.createButtons();
}

tvguide.createButton = function(name, color, eventHandler)
{
    // tvguide.description

    tvguide.description.buttonContainer = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);

    var container = tvguide.description.buttonContainer;
    container.style.marginTop = tvguide.descriptionConstants.boxPaddingTop;

    var button = WebLibSimple.createAnyAppend("div", container);;
    button.style.height        = "50px";
    button.style.textAlign     = "center";
    button.style.paddingTop    = "25px";
    button.style.borderRadius  = "10px";

    button.innerHTML           = name;
    button.onTouchClick        = eventHandler;
    WebLibSimple.setBGColor(button, color);
}

tvguide.createRecButton = function(epg)
{
    var now  = new Date().getTime();
    var stop = new Date(epg.stop).getTime();

    if ((epg.iptv == true) && (stop > now))
    {
        tvguide.createButton(tvguide.descriptionConstants.recordingButtonTitle,
            tvguide.descriptionConstants.recordingButtonColor,
            tvguide.addRecord);
    }
}

tvguide.createPlayButton = function(epg)
{
    var recordings = JSON.parse(WebAppMedia.getRecordedItems());

    for (var index in recordings)
    {
        recording = recordings[ index ];

        // if (epg.subtitle &&
        //     epg.title    == recording.title &&
        //     epg.channel  == recording.channel &&
        //     epg.subtitle == recording.subtitle)
        // {
        //     tvguide.playFile = recording.mediafile;
        //
        //     tvguide.createButton(tvguide.descriptionConstants.playButtonTitle,
        //         tvguide.descriptionConstants.playButtonColor,
        //         tvguide.play);
        //
        //     return true;
        // }

        if (recording.title    == epg.title &&
            recording.channel  == epg.channel &&
            recording.start    == epg.start &&
            recording.stop     == epg.stop)
        {
            tvguide.playFile = recording.mediafile;

            tvguide.createButton(tvguide.descriptionConstants.playButtonTitle,
                tvguide.descriptionConstants.playButtonColor,
                tvguide.play);

            return true;
        }
    }

    return false;
}

tvguide.createPlannedButton = function(epg)
{
    var planned = JSON.parse(WebAppMedia.getRecordings());

    for (var index in planned)
    {
        rec = planned[ index ];

        if (rec.title    == epg.title &&
            rec.channel  == epg.channel &&
            rec.start    == epg.start)
        {
            tvguide.removeRec = rec;

            tvguide.createButton(tvguide.descriptionConstants.plannedButtonTilte,
                tvguide.descriptionConstants.plannedButtonColor,
                tvguide.removeRecording);

            return true;
        }
    }

    return false;
}

tvguide.createButtons = function()
{
    // Rec || Play || Planned/Remove

    var epg = tvguide.description.epg;

    if (! epg.iptv) return;

    var nukeContainer = tvguide.description.buttonContainer;
    if (nukeContainer) nukeContainer.innerHTML = "";

    var playButton = tvguide.createPlayButton(epg);

    if (! playButton)
    {
        var plannedButton = tvguide.createPlannedButton(epg);

        if (! plannedButton) tvguide.createRecButton(epg);
    }
}

tvguide.animateInfoIn = function()
{
    var actpos = tvguide.description.topdiv.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));
    tvguide.descriptionPosition = actpos;

    if (actpos > 50)
    {
        actpos = actpos - 10;
        tvguide.description.topdiv.style.left = actpos + "%";

        tvguide.animateInfoStatus = "RunningIn";

        tvguide.animateInfo = setTimeout(tvguide.animateInfoIn, 40);
    }
    else
    {
        tvguide.animateInfoStatus = "Finish";
    }
}

tvguide.animateInfoOut = function()
{
    var actpos = tvguide.description.topdiv.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));
    tvguide.descriptionPosition = actpos;

    if (actpos < 100)
    {
        actpos = actpos + 10;
        tvguide.description.topdiv.style.left = actpos + "%";

        tvguide.animateInfoStatus = "RunningOut";
        tvguide.animateInfo = setTimeout(tvguide.animateInfoOut, 40);
    }
    else
    {
        tvguide.animateInfoStatus = null;
        tvguide.description = null;
    }
}

tvguide.descriptionMain = function(target, epg)
{
    // console.log("--> tvguide.onEPGTouchClick element: " + epg.title);
    // console.log("--> epg: " + JSON.stringify(epg));

    tvguide.createDescriptionSetup();

    tvguide.description.epg = epg;

    tvguide.createTitleBar(epg.title, epg.subtitle);

    var day       = WebLibSimple.getNiceDay(epg.start);
    var timeSpan  = WebLibSimple.getNiceTime(epg.start) + "-" + WebLibSimple.getNiceTime(epg.stop);
    var duration  = Math.floor(WebLibSimple.getDuration(epg.start, epg.stop) / 1000 / 60) + "min";
    var landscape = true;

    if (epg.imgsize)
    {
        var size = epg.imgsize.split("x");

        var imgWidth  = parseInt(size[0]);
        var imgHeight = parseInt(size[1]);

        var partentWidth = tvguide.description.topdiv.clientWidth;

        if (imgWidth < imgHeight || imgWidth < partentWidth / 2)
        {
            landscape = false;
        }

        var imgSrc = epg.title;

        if (epg.imgname) imgSrc = epg.imgname;

        var src = "http://" + WebApp.manifest.appserver +
            "/pgminfo/tv/de/" + encodeURIComponent(imgSrc) + ".orig.jpg";

        tvguide.createPic(src, landscape);
    }

    tvguide.createSpliteDiv(day + ":", timeSpan, landscape);
    tvguide.createSpliteDiv("Duration:", duration, landscape);
    tvguide.createSpliteDiv("Channel:", epg.channel, landscape);

    tvguide.createInfoBox(epg.description);

    tvguide.createWiki(epg.wikifilm);

    tvguide.createButtons();
}

tvguide.checkInfoStatus = function()
{
    if (tvguide.animateInfoStatus == "Finish")
    {
        tvguide.animateInfoOut();

        return;
    }

    if (tvguide.animateInfoStatus == "RunningIn")
    {
        clearTimeout(tvguide.animateInfo);

        tvguide.animateInfoOut();
    }
}

tvguide.onEPGTouchClick = function(target, element)
{
    WebAppUtility.makeClick();

    tvguide.checkInfoStatus();

    if (! tvguide.animateInfoStatus)
    {
        var epg = element.epg;

        if (! epg) return;

        if (epg.isbd)
        {
            epg = DeMoronize.cleanEpg(epg);
        }

        tvguide.descriptionMain(target, epg);
        tvguide.animateInfoIn();
    }
}
