#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

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

int movesmax = 20000;
int movesact = 0;

struct move moves[ 20000 ];

char hashes[ 100000000 ];

int solved = 0;
int sollev = 0;
int freesp = 0;

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

int hashGame1(char *game)
{
    unsigned int hash = 0;

    for (int inx = 0; inx < 36; ++inx)
    {
        hash = game[ inx ] + (hash << 6) + (hash << 16) - hash;
    }

    return hash % sizeof(hashes);
}

void storeGame(char *game, int from, int level, int ccc, int way, int steps)
{
    if (solved) return;

    int hash = hashGame1(game);

    if (hashes[ hash ]) return;

    hashes[ hash ] = 1;

    if (movesact < movesmax)
    {
        memcpy(moves[ movesact ].game, game, 36);

        moves[ movesact ].from  = from;
        moves[ movesact ].level = level;
        moves[ movesact ].ccc   = ccc;
        moves[ movesact ].way   = way;
        moves[ movesact ].steps = steps;

        movesact++;

        solved = (game[ 16 ] == '#') && (game[ 17 ] == '#');
        sollev = level;
    }
}

void testGame()
{
    char game[ 36 ];

    memset(game, '_', 36);

    freesp = 36;

    blocks[ '#' ].len = 2;
    blocks[ '#' ].dir = 0;
    game[ 15 ] = '#';
    game[ 16 ] = '#';
    freesp -= 2;

    blocks[ 'a' ].len = 3;
    blocks[ 'a' ].dir = 1;
    game[  0 ] = 'a';
    game[  6 ] = 'a';
    game[ 12 ] = 'a';
    freesp -= 3;

    blocks[ 'b' ].len = 2;
    blocks[ 'b' ].dir = 0;
    game[  1 ] = 'b';
    game[  2 ] = 'b';
    freesp -= 2;

    blocks[ 'c' ].len = 2;
    blocks[ 'c' ].dir = 1;
    game[  4 ] = 'c';
    game[ 10 ] = 'c';
    freesp -= 2;

    blocks[ 'd' ].len = 2;
    blocks[ 'd' ].dir = 1;
    game[  7 ] = 'd';
    game[ 13 ] = 'd';
    freesp -= 2;

    blocks[ 'e' ].len = 2;
    blocks[ 'e' ].dir = 1;
    game[  8 ] = 'e';
    game[ 14 ] = 'e';
    freesp -= 2;

    blocks[ 'f' ].len = 3;
    blocks[ 'f' ].dir = 1;
    game[ 11 ] = 'f';
    game[ 17 ] = 'f';
    game[ 23 ] = 'f';
    freesp -= 3;

    blocks[ 'g' ].len = 3;
    blocks[ 'g' ].dir = 0;
    game[ 18 ] = 'g';
    game[ 19 ] = 'g';
    game[ 20 ] = 'g';
    freesp -= 3;

    blocks[ 'h' ].len = 2;
    blocks[ 'h' ].dir = 1;
    game[ 21 ] = 'h';
    game[ 27 ] = 'h';

    blocks[ 'i' ].len = 2;
    blocks[ 'i' ].dir = 1;
    game[ 26 ] = 'i';
    game[ 32 ] = 'i';
    freesp -= 2;

    blocks[ 'j' ].len = 2;
    blocks[ 'j' ].dir = 0;
    game[ 28 ] = 'j';
    game[ 29 ] = 'j';
    freesp -= 2;

    blocks[ 'k' ].len = 2;
    blocks[ 'k' ].dir = 0;
    game[ 30 ] = 'k';
    game[ 31 ] = 'k';
    freesp -= 2;

    blocks[ 'l' ].len = 2;
    blocks[ 'l' ].dir = 0;
    game[ 33 ] = 'l';
    game[ 34 ] = 'l';
    freesp -= 2;

    movesact = 0;

    solved = 0;
    sollev = 0;

    storeGame(game, 0, 0, 0, 0, 0);
}

void createGame()
{
    memset(hashes, 0, sizeof(hashes));

    char game[ 36 ];

    memset(game, '_', 36);

    blocks[ '#' ].len = 2;
    blocks[ '#' ].dir = 0;

    int off = ((random() % 100) <= 25) ? 0 :
              ((random() % 100) <= 33) ? 1 :
              ((random() % 100) <= 50) ? 2 : 3;

    game[ 12 + off ] = '#';
    game[ 13 + off ] = '#';

    //
    // Minimum number of free fields.
    //

    freesp = 6 + (random() % 10);

    //
    // Random position translate array.
    //

    int rpos[ 36 ];

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

    int next = 0;

    for (int inx = 0; inx < 26; inx++)
    {
        int ccc = 97 + next;
        int len = ((random() % 100) <= 20) ? 3 : 2;
        int dir = ((random() % 100) <= 50) ? 0 : 1;

        for (int pos = 0; pos < 36; pos++)
        {
            int ppp = rpos[ pos ];

            if ((dir == 0) && (12 <= ppp) && (ppp <= 17))
            {
                //
                // Do not put horizontal blocks in line 3.
                //

                continue;
            }

            if (fitBlock(game, ccc, len, dir, ppp))
            {
                blocks[ ccc ].len = len;
                blocks[ ccc ].dir = dir;

                free -= len;
                next++;

                break;
            }
        }

        if (free <= freesp) break;
    }

    movesact = 0;

    solved = 0;
    sollev = 0;

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
    int movesinx = 0;

    while (movesinx < movesact)
    {
        doallMoves(movesinx++);

        if (solved) break;
    }

    if (solved && (sollev > 1) && (sollev >= 23))
    {
        char file[ 256 ];

        snprintf(file, 128, "./games/level.%02d.txt", sollev);

        FILE *fd = fopen(file, "a");
        fprintf(fd,"%36s\n", moves[ 0 ].game);
        fclose(fd);

        printf("Moves=%05d act=%05d freesp=%02d %36s level=%d\n",
               movesact, movesinx, freesp, moves[ 0 ].game, sollev);
    }
}

int main()
{
    printf("Block game generator.\n");

    srandom(time(NULL));

    testGame();
    evaluateGame();

    while (1)
    {
        createGame();
        evaluateGame();
    }

    return 0;
}