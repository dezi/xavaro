tvguide.constants =
{
    title : "Title",
    loadHours : 48,
    timelineInterval : 15,

    today : Math.floor(WebLibSimple.getTodayDate().getTime() / 1000 / 60),

    epgDataSrcPrefix  : "http://" + WebApp.manifest.appserver + "/epgdata/",
    epgDataSrcPostfix : "/current.json.gz",

    titlebarHeight : 80,
    senderbarWidth : 80,
    senderHeight   : 64,
    timelineHeight : 30,
    hoursPix       : 360,

    titlebarColor       : "#888888",
    content1Color       : "#dedede",
    content2Color       : "#acacac",

    senderImgDivColor   : "#ffffff",
    senderbarColor      : "#585858",

    epgdataColor        : "#dedede",
    epgScrollColor      : "#ffffff",
    epgScrollStyleColor : "#ffffff",
    epgScrollfontWeight : "bold",

    timelineColor            : "#dedede",
    timelineDivColor         : "#ffffff",
    timelineScrollStyleColor : "#000000",
    timelineScrollfontWeight : "bold",

    programBGColor : "#a0a0a0",
    programBoder   : "0px solid black"
}

tvguide.getSenderList = function()
{
//    var obj = JSON.parse(WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));
//
//    tvguide.senderList = [];
//
//    for(var index in obj)
//    {
//        tvguide.senderList.push("tv/de/" + obj[ index ].name);
//    }

    tvguide.senderList =
    [
        "tv/de/Das Erste",
        "tv/de/ZDF",
        "tv/de/Sat. 1 Deutschland",
        "tv/de/RTL Deutschland",
        "tv/de/Pro Sieben Deutschland",
        "tv/de/Kabel Eins Deutschland",
        "tv/de/Vox Deutschland",
        "tv/de/RTL 2 Deutschland",
        "tv/de/Tele 5",
        "tv/de/Sixx Deutschland",
        "tv/de/Eins Plus",
        "tv/de/3sat",
        "tv/de/NDR Fernsehen Hamburg",
        "tv/de/WDR Fernsehen Köln",
        "tv/de/MDR Fernsehen Sachsen-Anhalt",
        "tv/de/Bayerisches Fernsehen Nord",
        "tv/de/ZDF info",
        "tv/de/ZDF Kultur",
        "tv/de/ZDF neo",
        "tv/de/Einsfestival"
    ];
}

tvguide.createFrameSetup = function()
{
    //
    // tvguide.topdiv
    //

    tvguide.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);

    //
    // tvguide.titlebar
    //

    tvguide.titlebar = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constants.titlebarHeight, "titlebar", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.titlebar, tvguide.constants.titlebarColor);

    tvguide.title = WebLibSimple.createDiv(
        tvguide.constants.titlebarHeight,
        tvguide.constants.titlebarHeight / 4,
        tvguide.constants.titlebarHeight,
        0,
        "title",
        tvguide.titlebar
    );

    tvguide.title.style.textAlign = "center";
    tvguide.title.innerHTML       = tvguide.constants.title;

    WebLibSimple.setFontSpecs(tvguide.title, 34, "bold", "#ffffff");

    //
    // tvguide.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(0, tvguide.constants.titlebarHeight, 0, 0, "content1", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.content1, tvguide.constants.titlebarColor);

    //
    // tvguide.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constants.timelineHeight, "timeline", tvguide.content1);
    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, tvguide.constants.timelineColor);

    //
    // tvguide.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(0, tvguide.constants.timelineHeight, 0, 0, "content2", tvguide.content1);
    WebLibSimple.setBGColor(tvguide.content2, tvguide.constants.content2Color);

    //
    // tvguide.epgbody
    //

    tvguide.epgbody = WebLibSimple.createDiv(0, 0, 0, 0, "epgdata", tvguide.content2);
    tvguide.epgbody.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgbody, tvguide.constants.epgdataColor);

    //
    // tvguide.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(0, 0, tvguide.constants.senderbarWidth, 0, "senderbar", tvguide.content2);
    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, tvguide.constants.senderbarColor);

    //
    // tvguide.info
    //

    tvguide.info = WebLibSimple.createDivWidth("50%", 0, "50%", 0, "info", tvguide.content1);
    WebLibSimple.setBGColor(tvguide.info, "#33ffff00");
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        null,
        "senderbarScroll",
        tvguide.senderbar
    );

    tvguide.senderbarScroll.scrollVertical = true;
    tvguide.senderbarScroll.onTouchScroll  = tvguide.onSenderBarScroll;
    tvguide.senderbarScroll.style.zIndex = "2";

    for(var index in tvguide.senderList)
    {
        var senderImgDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderImgDiv.style.position = "relative";
        senderImgDiv.style.height = tvguide.constants.senderHeight + "px";
        senderImgDiv.style.backgroundColor = tvguide.constants.senderImgDivColor;

        var paddingDiv = WebLibSimple.createAnyAppend("div", senderImgDiv);
        paddingDiv.style.position        = "absolute";

        paddingDiv.style.paddingLeft     = "7px";
        paddingDiv.style.paddingTop      = "8px";
        paddingDiv.style.paddingRight    = "7px";
        paddingDiv.style.paddingBottom   = "7px";

        paddingDiv.style.left            = "0px";
        paddingDiv.style.top             = "0px";
        paddingDiv.style.right           = "0px";
        paddingDiv.style.bottom          = "0px";

        var image = WebLibSimple.createAnyAppend("img", paddingDiv);
        image.style.height = "100%";
        image.style.width  = "100%";

        image.src = encodeURI("http://" +
            WebApp.manifest.appserver + "/channels/" +
            tvguide.senderList[ index ] + ".png"
        );
    }
}

tvguide.createTimeLine = function()
{
    tvguide.timelineScroll = WebLibSimple.createDivWidth(
        0,
        0,
        (tvguide.constants.loadHours * tvguide.constants.hoursPix),
        0,
        "timelineScroll",
        tvguide.timeline
    );

    tvguide.timelineScroll.scrollHorizontal = true;
    tvguide.timelineScroll.onTouchScroll    = tvguide.onTimeLineScroll;
    tvguide.timelineScroll.style.color      = tvguide.constants.timelineScrollStyleColor;
    tvguide.timelineScroll.style.fontWeight = tvguide.constants.timelineScrollfontWeight;

    for(var min = 0; min < (tvguide.constants.loadHours * 60); min += tvguide.constants.timelineInterval)
    {
        var timelineDiv = WebLibSimple.createDivWidth(
            (tvguide.constants.hoursPix / 60 * min),
            0,
            tvguide.constants.hoursPix / 60 * tvguide.constants.timelineInterval,
            0,
            null,
            tvguide.timelineScroll
        );

        WebLibSimple.setBGColor(timelineDiv, tvguide.constants.timelineDivColor);


        if (min % (tvguide.constants.timelineInterval * 2))
        {
            var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
            timeSpan.style.border = "1px dotted #e00073";

        }
        else
        {
            var time = tvguide.constants.today + min;
            var date = new Date(time * 1000 * 60);

            var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
            timeSpan.textContent = WebLibSimple.padNum(date.getHours(), 2) + ":" +
                                   WebLibSimple.padNum(date.getMinutes(), 2);
        }
    }
}

tvguide.createEpgProgram = function(channel, epgdata)
{
    console.log("Create channel: " + channel + " ==> " + epgdata.length);

    var senderIndex = tvguide.senderList.indexOf(channel);
    var minPix = tvguide.constants.hoursPix / 60;

    for (var programIndex in epgdata)
    {
        var startTime = Math.floor((new Date(epgdata[ programIndex ].start).getTime() / 1000 / 60) - tvguide.constants.today);
        var stopTime  = Math.floor((new Date(epgdata[ programIndex ].stop ).getTime() / 1000 / 60) - tvguide.constants.today);
        var duration  = stopTime - startTime;

        var programDiv = WebLibSimple.createDivWidHei(
                        startTime * minPix,
                        tvguide.constants.senderHeight * senderIndex,
                        duration * minPix,
                        tvguide.constants.senderHeight,
                        null, tvguide.epgScroll);

        programDiv.style.position = "absolute";

        var paddingDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, programDiv);
        paddingDiv.style.marginLeft   = "4px";
        paddingDiv.style.marginTop    = "2px";
        paddingDiv.style.marginRight  = "0px";
        paddingDiv.style.marginBottom = "2px";

        paddingDiv.style.padding      = "8px";
        paddingDiv.style.borderRadius = "8px";

        paddingDiv.style.border          = tvguide.constants.programBoder;
        paddingDiv.style.backgroundColor = tvguide.constants.programBGColor;

        var timeSpan = WebLibSimple.createAnyAppend("span", paddingDiv);
        timeSpan.textContent = epgdata[ programIndex ].title;
    }
}

tvguide.animateInfoOut = function()
{
    var actpos = tvguide.info.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));

    if (actpos < 100)
    {
        actpos = actpos + 1;
        tvguide.info.style.left = actpos + "%";

        tvguide.animateInfo = setTimeout(tvguide.animateInfoOut, 100);
    }
}

tvguide.onEPGTouchClick = function(target, element)
{
    console.log("tvguide.onEPGTouchClick:" + element.innerHTML);
    tvguide.animateInfoOut();
}

tvguide.createEpgBodyScroll = function()
{
    tvguide.epgScroll = WebLibSimple.createDivWidHei(
        0,
        0,
        tvguide.constants.loadHours * tvguide.constants.hoursPix,
        tvguide.senderbarScroll.clientHeight,
        "epgScroll",
        tvguide.epgbody
    );

    tvguide.epgScroll.scrollHorizontal = true;
    tvguide.epgScroll.scrollVertical   = true;
    tvguide.epgScroll.onTouchScroll    = tvguide.onEPGTouchScroll;
    tvguide.epgScroll.onTouchClick     = tvguide.onEPGTouchClick;

    tvguide.epgScroll.style.color      = tvguide.constants.epgScrollStyleColor;
    tvguide.epgScroll.style.fontWeight = tvguide.constants.epgScrollfontWeight;

    WebLibSimple.setBGColor(tvguide.epgScroll, tvguide.constants.epgScrollColor);

    for (var sender in tvguide.senderList)
    {
        var epgurl = encodeURI(tvguide.constants.epgDataSrcPrefix + tvguide.senderList[ sender ]  + tvguide.constants.epgDataSrcPostfix);

        console.log("createEpgBodyScroll:" + epgurl);

        WebAppRequest.loadAsyncJSON(epgurl);
    }
}

WebAppRequest.onLoadAsyncJSON = function(src, data)
{
    var channel = WebLibSimple.substring(decodeURI(src), tvguide.constants.epgDataSrcPrefix.length, -tvguide.constants.epgDataSrcPostfix.length);

    var epgdata = [];

    for (var index in data.epgdata)
    {
        var indexDate = Math.floor(new Date(data.epgdata[ index ].start).getTime() / 1000 / 60);

        if (indexDate > tvguide.constants.today && indexDate < (tvguide.constants.today + (tvguide.constants.loadHours * 60)))
        {
            epgdata.push(data.epgdata[ index ]);
        }
    }

    tvguide.createEpgProgram(channel, epgdata);
}

tvguide.onTimeLineScroll = function(newX, newY)
{
//    tvguide.epgScroll.style.left = Math.floor(newX) + "px";
    tvguide.epgScroll.style.left = newX + "px";
}

tvguide.onSenderBarScroll = function(newX, newY)
{
//    tvguide.epgScroll.style.top = Math.floor(newY) + "px";
    tvguide.epgScroll.style.top = newY + "px";
}

tvguide.onEPGTouchScroll = function(newX, newY)
{
    if (newX !== null)
    {
//        tvguide.timelineScroll.style.left = Math.floor(newX) + "px";
        tvguide.timelineScroll.style.left = newX + "px";
    }

    if (newY !== null)
    {
//        tvguide.senderbarScroll.style.top = Math.floor(newY) + "px";
        tvguide.senderbarScroll.style.top = newY + "px";
    }
}

tvguide.createLeftTimeDimmer = function()
{
    var now         = Math.floor( new Date().getTime() / 1000 / 60);
    var minPix      = tvguide.constants.hoursPix / 60;
    var nowPosition = minPix * (tvguide.constants.today - now);
    var padding     = tvguide.constants.timelineInterval * 4 * minPix;

    tvguide.epgScroll.style.left      = nowPosition + padding + "px";
    tvguide.timelineScroll.style.left = nowPosition + padding + "px";

    tvguide.LeftTimeDimmer = WebLibSimple.createDivWidth(
        0,
        0,
        nowPosition * -1,
        0,
        "LeftTimeDimmer",
        tvguide.epgScroll
    );

    WebLibSimple.setBGColor(tvguide.LeftTimeDimmer, "#33ff0000");
    tvguide.LeftTimeDimmer.style.zIndex = "1";
}


tvguide.createFrameSetup();
tvguide.getSenderList();

tvguide.createSenderBar();
tvguide.createTimeLine();
tvguide.createEpgBodyScroll();
tvguide.createLeftTimeDimmer();
