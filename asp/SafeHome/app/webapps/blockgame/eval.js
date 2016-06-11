blockgame.storeGame = function(game, from, level, ccc, way)
{
    if (blockgame.solved) return;

    var gamekey = game.join('');

    if (! blockgame.knowns[ gamekey ])
    {
        blockgame.knowns[ gamekey ] = { from: from, level: level, ccc: ccc, way: way };
        blockgame.boards.push(gamekey);

        blockgame.solved = (game[ 16 ] == '#') && (game[ 17 ] == '#');
        blockgame.sollev = level;
    }
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

blockgame.printGamePath = function(gamekey)
{
    blockgame.solvepath = [];

    if (blockgame.boards.length > 1)
    {
        while (gamekey)
        {
            var move = blockgame.knowns[ gamekey ];

            blockgame.solvepath.unshift(move);

            gamekey = blockgame.boards[ move.from ];

            if (move.from == 0) break;
        }

        for (var inx in blockgame.solvepath)
        {
            var move = blockgame.solvepath[ inx ];
            console.log("move: " + move.level + "=" + move.ccc + ":" + move.way);
        }
    }
}

blockgame.solveGame = function(game, blocks)
{
    blockgame.blocks  = blocks;
    blockgame.knowns  = {};
    blockgame.boards  = [];
    blockgame.solved  = false;
    blockgame.evalinx = 0;

    blockgame.storeGame(game.split(''), 0, 0);

    if (! blockgame.solved)
    {
        for (count = 0; blockgame.evalinx < blockgame.boards.length; count++)
        {
            blockgame.doallMoves(blockgame.evalinx++);

            if (blockgame.solved) break;
        }
    }

    if (blockgame.solved)
    {
        var gamekey = blockgame.boards[ blockgame.boards.length - 1 ];

        blockgame.printGamePath(gamekey);
    }

    blockgame.knowns = null;
    blockgame.boards = null;
}
