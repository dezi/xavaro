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

    containerBG : "#ffffffff",
    containerRadius : "4px",
    boxPaddingTop : "10px"
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
    leftSpan.style.width     = "50%";
    leftSpan.style.textAlign = "left";

    leftSpan.innerHTML = leftText;

    //
    // right span
    //

    var rightSpan = WebLibSimple.createAnyAppend("span", container);

    rightSpan.style.display   = "inline-block";
    rightSpan.style.width     = "50%";
    rightSpan.style.textAlign = "right";

    rightSpan.innerHTML = rightText;
}

tvguide.createInfoBox = function(description)
{
    if (! description) return;

    //
    // tvguide.description.infoBox
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

tvguide.createButton = function(name, color, eventHandler)
{
    var container = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    container.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    var button = WebLibSimple.createAnyAppend("div", container);
    button.style.height        = "50px";
    button.innerHTML           = name;
    button.style.textAlign     = "center";
    button.style.paddingTop    = "25px";
    button.style.borderRadius  = "10px";
    button.onTouchClick        = eventHandler;

    WebLibSimple.setBGColor(button, color);
}

tvguide.createButtons = function()
{
    var epg = tvguide.description.epg;
    var recordings = JSON.parse(WebAppMedia.getRecordedItems());

    for (var index in recordings)
    {
        recording = recordings[ index ];

        if ((recording.title    == epg.title &&
             recording.channel  == epg.channel) &&
            (recording.start    == epg.start ||
             recording.stop     == epg.stop))
        {
            tvguide.createButton("Play", "#4553c1", null);
            return;
        }

//        if (recording.title    == epg.title &&
//            recording.channel  == epg.channel &&
//            recording.subtitle == epg.subtitle)
//        {
//            tvguide.createButton("Play", "#4553c1", null);
//            return;
//        }
    }

    // WebAppMedia.getRecordings();
    var now  = new Date().getTime();
    var stop = new Date(epg.stop).getTime();

    if ((epg.iptv == true) && (stop > now))
    {
        // tvguide.createRecordingButton();
        tvguide.createButton("Record", "#ff7a7a", tvguide.record);
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
        tvguide.description.innerHTML = "";
    }
}

tvguide.record = function()
{
    var epg = tvguide.description.epg;

    var preload  = tvguide.constants.recordPreload  * 1000 * 60;
    var postload = tvguide.constants.recordPostload * 1000 * 60;

    var start = new Date(epg.start).getTime() - preload;
    var stop  = new Date(epg.stop ).getTime()  + postload;

    epg.date = new Date(start);
    epg.stop = new Date(stop);

    WebAppMedia.addRecording(JSON.stringify(epg));

    console.log(JSON.stringify(tvguide.description.epg));

    alert("created recording");
}

tvguide.descriptionMain = function(target, element)
{
    var epg = element.epg;

    if (! epg.title) return;

    console.log("--> tvguide.onEPGTouchClick element: " + epg.title);
    console.log("--> epg: " + JSON.stringify(epg));

    tvguide.createDescriptionSetup();

    tvguide.description.epg = epg;

    tvguide.createTitleBar(epg.title, epg.subtitle);

    var day = WebLibSimple.getNiceDay(epg.start);
    var timeSpan = WebLibSimple.getNiceTime(epg.start) + "-" + WebLibSimple.getNiceTime(epg.stop);

    var duration = Math.floor(WebLibSimple.getDuration(epg.start, epg.stop) / 1000 / 60) + "min";

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

    tvguide.createButtons();
}

tvguide.onEPGTouchClick = function(target, element)
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

    if (! tvguide.animateInfoStatus)
    {
        tvguide.descriptionMain(target, element);
        tvguide.animateInfoIn();
    }
}
