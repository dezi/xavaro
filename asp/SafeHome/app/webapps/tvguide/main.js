tvguide.constans =
{
    titlebarHeight : 80,
    senderbarWidth : 80,
    timelineHeight : 20,
    senderHeight   : 50
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

tvguide.onTouchStart = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    console.log("onTouchStart:" + touchobj.target.id + "=" + touchobj.clientX + "/" + touchobj.clientY);
    console.log("onTouchStart:" + touchobj.target.innerHTML);

    tvguide.touch = {};

    target = touchobj.target;

    while (target && target.id != "senderbarScroll")
    {
        target = target.parentElement;
    }

    if (!target) return;

    tvguide.touch.startX = touchobj.clientX;
    tvguide.touch.startY = touchobj.clientY;

    tvguide.touch.offsetTop  = target.offsetTop;
    tvguide.touch.offsetLeft = target.offsetLeft;

    tvguide.touch.target = target;

    event.preventDefault();
}

tvguide.onTouchMove = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    var deltay = touchobj.clientY - tvguide.touch.startY;
    tvguide.touch.target.style.top = (tvguide.touch.offsetTop + deltay) + "px";

    event.preventDefault();
}

tvguide.onTouchEnd = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    console.log("onTouchEnd:" + touchobj.clientX + "/" + touchobj.clientY);

    event.preventDefault();
}

window.addEventListener("orientationchange", tvguide.onOrientationChange);
window.addEventListener("touchstart", tvguide.onTouchStart);
window.addEventListener("touchmove", tvguide.onTouchMove);
window.addEventListener("touchend", tvguide.onTouchEnd);

tvguide.createFrameSetup = function()
{
    document.body.style.margin = '0px';
    document.body.style.padding = '0px';

    //
    // topdiv
    //

    var div                   = document.createElement("div");
    div.id                    = "topdiv";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "#80ff00";
    tvguide.topdiv            = div;

    document.body.appendChild(div);

    //
    // topdiv.titlebar
    //

    var div                   = document.createElement("div");
    div.id                    = "titlebar";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.height          = tvguide.constans.titlebarHeight + "px";
    div.style.backgroundColor = "#8080ff";
    tvguide.titlebar          = div;

    tvguide.topdiv.appendChild(div);

    //
    // topdiv.content1
    //

    var div                   = document.createElement("div");
    div.id                    = "content1";
    div.style.position        = "absolute";
    div.style.top             = tvguide.constans.titlebarHeight + "px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "#ff8000";
    tvguide.content1          = div;

    tvguide.topdiv.appendChild(div);

    //
    // topdiv.content1.senderbar
    //

    var div                   = document.createElement("div");
    div.id                    = "senderbar";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.bottom          = "0px";
    div.style.width           = tvguide.constans.senderbarWidth + "px";
    div.style.backgroundColor = "#000000";
    div.style.overflow        = "hidden";
    tvguide.senderbar         = div;

    tvguide.content1.appendChild(div);

    //
    // topdiv.content1.content2
    //

    var div                   = document.createElement("div");
    div.id                    = "content2";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = tvguide.constans.senderbarWidth + "px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "#ffff00";
    tvguide.content2          = div;

    tvguide.content1.appendChild(div);

    //
    // topdiv.content1.content2.timeline
    //

    var div                   = document.createElement("div");
    div.id                    = "timeline";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.height          = tvguide.constans.timelineHeight + "px";
    div.style.backgroundColor = "#ff00ff";
    tvguide.timeline          = div;

    tvguide.content2.appendChild(div);

    //
    // topdiv.content1.content2.epgdata
    //

    var div                   = document.createElement("div");
    div.id                    = "epgdata";
    div.style.position        = "absolute";
    div.style.top             = tvguide.constans.timelineHeight + "px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "#ffffff";
    tvguide.epgdata           = div;

    tvguide.content2.appendChild(div);
}

tvguide.createSenderBar = function()
{
    var div            = document.createElement("div");
    div.id             = "senderbarScroll";
    div.style.position = "absolute";
    div.style.top      = "0px";
    div.style.left     = "0px";
    div.style.right    = "0px";

    tvguide.senderbarScroll = div;
    tvguide.senderbar.appendChild(div);

    for(var index in tvguide.senderList)
    {
        var senderDiv            = document.createElement("div");
        senderDiv.style.position = "absolute";
        senderDiv.style.top      = tvguide.constans.timelineHeight +
                                   tvguide.constans.senderHeight * index + "px";
        senderDiv.style.height   = tvguide.constans.senderHeight + "px";
        senderDiv.style.width    = tvguide.constans.senderbarWidth + "px";
        senderDiv.style.left     = "0px";
        senderDiv.style.right    = "0px";

        var paddingDiv                   = document.createElement("div");
        paddingDiv.align                 = "center";
        paddingDiv.style.padding         = "5px";
        paddingDiv.style.backgroundColor = "#ed1a79";

        var image = document.createElement("img");

        image.src = encodeURI("http://" +
            tvguide.manifest.appserver + "/channels/" +
            tvguide.senderList[ index ] + ".png"
        );

        image.id           = tvguide.senderList[ index ];
        image.style.top    = "0px";
        image.style.left   = "0px";
        image.style.right  = "0px";
        image.style.bottom = "0px";
        image.style.height = tvguide.constans.senderHeight + "px";


        paddingDiv.appendChild(image)
        senderDiv.appendChild(paddingDiv)

        tvguide.senderbarScroll.appendChild(senderDiv);
    }

    senderbarScroll.clientHeight = tvguide.senderList.length * tvguide.constans.senderHeight;
//
// senderbarScroll
// senderbarScroll.style.height = x * iconheight;
// senderbarScroll.clientHeight = x * iconheight;
// senderbarScroll.clientWidth
// senderbarScroll.offsetTop
// senderbarScroll.offsetLeft

    console.log("===============> senderbar height=" + tvguide.senderbar.clientHeight);
}

tvguide.createFrameSetup();
tvguide.getSenderList();
tvguide.createSenderBar();
