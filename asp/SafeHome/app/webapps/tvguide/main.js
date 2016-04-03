WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));
}

tvguide.constants =
{
    loadDaysFuture           : 1,
    loadDaysPast             : 0,
    timelineInterval         : 15,

    recordPreload            : 1,
    recordPostload           : 2,

    locale                   : WebAppUtility.getLocale(),
    localCountry             : WebAppUtility.getLocaleCountry(),
    localLanguage            : WebAppUtility.getLocaleLanguage(),

    today                    : WebLibSimple.getTodayDate().getTime() / 1000 / 60,

    epgDataSrcPrefix         : "http://" + WebApp.manifest.appserver + "/epgdata/",
    epgDataSrcPostfix        : ".json.gz",

    titlebarHeight           : 80,
    senderbarWidth           : 80,
    senderHeight             : 64,
    timelineHeight           : 30,
    hoursPix                 : 360,

    titleColor               : "#ffffff",
    titleSize                : 34,
    titleFontWeight          : "bold",

    titlebarColor            : "#888888",
    content1Color            : "#dedede",
    content2Color            : "#acacac",
    senderImgDivColor        : "#ffffff",
    senderbarColor           : "#dedede",

    epgdataColor             : "#dedede",
    epgScrollColor           : "#ffffff",
    epgScrollStyleColor      : "#ffffff",
    epgScrollfontWeight      : "bold",

    timelineColor            : "#dedede",
    timelineDivColor         : "#ebe9e9",
    timelineScrollStyleColor : "#000000",
    timelineScrollfontWeight : "bold",
    timelineScrollTextSize   : 15,

    programBGColor           : "#a0a0a0",
    descriptionColor         : "#ffffff",
    pastLineColor            : "#33ff0000",
}

tvguide.calculateDates = function()
{
    var today          = tvguide.constants.today;
    var hoursPix       = tvguide.constants.hoursPix;
    var loadDaysPast   = tvguide.constants.loadDaysPast;
    var loadDaysFuture = tvguide.constants.loadDaysFuture;

    tvguide.constants.minPix    = hoursPix / 60;
    tvguide.constants.loadHours = (loadDaysPast + loadDaysFuture) * 24;
    tvguide.constants.timeShift = today - (loadDaysPast * 24 * 60);
}

tvguide.createFrameSetup = function()
{
    var constants = tvguide.constants;

    //
    // tvguide.topdiv
    //

    tvguide.topdiv = WebLibSimple.createDiv(
        0,
        0,
        0,
        0,
        "topdiv",
        document.body);

    //
    // tvguide.titlebar
    //

    tvguide.titlebar = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        constants.titlebarHeight,
        "titlebar",
        tvguide.topdiv);

    WebLibSimple.setBGColor(tvguide.titlebar, constants.titlebarColor);

    //
    // tvguide.title
    //

    tvguide.title = WebLibSimple.createDiv(
        constants.titlebarHeight,
        constants.titlebarHeight / 4,
        constants.titlebarHeight,
        0,
        "title",
        tvguide.titlebar);

    tvguide.title.style.textAlign = "center";
    tvguide.title.innerHTML       = WebLibSimple.getNiceDay(constants.today * 1000 * 60);

    WebLibSimple.setFontSpecs(
        tvguide.title,
        constants.titleSize,
        constants.titleFontWeight,
        constants.titleColor);

    //
    // tvguide.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(
        0,
        constants.titlebarHeight,
        0,
        0,
        "content1",
        tvguide.topdiv);

    tvguide.content1.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.content1, constants.titlebarColor);

    //
    // tvguide.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        constants.timelineHeight,
        "timeline",
        tvguide.content1);

    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, constants.timelineColor);

    //
    // tvguide.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(
        0,
        constants.timelineHeight,
        0,
        0,
        "content2",
        tvguide.content1);

    WebLibSimple.setBGColor(tvguide.content2, constants.content2Color);

    //
    // tvguide.epgbody
    //

    tvguide.epgbody = WebLibSimple.createDiv(
        0,
        0,
        0,
        0,
        "epgdata",
        tvguide.content2);

    tvguide.epgbody.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgbody, constants.epgdataColor);

    //
    // tvguide.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(
        0,
        0,
        constants.senderbarWidth,
        0,
        "senderbar",
        tvguide.content2);

    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, constants.senderbarColor);
}

tvguide.getSenderList = function()
{
    tvguide.senderList = {};

    var localChannels = JSON.parse(WebAppMedia.getLocaleDefaultChannels("tv"));

    for (var channelIndex in localChannels)
    {
        var name = localChannels[ channelIndex ];
        var cparts = name.split("/");

        tvguide.senderList[ name ] = {};
        var sender = tvguide.senderList[ name ];

        sender.index   = parseInt(channelIndex);
        sender.type    = cparts[0];
        sender.country = cparts[1];
        sender.channel = cparts[2];
        sender.iptv    = false;
        sender.isbd    = false;
    }

    var iptvChannels  = JSON.parse(WebAppMedia.getLocaleInternetChannels("tv"));
    var iptvAliase    = {};

    for (var webKey in iptvChannels)
    {
        if (! iptvChannels[ webKey ][ "channels" ]) continue;

        for (var channels in iptvChannels[ webKey ][ "channels" ])
        {
            var aliase   = iptvChannels[ webKey ][ "channels" ][ channels ][ "aliase" ];
            var videourl = iptvChannels[ webKey ][ "channels" ][ channels ][ "videourl" ];

            for (var aliasIndex in aliase)
            {
                iptvAliase[ aliase[ aliasIndex ] ] = videourl;
            }
        }
    }

    for (var iptv in iptvAliase)
    {
        var channel = tvguide.senderList[ iptv ];

        if (channel)
        {
            channel.iptv     = true;
            channel.videourl = iptvAliase[ iptv ];
        }
    }

    var loacation  = tvguide.constants.localLanguage;
    var bdsrc      = "http://" + WebApp.manifest.appserver + "/channels/tv/" + loacation + ".json.gz";
    var brainDeads = JSON.parse(WebAppRequest.loadSync(bdsrc));

    for (var index in brainDeads)
    {
        var tmp       = brainDeads[ index ]
        var bdChannel = tmp.type + "/" + tmp.isocc + "/" + tmp.name;

        for (var sender in tvguide.senderList)
        {
            if (sender == bdChannel && tmp.isbd)
            {
                tvguide.senderList[ sender ].isbd = tmp.isbd;
            }
        }
    }
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(0, 0, 0, null, "senderbarScroll", tvguide.senderbar);

    tvguide.senderbarScroll.scrollVertical = true;
    tvguide.senderbarScroll.onTouchScroll  = tvguide.onSenderBarScroll;
    tvguide.senderbarScroll.style.zIndex   = "2";

    for (var channel in tvguide.senderList)
    {
        // var channel = tvguide.senderList[ index ];

        var senderImgDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderImgDiv.style.position = "relative";
        senderImgDiv.style.height   = tvguide.constants.senderHeight + "px";

        WebLibSimple.setBGColor(senderImgDiv, tvguide.constants.senderImgDivColor);

        var paddingDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, senderImgDiv);

        paddingDiv.style.paddingLeft     = "7px";
        paddingDiv.style.paddingTop      = "8px";
        paddingDiv.style.paddingRight    = "7px";
        paddingDiv.style.paddingBottom   = "7px";

        var image = WebLibSimple.createAnyAppend("img", paddingDiv);
        image.style.height = "100%";
        image.style.width  = "100%";

        image.src = encodeURI("http://" + WebApp.manifest.appserver   + "/channels/"
            + channel + ".png");
    }
}

tvguide.updateTimeLine = function(startHour, stopHour)
{
    var startMin         = startHour * 60;
    var stopMin          = stopHour  * 60;

    var timelineScroll   = tvguide.timelineScroll;
    var timelineInterval = tvguide.constants.timelineInterval;
    var today            = tvguide.constants.today;
    var hoursPix         = tvguide.constants.hoursPix;
    var minPix           = tvguide.constants.minPix;

    var timelineWidth          = WebLibSimple.getPxInt(timelineScroll.style.left) + ((startHour + stopHour) * hoursPix);
    timelineScroll.style.width = timelineWidth + "px";

    for(var min = startMin; min < stopMin; min += timelineInterval)
    {
        var timelineDiv = WebLibSimple.createDivWidth(
            minPix * min,
            0,
            minPix * timelineInterval,
            0,
            null,
            timelineScroll);

        WebLibSimple.setBGColor(timelineDiv, tvguide.constants.timelineDivColor);

        if (min % (timelineInterval * 2))
        {
            var div = WebLibSimple.createDivWidth(0, 0, 2, 0, null, timelineDiv);
            div.style.backgroundColor = "#e00073";

//            var span = WebLibSimple.createAnyAppend("span", timelineDiv);
//            span.style.backgroundColor = "#e00073";
//            span.style.left = "0px";
//            span.style.top = "0px";
//            span.style.right = "0px";
//            span.style.bottom = "0px";
//            span.style.width = "10px"
//            span.style.border          = "15px solid black";
        }
        else
        {
            var time = today + min;
            var date = new Date(time * 1000 * 60);

            var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
            timeSpan.textContent = WebLibSimple.padNum(date.getHours(), 2) + ":" +
                                   WebLibSimple.padNum(date.getMinutes(), 2);
        }
    }
}

tvguide.createTimeLine = function()
{
    var timelineInterval = tvguide.constants.timelineInterval;
    var hoursPix         = tvguide.constants.hoursPix;
    var minPix           = tvguide.constants.minPix;
    var loadHours        = tvguide.constants.loadHours;
    var timelineWidth    = loadHours * hoursPix;

    tvguide.timelineScroll = WebLibSimple.createDivWidth(
        0,
        0,
        timelineWidth,
        0,
        "timelineScroll",
        tvguide.timeline);

    var timelineScroll = tvguide.timelineScroll;

    WebLibSimple.setFontSpecs(
        timelineScroll,
        tvguide.constants.timelineScrollTextSize,
        tvguide.constants.timelineScrollfontWeight,
        tvguide.constants.timelineScrollStyleColor);

    timelineScroll.scrollHorizontal = true;
    timelineScroll.onTouchScroll    = tvguide.onTimeLineScroll;

    tvguide.updateTimeLine(0, loadHours);
}

tvguide.createEpgProgram = function(channelName, epgdata)
{
   // console.log("--> channel:" + channelName + " isbd:" + isbd);
   // console.log("--> epgdata:" + JSON.stringify(epgdata));

    var channel     = tvguide.senderList[ channelName ]
    var senderIndex = channel.index;

    var minPix    = tvguide.constants.minPix;
    var timeShift = tvguide.constants.timeShift;

    for (var programIndex in epgdata)
    {
        var epg     = epgdata[ programIndex ];
        epg.type    = channel.type;
        epg.country = channel.country;
        epg.channel = channel.channel;
        epg.iptv    = channel.iptv;
        epg.isbd    = channel.isbd;

        if (channel.iptv)
        {
            epg.videourl =  channel.videourl;
        }

        var moronDateStart = new Date(epg.start);
        var moronDateStop  = new Date(epg.stop );

        if (moronDateStart.getSeconds() > 0 || moronDateStop.getSeconds() > 0)
        {
            epg = DeMoronize.cleanTime(epg);
        }

        var startDate = new Date(epg.start).getTime() / 1000 / 60;
        var stopDate  = new Date(epg.stop ).getTime() / 1000 / 60;

        var startTime = Math.floor(startDate - timeShift);
        var stopTime  = Math.floor(stopDate  - timeShift);

        var duration  = stopTime - startTime;

        var programDiv = WebLibSimple.createDivWidHei(
            startTime * minPix,
            tvguide.constants.senderHeight * senderIndex,
            duration * minPix,
            tvguide.constants.senderHeight,
            null, tvguide.epgScroll);

        var paddingDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, programDiv);
        paddingDiv.style.marginLeft   = "4px";
        paddingDiv.style.marginTop    = "2px";
        paddingDiv.style.marginRight  = "0px";
        paddingDiv.style.marginBottom = "2px";
        paddingDiv.style.padding      = "8px";
        paddingDiv.style.borderRadius = "8px";

        paddingDiv.style.lineHeight   = "3";

        if (channel.iptv)
        {
            // #a0a0a0 rgb(159, 159, 159)
            WebLibSimple.setBGColor(paddingDiv, "#8b8888");
        }
        else
        {
            WebLibSimple.setBGColor(paddingDiv, tvguide.constants.programBGColor);
        }

        paddingDiv.epg = epg;

        if (paddingDiv.epg.title2)
        {
            paddingDiv.innerHTML = paddingDiv.epg.title + ", " + paddingDiv.epg.title2;
        }
        else
        {
            paddingDiv.innerHTML = paddingDiv.epg.title;
        }
    }
}

tvguide.updateEpgData = function(loadDay)
{
    var cDate = new Date().getTime() + (loadDay * 1000 * 60 * 60 * 24);
    var date  = new Date(cDate);

    var year  = WebLibSimple.padNum(date.getFullYear(),  4);
    var month = WebLibSimple.padNum(date.getMonth() + 1, 2);
    var day   = WebLibSimple.padNum(date.getDate(),      2);

    var current = year + "." + month + "." + day;

    // console.log("--> get: " + current);

    for (var sender in tvguide.senderList)
    {
        var epgurl = encodeURI(
            tvguide.constants.epgDataSrcPrefix +
            sender + "/" + current +
            tvguide.constants.epgDataSrcPostfix);

        WebAppRequest.loadAsyncJSON(epgurl);
    }
}

tvguide.getEpgData = function()
{
    var loadDaysFuture = tvguide.constants.loadDaysFuture;
    var loadDaysPast   = tvguide.constants.loadDaysPast * -1;

    for (var day = loadDaysPast; day < loadDaysFuture; day++)
    {
        tvguide.updateEpgData(day);
    }
}

tvguide.createEpgBodyScroll = function()
{
    var hoursPix  = tvguide.constants.hoursPix;
    var loadHours = tvguide.constants.loadHours;

    tvguide.epgScroll = WebLibSimple.createDivWidHei(
        0,
        0,
        loadHours * hoursPix,
        tvguide.senderbarScroll.clientHeight,
        "epgScroll",
        tvguide.epgbody);

    var epgScroll = tvguide.epgScroll;

    epgScroll.style.color      = tvguide.constants.epgScrollStyleColor;
    epgScroll.style.fontWeight = tvguide.constants.epgScrollfontWeight;

    epgScroll.scrollHorizontal = true;
    epgScroll.scrollVertical   = true;

    epgScroll.onTouchScroll    = tvguide.onEPGTouchScroll;
    epgScroll.onTouchClick     = tvguide.onEPGTouchClick;

    WebLibSimple.setBGColor(epgScroll, tvguide.constants.epgScrollColor);

    tvguide.getEpgData();
}

tvguide.updateEpgPast = function()
{
    var updateHours  = 24;
    var hoursPix     = tvguide.constants.hoursPix;
    var loadHours    = tvguide.constants.loadHours;
    var loadDaysPast = tvguide.constants.loadDaysPast + 1;
    var today        = tvguide.constants.today;

    var epgScroll = tvguide.epgScroll;
    epgScroll.style.width = (hoursPix * loadHours) + (hoursPix * updateHours) + "px";
    epgScroll.style.left  = WebLibSimple.getPxInt(epgScroll.style.left) - hoursPix * 24 + "px";

    var timelineScroll = tvguide.timelineScroll;
    timelineScroll.style.left = WebLibSimple.getPxInt(timelineScroll.style.left) - hoursPix * 24 + "px";

    var childLength = epgScroll.childNodes.length;

    tvguide.updateTimeLine(loadHours, loadHours + updateHours);

    for (var broadcast = 0; broadcast < childLength; broadcast++)
    {
        var child         = epgScroll.childNodes[ broadcast ];
        var childLeftPosi = WebLibSimple.getPxInt(child.style.left);
        child.style.left  = (hoursPix * updateHours) + childLeftPosi + "px";
    }

    tvguide.PastLine.style.left = "0px";
    tvguide.PastLine.style.width = tvguide.PastLine.clientWidth + hoursPix * 24 + "px";

    tvguide.constants.timeShift = today - (loadDaysPast * 24 * 60);

    tvguide.updateEpgData(loadDaysPast * -1);

    tvguide.constants.loadDaysPast = loadDaysPast;
    tvguide.constants.loadHours    = loadHours + updateHours;
}

tvguide.updateEpgFuture = function()
{
    var updateHours    = 24;
    var hoursPix       = tvguide.constants.hoursPix;
    var loadHours      = tvguide.constants.loadHours + updateHours;

    tvguide.updateTimeLine(loadHours - updateHours, loadHours);

    var epgScroll         = tvguide.epgScroll;
    epgScroll.style.width = (loadHours * hoursPix) + "px";

    tvguide.updateEpgData(tvguide.constants.loadDaysFuture);

    tvguide.constants.loadHours      = loadHours;
    tvguide.constants.loadDaysFuture = tvguide.constants.loadDaysFuture + 1;
}

WebAppRequest.onLoadAsyncJSON = function(src, data)
{
    if (! data[ "epgdata" ]) return;

    // /2016.01.01.json.gz
    var channel = WebLibSimple.substring(
        decodeURI(src),
        tvguide.constants.epgDataSrcPrefix.length,
      -(tvguide.constants.epgDataSrcPostfix.length + 11));

    // console.log(src);
    // console.log(channel);

    var data = data[ "epgdata" ];
    data = DeMoronize.cleanShortShows(data);

    tvguide.createEpgProgram(channel, data);
}

tvguide.PastLineTimeout = function()
{
    var updateInterval = 3;
    var minPix = tvguide.constants.hoursPix / (60 * updateInterval);

    var actpos = tvguide.PastLine.clientWidth;
    actpos     = actpos + minPix;

    tvguide.PastLine.style.width = actpos + "px";

    tvguide.animatePastLine = setTimeout(tvguide.PastLineTimeout, 1000 * (60 / updateInterval));
}

tvguide.createPastLine = function()
{
    var hoursPix  = tvguide.constants.hoursPix;
    var minPix    = tvguide.constants.minPix;
    var timeShift = tvguide.constants.timeShift;

    var now         = Math.floor(new Date().getTime() / 1000 / 60);
    var nowPosition = minPix * (timeShift - now);
    var padding     = tvguide.constants.timelineInterval * 4 * minPix;

    //
    // timeline & epg --> current time
    //

    tvguide.epgScroll.style.left      = nowPosition + padding + "px";
    tvguide.timelineScroll.style.left = nowPosition + padding + "px";

    //
    // tvguide.PastLine
    //

    tvguide.PastLine = WebLibSimple.createDivWidth(
        0,
        0,
        nowPosition * -1,
        0,
        "PastLine",
        tvguide.epgScroll);

    WebLibSimple.setBGColor(tvguide.PastLine, tvguide.constants.pastLineColor);

    tvguide.PastLine.style.zIndex        = "1";
    tvguide.PastLine.style.pointerEvents = "none";

    //
    // Past time animation
    //

    tvguide.PastLineTimeout();
}

tvguide.onTimeLineScroll = function(newX, newY)
{
    var timeShift = tvguide.constants.timeShift;
    var hoursPix  = tvguide.constants.hoursPix;

    tvguide.epgScroll.style.left = newX + "px";

    if (tvguide.epgScroll.style.left != tvguide.timelineScroll.style.left)
    {
        tvguide.timelineScroll.style.left = newX + "px";
    }

    // selectedDay --> Min
    var selectedDay = (timeShift - (newX / hoursPix * 60));

    tvguide.title.innerHTML = WebLibSimple.getNiceDay(new Date(selectedDay * 1000 * 60));
}

tvguide.onSenderBarScroll = function(newX, newY)
{
    tvguide.epgScroll.style.top = newY + "px";

    if (tvguide.senderbarScroll.style.top != tvguide.epgScroll.style.top)
    {
        tvguide.senderbarScroll.style.top = newY + "px";
    }
}

tvguide.onEPGTouchScroll = function(newX, newY)
{
    if (newX !== null)
    {
        tvguide.onTimeLineScroll(newX, null);

        if (newX == 0)
        {
            console.log("Update EPG past");
            tvguide.updateEpgPast();
        }

        if (tvguide.epgScroll.clientWidth + newX == tvguide.content1.clientWidth)
        {
            console.log("Update EPG future");
            tvguide.updateEpgFuture();
        }
    }

    if (newY !== null)
    {
        tvguide.onSenderBarScroll(null, newY);
    }
}

WebAppRequest.onBackkeyPressed = function()
{
    if (tvguide.description)
    {
        WebAppRequest.haveBackkeyPressed(true);

        if (WebLibSimple.wikiFrame)
        {
            WebLibSimple.nukeWiki();
        }
        else
        {
            tvguide.checkInfoStatus();
        }
    }
    else
    {
        WebAppRequest.haveBackkeyPressed(false);
    }
}

tvguide.main = function()
{
    tvguide.calculateDates();

    //
    // frame setup
    //

    tvguide.createFrameSetup();

    //
    // sender list
    //

    tvguide.getSenderList();

    //
    // create scroll components
    //

    tvguide.createSenderBar();
    tvguide.createTimeLine();

    tvguide.createEpgBodyScroll();

    //
    // create pastline
    //

    tvguide.createPastLine();
}

tvguide.main();
