tinderella.config =
{
    "iconsize" : 120
}

tinderella.createFrame = function()
{
    var ti = tinderella;

    ti.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setBGColor(ti.topdiv, "#dddddd");
    ti.topdiv.style.overflow = "hidden";

    ti.theight = ti.config.iconsize + 8 + 8

    ti.titlediv = WebLibSimple.createDivHeight(0, 0, 0, ti.theight, "titlediv", ti.topdiv);
    WebLibSimple.setBGColor(ti.titlediv, "#ff7755cc");
    ti.titlediv.style.overflow = "hidden";

    ti.titlescroll = WebLibSimple.createDivWidth(0, 0, null, 0, "titlescroll", ti.titlediv);
    ti.titlescroll.scrollHorizontal = true;

    ti.contentdiv = WebLibSimple.createDiv(0, 0, 0, 0, "contentdiv", ti.topdiv);
    ti.contentdiv.style.overflow = "hidden";

    ti.contentscroll = WebLibSimple.createDivHeight(0, 0, 0, 0, "contentscroll", ti.contentdiv);

    ti.titlediv.style.top = 0 + "px";
    ti.contentdiv.style.top = ti.theight + "px";
}

tinderella.createFrame();

