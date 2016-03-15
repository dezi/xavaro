tvscrape.channels = {};

tvscrape.onClickSelect = function(event)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

    tvscrape.iframe.style.display = "none";
    tvscrape.selector.style.display = "block";

    if (event) event.stopPropagation();
}

tvscrape.onClickNext = function(event)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

    tvscrape.loadNextInfo();

    if (event) event.stopPropagation();
}

tvscrape.onClickTitle = function(event)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

    var search = "http://www.google.de/search?tbm=isch&q=";

    tvscrape.info.search = tvscrape.info.name;

    if (tvscrape.info.name.startsWith("@movie "))
    {
        tvscrape.info.name = tvscrape.info.name.substring(7);
        tvscrape.info.search = "Film: " + tvscrape.info.name;
    }

    if (tvscrape.info.issender)
    {
        tvscrape.info.issender = false;
    }
    else
    {
        var sender = tvscrape.info.sender;

        sender = sender.replace("Fernsehen", " ");
        sender = sender.replace("Hamburg", " ");
        sender = sender.replace("Sachsen-Anhalt", " ");
        sender = sender.replace("Köln", " ");
        sender = sender.replace("Berlin", " ");
        sender = sender.replace("Brandenburg", " ");
        sender = sender.replace("Baden-Württemberg", " ");
        sender = sender.replace("Bayerisches", "BR");
        sender = sender.replace("Nord", " ");

        sender = sender.replace("Das Erste", "DasErste");
        sender = sender.replace("Sat. 1", "Sat1");
        sender = sender.replace("Pro Sieben", "Pro7");
        sender = sender.replace("Kabel Eins", "Kabel1");
        sender = sender.replace("RTL 2", "RTL2");
        sender = sender.replace("Eins Plus", "EinsPlus");
        sender = sender.replace("ZDF info", "ZDFinfo");
        sender = sender.replace("ZDF Kultur", "ZDFKultur");
        sender = sender.replace("ZDF neo", "ZDFneo");

        sender = sender.replace("  ", " ");
        sender = sender.replace("  ", " ");

        tvscrape.info.search += " " + sender;
        tvscrape.info.issender = true;
    }

    tvscrape.iframe.src = search + encodeURIComponent(tvscrape.info.search);

    if (event) event.stopPropagation();
}

tvscrape.onClickCancel = function(event)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

    if (event) event.stopPropagation();
}

tvscrape.onClickOk = function(event)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

    if (! tvscrape.saveInfo())
    {
        alert("Das war nix!!!")
    }
    else
    {
        tvscrape.loadNextInfo();
    }

    if (event) event.stopPropagation();
}

WebAppIntercept.onUserHrefClick = function(url)
{
    WebLibSimple.detachElement(tvscrape.dimmer);

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

    tvscrape.info.imgurl = imgurl;

    var imgrefurl = url.match(/imgrefurl=([^&]*)/);
    imgrefurl = imgrefurl && imgrefurl.length ? imgrefurl[ 1 ] : null;
    if (! imgrefurl) return false;
    imgrefurl = decodeURIComponent(imgrefurl);
    if (imgrefurl.startsWith("https://")) imgrefurl = "http" + imgrefurl.substring(5);

    tvscrape.info.imgrefurl = imgrefurl;

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

    tvscrape.dimmer = WebLibSimple.createDiv(0, 0, 0, 0, "dimmer", tvscrape.content1);
    WebLibSimple.setBGColor(tvscrape.dimmer, "#99000000");
    tvscrape.dimmer.onclick = tvscrape.onClickCancel;

    var cwid  = tvscrape.dimmer.clientWidth;
    var chei  = tvscrape.dimmer.clientHeight;
    var cleft = ((cwid - dwid) / 2);
    var ctop  = ((chei - dhei) / 2);

    tvscrape.okdiv = WebLibSimple.createDivWidHei(cleft, ctop, dwid, dhei, "okdiv", tvscrape.dimmer);
    WebLibSimple.setBGColor(tvscrape.okdiv, "#ffffff");

    tvscrape.imgdiv = WebLibSimple.createImgWidHei(10, 10, dwid - 20, "auto", "imgdiv", tvscrape.okdiv);

    tvscrape.sizediv = WebLibSimple.createDivHeight(10, 10, null, -50, "sizediv", tvscrape.okdiv);
    WebLibSimple.setFontSpecs(tvscrape.sizediv, 28, "bold", "#000000");
    tvscrape.sizediv.innerHTML = width + "x" + height;

    tvscrape.buttdiv = WebLibSimple.createDivHeight(null , 10, 10, -50, "butdiv", tvscrape.okdiv);
    WebLibSimple.setFontSpecs(tvscrape.buttdiv, 28, "bold", "#448844");

    tvscrape.cancelbutton = WebLibSimple.createSpanPadded(25, 0, 25, 0, "cancelbutton", tvscrape.buttdiv);
    tvscrape.cancelbutton.innerHTML = "Cancel".toUpperCase();
    tvscrape.cancelbutton.onclick = tvscrape.onClickCancel;

    tvscrape.okbutton = WebLibSimple.createSpanPadded(25, 0, 25, 0, "okbutton", tvscrape.buttdiv);
    tvscrape.okbutton.innerHTML = "Ok".toUpperCase();
    tvscrape.okbutton.onclick = tvscrape.onClickOk;

    console.log("Image: " + imgurl);

    tvscrape.imgdiv.src = imgurl;
}

tvscrape.createFrameSetup = function()
{
    tvscrape.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);

    //
    // Titlebar setup.
    //

    tvscrape.titlebar = WebLibSimple.createDivHeight(0, 0, 0, 80, "titlebar", tvscrape.topdiv);
    WebLibSimple.setBGColor(tvscrape.titlebar, "#8080ff");

    tvscrape.counter = WebLibSimple.createDivWidth(8, 24, 100, 0, "counter", tvscrape.titlebar);
    WebLibSimple.setFontSpecs(tvscrape.counter, 28, "bold", "#444444");
    tvscrape.counter.style.textAlign = "center";
    tvscrape.counter.onclick = tvscrape.onClickSelect;

    tvscrape.title = WebLibSimple.createDiv(80, 20, 80, 0, "title", tvscrape.titlebar);
    WebLibSimple.setFontSpecs(tvscrape.title, 34, "bold", "#444444");
    tvscrape.title.style.textAlign = "center";
    tvscrape.title.onclick = tvscrape.onClickTitle;

    tvscrape.nextbutton = WebLibSimple.createDivWidth(0, 0, -80, 0, "nextbutton", tvscrape.titlebar);
    WebLibSimple.setFontSpecs(tvscrape.nextbutton, 60, "bold", "#cccccc");
    tvscrape.nextbutton.style.textAlign = "center";
    tvscrape.nextbutton.innerHTML = ">>";
    tvscrape.nextbutton.onclick = tvscrape.onClickNext;

    //
    // Content setup.
    //

    tvscrape.content1 = WebLibSimple.createDiv(0, 80, 0, 0, "content1", tvscrape.topdiv);
    WebLibSimple.setBGColor(tvscrape.content1, "#888888");
    tvscrape.content1.style.overflow = "hidden";

    tvscrape.selector = WebLibSimple.createDiv(0, 0, 0, null, "selector", tvscrape.content1);
    WebLibSimple.setBGColor(tvscrape.selector, "#ffffff");
    WebLibSimple.setFontSpecs(tvscrape.selector, 18, "bold", "#444444");
    tvscrape.selector.scrollVertical = true;

    tvscrape.iframe = WebLibSimple.createAnyWidHei("iframe", 0, 0, "100%", "100%", "iframe", tvscrape.content1);
    tvscrape.iframe.style.border = "0px solid black";
    tvscrape.iframe.style.display = "none";
}

tvscrape.onChannelClick = function(channelspan)
{
    console.log("onChannelClick");

    var channel = channelspan.channel;
    var channelpath = channel.type + "/" + channel.isocc + "/" + channel.name + ".json";

    if (channelspan.channelcheck.checked)
    {
        for (var inx = 0; inx < tvscrape.infolist.length; inx++)
        {
            if (tvscrape.infolist[ inx ].channelpath == channelpath)
            {
                tvscrape.infolist.splice(inx--, 1);
            }
        }

        channelspan.channelcheck.checked = false;
    }
    else
    {
        var loadurl = "http://epg.xavaro.de/epginfo/" + channelpath;
        var loadlist = JSON.parse(WebAppRequest.loadSync(encodeURI(loadurl)));

        if (loadlist)
        {
            for (var inx = 0; inx < loadlist.length; inx++)
            {
                if (tvscrape.infodups[ loadlist[ inx ].t ]) continue;

                loadlist[ inx ].type = channel.type;
                loadlist[ inx ].country = channel.isocc;
                loadlist[ inx ].channelpath = channelpath;

                tvscrape.infolist.push(loadlist[ inx ]);
            }
        }

        channelspan.channelcheck.checked = true;
    }

    //
    // Rebuild duplicate check list.
    //

    tvscrape.infodups = {};

    for (var inx = 0; inx < tvscrape.infolist.length; inx++)
    {
        tvscrape.infodups[ tvscrape.infolist[ inx ].t ] = true;
    }

    tvscrape.counter.innerHTML = tvscrape.infolist.length;
}

tvscrape.sortChannelList = function(a, b)
{
    if (a.prio == b.prio) return 0;
    return (a.prio < b.prio) ? -1 : 1;
}

tvscrape.loadChannelList = function()
{
    tvscrape.infolist = [];
    tvscrape.infodups = {};

    //
    // Read and sort channels by priority.
    //

    var standard = JSON.parse(WebAppMedia.getLocaleDefaultChannels("tv"));
    var channels = JSON.parse(WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));

    for (var inx = 0; inx < channels.length; inx++)
    {
        var channel = channels[ inx ];

        var name = channel.name;
        if (name.endsWith(" HD")) name = name.substring(0, name.length - 3);

        var channeltag = channel.type + "/" + channel.isocc + "/" + name;


        channels[ inx ].prio = 9999;
        channels[ inx ].selected = false;

        for (var cnt = 0; cnt < standard.length; cnt++)
        {
            if (standard[ cnt ] == channeltag)
            {
                channels[ inx ].prio = cnt;
                channels[ inx ].selected = true;
                break;
            }
        }
    }

    channels.sort(tvscrape.sortChannelList);

    //
    // Create channel content.
    //

    tvscrape.channels = {};

    for (var inx = 0; inx < channels.length; inx++)
    {
        var channel = channels[ inx ];
        if (channel.isen) continue;

        var name = channel.name;
        if (name.startsWith("Sky ")) continue;

        if (name.endsWith(" HD")) name = name.substring(0, name.length - 3);
        if (name.endsWith(" Deutsch")) name = name.substring(0, name.length - 8);
        if (name.endsWith(" Deutschland")) name = name.substring(0, name.length - 12);
        if (name.endsWith(" Einsfestival")) name = name.substring(0, name.length - 13);

        if (tvscrape.channels[ name ]) continue;
        tvscrape.channels[ name ] = channel;

        var imgurl = "http://epg.xavaro.de/channels/"
            + channel.type + "/"
            + channel.isocc + "/"
            + channel.name + ".png";

        var channelspan = document.createElement("div");
        channelspan.style.position = "relative";
        channelspan.style.display = "inline-block";
        channelspan.style.width = "50%";
        channelspan.style.height = "74px";
        channelspan.onTouchClick = tvscrape.onChannelClick;

        tvscrape.selector.appendChild(channelspan);

        var channelimg = WebLibSimple.createImgWidHei(8, 12, 66, 50, null, channelspan);
        channelimg.src = imgurl;

        var channeldiv = WebLibSimple.createDiv(84, 26, 40, null, null, channelspan);
        channeldiv.innerHTML = name;

        var channelcheck = WebLibSimple.createAny("input", null, 26, 20, null, null, channelspan);
        channelcheck.type = "checkbox";

        channelspan.channel = channel;
        channelspan.channelcheck = channelcheck;

        if (channel.selected) tvscrape.onChannelClick(channelspan);
    }
}

tvscrape.loadNextInfo = function()
{
    if (tvscrape.infolist.length == 0)
    {
        tvscrape.iframe.style.display = "none";
        tvscrape.selector.style.display = "block";

        return;
    }
    else
    {
        tvscrape.iframe.style.display = "block";
        tvscrape.selector.style.display = "none";
    }

    var info = tvscrape.infolist.shift();

    tvscrape.info = {};
    tvscrape.info.type = info.type;
    tvscrape.info.name = info.t;
    tvscrape.info.sender = info.s;
    tvscrape.info.country = info.country;
    tvscrape.info.issender = false;

    tvscrape.counter.innerHTML = tvscrape.infolist.length;
    tvscrape.title.innerHTML = tvscrape.info.sender + " (" + info.c + ")";

    tvscrape.onClickTitle();
}

tvscrape.saveInfo = function()
{
    var saveInfo = {};

    saveInfo.name      = tvscrape.info.name;
    saveInfo.type      = tvscrape.info.type;
    saveInfo.country   = tvscrape.info.country;
    saveInfo.imgurl    = tvscrape.info.imgurl;
    saveInfo.imgrefurl = tvscrape.info.imgrefurl;

    return WebAppRequest.saveSync("http://epg.xavaro.de/pgmuploader", JSON.stringify(saveInfo));
}

tvscrape.createFrameSetup();
tvscrape.loadChannelList();
