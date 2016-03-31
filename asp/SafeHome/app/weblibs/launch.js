//
// Launcher library.
//

WebLibLaunch = {};

WebLibLaunch.createFrame = function()
{
    var wl = WebLibLaunch;

    wl.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setDefaultBGColor(wl.topDiv);

    wl.horzSize = WebAppUtility.getLaunchItemSize();
    wl.vertSize = WebAppUtility.getLaunchItemSize();

    wl.launchPage = 0;
    wl.launchItems = [];
    wl.launchPages = [];

    WebLibLaunch.onResize();
}

WebLibLaunch.addTopScreen = function(div)
{
    var wl = WebLibLaunch;

    if (wl.topScreen) WebLibSimple.detachElement(wl.topScreen);

    wl.topScreen = div;

    if (div != null) wl.topDiv.appendChild(wl.topScreen);
}

WebLibLaunch.getWidth = function()
{
    return WebLibLaunch.realWidth;
}

WebLibLaunch.getHeight = function()
{
    return WebLibLaunch.realHeight;
}

WebLibLaunch.onResize = function()
{
    var wl = WebLibLaunch;

    wl.realWidth  = wl.topDiv.clientWidth;
    wl.realHeight = wl.topDiv.clientHeight;

    wl.horzItems = Math.floor(wl.realWidth  / wl.horzSize);
    wl.vertItems = Math.floor(wl.realHeight / wl.vertSize);

    wl.horzSpace = Math.floor((wl.realWidth  - (wl.horzItems * wl.horzSize)) / (wl.horzItems + 1));
    wl.vertSpace = Math.floor((wl.realHeight - (wl.vertItems * wl.vertSize)) / wl.vertItems);

    wl.horzStart = Math.floor(((wl.realWidth  - (wl.horzItems * wl.horzSize)) % (wl.horzItems + 1)) / 2);
    wl.vertStart = Math.floor(((wl.realHeight - (wl.vertItems * wl.vertSize)) % wl.vertItems) / 2);

    wl.horzStart += wl.horzSpace;
    wl.vertStart += wl.vertSpace / 2;

    console.log("Resize: " + orientation
        + "=" + wl.realWidth + "/" + wl.realHeight
        + " " + wl.horzItems + ":" + wl.vertItems);

    wl.positionLaunchItems();

    if (wl.topScreen && wl.topScreen.onresize)
    {
        wl.topScreen.onresize(wl.topScreen);
    }
}

WebLibLaunch.getIconDivElem = function(launchitem)
{
    return launchitem.imgDivElem;
}

WebLibLaunch.getOverIconImgElem = function(launchitem)
{
    return launchitem.overImgElem;
}

WebLibLaunch.onClickBackItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target && target.config)) return;

    WebAppUtility.makeClick();

    WebAppRequest.doBackkeyPressed();
}

WebLibLaunch.onClickPrevItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target && target.config)) return;

    WebAppUtility.makeClick();

    var wl = WebLibLaunch;

    var pageIndex = target.launchPageIndex;

    wl.launchPages[ pageIndex ].style.display = "none";
    wl.launchPages[ pageIndex - 1 ].style.display = "block";
}

WebLibLaunch.onClickNextItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target && target.config)) return;

    WebAppUtility.makeClick();

    var wl = WebLibLaunch;

    var pageIndex = target.launchPageIndex;

    wl.launchPages[ pageIndex ].style.display = "none";
    wl.launchPages[ pageIndex + 1 ].style.display = "block";
}

WebLibLaunch.createLaunchItem = function(config)
{
    var wl = WebLibLaunch;

    var maxSlots = wl.horzItems * wl.vertItems;
    var nextSlot = wl.launchItems.length;

    if (((nextSlot + 1) % maxSlots) == 0)
    {
        var next = {};

        next.label = "Weiter";
        next.icon = "/weblibs/launch/next_600x600.png";

        next.onclick = WebLibLaunch.onClickNextItem;

        WebLibLaunch.createLaunchItemInternal(next);

        nextSlot++;
    }

    if ((nextSlot % maxSlots) == 0)
    {
        var prev = {};

        prev.label = "Zurück";
        prev.icon = "/weblibs/launch/prev_600x600.png";

        prev.onclick = (nextSlot == 0)
            ? WebLibLaunch.onClickBackItem
            : WebLibLaunch.onClickPrevItem;

        WebLibLaunch.createLaunchItemInternal(prev);
    }

    return WebLibLaunch.createLaunchItemInternal(config);
}

WebLibLaunch.createLaunchItemInternal = function(config)
{
    var wl = WebLibLaunch;

    var li = WebLibSimple.createDivWidHei(0, 0, wl.horzSize, wl.vertSize, "launchItem");
    wl.launchItems.push(li);
    li.config = config;

    var url = "url('/weblibs/launch/shadow_black_400x400.png')";
    if (config.frame) url = "url('/weblibs/launch/shadow_" + config.frame + "_400x400.png')";

    li.style.backgroundSize = WebLibSimple.addPixel(wl.horzSize);
    li.style.backgroundImage = url;

    li.divElem = WebLibSimple.createDiv(0, 0, 0, 0, "divElem", li);
    li.divElem.style.margin = WebLibSimple.addPixel(18);

    li.imgDivElem = WebLibSimple.createDiv(20, 0, 20, 40, "imgDivElem", li.divElem);

    li.iconImgElem = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", "iconImgElem", li.imgDivElem);
    li.iconImgElem.src = config.icon;

    li.labelElem = WebLibSimple.createAny("center", 0, null, 0, 0, "labelElem", li.divElem);
    WebLibSimple.setFontSpecs(li.labelElem, 24, "bold", "#777777");
    li.labelElem.innerHTML = config.label;

    var overwid = - Math.floor(wl.horzSize / 3);
    var overhei = + Math.floor(wl.vertSize / 3);

    li.overDivElem = WebLibSimple.createDivWidHei(0, 0, overwid, overhei, "overDivElem", li.divElem);
    li.overImgElem = WebLibSimple.createImgWidHei(0, 0, "100%", "100%", "overImgElem", li.overDivElem);
    li.overImgElem.src = config.overicon ? config.overicon : WebLibSimple.getNixPixImg();

    if (config.onclick) li.onclick = config.onclick;

    wl.positionLaunchItems();

    return li;
}

WebLibLaunch.positionLaunchItems = function()
{
    var wl = WebLibLaunch;

    var pag = 0;
    var col = 0;
    var row = 0;

    var xpos = wl.horzStart;
    var ypos = wl.vertStart;

    for (var linx in wl.launchItems)
    {
        if (wl.launchPages.length < (pag + 1))
        {
            var lp = WebLibSimple.createDiv(0, 0, 0, 0, null, wl.topDiv);
            if (wl.launchPages.length > 0) lp.style.display = "none";
            WebLibSimple.setDefaultBGColor(lp);
            wl.launchPages.push(lp);
        }

        var li = wl.launchItems[ linx ];

        li.style.left = WebLibSimple.addPixel(xpos);
        li.style.top  = WebLibSimple.addPixel(ypos);

        li.launchPageIndex = pag;

        WebLibSimple.attachElement(li, wl.launchPages[ pag ]);

        xpos += wl.horzSize + wl.horzSpace;

        if (++col >= wl.horzItems)
        {
            xpos = wl.horzStart;

            ypos += wl.vertSize + wl.vertSpace;

            col = 0;

            if (++row >= wl.vertItems)
            {
                ypos = wl.vertStart;

                row = 0;

                pag++;
            }
        }
    }
}

addEventListener("resize", WebLibLaunch.onResize);
