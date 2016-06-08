#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct block
{
    int len;
    int dir;
};

struct move
{
    char game[ 36 ];

    int from;
    int level;
    int ccc;
    int way;
    int steps;
};

struct block blocks[ 256 ];

int movesmax = 10000;
int movesact = 0;
int moveseva = 0;

struct move moves[ 10000 ];

int solved = 0;
int sollev = 0;

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

void storeGame(char *game, int from, int level, int ccc, int way, int steps)
{
    if (solved) return;

    int fund = 0;

    for (int inx = 0; inx < movesact; inx++)
    {
        if (strncmp(moves[ inx ].game, game, 36) == 0)
        {
            return;
        }
    }

    memcpy(moves[ movesact ].game, game, 36);

    moves[ movesact ].from  = from;
    moves[ movesact ].level = level;
    moves[ movesact ].ccc   = ccc;
    moves[ movesact ].way   = way;
    moves[ movesact ].steps = steps;

    movesact++;
}

void createGame()
{
    blocks[ '#' ].len = 2;
    blocks[ '#' ].dir = 0;

    char game[ 36 ];

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
                blocks[ ccc ].len = len;
                blocks[ ccc ].dir = dir;

                free -= len;

                break;
            }
        }

        if (free <= minf) break;
    }

    printf("Game: %36s\n", game);

    movesact = 0;

    solved = 0;

    storeGame(game, 0, 0, 0, 0, 0);
}

void moveLeft(char *game, int from, int level, int pos, int ccc, int len, int stp)
{
    if ((pos % 6) && (game[ pos - 1 ] == '_'))
    {
        game[ pos - 1 ] = ccc;
        game[ pos - 1 + len ] = '_';

        storeGame(game, from, level, ccc, 'l', stp);
        if (solved) return;

        moveLeft(game, from, level, pos - 1, ccc, len, stp + 1);

        game[ pos - 1 ] = '_';
        game[ pos - 1 + len ] = ccc;
    }
}

void moveRight(char *game, int from, int level, int pos, int ccc, int len, int stp)
{
    if ((((pos % 6) + len) < 6) && (game[ pos + len ] == '_'))
    {
        game[ pos ] = '_';
        game[ pos + len ] = ccc;

        storeGame(game, from, level, ccc, 'r', stp);
        if (solved) return;

        moveRight(game, from, level, pos + 1, ccc, len, stp + 1);

        game[ pos ] = ccc;
        game[ pos + len ] = '_';
    }
}

void moveUp(char *game, int from, int level, int pos, int ccc, int len, int stp)
{
    if ((pos >= 6) && (game[ pos - 6 ] == '_'))
    {
        game[ pos - 6  ] = ccc;
        game[ pos + 6 * (len - 1) ] = '_';

        storeGame(game, from, level, ccc, 'u', stp);
        if (solved) return;

        moveUp(game, from, level, pos - 6, ccc, len, stp + 1);

        game[ pos - 6  ] = '_';
        game[ pos + 6 * (len - 1) ] = ccc;
    }
}

void moveDown(char *game, int from, int level, int pos, int ccc, int len, int stp)
{
    if (((pos + (6 * len)) < 36) && (game[ pos + (6 * len) ] == '_'))
    {
        game[ pos ] = '_';
        game[ pos + (6 * len) ] = ccc;

        storeGame(game, from, level, ccc, 'd', stp);
        if (solved) return;

        moveDown(game, from, level, pos + 6, ccc, len, stp + 1);

        game[ pos ] = ccc;
        game[ pos + (6 * len) ] = '_';
    }
}

void doallMoves(from)
{
    char game[ 36 ];
    memcpy(game, moves[ from ].game, 36);

    int level = moves[ from ].level + 1;

    char have[ 256 ];
    memset(have, 0, 256);

    for (int pos = 0; pos < 36; pos++)
    {
        int ccc = game[ pos ];

        if ((ccc == '_') || have[ ccc ]) continue;

        have[ ccc ] = 1;

        int len = blocks[ ccc ].len;
        int dir = blocks[ ccc ].dir;

        if (dir == 0)
        {
            moveLeft(game, from, level, pos, ccc, len, 1);
            if (solved) return;

            moveRight(game, from, level, pos, ccc, len, 1);
            if (solved) return;
        }
        else
        {
            moveUp(game, from, level, pos, ccc, len, 1);
            if (solved) return;

            moveDown(game, from, level, pos, ccc, len, 1);
            if (solved) return;
        }
    }
}

void evaluateGame()
{
    for (int moveinx = 0; moveinx < movesact; moveinx++)
    {
        doallMoves(moveinx);

        if (solved) break;
    }

    printf("Fettig %d", movesact);
}

int main()
{
    printf("Block game generator.\n");

    createGame();
    evaluateGame();

    return 0;
}