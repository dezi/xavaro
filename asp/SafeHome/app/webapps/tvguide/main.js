tvguide.constanst =
{
    titlebarHeight : 80,
    senderbarWidth : 80,
    senderHeight   : 64,
    timelineHeight : 60,
    hoursPix       : 360
}

tvguide.getSenderList = function()
{
    // http://epg.xavaro.de/channels/tv/de/ZDF.json
    // http://epg.xavaro.de/channels/tv/de/ZDF.png
    // <img src="http://epg.xavaro.de/channels/tv/de/ZDF.png"/>
    //
    // image = document.createElement("img");
    // image.src = "http://epg.xavaro.de/channels/tv/de/ZDF.png";
    //
    //

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
        "tv/de/ZDF",
        "tv/de/Folx",
        "tv/de/N24",
        "tv/de/N-TV",
        "tv/de/NDR Fernsehen Hamburg",
        "tv/de/Phoenix",
        "tv/de/TV Allgäu",
        "tv/de/Sport 1",
        "tv/de/Tele 5",
        "tv/de/WDR Studio Dortmund",
        "tv/de/Sky Select 9",
        "tv/de/Sky Nostalgie",
        "tv/de/RTL Passion",
        "tv/de/Romance TV Deutschland",
        "tv/de/QVC Deutschland",
        "tv/de/Pearl TV",
        "tv/de/NDR Fernsehen Hamburg",
        "tv/de/Nickelodeon Deutschland HD",
        "tv/de/MTV Germany HD",
        "tv/de/MDR Fernsehen Thüringen HD",
        "tv/de/Kinowelt TV",
        "tv/de/Juwelo",
        "tv/de/Das Erste"
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

    tvguide.titlebar = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constanst.titlebarHeight, "titlebar", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.titlebar, "#8080ff");

    //
    // topdiv.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(0, tvguide.constanst.titlebarHeight, 0, 0, "content1", tvguide.topdiv);
    WebLibSimple.setBGColor(tvguide.content1, "#ff8000");

    //
    // topdiv.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(0, 0, 0, tvguide.constanst.timelineHeight, "timeline", tvguide.content1);
    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, "#ff00ff");

    //
    // topdiv.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(0, tvguide.constanst.timelineHeight, 0, 0, "content2", tvguide.content1);
    WebLibSimple.setBGColor(tvguide.content2, "#ffff00");

    //
    // topdiv.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(0, 0, tvguide.constanst.senderbarWidth, 0, "senderbar", tvguide.content2);
    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, "#000000");

    //
    // topdiv.epgdata
    //

    tvguide.epgdata = WebLibSimple.createDiv(tvguide.constanst.senderbarWidth, 0, 0, 0, "epgdata", tvguide.content2);
    tvguide.epgdata.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgdata, "#ffffff");
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "senderbarScroll", tvguide.senderbar);
    tvguide.senderbarScroll.scrollVertical = true;

    for(var index in tvguide.senderList)
    {
        var senderDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderDiv.style.position = "relative";
        senderDiv.style.height = tvguide.constanst.senderHeight + "px";
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

    tvguide.timelineScroll = WebLibSimple.createDivWidth(0, 0, (hours * tvguide.constanst.hoursPix), 0, "timelineScroll", tvguide.timeline);
    tvguide.timelineScroll.scrollHorizontal = true;
//    tvguide.timelineScroll.style.backgroundColor = "#ffff00";

    for(var inx = 0; inx < hours; inx++)
    {
        var timeDiv = WebLibSimple.createAnyAppend("div", tvguide.timelineScroll);
        timeDiv.style.backgroundColor = "#6ff9ec";
        timeDiv.style.position = "absolute";
        timeDiv.style.width    = tvguide.constanst.hoursPix + "px";
        timeDiv.style.left     = (tvguide.constanst.hoursPix * inx) +"px";
        timeDiv.style.top      = "0px";
        timeDiv.style.bottom   = "0px";

        var timeSpan = WebLibSimple.createAnyAppend("span", timeDiv);
        timeSpan.textContent = inx;
    }

    console.log("===============> tvguide.timelineScroll Width=" + tvguide.timelineScroll.clientWidth);
}

tvguide.createEpgData = function()
{
    tvguide.epgScroll = WebLibSimple.createDiv(0, 0, 0, 0, "epgScroll", tvguide.epgdata);
    tvguide.epgScroll.scrollHorizontal = true;
    tvguide.epgScroll.scrollVertical   = true;
    WebLibSimple.setBGColor(tvguide.epgScroll, "#9e5151");

    var time = 48 * 60;
    var startTime = 0;
    var minPix = tvguide.constanst.hoursPix / 60;

    while (startTime < time)
    {
        var randomTvShowTime = Math.floor(Math.random() * 100);

        var randomTvShow = WebLibSimple.createDivWidHei(
                        startTime * minPix,
                        0,
                        (startTime + randomTvShowTime) * minPix,
                        tvguide.constanst.senderHeight,
                        null, tvguide.epgScroll);

        randomTvShow.style.margin = "4px";
        randomTvShow.style.border = "1px solid black";

        startTime += randomTvShowTime;
        console.log("============> startTime: " + startTime + "=" + startTime * minPix);
    }
}


tvguide.createFrameSetup();
tvguide.getSenderList();
tvguide.createSenderBar();
tvguide.createTimeLine();
tvguide.createEpgData();
