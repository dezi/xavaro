//
// Launcher library.
//

WebLibLaunch = {};

WebLibLaunch.createFrame = function()
{
    var wl = WebLibLaunch;

    wl.bgcol = "#ffffffee";
    wl.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);

    wl.horzSize = WebAppUtility.getLaunchItemSize();
    wl.vertSize = WebAppUtility.getLaunchItemSize();

    wl.launchPage = 0;
    wl.launchItems = [];
    wl.launchPages = [];

    WebLibLaunch.onResize();
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

    console.log("Resize: " + orientation + "=" + wl.realWidth + "/" + wl.realHeight + " " + wl.horzItems + ":" + wl.vertItems);

    wl.positionLaunchItems();
}

WebLibLaunch.createLaunchItem = function(config)
{
    var wl = WebLibLaunch;

    var li = WebLibSimple.createDivWidHei(0, 0, wl.horzSize, wl.vertSize);
    wl.launchItems.push(li);
    li.config = config;

    li.style.backgroundSize = WebLibSimple.addPixel(wl.horzSize);

    var url = "url('/weblibs/launch/shadow_black_400x400.png')";
    if (config.frame) url = "url('/weblibs/launch/shadow_" + config.frame + "_400x400.png')";

    li.style.backgroundImage = url;

    li.divElem = WebLibSimple.createDiv(0, 0, 0, 0, null, li);
    li.divElem.style.margin = WebLibSimple.addPixel(18);

    li.imgDivElem = WebLibSimple.createDiv(20, 0, 20, 40, null, li.divElem);

    li.iconImgElem = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", null, li.imgDivElem);
    li.iconImgElem.src = config.icon;

    li.labelElem = WebLibSimple.createAny("center", 0, null, 0, 0, null, li.divElem);
    WebLibSimple.setFontSpecs(li.labelElem, 24, "bold", "#777777");
    li.labelElem.innerHTML = config.label;

    var overwid = - (wl.horzSize >> 2);
    var overhei = + (wl.vertSize >> 2);

    li.overDivElem = WebLibSimple.createDivWidHei(0, 0, overwid, overhei, null, li.divElem);
    li.overImgElem = WebLibSimple.createImgWidHei(0, 0, "100%", "100%", null, li.overDivElem);
    li.overImgElem.src = config.overicon ? config.overicon : WebLibSimple.getNixPixImg();

    wl.positionLaunchItems();
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
            WebLibSimple.setBGColor(lp, wl.bgcol);
            wl.launchPages.push(lp);
        }

        var li = wl.launchItems[ linx ];

        li.style.left = WebLibSimple.addPixel(xpos);
        li.style.top  = WebLibSimple.addPixel(ypos);

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
