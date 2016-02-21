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

//
// target =  touchobj.target;
// while (target && target.id != "senderbarScroll")
//{ target = target.parent;}
// if (!target) return // war nix
//
// =====> dieses element muss gescrollt werden.
//

    tvguide.touch.startX = touchobj.clientX;
    tvguide.touch.startY = touchobj.clientY;
    tvguide.touch.target = target;

    event.preventDefault();
}

tvguide.onTouchMove = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    //console.log("onTouchMove:" + touchobj.clientX + "/" + touchobj.clientY);

    // hier senderbarScroll am offsetTop rumdüdeln.-...

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

    var div = document.createElement("div");
    div.id = "topdiv";
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#80ff00";
    document.body.appendChild(div);

    tvguide.topdiv = div;

    var div = document.createElement("div");
    div.id = "titlebar";
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.height = tvguide.constans.titlebarHeight + "px";
    div.style.right = "0px";
    div.style.backgroundColor = "#8080ff";
    tvguide.topdiv.appendChild(div);

    tvguide.titlebar = div;

    var div = document.createElement("div");
    div.id = "content1";
    div.style.position = "absolute";
    div.style.top = tvguide.constans.titlebarHeight + "px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ff8000";
    tvguide.topdiv.appendChild(div);

    tvguide.content1 = div;

    var div = document.createElement("div");
    div.id = "senderbar";
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
//    div.style.bottom = "0px";
    div.style.bottom = "400px";
    div.style.width = tvguide.constans.senderbarWidth + "px";
    div.style.backgroundColor = "#000000";
    div.style.overflow = "hidden";

    tvguide.content1.appendChild(div);

    tvguide.senderbar = div;

    var div = document.createElement("div");
    div.id = "content2";
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = tvguide.constans.senderbarWidth + "px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ffff00";
    tvguide.content1.appendChild(div);

    tvguide.content2 = div;

    var div = document.createElement("div");
    div.id = "timeline";
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.height = tvguide.constans.timelineHeight + "px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ff00ff";
    tvguide.content2.appendChild(div);

    tvguide.timeline = div;

    var div = document.createElement("div");
    div.id = "epgdata";
    div.style.position = "absolute";
    div.style.top = tvguide.constans.timelineHeight + "px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ffffff";
    tvguide.content2.appendChild(div);

    tvguide.epgdata = div;
}

tvguide.createSenderBar = function()
{
    // tvguide.senderList

    for(var index in tvguide.senderList)
    {
        console.log("----------------> " + tvguide.senderList[ index ]);

        var div = document.createElement("div");
        div.style.position = "absolute";
        div.style.top      = tvguide.constans.timelineHeight + tvguide.constans.senderHeight * index + "px";
        div.style.height   = tvguide.constans.senderHeight + "px";
        div.style.width    = tvguide.constans.senderbarWidth + "px";
        div.style.left     = "0px";
        div.style.right    = "0px";

        var divPadding = document.createElement("div");
//        divPadding.style.position = "absolute";
        divPadding.style.padding = "5px";
        divPadding.style.backgroundColor = "#ed1a79";

        var image = document.createElement("img");

        image.src = encodeURI(
            "http://" + tvguide.manifest.appserver +
            "/channels/" + tvguide.senderList[ index ] + ".png"
        );

        console.log(image.src);

        image.id = tvguide.senderList[ index ];
        image.style.top    = "0px";
        image.style.left   = "0px";
        image.style.right  = "0px";
        image.style.bottom = "0px";
        image.style.height = tvguide.constans.senderHeight + "px";


        divPadding.appendChild(image)
        div.appendChild(divPadding)

        // tvguide.topdiv = div;
        tvguide.senderbar.appendChild(div);
    }

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
