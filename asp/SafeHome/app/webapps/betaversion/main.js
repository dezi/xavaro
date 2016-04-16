betaversion.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var bv = betaversion;

    bv.topDiv = WebLibSimple.createAnyAppend("div", document.body);
    WebLibSimple.setFontSpecs(bv.topDiv, 24, "bold", "#666666");
    bv.topDiv.style.padding = "20px";

    bv.appname = WebAppUtility.getAppName();
    bv.thisversion  = WebAppUtility.getBetaVersion();

    bv.releases = JSON.parse(WebAppRequest.loadSync("/weblibs/beta.json"));
    bv.latestversion = bv.releases[ bv.appname ].latest;

    for (var version in bv.releases[ bv.appname ].versions)
    {
        var vi = bv.releases[ bv.appname ].versions[ version ];

        var div = WebLibSimple.createAnyAppend("center", bv.topDiv);
        WebLibSimple.setBGColor(div, "#888888");
        WebLibSimple.setFontSpecs(div, 24, "bold", "#ffffff");
        div.style.padding = "8px";

        var what = (version == bv.thisversion) ? "Aktuell" : "Alt";

        if (version == bv.latestversion)
        {
            what = "Neu";
            WebLibSimple.setBGColor(div, "#ff8888");
        }

        if (version == bv.thisversion) what = "Aktuell";

        if (what.length > 0) what += ": ";

        div.innerHTML = what + bv.appname + " – " + version + " – "
            + WebLibSimple.getNiceDay(new Date(vi.date));

        if (vi.notes)
        {
            var div = WebLibSimple.createAnyAppend("div", bv.topDiv);
            div.style.padding = "8px";
            div.innerHTML = "Features in dieser Version:";

            for (var inx = 0; inx < vi.notes.length; inx++)
            {
                var div = WebLibSimple.createAnyAppend("div", bv.topDiv);
                div.style.paddingTop   = "8px";
                div.style.paddingLeft  = "24px";
                div.style.paddingRight = "24px";
                div.innerHTML = "– " + vi.notes[ inx ].title;

                var summary = vi.notes[ inx ].summary;

                if (summary)
                {
                    var div = WebLibSimple.createAnyAppend("div", bv.topDiv);
                    WebLibSimple.setFontSpecs(div, 16, "bold", "#666666");
                    div.style.paddingTop   = "4px";
                    div.style.paddingLeft  = "48px";
                    div.style.paddingRight = "24px";

                    if (Array.isArray(summary))
                    {
                        div.innerHTML = summary.join("<br />");
                    }
                    else
                    {
                        div.innerHTML = summary;
                    }
                }
            }
        }

        if (version == bv.latestversion)
        {
            var div = WebLibSimple.createAnyAppend("center", bv.topDiv);
            WebLibSimple.setBGColor(div, "#ffffff");
            div.style.color = "#338833";
            div.style.marginTop = "20px";
            div.style.marginLeft = "40px";
            div.style.marginRight = "40px";
            div.style.padding = "10px";
            div.style.borderRadius = "10px";
            div.style.border = "1px solid black";

            if (bv.thisversion == bv.latestversion)
            {
                div.innerHTML = "Sie haben bereits die aktuellste Version";
            }
            else
            {
                div.innerHTML = "Diese Version herunterladen und installieren";
                div.onTouchClick = betaversion.downLoadClick;
            }

            bv.loadButton = div;
       }

        var div = WebLibSimple.createAnyAppend("div", bv.topDiv);
        div.style.paddingTop = "20px";
    }
}

betaversion.downLoadClick = function()
{
    WebAppUtility.makeClick();

    var bv = betaversion;

    bv.loadButton.innerHTML = "Die Version wird geladen...";

    setTimeout(bv.downLoadStart, 100);
}

betaversion.downLoadStart = function()
{
    var bv = betaversion;

    var ok = WebAppBeta.getBetaDownload(bv.latestversion);

    if (ok)
    {
        bv.loadButton.innerHTML = "Die Version ist nun zur Installation bereit.";

        setTimeout(bv.installDat, 100);
    }
    else
    {
        bv.loadButton.innerHTML = "Der Download ist fehlgeschlagen.";
    }
}

betaversion.installDat = function()
{
    WebAppBeta.makeBetaInstall();
}

betaversion.createFrame();
