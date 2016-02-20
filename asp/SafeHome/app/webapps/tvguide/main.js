tvguide = new Object();

tvguide.onOrientationChange = function()
{
    console.log(window.orientation);
}

tvguide.onTouchStart = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    console.log("onTouchStart:" + touchobj.clientX + "/" + touchobj.clientY);

    event.preventDefault();
}

tvguide.onTouchMove = function(event)
{
    var touchobj = event.changedTouches[ 0 ];

    console.log("onTouchMove:" + touchobj.clientX + "/" + touchobj.clientY);

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
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#80ff00";
    document.body.appendChild(div);

    tvguide.topdiv = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.height = "80px";
    div.style.right = "0px";
    div.style.backgroundColor = "#8080ff";
    tvguide.topdiv.appendChild(div);

    tvguide.titlebar = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "80px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ff8000";
    tvguide.topdiv.appendChild(div);

    tvguide.content1 = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.width = "80px";
    div.style.backgroundColor = "#000000";
    tvguide.content1.appendChild(div);

    tvguide.senderbar = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "80px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ffff00";
    tvguide.content1.appendChild(div);

    tvguide.content2 = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "0px";
    div.style.left = "0px";
    div.style.height = "20px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ff00ff";
    tvguide.content2.appendChild(div);

    tvguide.timeline = div;

    var div = document.createElement("div");
    div.style.position = "absolute";
    div.style.top = "20px";
    div.style.left = "0px";
    div.style.bottom = "0px";
    div.style.right = "0px";
    div.style.backgroundColor = "#ffffff";
    tvguide.content2.appendChild(div);

    tvguide.epgdata = div;
}

tvguide.createFrameSetup();
