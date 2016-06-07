blockgame.createFrame = function()
{
    var xx = blockgame;

    xx.topdiv = WebLibSimple.createAnyAppend("pre", document.body);
    xx.topdiv.style.fontSize = "20px";
}

blockgame.printGame = function(gamekey, level)
{
    blockgame.topdiv.innerHTML += gamekey + "=" + level + "\n";
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

blockgame.createGame = function()
{
    var blocks = {};

    blocks[ '#' ] = { ccc: '#', len : 2, dir : 0 };

    var game = "_".repeat(36).split('');

    game[ 16 ] = '#';
    game[ 17 ] = '#';

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
            if (blockgame.fitBlock(game, block, rpos[ pos ]))
            {
                blocks[ block.ccc ] = block;
                free -= block.len;

                break;
            }
        }

        if (free <= minf) break;
    }

    blockgame.blocks = blocks;

    blockgame.knowns = {};
    blockgame.boards = [];

    blockgame.storeGame(game, 0);
}

blockgame.storeGame = function(game, level)
{
    var gamekey = game.join('');

    if (! blockgame.knowns[ gamekey ])
    {
        blockgame.knowns[ gamekey ] = { ginx: blockgame.boards.length, level: level };
        blockgame.boards.push(gamekey);

        blockgame.printGame(gamekey, level)
    }
}

blockgame.evaluateGame = function()
{
    for (var ginx = 0; ginx < blockgame.boards.length; ginx++)
    {
        blockgame.doallMoves(ginx);

        if (ginx > 1000) break;
    }
}

blockgame.doallMoves = function(ginx)
{
    var gamekey = blockgame.boards[ ginx ];
    var level = blockgame.knowns[ gamekey ].level + 1;
    var game = gamekey.split('');

    var have = {};

    for (var pos = 0; pos < 36; pos++)
    {
        var ccc = game[ pos ];

        if ((ccc == '_') || have[ ccc ]) continue;

        have[ ccc ] = true;

        var block = blockgame.blocks[ ccc ];

        if (block.dir == 0)
        {
            //
            // Move left.
            //

            if ((pos % 6) && (game[ pos - 1 ] == '_'))
            {
                game[ pos - 1 ] = ccc;
                game[ pos - 1 + block.len ] = '_';

                blockgame.storeGame(game, level);

                game[ pos - 1 ] = '_';
                game[ pos - 1 + block.len ] = ccc;
            }

            //
            // Move right.
            //

            if ((((pos % 6) + block.len) < 6) && (game[ pos + block.len ] == '_'))
            {
                game[ pos ] = "_";
                game[ pos + block.len ] = ccc;

                blockgame.storeGame(game, level);

                game[ pos ] = ccc;
                game[ pos + block.len ] = '_';
            }
        }
        else
        {
            //
            // Move up.
            //

            if ((pos >= 6) && (game[ pos - 6 ] == '_'))
            {
                game[ pos - 6  ] = ccc;
                game[ pos + 6 * (block.len - 1) ] = '_';

                blockgame.storeGame(game, level);

                game[ pos - 6  ] = '_';
                game[ pos + 6 * (block.len - 1) ] = ccc;
            }

             //
             // Move down.
             //

             if (((pos + (6 * block.len)) < 36) && (game[ pos + (6 * block.len) ] == '_'))
             {
                 game[ pos ] = "_";
                 game[ pos + (6 * block.len) ] = ccc;

                 blockgame.storeGame(game, level);

                 game[ pos ] = ccc;
                 game[ pos + (6 * block.len) ] = "_";
             }
       }
    }
}

blockgame.createFrame();
blockgame.createGame();
blockgame.evaluateGame();
