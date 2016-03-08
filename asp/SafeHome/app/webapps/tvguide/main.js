tvguide.constants =
{
    titlebarHeight : 80,
    senderbarWidth : 80,
    senderHeight   : 64,
    timelineHeight : 60,
    hoursPix       : 360
}

tvguide.getSenderList = function()
{
//    var obj = JSON.parse(extras.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));
//
//    tvguide.senderList = [];
//
//    for(var index in obj)
//    {
//        tvguide.senderList.push("tv/de/" + obj[ index ].name);
//    }
//
//    console.log(tvguide.getSenderList);

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

tvguide.onOrientationChange = function()
{
    console.log(window.orientation);
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
    WebLibSimple.setBGColor(tvguide.titlebar, "#8080ff");

    //
    // topdiv.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(0, tvguide.constants.titlebarHeight, 0, 0, "content1", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.content1, "#ff8000");

    //
    // topdiv.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constants.timelineHeight, "timeline", tvguide.content1);
    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, "#ff00ff");

    //
    // topdiv.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(0, tvguide.constants.timelineHeight, 0, 0, "content2", tvguide.content1);
    WebLibSimple.setBGColor(tvguide.content2, "#ffff00");

    //
    // topdiv.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(0, 0, tvguide.constants.senderbarWidth, 0, "senderbar", tvguide.content2);
    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, "#000000");

    //
    // topdiv.epgdata
    //

    tvguide.epgbody = WebLibSimple.createDiv(tvguide.constants.senderbarWidth, 0, 0, 0, "epgdata", tvguide.content2);
    tvguide.epgbody.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgbody, "#ffffff");
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "senderbarScroll", tvguide.senderbar);
    tvguide.senderbarScroll.scrollVertical = true;

    for(var index in tvguide.senderList)
    {
        var senderDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderDiv.style.position = "relative";
        senderDiv.style.height = tvguide.constants.senderHeight + "px";
        senderDiv.style.backgroundColor = "#00ff00";

        var paddingDiv = WebLibSimple.createAnyAppend("div", senderDiv);
        paddingDiv.style.backgroundColor = "#ed1a79";
        paddingDiv.style.position        = "absolute";
        paddingDiv.style.padding         = "7px";
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

    for(var inx = 0; inx < hours; inx++)
    {
        var timeDiv = WebLibSimple.createAnyAppend("div", tvguide.timelineScroll);
        timeDiv.style.backgroundColor = "#6ff9ec";
        timeDiv.style.position = "absolute";
        timeDiv.style.width    = tvguide.constants.hoursPix + "px";
        timeDiv.style.left     = (tvguide.constants.hoursPix * inx) +"px";
        timeDiv.style.top      = "0px";
        timeDiv.style.bottom   = "0px";

        var timeSpan = WebLibSimple.createAnyAppend("span", timeDiv);
        timeSpan.textContent = inx;
    }

//    console.log("===============> tvguide.timelineScroll Width=" + tvguide.timelineScroll.clientWidth);
}

tvguide.createEpgProgram = function(channel, epgdata)
{
    var senderIndex = tvguide.senderList.indexOf(channel);

    console.log("Create channel: " + channel + " ==> " + epgdata.length);
//    console.log("epgdata.lenght: " + epgdata.length);

    var minPix = tvguide.constants.hoursPix / 60;
    var today  = Math.floor(WebLibSimple.getTodayDate().getTime() / 1000 / 60);

    for (var programIndex in epgdata)
    {
        var startTime = Math.floor((new Date(epgdata[ programIndex ].start).getTime() / 1000 / 60) - today);
        var stopTime  = Math.floor((new Date(epgdata[ programIndex ].stop ).getTime() / 1000 / 60) - today);
        var duration  = stopTime - startTime;

        console.log(channel + ": " + epgdata[ programIndex ].title + " ==> duration: " + duration);

        var programDiv = WebLibSimple.createDivWidHei(
                        startTime * minPix,
                        tvguide.constants.senderHeight * senderIndex,
                        duration * minPix,
                        tvguide.constants.senderHeight,
                        null, tvguide.epgScroll);

        programDiv.style.position = "absolute";
        //programDiv.style.backgroundColor = "#ffff00";

        var paddingDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, programDiv);
        paddingDiv.style.marginLeft   = "4px";
        paddingDiv.style.marginTop    = "2px";
        paddingDiv.style.marginRight  = "0px";
        paddingDiv.style.marginBottom = "2px";

        paddingDiv.style.padding      = "8px";
        paddingDiv.style.borderRadius = "8px";

        paddingDiv.style.border = "2px solid black";
        paddingDiv.style.backgroundColor = "#45d207";

        var timeSpan = WebLibSimple.createAnyAppend("span", paddingDiv);
        timeSpan.textContent = epgdata[ programIndex ].title;
    }
}

tvguide.createEpgBodyScroll = function()
{
    tvguide.epgScroll = WebLibSimple.createDivWidHei(0, 0, 48 * tvguide.constants.hoursPix, tvguide.senderbarScroll.clientHeight, "epgScroll", tvguide.epgbody);
    tvguide.epgScroll.scrollHorizontal = true;
    tvguide.epgScroll.scrollVertical   = true;

    WebLibSimple.setBGColor(tvguide.epgScroll, "#585858");


    for (var sender in tvguide.senderList)
    {
        var epgurl = encodeURI("http://epg.xavaro.de/epgdata/" + tvguide.senderList[ sender ]  + "/current.json.gz");

        console.log("createEpgBodyScroll:" + epgurl);

        WebAppRequest.loadAsyncJSON(epgurl);
    }
}

WebAppRequest.onLoadAsyncJSON = function(src, data)
{
    console.log("WebAppRequest.onAsyncLoad: " + src + " => " + data.epgdata.length);

    var prefix = "http://epg.xavaro.de/epgdata/";
    var postfix = "/current.json.gz";
    var channel = WebLibSimple.substring(decodeURI(src), prefix.length, -postfix.length);

    var today = Math.floor(WebLibSimple.getTodayDate().getTime() / 1000);
    var epgdata = [];

    for (var index in data.epgdata)
    {
        var indexDate = Math.floor(new Date(data.epgdata[ index ].start).getTime() / 1000);

        if (indexDate > today && indexDate < (today + (24 * 60 * 60)))
        {
            epgdata.push(data.epgdata[ index ]);
        }
    }

    tvguide.createEpgProgram(channel, epgdata);
}

tvguide.createFrameSetup();
tvguide.getSenderList();

tvguide.createSenderBar();
tvguide.createTimeLine();
tvguide.createEpgBodyScroll();
