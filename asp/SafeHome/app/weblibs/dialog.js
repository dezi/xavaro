//
// Dialogs library.
//

WebLibDialog = {};

WebLibDialog.setOkButtonEnable = function(enable)
{
    var wld = WebLibDialog;

    if (wld.currentDialog.okButton)
    {
        wld.currentDialog.okButton.style.color = (enable ? "#448844" : "#cccccc");
        wld.currentDialog.okButton.isenabled = enable;
    }
}

WebLibDialog.onClickOk = function(target)
{
    if (target.isenabled)
    {
        WebAppUtility.makeClick();

        var wld = WebLibDialog;

        var close = true;

        if (wld.currentConfig && wld.currentConfig.onClickOk)
        {
            close = wld.currentConfig.onClickOk(target);
        }

        if (close) WebLibDialog.closeDialog();
    }
}

WebLibDialog.onClickOther = function(target)
{
    if (target.isenabled)
    {
        WebAppUtility.makeClick();

        var wld = WebLibDialog;

        var close = true;

        if (wld.currentConfig && wld.currentConfig.onClickOther)
        {
            close = wld.currentConfig.onClickOther(target);
        }

        if (close) WebLibDialog.closeDialog();
    }
}

WebLibDialog.onClickCancel = function(target)
{
    WebAppUtility.makeClick();

    WebLibDialog.closeDialog();
}

WebLibDialog.onBackKeyPressed = function()
{
    console.log("WebLibDialog.onBackKeyPressed:");

    WebLibDialog.closeDialog();
}

WebLibDialog.closeDialog = function()
{
    var wld = WebLibDialog;

    WebAppRequest.releaseBackKey("WebLibDialog.onBackKeyPressed");

    WebLibSimple.detachElement(wld.currentDialog.topDiv);

    wld.currentConfig = null;
    wld.currentDialog = null;
}

WebLibDialog.onClickIgnore = function(target)
{
    console.log("WebLibDialog.onClickIgnore:" + target);
}

WebLibDialog.createDialog = function(config)
{
    var wld = WebLibDialog;

    wld.currentConfig = config;
    wld.currentDialog = dialog = {};

    dialog.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, document.body);
    WebLibSimple.setBGColor(dialog.topDiv, "#99000000");
    dialog.topDiv.onTouchClick = wld.onClickCancel;

    dialog.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 0, 0, null, dialog.topDiv);

    dialog.dialogDiv = WebLibSimple.createAnyAppend("div", dialog.centerDiv);
    WebLibSimple.setBGColor(dialog.dialogDiv, "#ffffff");
    dialog.dialogDiv.style.display = "inline-block";
    dialog.dialogDiv.style.padding = WebLibSimple.addPixel(20);
    dialog.dialogDiv.onTouchClick = WebLibDialog.onClickIgnore;

    dialog.titleDiv = WebLibSimple.createAnyAppend("div", dialog.dialogDiv);
    dialog.titleDiv.style.paddingBottom = WebLibSimple.addPixel(28);
    WebLibSimple.setFontSpecs(dialog.titleDiv, 28, "bold", "#444444");
    dialog.titleDiv.innerHTML = config.title;

    dialog.contentDiv = WebLibSimple.createAnyAppend("div", dialog.dialogDiv);
    dialog.contentDiv.style.paddingBottom = WebLibSimple.addPixel(20);
    if (config.content) dialog.contentDiv.appendChild(config.content);

    dialog.buttDiv = WebLibSimple.createAnyAppend("div", dialog.dialogDiv);
    WebLibSimple.setFontSpecs(dialog.buttDiv, 24, "bold", "#448844");
    dialog.buttDiv.style.textAlign = "right";

    dialog.cancelButton = WebLibSimple.createSpanPadded(10, 0, 50, 0, null, dialog.buttDiv);
    dialog.cancelButton.innerHTML = (config.cancel ? config.cancel : "Abbrechen").toUpperCase();
    dialog.cancelButton.onTouchClick = WebLibDialog.onClickCancel;

    if (config.other)
    {
        dialog.otherButton = WebLibSimple.createSpanPadded(25, 0, 0, 0, null, dialog.buttDiv);
        dialog.otherButton.innerHTML = config.other.toUpperCase();
        dialog.otherButton.style.color = (config.otherEnabled ? "#448844" : "#cccccc");
        dialog.otherButton.isenabled = (config.otherEnabled == true);
        dialog.otherButton.onTouchClick = WebLibDialog.onClickOther;
    }

    dialog.okButton = WebLibSimple.createSpanPadded(25, 0, 25, 0, null, dialog.buttDiv);
    dialog.okButton.innerHTML = (config.ok ? config.ok : "Ok").toUpperCase();
    dialog.okButton.style.color = (config.okEnabled ? "#448844" : "#cccccc");
    dialog.okButton.isenabled = (config.okEnabled == true);
    dialog.okButton.onTouchClick = WebLibDialog.onClickOk;

    //
    // Position dialog div ontop of the center div makes
    // it stay centered when orientation changes.
    //

    var dwid  = dialog.dialogDiv.clientWidth;
    var dhei  = dialog.dialogDiv.clientHeight;

    dialog.dialogDiv.style.position = "absolute";
    dialog.dialogDiv.style.left = WebLibSimple.addPixel(- (dwid >> 1));
    dialog.dialogDiv.style.top  = WebLibSimple.addPixel(- (dhei >> 1));

    WebLibSimple.findFocus(dialog.topDiv);

    WebAppRequest.requestBackKey("WebLibDialog.onBackKeyPressed");
}
