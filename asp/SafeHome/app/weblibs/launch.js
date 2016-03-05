//
// Launcher library.
//

WebLibLaunch = {};

WebLibLaunch.createFrame = function()
{
    var wl = WebLibLaunch;

    wl.bgcol = "#ffffffee";
    wl.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setBGColor(wl.topDiv, "#88880000");

    wl.horzSize = WebAppUtility.getLaunchItemSize();
    wl.vertSize = WebAppUtility.getLaunchItemSize();

    wl.launchPage = 0;
    wl.launchItems = [];
    wl.launchPages = [];

    WebLibLaunch.onOrientationChange();
}

WebLibLaunch.onOrientationChange = function()
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

    console.log("Orientation: " + orientation + "=" + wl.realWidth + "/" + wl.realHeight + " " + wl.horzItems + ":" + wl.vertItems);
}

WebLibLaunch.createLaunchItem = function(config)
{
    var wl = WebLibLaunch;

    var li = WebLibSimple.createDivWidHei(0, 0, wl.horzSize, wl.vertSize);
    WebLibSimple.setBGColor(li, "#80008000");
    wl.launchItems.push(li);
    li.config = config;

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
        var li = wl.launchItems[ linx ];

        if (wl.launchPages.length < (pag + 1))
        {
            var lp = WebLibSimple.createDiv(0, 0, 0, 0, null, wl.topDiv);
            WebLibSimple.setBGColor(lp, wl.bgcol);
            wl.launchPages.push(lp);
        }

        WebLibSimple.attachElement(li, wl.launchPages[ pag ]);

        li.style.left = WebLibSimple.addPixel(xpos);
        li.style.top  = WebLibSimple.addPixel(ypos);

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

addEventListener("orientationchange", WebLibLaunch.onOrientationChange);
