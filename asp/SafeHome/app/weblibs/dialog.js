//
// Dialogs library.
//

WebLibDialog = {};

WebLibDialog.setOkButtonEnable = function(enable)
{
    var wld = WebLibDialog;

    if (wld.topDiv && wld.topDiv.okButton)
    {
        wld.topDiv.okButton.style.color = (enable ? "#448844" : "#cccccc");
        wld.topDiv.okButton.isenabled = enable;
    }
}

WebLibDialog.onClickOk = function(event)
{
    event.stopPropagation();

    if (event.target.isenabled)
    {
        WebAppUtility.makeClick();

        var wld = WebLibDialog;

        var close = true;

        if (wld.currentConfig && wld.currentConfig.onClickOk)
        {
            close = wld.currentConfig.onClickOk(event);
        }

        if (close)
        {
            WebLibSimple.detachElement(wld.topDiv);
            wld.currentConfig = null;
            wld.topDiv = null;
        }
    }
}

WebLibDialog.onClickOther = function(event)
{
    event.stopPropagation();

    if (event.target.isenabled)
    {
        WebAppUtility.makeClick();

        var wld = WebLibDialog;

        var close = true;

        if (wld.currentConfig && wld.currentConfig.onClickOther)
        {
            close = wld.currentConfig.onClickOther(event);
        }

        if (close)
        {
            WebLibSimple.detachElement(wld.topDiv);
            wld.currentConfig = null;
            wld.topDiv = null;
        }
    }
}

WebLibDialog.onClickCancel = function(event)
{
    //console.log("onClickCancel:" + event.target);

    event.stopPropagation();

    WebAppUtility.makeClick();

    var wld = WebLibDialog;

    WebLibSimple.detachElement(wld.topDiv);
    wld.currentConfig = null;
    wld.topDiv = null;
}

WebLibDialog.onClickIgnore = function(event)
{
    console.log("onClickIgnore:" + event.target);

    event.stopPropagation();
}

WebLibDialog.createDialog = function(config)
{
    var wld = WebLibDialog;

    wld.currentConfig = config;

    wld.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, document.body);
    WebLibSimple.setBGColor(wld.topDiv, "#99000000");
    wld.topDiv.onclick = wld.onClickCancel;

    wld.topDiv.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 0, 0, null, wld.topDiv);

    wld.topDiv.dialogDiv = WebLibSimple.createAnyAppend("div", wld.topDiv.centerDiv);
    WebLibSimple.setBGColor(wld.topDiv.dialogDiv, "#ffffff");
    wld.topDiv.dialogDiv.style.display = "inline-block";
    wld.topDiv.dialogDiv.style.padding = WebLibSimple.addPixel(20);
    wld.topDiv.dialogDiv.onclick = WebLibDialog.onClickIgnore;

    wld.topDiv.titleDiv = WebLibSimple.createAnyAppend("div", wld.topDiv.dialogDiv);
    wld.topDiv.titleDiv.style.paddingBottom = WebLibSimple.addPixel(28);
    WebLibSimple.setFontSpecs(wld.topDiv.titleDiv, 28, "bold", "#444444");
    wld.topDiv.titleDiv.innerHTML = config.title;

    wld.topDiv.contentDiv = WebLibSimple.createAnyAppend("div", wld.topDiv.dialogDiv);
    wld.topDiv.contentDiv.style.paddingBottom = WebLibSimple.addPixel(20);
    if (config.content) wld.topDiv.contentDiv.appendChild(config.content);

    wld.topDiv.buttDiv = WebLibSimple.createAnyAppend("div", wld.topDiv.dialogDiv);
    WebLibSimple.setFontSpecs(wld.topDiv.buttDiv, 24, "bold", "#448844");
    wld.topDiv.buttDiv.style.textAlign = "right";

    wld.topDiv.cancelButton = WebLibSimple.createSpanPadded(10, 0, 50, 0, null, wld.topDiv.buttDiv);
    wld.topDiv.cancelButton.innerHTML = (config.cancel ? config.cancel : "Abbrechen").toUpperCase();
    wld.topDiv.cancelButton.onclick = WebLibDialog.onClickCancel;

    if (config.other)
    {
        wld.topDiv.otherButton = WebLibSimple.createSpanPadded(25, 0, 0, 0, null, wld.topDiv.buttDiv);
        wld.topDiv.otherButton.innerHTML = config.other.toUpperCase();
        wld.topDiv.otherButton.style.color = (config.otherEnabled ? "#448844" : "#cccccc");
        wld.topDiv.otherButton.isenabled = (config.otherEnabled == true);
        wld.topDiv.otherButton.onclick = WebLibDialog.onClickOther;
    }

    wld.topDiv.okButton = WebLibSimple.createSpanPadded(25, 0, 25, 0, null, wld.topDiv.buttDiv);
    wld.topDiv.okButton.innerHTML = (config.ok ? config.ok : "Ok").toUpperCase();
    wld.topDiv.okButton.style.color = (config.okEnabled ? "#448844" : "#cccccc");
    wld.topDiv.okButton.isenabled = (config.okEnabled == true);
    wld.topDiv.okButton.onclick = WebLibDialog.onClickOk;

    //
    // Position dialog div ontop of the center div makes
    // it stay centered when orientation changes.
    //

    var dwid  = wld.topDiv.dialogDiv.clientWidth;
    var dhei  = wld.topDiv.dialogDiv.clientHeight;

    wld.topDiv.dialogDiv.style.position = "absolute";
    wld.topDiv.dialogDiv.style.left = WebLibSimple.addPixel(- (dwid >> 1));
    wld.topDiv.dialogDiv.style.top  = WebLibSimple.addPixel(- (dhei >> 1));
}
