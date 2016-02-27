testing.onClickNext = function(event)
{
    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    testing.loadNextInfo();

    event.stopPropagation();
}

testing.onClickNext100 = function(event)
{
    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    var hundert = (testing.infolist.length < 1000) ? testing.infolist : 1000;

    while (hundert-- > 0) testing.infolist.shift();

    testing.loadNextInfo();

    event.stopPropagation();
}

testing.onClickTitle = function(event)
{
    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    var search = "http://www.google.de/search?tbm=isch&q=";
    var query = testing.info.search + " " + testing.info.sender;

    testing.iframe.src = search + encodeURIComponent(query);

    event.stopPropagation();
}

testing.onClickCancel = function(event)
{
    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    event.stopPropagation();
}

testing.onClickOk = function(event)
{
    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    if (! testing.saveInfo())
    {
        alert("Das war nix!!!")
    }
    else
    {
        testing.loadNextInfo();
    }

    event.stopPropagation();
}

WebAppIntercept.onUserHrefClick = function(url)
{
    //
    // http://images.google.de/imgres
    //  ?imgurl=http%3A%2F%2Fwww.rosenheim24.de%2Fbilder%2F2013%2F05%2F14%2F2905337
    //          %2F1841037453-rosenheim-cops-dreh-staffel-g77aNPPwiMG.jpg
    //  &imgrefurl=http%3A%2F%2Fwww.rosenheim24.de%2Ftv-kino%2Frosenheim-cops-
    //             dreh-staffel-rosenheim24-2905337.html
    //  &h=747&w=1000
    //  &tbnid=wvleiuf4g-D5aM%3A&q=Rosenheim%20Cops&docid=zQCaOGaSj5-_NM
    //  &ei=trPOVoLWKoq56AT336DADg
    //  &tbm=isch&iact=rc&uact=3&page=1&start=0&ndsp=11
    //  &ved=0ahUKEwiCidqtuJLLAhWKHJoKHfcvCOgQrQMIKTAB
    //

    var imgurl = url.match(/imgurl=([^&]*)/);
    imgurl = imgurl && imgurl.length ? imgurl[ 1 ] : null;
    if (! imgurl) return false;
    imgurl = decodeURIComponent(imgurl);
    if (imgurl.startsWith("https://")) imgurl = "http" + imgurl.substring(5);

    var width = url.match(/&w=([^&]*)/);
    width = width && width.length ? width[ 1 ] : null;
    if (! width) return false;

    var height = url.match(/&h=([^&]*)/);
    height = height && height.length ? height[ 1 ] : null;
    if (! height) return false;

    if (testing.dimmer && testing.dimmer.parentElement)
    {
        testing.dimmer.parentElement.removeChild(testing.dimmer);
    }

    width = parseInt(width);
    height = parseInt(height);

    testing.info.imgurl = imgurl;

    var aspect = width / height;

    var dwid = width;
    var dhei = height;

    if (dwid > 500)
    {
        dwid = 500;
        dhei = Math.round(dwid / aspect);
    }

    dwid +=  20;
    dhei += 100;

    var div                   = document.createElement("div");
    div.id                    = "dimmer";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.backgroundColor = "rgba(0, 0, 0, 0.6)";
    div.onclick               = testing.onClickCancel;

    testing.content1.appendChild(testing.dimmer = div);

    var cwid = testing.dimmer.clientWidth;
    var chei = testing.dimmer.clientHeight;

    console.log(dwid + "=" + dhei + "=" + cwid + "=" + chei);

    var div                   = document.createElement("div");
    div.id                    = "okdiv";
    div.style.position        = "absolute";
    div.style.top             = ((chei - dhei) / 2) + "px";
    div.style.left            = ((cwid - dwid) / 2) + "px";
    div.style.width           = dwid + "px";
    div.style.height          = dhei + "px";
    div.style.fontSize        = "28px";
    div.style.fontWeight      = "bold";
    div.style.backgroundColor = "#ffffff";

    testing.dimmer.appendChild(testing.okdiv = div);

    var img            = document.createElement("img");
    img.id             = "imgdiv";
    img.style.position = "absolute";
    img.style.top      = "10px";
    img.style.left     = "10px";
    img.style.width    = (dwid - 20) + "px";
    img.style.height   = "auto";

    testing.okdiv.appendChild(testing.imgdiv = img);

    var div            = document.createElement("div");
    div.id             = "dimdiv";
    div.style.position = "absolute";
    div.style.left     = "10px";
    div.style.right    = "10px";
    div.style.bottom   = "10px";
    div.style.height   = "50px";
    div.innerHTML      = width + "x" + height;

    testing.okdiv.appendChild(testing.dimdiv = div);

    var div            = document.createElement("div");
    div.id             = "butdiv";
    div.style.color    = "#448844";
    div.style.position = "absolute";
    div.style.right    = "10px";
    div.style.bottom   = "10px";
    div.style.height   = "50px";

    testing.okdiv.appendChild(testing.butdiv = div);

    var span                = document.createElement("span");
    span.id                 = "cancelbutton";
    span.style.paddingLeft  = "20px";
    span.style.paddingRight = "20px";
    span.style.marginLeft   = "10px";
    span.style.marginRight  = "10px";
    span.innerHTML          = "Cancel";
    span.onclick            = testing.onClickCancel;

    testing.butdiv.appendChild(span);

    var span                = document.createElement("span");
    span.id                 = "okbutton";
    span.style.paddingLeft  = "20px";
    span.style.paddingRight = "20px";
    span.style.marginLeft   = "10px";
    span.style.marginRight  = "10px";
    span.innerHTML          = "Ok";
    span.onclick            = testing.onClickOk;

    testing.butdiv.appendChild(span);

    testing.imgdiv.src = imgurl;
    console.log("============================>" + imgurl);

    return false;
}

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

    testing.topdiv.appendChild(testing.titlebar = div);

    var div                   = document.createElement("div");
    div.id                    = "counter";
    div.style.position        = "absolute";
    div.style.top             = "24px";
    div.style.left            = "8px";
    div.style.width           = "100px";
    div.style.bottom          = "0px";
    div.style.fontSize        = "28px";
    div.style.fontWeight      = "bold";
    div.style.textAlign       = "center";
    div.style.color           = "#444444";

    testing.titlebar.appendChild(testing.counter = div);


    var div                   = document.createElement("div");
    div.id                    = "title";
    div.style.position        = "absolute";
    div.style.top             = "20px";
    div.style.left            = "80px";
    div.style.right           = "80px";
    div.style.bottom          = "0px";
    div.style.fontSize        = "34px";
    div.style.fontWeight      = "bold";
    div.style.textAlign       = "center";
    div.style.color           = "#444444";
    div.onclick               = testing.onClickTitle;

    testing.titlebar.appendChild(testing.title = div);

    var div                   = document.createElement("div");
    div.id                    = "nextbutton";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.width           = "80px";
    div.style.right           = "0px";
    div.style.bottom          = "0px";
    div.style.fontSize        = "60px";
    div.style.fontWeight      = "bold";
    div.style.textAlign       = "center";
    div.style.color           = "#cccccc";
    div.innerHTML             = ">>";
    div.onclick               = testing.onClickNext;
    div.ondblclick            = testing.onClickNext100;

    testing.titlebar.appendChild(testing.nextbutton = div);

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

    testing.topdiv.appendChild(testing.content1 = div);

    var div                   = document.createElement("iframe");
    div.id                    = "iframe";
    div.style.position        = "absolute";
    div.style.top             = "0px";
    div.style.left            = "0px";
    div.style.width           = "100%";
    div.style.height          = "100%";
    div.style.border          = "0px solid black";

    testing.content1.appendChild(testing.iframe = div);
}

testing.loadInfoList = function()
{
    testing.acttype = "tv";
    testing.actcountry = "de";

    testing.actjson = "http://epg.xavaro.de/epginfo/"
        + testing.acttype + "/"
        + testing.actcountry
        + ".json";

    testing.infolist = JSON.parse(WebAppRequest.loadSync(encodeURI(testing.actjson)));
}

testing.loadNextInfo = function()
{
    //var which = Math.floor(Math.random() * testing.infolist.length);
    //var info = testing.infolist.splice(which, 1);
    //info = info[ 0 ];

    if (testing.infolist.length == 0)
    {
        alert("Fertig...");
        return;
    }

    var info = testing.infolist.shift();

    testing.info = {};
    testing.info.name = info.t;
    testing.info.search = info.t;
    testing.info.sender = info.s;

    testing.counter.innerHTML = testing.infolist.length;
    testing.title.innerHTML = testing.info.sender + " (" + info.c + ")";

    var search = "http://www.google.de/search?tbm=isch&q=";

    if (testing.info.name.startsWith("@movie "))
    {
        testing.info.name = testing.info.name.substring(7);
        testing.info.search = "Film: " + testing.info.name;
    }

    testing.iframe.src = search + encodeURIComponent(testing.info.search);
}

testing.saveInfo = function()
{
    var saveInfo = {};

    saveInfo.type    = testing.acttype;
    saveInfo.country = testing.actcountry;
    saveInfo.name    = testing.info.name;
    saveInfo.imgurl  = testing.info.imgurl;

    return WebAppRequest.saveSync("http://epg.xavaro.de/pgmuploader", JSON.stringify(saveInfo));
}

testing.createFrameSetup();
testing.loadInfoList();
testing.loadNextInfo();
