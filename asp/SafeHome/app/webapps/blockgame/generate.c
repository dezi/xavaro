#include <stdio.h>
#include <stdlib.h>

struct block {
    char ccc;
    int len;
    int dir;
};

struct block *blocks;

void createGame() {

    blocks['#'] = {ccc: '#', len : 2, dir : 0};

    var game = "_".repeat(36).split('');

    game[12] = '#';
    game[13] = '#';
}

/*

//
// Minimum number of free fields.
//

    var minf = 7 + (Math.floor(Math.random() * 70) % 7);

//
// Random position translate array.
//

    var
    rpos = [];

    for (var inx = 0; inx < 36; inx++) rpos[inx] = inx;

    for (var inx = 0; inx < 36; inx++) {
        var inx1 = Math.floor(Math.random() * 36);
        var inx2 = Math.floor(Math.random() * 36);

        var tmp = rpos[inx1];
        rpos[inx1] = rpos[inx2];
        rpos[inx2] = tmp;
    }

//
// Start building game.
//

    var free = 36 - 2;

    for (var inx = 0; inx < 26; inx++) {
        var block = {};

        block.ccc = String.fromCharCode(97 + inx);
        block.len = (Math.random() <= 0.2) ? 3 : 2;
        block.dir = (Math.random() <= 0.5) ? 0 : 1

        for (var pos = 0; pos < 36; pos++) {
            if ((12 <= pos) && (pos <= 17) && (block.dir == 0)) {
//
// Do not put horizontal blocks in line 3.
//

                continue;
            }

            if (blockgame.fitBlock(game, block, rpos[pos])) {
                blocks[block.ccc] = block;
                free -= block.len;

                break;
            }
        }

        if (free <= minf) break;
    }

    blockgame.blocks = blocks;
    blockgame.knowns = {};
    blockgame.boards = [];
    blockgame.solved = false;
    blockgame.evalinx = 0;

    blockgame.storeGame(game, 0, 0);
}
*/

int main() {
    printf("Hello world\n");

    blocks = malloc(sizeof(struct block *) * 256);

    for (int inx = 0; inx < 256; inx++) {
        blocks[inx] = malloc(sizeof(struct block));
    }

    return 0;
}