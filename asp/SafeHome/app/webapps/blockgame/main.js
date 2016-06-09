blockgame.createFrame = function()
{
    var xx = blockgame;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    xx.gamesize  = Math.min(wid, hei) - 100;
    xx.gamesize -= Math.floor(xx.gamesize % 6);
    xx.fieldsize = Math.floor(xx.gamesize / 6);

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffdddd");

    xx.audio = WebLibSimple.createAnyAppend("audio", null);
    xx.audio.src = "http://192.168.2.103/webapps/blockgame/sounds/finish_level_2.ogg";
    xx.audio.preload = "auto";
}

blockgame.readLevel = function(level)
{
    var xx = blockgame;

    var lstring = ((level < 10) ? "0" : "") + level;
    var url = "/games/level." + lstring + ".txt";
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

    console.log("=======>>>>>>>> xpos=" + xpos);
    console.log("=======>>>>>>>> ypos=" + ypos);

    target.myblock.pos = (ypos * 6) + xpos;

    xx.moveBlock(target.myblock);
    xx.solveGame(xx.game, xx.blocks);

    if (! xx.getHint())
    {
        setTimeout(blockgame.playGameEnd, 10);
    }

    event.preventDefault();
    return true;
}

blockgame.playGameEnd = function()
{
    blockgame.audio.load();
    blockgame.audio.play();

    setTimeout(blockgame.buildGame, 1000);
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

blockgame.getHint = function()
{
    var xx = blockgame;

    var movescnt = xx.solvepath.length;
    var nextmove = xx.solvepath.shift();

    for (var ccc in xx.blocks)
    {
        var block = xx.blocks[ ccc ];
        block.blockcen.innerHTML = "";

        if (ccc != nextmove.ccc) continue;

        block.blockcen.innerHTML = nextmove.way + " (" + movescnt + ")";

        return true;
    }

    return false;
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
        }
        else
        {
            WebLibSimple.setBGColor(block.blockdiv, "#cccccc");
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
    blockgame.getHint();
}

blockgame.createFrame();
blockgame.readLevel(41);
blockgame.readLevel(33);
blockgame.readLevel(6);
blockgame.buildGame();
