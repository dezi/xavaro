testing.channels = {};
testing.channels.standard = [];

/*
testing.channels.standard.push("tv/de/Das Erste");
testing.channels.standard.push("tv/de/ZDF");
testing.channels.standard.push("tv/de/RTL Deutschland");
testing.channels.standard.push("tv/de/Pro Sieben Deutschland");
testing.channels.standard.push("tv/de/Kabel Eins Deutschland");
testing.channels.standard.push("tv/de/Vox Deutschland");
testing.channels.standard.push("tv/de/Sat. 1 Deutschland");
testing.channels.standard.push("tv/de/RTL 2 Deutschland");
testing.channels.standard.push("tv/de/Tele 5");
testing.channels.standard.push("tv/de/Sixx Deutschland");
testing.channels.standard.push("tv/de/Eins Plus");
testing.channels.standard.push("tv/de/3sat");
testing.channels.standard.push("tv/de/ZDF info");
testing.channels.standard.push("tv/de/ZDF Kultur");
testing.channels.standard.push("tv/de/ZDF neo");
testing.channels.standard.push("tv/de/NDR Fernsehen Hamburg");
testing.channels.standard.push("tv/de/WDR Fernsehen Köln");
testing.channels.standard.push("tv/de/MDR Fernsehen Sachsen-Anhalt");
testing.channels.standard.push("tv/de/Einsfestival");
testing.channels.standard.push("tv/de/Bayerisches Fernsehen Nord");
*/

testing.onClickSelect = function(event)
{
    WebLibSimple.detachElement(testing.dimmer);

    testing.iframe.style.display = "none";
    testing.selector.style.display = "block";

    event.stopPropagation();
}


testing.onClickNext = function(event)
{
    WebLibSimple.detachElement(testing.dimmer);

    testing.loadNextInfo();

    event.stopPropagation();
}

testing.onClickTitle = function(event)
{
    WebLibSimple.detachElement(testing.dimmer);

    var search = "http://www.google.de/search?tbm=isch&q=";
    var query = testing.info.search + " " + testing.info.sender;

    testing.iframe.src = search + encodeURIComponent(query);

    event.stopPropagation();
}

testing.onClickCancel = function(event)
{
    WebLibSimple.detachElement(testing.dimmer);

    event.stopPropagation();
}

testing.onClickOk = function(event)
{
    WebLibSimple.detachElement(testing.dimmer);

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
    WebLibSimple.detachElement(testing.dimmer);

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

    testing.info.imgurl = imgurl;

    var imgrefurl = url.match(/imgrefurl=([^&]*)/);
    imgrefurl = imgrefurl && imgrefurl.length ? imgrefurl[ 1 ] : null;
    if (! imgrefurl) return false;
    imgrefurl = decodeURIComponent(imgrefurl);
    if (imgrefurl.startsWith("https://")) imgrefurl = "http" + imgrefurl.substring(5);

    testing.info.imgrefurl = imgrefurl;

    var width = url.match(/&w=([^&]*)/);
    width = width && width.length ? width[ 1 ] : null;
    if (! width) return false;

    var height = url.match(/&h=([^&]*)/);
    height = height && height.length ? height[ 1 ] : null;
    if (! height) return false;

    width = parseInt(width);
    height = parseInt(height);

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

    testing.dimmer = WebLibSimple.createDiv(0, 0, 0, 0, "dimmer", testing.content1);
    WebLibSimple.setBGColor(testing.dimmer, "#99000000");
    testing.dimmer.onclick = testing.onClickCancel;

    var cwid  = testing.dimmer.clientWidth;
    var chei  = testing.dimmer.clientHeight;
    var cleft = ((cwid - dwid) / 2);
    var ctop  = ((chei - dhei) / 2);

    testing.okdiv = WebLibSimple.createDivWidHei(cleft, ctop, dwid, dhei, "okdiv", testing.dimmer);
    WebLibSimple.setBGColor(testing.okdiv, "#ffffff");

    testing.imgdiv = WebLibSimple.createImgWidHei(10, 10, dwid - 20, "auto", "imgdiv", testing.okdiv);

    testing.sizediv = WebLibSimple.createDivHeight(10, 10, null, -50, "sizediv", testing.okdiv);
    WebLibSimple.setFontSpecs(testing.sizediv, 28, "bold", "#000000");
    testing.sizediv.innerHTML = width + "x" + height;

    testing.buttdiv = WebLibSimple.createDivHeight(null , 10, 10, -50, "butdiv", testing.okdiv);
    WebLibSimple.setFontSpecs(testing.buttdiv, 28, "bold", "#448844");

    testing.cancelbutton = WebLibSimple.createSpanPadded(25, 0, 25, 0, "cancelbutton", testing.buttdiv);
    testing.cancelbutton.innerHTML = "Cancel";
    testing.cancelbutton.onclick = testing.onClickCancel;

    testing.okbutton = WebLibSimple.createSpanPadded(25, 0, 25, 0, "okbutton", testing.buttdiv);
    testing.okbutton.innerHTML = "Ok";
    testing.okbutton.onclick = testing.onClickOk;

    testing.imgdiv.src = imgurl;
}

testing.createFrameSetup = function()
{
    testing.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);

    //
    // Titlebar setup.
    //

    testing.titlebar = WebLibSimple.createDivHeight(0, 0, 0, 80, "titlebar", testing.topdiv);
    WebLibSimple.setBGColor(testing.titlebar, "#8080ff");

    testing.counter = WebLibSimple.createDivWidth(8, 24, 100, 0, "counter", testing.titlebar);
    WebLibSimple.setFontSpecs(testing.counter, 28, "bold", "#444444");
    testing.counter.style.textAlign = "center";
    testing.counter.onclick = testing.onClickSelect;

    testing.title = WebLibSimple.createDiv(80, 20, 80, 0, "title", testing.titlebar);
    WebLibSimple.setFontSpecs(testing.title, 34, "bold", "#444444");
    testing.title.style.textAlign = "center";
    testing.title.onclick = testing.onClickTitle;

    testing.nextbutton = WebLibSimple.createDivWidth(0, 0, -80, 0, "nextbutton", testing.titlebar);
    WebLibSimple.setFontSpecs(testing.nextbutton, 60, "bold", "#cccccc");
    testing.nextbutton.style.textAlign = "center";
    testing.nextbutton.innerHTML = ">>";
    testing.nextbutton.onclick = testing.onClickNext;

    //
    // Content setup.
    //

    testing.content1 = WebLibSimple.createDiv(0, 80, 0, 0, "content1", testing.topdiv);
    WebLibSimple.setBGColor(testing.content1, "#888888");
    testing.content1.style.overflow = "hidden";

    testing.selector = WebLibSimple.createDiv(0, 0, 0, null, "selector", testing.content1);
    WebLibSimple.setBGColor(testing.selector, "#ffffff");
    WebLibSimple.setFontSpecs(testing.selector, 18, "bold", "#444444");
    testing.selector.scrollVertical = true;

    testing.iframe = WebLibSimple.createAnyWidHei("iframe", 0, 0, "100%", "100%", "iframe", testing.content1);
    testing.iframe.style.border = "0px solid black";
    testing.iframe.style.display = "none";
}

testing.onChannelClick = function(channelspan)
{
    console.log("onChannelClick");

    var channel = channelspan.channel;
    var channelpath = channel.type + "/" + channel.isocc + "/" + channel.name + ".json";

    if (channelspan.channelcheck.checked)
    {
        for (var inx = 0; inx < testing.infolist.length; inx++)
        {
            if (testing.infolist[ inx ].channelpath == channelpath)
            {
                testing.infolist.splice(inx--, 1);
            }
        }

        channelspan.channelcheck.checked = false;
    }
    else
    {
        var loadurl = "http://epg.xavaro.de/epginfo/" + channelpath;
        var loadlist = JSON.parse(WebAppRequest.loadSync(encodeURI(loadurl)));

        for (var inx = 0; inx < loadlist.length; inx++)
        {
            if (testing.infodups[ loadlist[ inx ].t ]) continue;

            loadlist[ inx ].type = channel.type;
            loadlist[ inx ].country = channel.isocc;
            loadlist[ inx ].channelpath = channelpath;

            testing.infolist.push(loadlist[ inx ]);
        }

        channelspan.channelcheck.checked = true;
    }

    //
    // Rebuild duplicate check list.
    //

    testing.infodups = {};

    for (var inx = 0; inx < testing.infolist.length; inx++)
    {
        testing.infodups[ testing.infolist[ inx ].t ] = true;
    }

    testing.counter.innerHTML = testing.infolist.length;
}

testing.loadChannelList = function()
{
    testing.infolist = [];
    testing.infodups = {};

    var channels = JSON.parse(WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));

    testing.channels = {};

    for (var inx = 0; inx < channels.length; inx++)
    {
        var channel = channels[ inx ];
        if (channel.isen) continue;

        var name = channel.name;
        if (name.startsWith("Sky ")) continue;

        if (name.endsWith(" HD")) name = name.substring(0, name.length - 3);
        if (name.endsWith(" Deutschland")) name = name.substring(0, name.length - 12);

        if (testing.channels[ name ]) continue;
        testing.channels[ name ] = channel;

        var imgurl = "http://epg.xavaro.de/channels/"
            + channel.type + "/"
            + channel.isocc + "/"
            + channel.name + ".png";

        var channelspan = document.createElement("div");
        channelspan.style.position = "relative";
        channelspan.style.display = "inline-block";
        channelspan.style.width = "50%";
        channelspan.style.height = "74px";
        channelspan.onTouchClick = testing.onChannelClick;

        testing.selector.appendChild(channelspan);

        var channelimg = document.createElement("img");
        channelimg.style.position = "absolute";
        channelimg.style.top = "12px";
        channelimg.style.left = "8px";
        channelimg.style.width = "66px";
        channelimg.style.height = "50px";
        channelimg.src = imgurl;

        channelspan.appendChild(channelimg);

        var channeldiv = document.createElement("div");
        channeldiv.style.position = "absolute";
        channeldiv.style.top = "26px";
        channeldiv.style.left = "84px";
        channeldiv.style.right = "40px";
        channeldiv.innerHTML = name;

        channelspan.appendChild(channeldiv);

        var channelcheck = document.createElement("input");
        channelcheck.type="checkbox";
        channelcheck.style.position = "absolute";
        channelcheck.style.top = "26px";
        channelcheck.style.right = "20px";
        channelcheck.innerHTML = "#";

        channelspan.appendChild(channelcheck);

        channelspan.channel = channel;
        channelspan.channelcheck = channelcheck;
    }
}

testing.loadNextInfo = function()
{
    if (testing.infolist.length == 0)
    {
        testing.iframe.style.display = "none";
        testing.selector.style.display = "block";

        return;
    }
    else
    {
        testing.iframe.style.display = "block";
        testing.selector.style.display = "none";
    }

    var info = testing.infolist.shift();

    testing.info = {};
    testing.info.type = info.type;
    testing.info.name = info.t;
    testing.info.search = info.t;
    testing.info.sender = info.s;
    testing.info.country = info.country;

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

    saveInfo.name      = testing.info.name;
    saveInfo.type      = testing.info.type;
    saveInfo.country   = testing.info.country;
    saveInfo.imgurl    = testing.info.imgurl;
    saveInfo.imgrefurl = testing.info.imgrefurl;

    return WebAppRequest.saveSync("http://epg.xavaro.de/pgmuploader", JSON.stringify(saveInfo));
}

testing.createFrameSetup();
testing.loadChannelList();
