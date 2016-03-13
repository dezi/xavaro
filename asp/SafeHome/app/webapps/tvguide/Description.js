tvguide.descriptionConstants =
{
    containerBG : "#ffffff",
    containerRadius : "4px",
    boxPaddingTop : "10px"
}

tvguide.createTitleBar = function()
{
    //
    // tvguide.description.titlebar
    //

    tvguide.description.titlebar = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.titlebar.style.textAlign = "center";

    WebLibSimple.setBGColor(tvguide.description.titlebar, "#ffffff");
    WebLibSimple.setFontSpecs(tvguide.description.titlebar, 30, "bold", "#000000");

    //
    // tvguide.description.subtitlebar
    //

    tvguide.description.subtitlebar = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.subtitlebar.style.textAlign = "center";
    tvguide.description.subtitlebar.style.fontStyle = "italic";

    WebLibSimple.setBGColor(tvguide.description.subtitlebar, "#ffffff");
    WebLibSimple.setFontSpecs(tvguide.description.subtitlebar, 20, "normal", "#000000");
}

tvguide.createPic = function()
{
    //
    // pic container
    //

    tvguide.description.PicContainer = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.PicContainer.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;


    //
    // tvguide.description.pic
    //

    tvguide.description.pic = WebLibSimple.createAnyAppend("img", tvguide.description.PicContainer);
    tvguide.description.pic.style.borderRadius = "10px";
}

tvguide.createDurationBox = function()
{
    //
    // tvguide.description.DurationBox
    //

    tvguide.description.DurationBox = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.DurationBox.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    //
    // container div
    //

    tvguide.description.DurationBoxContainer = WebLibSimple.createAnyAppend("div", tvguide.description.DurationBox);

    var container = tvguide.description.DurationBoxContainer;

    container.style.position = "relative";
    container.style.borderRadius = tvguide.descriptionConstants.containerRadius;

    WebLibSimple.setBGColor(container, tvguide.descriptionConstants.containerBG);
    WebLibSimple.setFontSpecs(container, 20, "normal", "#000000");

    //
    // tvguide.description.DurationBoxTitle
    // todo: Locale
    //

    tvguide.description.DurationBoxTitle = WebLibSimple.createAnyAppend("span", container);

    var titleBox = tvguide.description.DurationBoxTitle;

    titleBox.style.display   = "inline-block";
    titleBox.style.width     = "50%";
    titleBox.style.textAlign = "left";

    titleBox.innerHTML = "Duration:";

    //
    // tvguide.description.DurationBoxTime
    // todo: Locale --> min
    //

    tvguide.description.DurationBoxTime = WebLibSimple.createAnyAppend("span", container);

    var timeBox = tvguide.description.DurationBoxTime;

    timeBox.style.display   = "inline-block";
    timeBox.style.width     = "50%";
    timeBox.style.textAlign = "right";
}

tvguide.createTimeBox = function()
{
    //
    // tvguide.description.timeBox
    //

    tvguide.description.timeBox = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.timeBox.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    //
    // container div
    //

    tvguide.description.timeBoxContainer = WebLibSimple.createAnyAppend("div", tvguide.description.timeBox);

    var container = tvguide.description.timeBoxContainer;

    container.style.position = "relative";
    container.style.borderRadius = tvguide.descriptionConstants.containerRadius;

    WebLibSimple.setBGColor(container, tvguide.descriptionConstants.containerBG);
    WebLibSimple.setFontSpecs(container, 20, "normal", "#000000");

    //
    // tvguide.description.timeBoxDay
    //

    tvguide.description.timeBoxDay = WebLibSimple.createAnyAppend("span", container);

    var timeBoxDay = tvguide.description.timeBoxDay;

    timeBoxDay.style.display = "inline-block";
    timeBoxDay.style.width = "50%";
    timeBoxDay.style.textAlign = "left";

    //
    // tvguide.description.timeBoxTime
    //

    tvguide.description.timeBoxTime = WebLibSimple.createAnyAppend("span", container);

    var timeBoxTime = tvguide.description.timeBoxTime;

    timeBoxTime.style.display = "inline-block";
    timeBoxTime.style.width = "50%";
    timeBoxTime.style.textAlign = "right";
}

tvguide.createInfoBox = function()
{
    //
    // tvguide.description.infoBox
    //

    tvguide.description.infoBox = WebLibSimple.createAnyAppend("div", tvguide.descriptionScroll);
    tvguide.description.infoBox.style.paddingTop = tvguide.descriptionConstants.boxPaddingTop;

    //
    // tvguide.description.infoBoxContainer
    //

    tvguide.description.infoBoxContainer = WebLibSimple.createAnyAppend("div", tvguide.description.infoBox);
    tvguide.description.infoBoxContainer.style.borderRadius = tvguide.descriptionConstants.containerRadius;

    WebLibSimple.setBGColor(tvguide.description.infoBoxContainer, tvguide.descriptionConstants.containerBG);
    WebLibSimple.setFontSpecs(tvguide.description.infoBoxContainer, 20, "normal", "#000000");
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
    // Scroll
    //

    tvguide.descriptionScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "descriptionScroll", tvguide.description.content);
    tvguide.descriptionScroll.scrollVertical = true;

    //
    // content setup
    //

    tvguide.createTitleBar();
    tvguide.createPic();
    tvguide.createTimeBox();
    tvguide.createDurationBox();
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
//    tvguide.description.infoBox.innerHTML  = JSON.stringify(element.epg);
    tvguide.description.infoBoxContainer.innerHTML = element.epg.description;

    tvguide.description.timeBoxDay.innerHTML  = WebLibSimple.getNiceDay(element.epg.start) + ":";
    tvguide.description.timeBoxTime.innerHTML = WebLibSimple.getNiceTime(element.epg.start) + " - "
                                              + WebLibSimple.getNiceTime(element.epg.stop);

    tvguide.description.DurationBoxTime.innerHTML = Math.floor(WebLibSimple.getDuration(element.epg.start, element.epg.stop) / 1000 / 60)
        + "min";

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
