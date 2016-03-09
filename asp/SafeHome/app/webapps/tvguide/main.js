tvguide.constants =
{
    epgDataSrcPrefix  : "http://" + WebApp.manifest.appserver + "/epgdata/",
    epgDataSrcPostfix : "/current.json.gz",

    titlebarHeight : 80,
    senderbarWidth : 80,
    senderHeight   : 64,
    timelineHeight : 60,
    hoursPix       : 360,

    titlebarColor       : "#888888",
    content1Color       : "#dedede",
    timelineColor       : "#dedede",
    timelineDivColor    : "#ffffff",
    content2Color       : "#acacac",

    senderImgDivColor   : "#ffffff",
    senderbarColor      : "#585858",

    epgdataColor        : "#dedede",
    epgScrollColor      : "#ffffff",
    epgScrollStyleColor : "#ffffff",
    epgScrollfontWeight : "bold",

//    programBGColor    : "#ffbb6b",
//    programBoder      : "1px solid orange"

    programBGColor      : "#a0a0a0",
    programBoder        : "0px solid black"
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
    // topdiv
    //

    tvguide.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);

    //
    // topdiv.titlebar
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
    tvguide.title.innerHTML       = "Title";

    WebLibSimple.setFontSpecs(tvguide.title, 34, "bold", "#ffffff");

    //
    // topdiv.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(0, tvguide.constants.titlebarHeight, 0, 0, "content1", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.content1, tvguide.constants.titlebarColor);

    //
    // topdiv.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constants.timelineHeight, "timeline", tvguide.content1);
    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, tvguide.constants.timelineColor);

    //
    // topdiv.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(0, tvguide.constants.timelineHeight, 0, 0, "content2", tvguide.content1);
    WebLibSimple.setBGColor(tvguide.content2, tvguide.constants.content2Color);

    //
    // topdiv.epgdata
    //

//    tvguide.epgbody = WebLibSimple.createDiv(tvguide.constants.senderbarWidth, 0, 0, 0, "epgdata", tvguide.content2);
    tvguide.epgbody = WebLibSimple.createDiv(0, 0, 0, 0, "epgdata", tvguide.content2);
    tvguide.epgbody.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgbody, tvguide.constants.epgdataColor);

    //
    // topdiv.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(0, 0, tvguide.constants.senderbarWidth, 0, "senderbar", tvguide.content2);
    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, tvguide.constants.senderbarColor);
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "senderbarScroll", tvguide.senderbar);
    tvguide.senderbarScroll.scrollVertical = true;
    tvguide.senderbarScroll.onTouchScroll  = tvguide.onSenderBarScroll;

    for(var index in tvguide.senderList)
    {
        var senderImgDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderImgDiv.style.position = "relative";
        senderImgDiv.style.height = tvguide.constants.senderHeight + "px";
        senderImgDiv.style.backgroundColor = tvguide.constants.senderImgDivColor;

        var paddingDiv = WebLibSimple.createAnyAppend("div", senderImgDiv);
        paddingDiv.style.position        = "absolute";

//        paddingDiv.style.padding         = "7px";
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
    var hours = 48;

    tvguide.timelineScroll = WebLibSimple.createDivWidth(0, 0, (hours * tvguide.constants.hoursPix), 0, "timelineScroll", tvguide.timeline);
    tvguide.timelineScroll.scrollHorizontal = true;
    tvguide.timelineScroll.onTouchScroll    = tvguide.onTimeLineScroll;

    for(var inx = 0; inx < hours; inx++)
    {
        var timelineDiv = WebLibSimple.createAnyAppend("div", tvguide.timelineScroll);
        timelineDiv.style.backgroundColor = tvguide.constants.timelineDivColor;
        timelineDiv.style.position = "absolute";
        timelineDiv.style.width    = tvguide.constants.hoursPix + "px";
        timelineDiv.style.left     = (tvguide.constants.hoursPix * inx) +"px";
        timelineDiv.style.top      = "0px";
        timelineDiv.style.bottom   = "0px";

        var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
        timeSpan.textContent = inx;
    }
}

tvguide.createEpgProgram = function(channel, epgdata)
{
    console.log("Create channel: " + channel + " ==> " + epgdata.length);

    var senderIndex = tvguide.senderList.indexOf(channel);
    var minPix = tvguide.constants.hoursPix / 60;
    var today  = Math.floor(WebLibSimple.getTodayDate().getTime() / 1000 / 60);

    for (var programIndex in epgdata)
    {
        var startTime = Math.floor((new Date(epgdata[ programIndex ].start).getTime() / 1000 / 60) - today);
        var stopTime  = Math.floor((new Date(epgdata[ programIndex ].stop ).getTime() / 1000 / 60) - today);
        var duration  = stopTime - startTime;

//        console.log(channel + ": " + epgdata[ programIndex ].title + " ==> duration: " + duration);

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
//        var tmp = new Date(epgdata[ programIndex ].start);
        timeSpan.textContent = epgdata[ programIndex ].title + " start: " + startTime + "min";
    }
}

tvguide.createEpgBodyScroll = function()
{
    tvguide.epgScroll = WebLibSimple.createDivWidHei(0, 0, 48 * tvguide.constants.hoursPix, tvguide.senderbarScroll.clientHeight, "epgScroll", tvguide.epgbody);
    tvguide.epgScroll.scrollHorizontal = true;
    tvguide.epgScroll.scrollVertical   = true;
    tvguide.epgScroll.onTouchScroll    = tvguide.onEPGTouchScroll;

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
    console.log("WebAppRequest.onAsyncLoad: " + src + " => " + data.epgdata.length);

    var channel = WebLibSimple.substring(decodeURI(src), tvguide.constants.epgDataSrcPrefix.length, -tvguide.constants.epgDataSrcPostfix.length);

    var today = Math.floor(WebLibSimple.getTodayDate().getTime() / 1000);
    var epgdata = [];

    for (var index in data.epgdata)
    {
        var indexDate = Math.floor(new Date(data.epgdata[ index ].start).getTime() / 1000);

        if (indexDate > today && indexDate < (today + (2 * 24 * 60 * 60)))
        {
            epgdata.push(data.epgdata[ index ]);
        }
    }

    tvguide.createEpgProgram(channel, epgdata);
}

tvguide.onTimeLineScroll = function(newX, newY)
{
    console.log("tvguide.onTimeLineScroll:" + newX + "/" + newY);
//    tvguide.epgScroll.style.left = Math.floor(newX) + "px";
    tvguide.epgScroll.style.left = newX + "px";
}

tvguide.onSenderBarScroll = function(newX, newY)
{
    console.log("tvguide.SenderBar:" + newX + "/" + newY);
//    tvguide.epgScroll.style.top = Math.floor(newY) + "px";
    tvguide.epgScroll.style.top = newY + "px";
}

tvguide.onEPGTouchScroll = function(newX, newY)
{
    console.log("tvguide.onEPGTouchScroll:" + newX + "/" + newY);

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

tvguide.createFrameSetup();
tvguide.getSenderList();

tvguide.createSenderBar();
tvguide.createTimeLine();
tvguide.createEpgBodyScroll();
