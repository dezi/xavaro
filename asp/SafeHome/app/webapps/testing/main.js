console.log("==================main.js loaded...");

//testing.alertest();

//console.log("==================" + extras.loadSync("http://epg.xavaro.de/channels/tv/de/ZDF.json"));

//testing.channels = WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz");
//console.log("==================" + testing.channels.length);

//testing.ZDF = WebAppRequest.loadSync("http://epg.xavaro.de/epgdata/tv/de/ZDF/current.json.gz");

/*
var pre = document.createElement("pre");
document.body.appendChild(pre);
pre.innerHTML = testing.channels;

json = { a:23, b:24, c:15 };

WebAppRequest.callback("hallo");
WebAppRequest.jsonfunz("pipa");
*/

testing.createFrameSetup = function()
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
    testing.topdiv            = div;

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
    div.style.height          = "80px";
    div.style.backgroundColor = "#8080ff";
    testing.titlebar          = div;

    testing.topdiv.appendChild(div);

    //
    // topdiv.content1
    //

    var div                   = document.createElement("div");
    div.id                    = "content1";
    div.style.position        = "absolute";
    div.style.top             = "80px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "#ff8000";
    testing.content1          = div;

    testing.topdiv.appendChild(div);

    var div                   = document.createElement("iframe");
    div.id                    = "iframe";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.width           = "100%";
    div.style.height          = "100%";
    div.style.border          = "0px solid black";
    testing.iframe            = div;

    testing.content1.appendChild(div);
}

testing.createFrameSetup();

//testing.iframe.src = "http://www.google.de/search?q=test&prmd=ivn&source=lnms&tbm=isch&sa=X&client=tablet-android-samsung&biw=768&bih=1024";
testing.iframe.src = "http://www.google.de/search?tbm=isch&q=Rosenheim%20Cops";
