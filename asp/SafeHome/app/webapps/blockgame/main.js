blockgame.createFrame = function()
{
    var xx = blockgame;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";
    xx.topdiv.style.backgroundImage = "url('walls/wood-seamless.jpg')";

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    xx.areasize  = Math.floor(Math.min(wid, hei) * 85 / 100);
    xx.areaPanel = WebLibSimple.createDivWidHei(
        -xx.areasize / 2, -xx.areasize / 2,
        xx.areasize, xx.areasize,
        "areaPanel", xx.centerDiv);

    xx.areaPanel.style.backgroundImage = "url('walls/brick-blue.jpg')";

    xx.gamesize   = xx.areasize - 40;
    xx.gamesize  -= Math.floor(xx.gamesize % 6);

    xx.bordersize = Math.floor((xx.areasize - xx.gamesize) / 2);
    xx.fieldsize  = Math.floor(xx.gamesize / 6);

    xx.exitDiv = WebLibSimple.createDivWidHei(
        xx.bordersize + (6 * xx.fieldsize),
        xx.bordersize + (2 * xx.fieldsize),
        xx.bordersize, xx.fieldsize,
        "exitDiv", xx.areaPanel);

    WebLibSimple.setBGColor(xx.exitDiv, "#ffffff");

    xx.gamePanel = WebLibSimple.createDivWidHei(
        xx.bordersize, xx.bordersize,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.areaPanel);

    xx.gamePanel.style.backgroundImage = "url('walls/green-wool.jpg')";

    xx.buttonTop1div = blockgame.createButton("–");
    xx.buttonTop2div = blockgame.createButton(" ");
    xx.buttonTop3div = blockgame.createButton("+");

    xx.buttonBot1div = blockgame.createButton("!");
    xx.buttonBot2div = blockgame.createButton(" ");
    xx.buttonBot3div = blockgame.createButton("?");

    xx.buttonTop1div.onTouchClick = blockgame.onButtonMinus;
    xx.buttonTop3div.onTouchClick = blockgame.onButtonPlus;
    xx.buttonBot1div.onTouchClick = blockgame.onLoadNewGame;
    xx.buttonBot3div.onTouchClick = blockgame.onHintPlayer;

    xx.audio = WebLibSimple.createAnyAppend("audio", null);
    xx.audio.src = "/webapps/blockgame/sounds/finish_level_2.wav";
    xx.audio.preload = "auto";

    xx.onWindowResize();

    addEventListener("resize", blockgame.onWindowResize);
}

blockgame.createButton = function(text)
{
    var xx = blockgame;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var butsiz = Math.floor((Math.max(wid, hei) - xx.gamesize) * 75 / 200);

    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, butsiz, butsiz, null, blockgame.topdiv);

    buttonImg = WebLibSimple.createAnyAppend("img", buttonDiv);
    buttonImg.style.width  = "100%";
    buttonImg.style.height = "100%";
    buttonImg.src = "black_button_256x256.png";

    buttonTab = WebLibSimple.createDivWidHei(0, 0, "100%", "100%", null, buttonDiv);
    buttonTab.style.display = "table";

    buttonCen = WebLibSimple.createAnyAppend("div", buttonTab);
    WebLibSimple.setFontSpecs(buttonCen, 60, "bold");
    buttonCen.style.display = "table-cell";
    buttonCen.style.width   = "100%";
    buttonCen.style.height  = "100%";
    buttonCen.style.color   = "#ffffff";
    buttonCen.style.verticalAlign = "middle";
    buttonCen.style.textAlign     = "center";
    buttonCen.innerHTML = text;

    buttonDiv.buttonCen = buttonCen;

    return buttonDiv;
}

blockgame.loadNewGameLevel = function()
{
    blockgame.newLevelTimeout = null;
    blockgame.readLevel(blockgame.level);
    blockgame.buildGame();
}

blockgame.onLoadNewGame = function(target, ctarget)
{
    WebAppUtility.makeClick();

    blockgame.buildGame();
}

blockgame.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    blockgame.getHint(true);
}

blockgame.onButtonMinus = function(target, ctarget)
{
    var xx = blockgame;

    WebAppUtility.makeClick();

    if (xx.level > 2) xx.buttonTop2div.buttonCen.innerHTML = --xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGameLevel, 1000);
}

blockgame.onButtonPlus = function(target, ctarget)
{
    var xx = blockgame;

    WebAppUtility.makeClick();

    if (xx.level < 43) xx.buttonTop2div.buttonCen.innerHTML = ++xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGameLevel, 1000);
}

blockgame.onWindowResize = function()
{
    var xx = blockgame;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var spacehorz = (wid - xx.gamesize) >> 1;
    var spacevert = (hei - xx.gamesize) >> 1;

    if (spacehorz < spacevert)
    {
        var spacevert = (spacevert - xx.buttonTop1div.clientHeight) >> 1;
        var spacemiddle = (wid >> 1) - (xx.buttonTop1div.clientWidth >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacemiddle + "px";
        xx.buttonTop2div.style.top    = spacevert + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = null;
        xx.buttonTop3div.style.top    = spacevert + "px";
        xx.buttonTop3div.style.right  = spacehorz + "px";
        xx.buttonTop3div.style.bottom = null;

        xx.buttonBot1div.style.left   = spacehorz + "px";
        xx.buttonBot1div.style.top    = null;
        xx.buttonBot1div.style.right  = null;
        xx.buttonBot1div.style.bottom = spacevert + "px";

        xx.buttonBot2div.style.left   = spacemiddle + "px";
        xx.buttonBot2div.style.top    = null;
        xx.buttonBot2div.style.right  = null;
        xx.buttonBot2div.style.bottom = spacevert + "px";

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
    else
    {
        var spacehorz = (spacehorz - xx.buttonTop1div.clientWidth) >> 1;
        var spacemiddle = (hei >> 1) - (xx.buttonTop1div.clientHeight >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacehorz + "px";
        xx.buttonTop2div.style.top    = spacemiddle + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = spacehorz + "px";
        xx.buttonTop3div.style.top    = null;
        xx.buttonTop3div.style.right  = null;
        xx.buttonTop3div.style.bottom = spacevert + "px";

        xx.buttonBot1div.style.left   = null;
        xx.buttonBot1div.style.top    = spacevert + "px";
        xx.buttonBot1div.style.right  = spacehorz + "px";
        xx.buttonBot1div.style.bottom = null;

        xx.buttonBot2div.style.left   = null;
        xx.buttonBot2div.style.top    = spacemiddle + "px";
        xx.buttonBot2div.style.right  = spacehorz + "px";
        xx.buttonBot2div.style.bottom = null;

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
}

blockgame.readLevel = function(level)
{
    var xx = blockgame;

    xx.level = level;
    xx.buttonTop2div.buttonCen.innerHTML = level;

    var lstring = ((level < 10) ? "0" : "") + level;
    var url = "games/level." + lstring + ".txt";
    var leveldata = WebAppRequest.loadSync(url);

    xx.games = leveldata.trim().split("\n");

    console.log("blockgame.readLevel: level=" + level + " games=" + xx.games.length);
}

blockgame.getTouchTarget = function(event)
{
    var touchobj = event.changedTouches[ 0 ];
    var target = touchobj.target;

    while (target)
    {
        if (target.myblock) return target;

        target = target.parentElement;
    }

    return null;
}

blockgame.onTouchStart = function(event)
{
    var xx = blockgame;

    var target = xx.getTouchTarget(event);
    if (! target) return false;
    var touchobj = event.changedTouches[ 0 ];

    xx.touch = {};

    xx.touch.startX = touchobj.clientX;
    xx.touch.startY = touchobj.clientY;

    xx.touch.clientX = touchobj.clientX;
    xx.touch.clientY = touchobj.clientY;

    var block = target.myblock;

    xx.touch.minX = block.left;
    xx.touch.maxX = block.left;
    xx.touch.minY = block.top;
    xx.touch.maxY = block.top;

    if (block.dir == 0)
    {
        var pos = block.pos;

        while (((pos % 6) > 0) && (xx.game[ pos - 1 ] == '_'))
        {
            xx.touch.minX -= xx.fieldsize;
            pos--;
        }

        var pos = block.pos;
        var len = block.len;

        while ((((pos % 6) + len) < 6) && (xx.game[ pos + len ] == '_'))
        {
            xx.touch.maxX += xx.fieldsize;
            pos++;
        }

        if ((((pos % 6) + len) == 6) && (13 <= block.pos) && (block.pos <= 18))
        {
            xx.touch.maxX += xx.fieldsize * 3;
        }
    }
    else
    {
        var pos = block.pos;

        while (((pos - 6) >= 0) && (xx.game[ pos - 6 ] == '_'))
        {
            xx.touch.minY -= xx.fieldsize;
            pos -= 6;
        }

        var pos = block.pos;
        var len = (block.len - 1) * 6;

        while (((pos + len + 6 ) < 36) && (xx.game[ pos + len + 6 ] == '_'))
        {
            xx.touch.maxY += xx.fieldsize;
            pos += 6;
        }
    }

    event.preventDefault();
    return true;
}

blockgame.onTouchMove = function(event)
{
    var xx = blockgame;

    var target = xx.getTouchTarget(event);
    if (! target) return false;
    var touchobj = event.changedTouches[ 0 ];

    xx.touch.clientX = touchobj.clientX;
    xx.touch.clientY = touchobj.clientY;

    xx.touch.newX = target.myblock.left + (xx.touch.clientX - xx.touch.startX);
    xx.touch.newY = target.myblock.top  + (xx.touch.clientY - xx.touch.startY);

    if (target.myblock.dir == 0)
    {
        if (xx.touch.newX <= xx.touch.minX)
        {
            xx.touch.newX = xx.touch.minX;

            if (xx.touch.newX != target.offsetLeft) WebAppUtility.makeClick();
        }

        if (xx.touch.newX >= xx.touch.maxX)
        {
            xx.touch.newX = xx.touch.maxX;

            if (xx.touch.newX != target.offsetLeft) WebAppUtility.makeClick();
        }

        target.style.left = xx.touch.newX + "px";
    }
    else
    {
        if (xx.touch.newY <= xx.touch.minY)
        {
            xx.touch.newY = xx.touch.minY;
            if (xx.touch.newY != target.offsetTop) WebAppUtility.makeClick();
        }

        if (xx.touch.newY >= xx.touch.maxY)
        {
            xx.touch.newY = xx.touch.maxY;
            if (xx.touch.newY != target.offsetTop) WebAppUtility.makeClick();
        }

        target.style.top  = xx.touch.newY + "px";
    }

    event.preventDefault();
    return true;
}

blockgame.onTouchEnd = function(event)
{
    var xx = blockgame;

    var target = xx.getTouchTarget(event);
    if (! target) return false;
    var touchobj = event.changedTouches[ 0 ];

    if (target.myblock.dir == 0)
    {
        var restX = xx.touch.newX % xx.fieldsize;

        if (restX)
        {
            if (restX > (xx.fieldsize >> 1))
            {
                xx.touch.newX += xx.fieldsize - restX;
            }
            else
            {
                xx.touch.newX -= restX;
            }

            target.style.left = xx.touch.newX + "px";
            WebAppUtility.makeClick();
        }

        target.myblock.left = xx.touch.newX;
    }
    else
    {
        var restY = xx.touch.newY % xx.fieldsize;

        if (restY)
        {
            if (restY > (xx.fieldsize >> 1))
            {
                xx.touch.newY += xx.fieldsize - restY;
            }
            else
            {
                xx.touch.newY -= restY;
            }

            target.style.top = xx.touch.newY + "px";
            WebAppUtility.makeClick();
        }

        target.myblock.top = xx.touch.newY;
    }

    var xpos = Math.floor(target.myblock.left / xx.fieldsize)
    var ypos = Math.floor(target.myblock.top  / xx.fieldsize);

    if ((target.myblock.dir == 0) && (xpos + target.myblock.len) >= 6)
    {
        //
        // Blue block in exit.
        //

        xpos = 6 - target.myblock.len;
    }

    console.log("=======>>>>>>>> xpos=" + xpos);
    console.log("=======>>>>>>>> ypos=" + ypos);

    target.myblock.pos = (ypos * 6) + xpos;

    xx.moveBlock(target.myblock);
    xx.clearHint();

    setTimeout(blockgame.solveAndHint, 0);

    event.preventDefault();
    return true;
}

blockgame.solveAndHint = function()
{
    var xx = blockgame;

    xx.solveGame(xx.game, xx.blocks);
    xx.getHint(false);

    if (xx.solvepath.length == 0)
    {
        blockgame.audio.play();

        setTimeout(blockgame.buildGame, 1000);
        setTimeout(blockgame.audio.load, 2000);
    }
}

blockgame.moveBlock = function(block)
{
    var xx = blockgame;

    var game = xx.game.split('');

    for (var pos = 0; pos < 36; pos++)
    {
        if (game[ pos ] == block.ccc) game[ pos ] = '_';
    }

    var pos = block.pos;
    var len = block.len;

    if (block.dir == 0)
    {
        while (len-- > 0)
        {
            game[ pos ] = block.ccc;
            pos += 1;
        }
    }
    else
    {
        while (len-- > 0)
        {
            game[ pos ] = block.ccc;
            pos += 6;
        }
    }

    xx.game = game.join('');
}

blockgame.clearHint = function()
{
    var xx = blockgame;

    for (var ccc in xx.blocks)
    {
        var block = xx.blocks[ ccc ];
        block.blockcen.innerHTML = "";
    }
}

blockgame.getHint = function(display)
{
    var xx = blockgame;

    if (display)
    {
        var nextmove = xx.solvepath[ 0 ];

        for (var ccc in xx.blocks)
        {
            var block = xx.blocks[ ccc ];

            if (nextmove && (ccc == nextmove.ccc))
            {
                block.blockcen.innerHTML = nextmove.way;
            }
        }
    }

    xx.buttonBot2div.buttonCen.innerHTML = xx.solvepath.length;
}

blockgame.buildGame = function()
{
    var xx = blockgame;

    xx.gamePanel.innerHTML = null;

    //
    // Random select a game.
    //

    var rnd = Math.floor(Math.random() * xx.games.length);
    xx.game = xx.games[ rnd ];

    //
    // Derive blocks from game.
    //

    console.log("blockgame.buildGame: game=" + xx.game);

    xx.blocks = {};

    for (var pos = 0; pos < 36; pos++)
    {
        var ccc = xx.game[ pos ];

        if ((ccc == "_") || xx.blocks[ ccc ]) continue;

        var block = {};

        block.ccc = ccc;
        block.pos = pos;
        block.dir = (xx.game[ pos + 1 ] == ccc) ? 0 : 1;

        if (block.dir == 0)
        {
            block.len = (xx.game[ pos + 2 ] == ccc) ? 3 : 2;
        }
        else
        {
            block.len = (xx.game[ pos + 12 ] == ccc) ? 3 : 2;
        }

        xx.blocks[ ccc ] = block;
    }

    //console.log("blockgame.buildGame: " + JSON.stringify(xx.blocks));

    //
    // Draw all blocks.
    //

    for (var ccc in xx.blocks)
    {
        var block = xx.blocks[ ccc ];

        console.log("blockgame.buildGame: " + JSON.stringify(block));

        block.left = Math.floor(block.pos % 6) * xx.fieldsize;
        block.top  = Math.floor(block.pos / 6) * xx.fieldsize;

        block.wid = xx.fieldsize * ((block.dir == 0) ? block.len : 1) - 4;
        block.hei = xx.fieldsize * ((block.dir == 1) ? block.len : 1) - 4;

        block.blockdiv = WebLibSimple.createDivWidHei(
            block.left, block.top,
            block.wid, block.hei,
            null, xx.gamePanel);

        block.blockdiv.style.border = "2px solid grey";
        block.blockdiv.style.borderRadius = "12px";

        if (block.ccc == '#')
        {
            WebLibSimple.setBGColor(block.blockdiv, "#8888ff");
            block.blockdiv.style.backgroundImage = "url('walls/brick-blue.jpg')";
        }
        else
        {
            WebLibSimple.setBGColor(block.blockdiv, "#cccccc");
            block.blockdiv.style.backgroundImage = "url('walls/brick-grey.jpg')";
        }

        block.blockdiv.scrollBoth = true;
        block.blockdiv.onTouchStart = blockgame.onTouchStart;
        block.blockdiv.onTouchMove = blockgame.onTouchMove;
        block.blockdiv.onTouchEnd = blockgame.onTouchEnd;

        block.blockdiv.myblock = block;

        block.blocktab = WebLibSimple.createAnyAppend("div", block.blockdiv);
        block.blocktab.style.display = "table";
        block.blocktab.style.width   = "100%";
        block.blocktab.style.height  = "100%";

        block.blockcen = WebLibSimple.createAnyAppend("div", block.blocktab);
        block.blockcen.style.display = "table-cell";
        block.blockcen.style.width   = "100%";
        block.blockcen.style.height  = "100%";
        block.blockcen.style.verticalAlign = "middle";
        block.blockcen.style.textAlign = "center";
    }

    blockgame.solveGame(xx.game, xx.blocks);
    blockgame.getHint(false);
}

blockgame.createFrame();
blockgame.readLevel(2);
blockgame.buildGame();
