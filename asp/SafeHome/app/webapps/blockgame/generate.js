blockgame.createFrame = function()
{
    blockgame.topdiv = WebLibSimple.createAnyAppend("pre", document.body);
    blockgame.topdiv.style.fontSize = "24px";

    blockgame.tries = 0;
}

blockgame.printGame = function(gamekey, level)
{
    blockgame.topdiv.innerHTML += gamekey + "=" + level + "\n";
}

blockgame.printFinalGame = function(gamekey, level)
{
    var text = "";

    while (gamekey.length > 0)
    {
        text += gamekey.substring(0,6) + "\n";
        gamekey = gamekey.substring(6);
    }

    blockgame.topdiv.innerHTML += "\n" + text + "\n" + "level=" + level + "\n";
}

blockgame.printGamePath = function(gamekey)
{
    var text = "";

    while (gamekey)
    {
        var move = blockgame.knowns[ gamekey ];

        text += "move: " + move.level + "=" + move.ccc + ":" + move.way + "\n";

        if (move.from == 0) break;

        gamekey = blockgame.boards[ move.from ];
    }

    blockgame.topdiv.innerHTML += "\n" + text + "\n";
}

blockgame.fitBlock = function(game, block, pos)
{
    if (block.dir == 0)
    {
        var max = pos - (pos % 6) + 6;

        for (var inx = 0; inx < block.len; inx++)
        {
            var gix = pos + inx;

            if ((gix >= max) || (game[ gix ] != '_'))
            {
                return false;
            }
        }

        for (var inx = 0; inx < block.len; inx++)
        {
            var gix = pos + inx;

            game[ gix ] = block.ccc;
        }
    }
    else
    {
        for (var inx = 0; inx < block.len; inx++)
        {
            var gix = pos + (inx * 6);

            if ((gix >= 36) || (game[ gix ] != '_'))
            {
                return false;
            }
        }

        for (var inx = 0; inx < block.len; inx++)
        {
            var gix = pos + (inx * 6);

            game[ gix ] = block.ccc;
        }
    }

    return true;
}

blockgame.testGame = function()
{
    var blocks = {};
    var game = "_".repeat(36).split('');

    blocks[ '#' ] = { ccc: '#', len : 2, dir : 0 };
    game[ 15 ] = '#';
    game[ 16 ] = '#';

    blocks[ 'a' ] = { ccc: 'a', len : 3, dir : 1 };
    game[  0 ] = 'a';
    game[  6 ] = 'a';
    game[ 12 ] = 'a';

    blocks[ 'b' ] = { ccc: 'b', len : 2, dir : 0 };
    game[  1 ] = 'b';
    game[  2 ] = 'b';

    blocks[ 'c' ] = { ccc: 'c', len : 2, dir : 1 };
    game[  4 ] = 'c';
    game[ 10 ] = 'c';

    blocks[ 'd' ] = { ccc: 'd', len : 2, dir : 1 };
    game[  7 ] = 'd';
    game[ 13 ] = 'd';

    blocks[ 'e' ] = { ccc: 'e', len : 2, dir : 1 };
    game[  8 ] = 'e';
    game[ 14 ] = 'e';

    blocks[ 'f' ] = { ccc: 'f', len : 3, dir : 1 };
    game[ 11 ] = 'f';
    game[ 17 ] = 'f';
    game[ 23 ] = 'f';

    blocks[ 'g' ] = { ccc: 'g', len : 3, dir : 0 };
    game[ 18 ] = 'g';
    game[ 19 ] = 'g';
    game[ 20 ] = 'g';

    blocks[ 'h' ] = { ccc: 'h', len : 2, dir : 1 };
    game[ 21 ] = 'h';
    game[ 27 ] = 'h';

    blocks[ 'i' ] = { ccc: 'i', len : 2, dir : 1 };
    game[ 26 ] = 'i';
    game[ 32 ] = 'i';

    blocks[ 'j' ] = { ccc: 'j', len : 2, dir : 0 };
    game[ 28 ] = 'j';
    game[ 29 ] = 'j';

    blocks[ 'k' ] = { ccc: 'k', len : 2, dir : 0 };
    game[ 30 ] = 'k';
    game[ 31 ] = 'k';

    blocks[ 'l' ] = { ccc: 'l', len : 2, dir : 0 };
    game[ 33 ] = 'l';
    game[ 34 ] = 'l';

    blockgame.blocks  = blocks;
    blockgame.knowns  = {};
    blockgame.boards  = [];
    blockgame.solved  = false;
    blockgame.evalinx = 0;

    blockgame.storeGame(game, 0, 0);
}

blockgame.createGame = function()
{
    var blocks = {};

    blocks[ '#' ] = { ccc: '#', len : 2, dir : 0 };

    var game = "_".repeat(36).split('');

    game[ 12 ] = '#';
    game[ 13 ] = '#';

    //
    // Minimum number of free fields.
    //

    var minf = 7 + (Math.floor(Math.random() * 70) % 7);

    //
    // Random position translate array.
    //

    var rpos = [];

    for (var inx = 0; inx < 36; inx++) rpos[ inx ] = inx;

    for (var inx = 0; inx < 36; inx++)
    {
        var inx1 = Math.floor(Math.random() * 36);
        var inx2 = Math.floor(Math.random() * 36);

        var tmp = rpos[ inx1 ];
        rpos[ inx1 ] = rpos[ inx2 ];
        rpos[ inx2 ] = tmp;
    }

    //
    // Start building game.
    //

    var free = 36 - 2;

    for (var inx = 0; inx < 26; inx++)
    {
        var block = {};

        block.ccc = String.fromCharCode(97 + inx);
        block.len = (Math.random() <= 0.2) ? 3 : 2;
        block.dir = (Math.random() <= 0.5) ? 0 : 1

        for (var pos = 0; pos < 36; pos++)
        {
            if ((12 <= pos) && (pos <= 17) && (block.dir == 0))
            {
                //
                // Do not put horizontal blocks in line 3.
                //

                continue;
            }

            if (blockgame.fitBlock(game, block, rpos[ pos ]))
            {
                blocks[ block.ccc ] = block;
                free -= block.len;

                break;
            }
        }

        if (free <= minf) break;
    }

    blockgame.blocks  = blocks;
    blockgame.knowns  = {};
    blockgame.boards  = [];
    blockgame.solved  = false;
    blockgame.evalinx = 0;

    blockgame.storeGame(game, 0, 0);
}

blockgame.storeGame = function(game, from, level, ccc, way)
{
    if (blockgame.solved) return;

    var gamekey = game.join('');

    if (! blockgame.knowns[ gamekey ])
    {
        blockgame.knowns[ gamekey ] = { from: from, level: level, ccc: ccc, way: way };
        blockgame.boards.push(gamekey);

        //if ((blockgame.boards.length % 250) == 0) blockgame.printGame(gamekey, level)

        blockgame.solved = (game[ 16 ] == '#') && (game[ 17 ] == '#');
        blockgame.sollev = level;
    }
}

blockgame.evaluateThousend = function()
{
    for (count = 0; blockgame.evalinx < blockgame.boards.length; count++)
    {
        blockgame.doallMoves(blockgame.evalinx++);

        if (blockgame.solved) break;
        if (count >= 100) break;
    }

    if (blockgame.solved || (blockgame.evalinx == blockgame.boards.length))
    {
        if (blockgame.solved)
        {
            if (blockgame.sollev >= 20)
            {
                blockgame.topdiv.innerHTML += "\n";

                var gamekey = blockgame.boards[ 0 ];
                var level = blockgame.knowns[ gamekey ].level;
                blockgame.printFinalGame(gamekey, level);

                var gamekey = blockgame.boards[ blockgame.boards.length - 1 ];
                var level = blockgame.knowns[ gamekey ].level;
                blockgame.printFinalGame(gamekey, level);

                blockgame.topdiv.innerHTML += "solved...\n";

                blockgame.printGamePath(gamekey);

                return;
            }
        }

        blockgame.topdiv.innerHTML = ++blockgame.tries + "=" + blockgame.boards.length;

        blockgame.createGame();
        setTimeout(blockgame.evaluateThousend, 0)
    }
    else
    {
        setTimeout(blockgame.evaluateThousend, 0)
    }
}

blockgame.evaluateGame = function()
{
    setTimeout(blockgame.evaluateThousend, 0)
}

blockgame.moveLeft = function(game, from, level, pos, ccc, len, stp)
{
    if ((pos % 6) && (game[ pos - 1 ] == '_'))
    {
        game[ pos - 1 ] = ccc;
        game[ pos - 1 + len ] = '_';

        blockgame.storeGame(game, from, level, ccc, 'l' + stp);
        if (blockgame.solved) return;

        blockgame.moveLeft(game, from, level, pos - 1, ccc, len, stp + 1);

        game[ pos - 1 ] = '_';
        game[ pos - 1 + len ] = ccc;
    }
}

blockgame.moveRight = function(game, from, level, pos, ccc, len, stp)
{
    if ((((pos % 6) + len) < 6) && (game[ pos + len ] == '_'))
    {
        game[ pos ] = "_";
        game[ pos + len ] = ccc;

        blockgame.storeGame(game, from, level, ccc, 'r' + stp);
        if (blockgame.solved) return;

        blockgame.moveRight(game, from, level, pos + 1, ccc, len, stp + 1);

        game[ pos ] = ccc;
        game[ pos + len ] = '_';
    }
}

blockgame.moveUp = function(game, from, level, pos, ccc, len, stp)
{
    if ((pos >= 6) && (game[ pos - 6 ] == '_'))
    {
        game[ pos - 6  ] = ccc;
        game[ pos + 6 * (len - 1) ] = '_';

        blockgame.storeGame(game, from, level, ccc, 'u' + stp);
        if (blockgame.solved) return;

        blockgame.moveUp(game, from, level, pos - 6, ccc, len, stp + 1);

        game[ pos - 6  ] = '_';
        game[ pos + 6 * (len - 1) ] = ccc;
    }
}

blockgame.moveDown = function(game, from, level, pos, ccc, len, stp)
{
    if (((pos + (6 * len)) < 36) && (game[ pos + (6 * len) ] == '_'))
    {
        game[ pos ] = "_";
        game[ pos + (6 * len) ] = ccc;

        blockgame.storeGame(game, from, level, ccc, 'd' + stp);
        if (blockgame.solved) return;

        blockgame.moveDown(game, from, level, pos + 6, ccc, len, stp + 1);

        game[ pos ] = ccc;
        game[ pos + (6 * len) ] = "_";
    }
}

blockgame.doallMoves = function(from)
{
    var gamekey = blockgame.boards[ from ];
    var level = blockgame.knowns[ gamekey ].level + 1;
    var game = gamekey.split('');

    var have = {};

    for (var pos = 0; pos < 36; pos++)
    {
        var ccc = game[ pos ];

        if ((ccc == '_') || have[ ccc ]) continue;

        have[ ccc ] = true;

        var block = blockgame.blocks[ ccc ];
        var len = block.len;

        if (block.dir == 0)
        {
            blockgame.moveLeft(game, from, level, pos, ccc, len, 1);
            if (blockgame.solved) return;

            blockgame.moveRight(game, from, level, pos, ccc, len, 1);
            if (blockgame.solved) return;
        }
        else
        {
            blockgame.moveUp(game, from, level, pos, ccc, len, 1);
            if (blockgame.solved) return;

            blockgame.moveDown(game, from, level, pos, ccc, len, 1);
            if (blockgame.solved) return;
       }
    }
}

blockgame.createFrame();
//blockgame.testGame();
blockgame.createGame();
blockgame.evaluateGame();
