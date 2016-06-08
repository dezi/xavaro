#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct block
{
    int ccc;
    int len;
    int dir;
};

struct block blocks[ 256 ];

int fitBlock(char *game, int ccc, int len, int dir, int pos)
{
    if (dir == 0)
    {
        int max = pos - (pos % 6) + 6;

        for (int inx = 0; inx < len; inx++)
        {
            int gix = pos + inx;

            if ((gix >= max) || (game[ gix ] != '_'))
            {
                return 0;
            }
        }

        for (int inx = 0; inx < len; inx++)
        {
            int gix = pos + inx;

            game[ gix ] = ccc;
        }
    }
    else
    {
        for (int inx = 0; inx < len; inx++)
        {
            int gix = pos + (inx * 6);

            if ((gix >= 36) || (game[ gix ] != '_'))
            {
                return 0;
            }
        }

        for (int inx = 0; inx < len; inx++)
        {
            int gix = pos + (inx * 6);

            game[ gix ] = ccc;
        }
    }

    return 1;
}

void createGame()
{
    blocks[ '#' ].ccc = '#';
    blocks[ '#' ].len = 2;
    blocks[ '#' ].dir = 0;

    char game[ 37 ];

    memset(game,   0, 37);
    memset(game, '_', 36);

    game[ 12 ] = '#';
    game[ 13 ] = '#';

    //
    // Minimum number of free fields.
    //

    int minf = 7 + (random() % 7);

    //
    // Random position translate array.
    //

    int rpos[36];

    for (int inx = 0; inx < 36; inx++) rpos[ inx ] = inx;

    for (int inx = 0; inx < 36; inx++)
    {
        int inx1 = random() % 36;
        int inx2 = random() % 36;

        int tmp = rpos[ inx1 ];
        rpos[ inx1 ] = rpos[ inx2 ];
        rpos[ inx2 ] = tmp;
    }

    //
    // Start building game.
    //

    int free = 36 - 2;

    for (int inx = 0; inx < 26; inx++)
    {
        int ccc = 97 + inx;
        int len = ((random() % 100) <= 20) ? 3 : 2;
        int dir = ((random() % 100) <= 50) ? 0 : 1;

        for (int pos = 0; pos < 36; pos++)
        {
            if ((12 <= pos) && (pos <= 17) && (dir == 0))
            {
                //
                // Do not put horizontal blocks in line 3.
                //

                continue;
            }

            if (fitBlock(game, ccc, len, dir, rpos[ pos ]))
            {
                blocks[ ccc ].ccc = ccc;
                blocks[ ccc ].len = len;
                blocks[ ccc ].dir = dir;

                free -= len;

                break;
            }
        }

        if (free <= minf) break;
    }

    printf("Game: %s\n", game);
}


/*
    blockgame.blocks = blocks;
    blockgame.knowns = {};
    blockgame.boards = [];
    blockgame.solved = false;
    blockgame.evalinx = 0;

    blockgame.storeGame(game, 0, 0);
}
*/

int main()
{
    printf("Hello world\n");

    createGame();

    return 0;
}