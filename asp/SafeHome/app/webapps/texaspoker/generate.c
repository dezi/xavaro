#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

//
// 12 = Ass
// 11 = König
// 10 = Dame
//  9 = Bube
//  8 = 10
//  7 = 9
//  6 = 8
//  5 = 7
//  4 = 6
//  3 = 5
//  2 = 4
//  1 = 3
//  0 = 2
//

char card2val[ 52 ];
char card2col[ 52 ];

char deck  [ 52 ];
char vals  [ 13 ];
char cols  [  4 ];

int  scores[ 10 ];

int c1, c2, c3, c4, c5, c6, c7;

int scoreSeven()
{
    //
    // Royal flush.
    //

    if ((deck[ 51 ] && deck[ 47 ] && deck[ 43 ] && deck[ 39 ] && deck[ 35 ]) ||
        (deck[ 50 ] && deck[ 46 ] && deck[ 42 ] && deck[ 38 ] && deck[ 34 ]) ||
        (deck[ 49 ] && deck[ 45 ] && deck[ 41 ] && deck[ 37 ] && deck[ 33 ]) ||
        (deck[ 48 ] && deck[ 44 ] && deck[ 40 ] && deck[ 36 ] && deck[ 32 ]))
    {
        return 9;
    }

    //
    // Straight flush.
    //

    if ((deck[ 47 ] && deck[ 43 ] && deck[ 39 ] && deck[ 35 ] && deck[ 31 ]) ||
        (deck[ 46 ] && deck[ 42 ] && deck[ 38 ] && deck[ 34 ] && deck[ 30 ]) ||
        (deck[ 45 ] && deck[ 41 ] && deck[ 37 ] && deck[ 33 ] && deck[ 29 ]) ||
        (deck[ 44 ] && deck[ 40 ] && deck[ 36 ] && deck[ 32 ] && deck[ 28 ]) ||
        (deck[ 43 ] && deck[ 39 ] && deck[ 35 ] && deck[ 31 ] && deck[ 27 ]) ||
        (deck[ 42 ] && deck[ 38 ] && deck[ 34 ] && deck[ 30 ] && deck[ 26 ]) ||
        (deck[ 41 ] && deck[ 37 ] && deck[ 33 ] && deck[ 29 ] && deck[ 25 ]) ||
        (deck[ 40 ] && deck[ 36 ] && deck[ 32 ] && deck[ 28 ] && deck[ 24 ]) ||
        (deck[ 39 ] && deck[ 35 ] && deck[ 31 ] && deck[ 27 ] && deck[ 23 ]) ||
        (deck[ 38 ] && deck[ 34 ] && deck[ 30 ] && deck[ 26 ] && deck[ 22 ]) ||
        (deck[ 37 ] && deck[ 33 ] && deck[ 29 ] && deck[ 25 ] && deck[ 21 ]) ||
        (deck[ 36 ] && deck[ 32 ] && deck[ 28 ] && deck[ 24 ] && deck[ 20 ]) ||
        (deck[ 35 ] && deck[ 31 ] && deck[ 27 ] && deck[ 23 ] && deck[ 19 ]) ||
        (deck[ 34 ] && deck[ 30 ] && deck[ 26 ] && deck[ 22 ] && deck[ 18 ]) ||
        (deck[ 33 ] && deck[ 29 ] && deck[ 25 ] && deck[ 21 ] && deck[ 17 ]) ||
        (deck[ 32 ] && deck[ 28 ] && deck[ 24 ] && deck[ 20 ] && deck[ 16 ]) ||
        (deck[ 31 ] && deck[ 27 ] && deck[ 23 ] && deck[ 19 ] && deck[ 15 ]) ||
        (deck[ 30 ] && deck[ 26 ] && deck[ 22 ] && deck[ 18 ] && deck[ 14 ]) ||
        (deck[ 29 ] && deck[ 25 ] && deck[ 21 ] && deck[ 17 ] && deck[ 13 ]) ||
        (deck[ 28 ] && deck[ 24 ] && deck[ 20 ] && deck[ 16 ] && deck[ 12 ]) ||
        (deck[ 27 ] && deck[ 23 ] && deck[ 19 ] && deck[ 15 ] && deck[ 11 ]) ||
        (deck[ 26 ] && deck[ 22 ] && deck[ 18 ] && deck[ 14 ] && deck[ 10 ]) ||
        (deck[ 25 ] && deck[ 21 ] && deck[ 17 ] && deck[ 13 ] && deck[  9 ]) ||
        (deck[ 24 ] && deck[ 20 ] && deck[ 16 ] && deck[ 12 ] && deck[  8 ]) ||
        (deck[ 23 ] && deck[ 19 ] && deck[ 15 ] && deck[ 11 ] && deck[  7 ]) ||
        (deck[ 22 ] && deck[ 18 ] && deck[ 14 ] && deck[ 10 ] && deck[  6 ]) ||
        (deck[ 21 ] && deck[ 17 ] && deck[ 13 ] && deck[  9 ] && deck[  5 ]) ||
        (deck[ 20 ] && deck[ 16 ] && deck[ 12 ] && deck[  8 ] && deck[  4 ]) ||
        (deck[ 19 ] && deck[ 15 ] && deck[ 11 ] && deck[  7 ] && deck[  3 ]) ||
        (deck[ 18 ] && deck[ 14 ] && deck[ 10 ] && deck[  6 ] && deck[  2 ]) ||
        (deck[ 17 ] && deck[ 13 ] && deck[  9 ] && deck[  5 ] && deck[  1 ]) ||
        (deck[ 16 ] && deck[ 12 ] && deck[  8 ] && deck[  4 ] && deck[  0 ]))
    {
        return 8;
    }

    //
    // Compute same cards values.
    //

    int twos = 0;
    int threes = 0;
    int fours = 0;

    int fourval1 = -1;

    int threeval1 = -1;
    int threeval2 = -1;

    int twoval1 = -1;
    int twoval2 = -1;
    int twoval3 = -1;

    for (int inx = 0; inx < 13; inx++)
    {
        if (vals[ inx ] == 4)
        {
            if (fourval1 < 0) fourval1 = inx;

            fours++;
        }
        else
        {
            if (vals[ inx ] == 3)
            {
                if (threeval1 < 0) threeval1 = inx;
                if (threeval2 < 0) threeval2 = inx;

                threes++;
            }
            else
            {
                if (vals[ inx ] == 2)
                {
                    if (twoval1 < 0) twoval1 = inx;
                    if (twoval2 < 0) twoval2 = inx;
                    if (twoval3 < 0) twoval3 = inx;

                    twos++;
                }
            }
        }
    }

    //
    // Four of a kind.
    //

    if (fours == 1)
    {
        return 7;
    }

    //
    // Full house.
    //

    if ((threes == 2) || ((threes == 1) && (twos >= 1)))
    {
        return 6;
    }

    //
    // Flush.
    //

    if ((cols[ 0 ] >= 5) || (cols[ 1 ] >= 5) || (cols[ 2 ] >= 5) || (cols[ 3 ] >= 5))
    {
        return 5;
    }

    //
    // Straight.
    //

    if ((vals[ 12 ] && vals[ 11 ] && vals[ 10 ] && vals[  9 ] && vals[  8 ]) ||
        (vals[ 11 ] && vals[ 10 ] && vals[  9 ] && vals[  8 ] && vals[  7 ]) ||
        (vals[ 10 ] && vals[  9 ] && vals[  8 ] && vals[  7 ] && vals[  6 ]) ||
        (vals[  9 ] && vals[  8 ] && vals[  7 ] && vals[  6 ] && vals[  5 ]) ||
        (vals[  8 ] && vals[  7 ] && vals[  6 ] && vals[  5 ] && vals[  4 ]) ||
        (vals[  7 ] && vals[  6 ] && vals[  5 ] && vals[  4 ] && vals[  3 ]) ||
        (vals[  6 ] && vals[  5 ] && vals[  4 ] && vals[  3 ] && vals[  2 ]) ||
        (vals[  5 ] && vals[  4 ] && vals[  3 ] && vals[  2 ] && vals[  1 ]) ||
        (vals[  4 ] && vals[  3 ] && vals[  2 ] && vals[  1 ] && vals[  0 ]))
    {
        return 4;
    }

    //
    // Three of a kind.
    //

    if (threes >= 1)
    {
        return 3;
    }

    //
    // Two pairs.
    //

    if (twos >= 2)
    {
        return 2;
    }

    //
    // Two of a kind.
    //

    if (twos >= 1)
    {
        return 1;
    }

    return 0;
}

void evalgame()
{
    for (int inx = 0; inx < 52; inx++)
    {
        card2val[ inx ] = inx / 4;
        card2col[ inx ] = inx % 4;
    }

    for (int inx = 0; inx < 10; inx++)
    {
        scores[ inx ] = 0;
    }

    for (int inx = 0; inx < 52; inx++)
    {
        deck[ inx ] = 0;
    }

    for (int inx = 0; inx < 13; inx++)
    {
        vals[ inx ] = 0;
    }

    for (int inx = 0; inx < 4; inx++)
    {
        cols[ inx ] = 0;
    }

    int score = 0;
    int total = 0;

    for (c1 = 51; c1 >= 0; c1--)
    {
        deck[ c1 ] = 1;

        vals[ card2val[ c1 ] ]++;
        cols[ card2col[ c1 ] ]++;

        for (c2 = c1 - 1; c2 >= 0; c2--)
        {
            deck[ c2 ] = 1;

            vals[ card2val[ c2 ] ]++;
            cols[ card2col[ c2 ] ]++;

            for (c3 = c2 - 1; c3 >= 0; c3--)
            {
                deck[ c3 ] = 1;

                vals[ card2val[ c3 ] ]++;
                cols[ card2col[ c3 ] ]++;

                for (c4 = c3 - 1; c4 >= 0; c4--)
                {
                    deck[ c4 ] = 1;

                    vals[ card2val[ c4 ] ]++;
                    cols[ card2col[ c4 ] ]++;

                    for (c5 = c4 - 1; c5 >= 0; c5--)
                    {
                        deck[ c5 ] = 1;

                        vals[ card2val[ c5 ] ]++;
                        cols[ card2col[ c5 ] ]++;

                        for (c6 = c5 - 1; c6 >= 0; c6--)
                        {
                            deck[ c6 ] = 1;

                            vals[ card2val[ c6 ] ]++;
                            cols[ card2col[ c6 ] ]++;

                            for (c7 = c6 - 1; c7 >= 0; c7--)
                            {
                                deck[ c7 ] = 1;

                                vals[ card2val[ c7 ] ]++;
                                cols[ card2col[ c7 ] ]++;

                                score = scoreSeven();

                                scores[ score ]++;

                                total++;

                                vals[ card2val[ c7 ] ]--;
                                cols[ card2col[ c7 ] ]--;

                                deck[ c7 ] = 0;
                            }

                            vals[ card2val[ c6 ] ]--;
                            cols[ card2col[ c6 ] ]--;

                            deck[ c6 ] = 0;
                        }

                        vals[ card2val[ c5 ] ]--;
                        cols[ card2col[ c5 ] ]--;

                        deck[ c5 ] = 0;
                    }

                    vals[ card2val[ c4 ] ]--;
                    cols[ card2col[ c4 ] ]--;

                    deck[ c4 ] = 0;
                }

                vals[ card2val[ c3 ] ]--;
                cols[ card2col[ c3 ] ]--;

                deck[ c3 ] = 0;
            }

            vals[ card2val[ c2 ] ]--;
            cols[ card2col[ c2 ] ]--;

            deck[ c2 ] = 0;
        }

        vals[ card2val[ c1 ] ]--;
        cols[ card2col[ c1 ] ]--;

        deck[ c1 ] = 0;

        printf("%d...\n", total);
    }

    printf("%d\n", total);

    for (int inx = 0; inx < 10; inx++)
    {
        printf("score: %d=%d\n", inx, scores[ inx ]);
    }
}

int main()
{
    printf("Texas poker generator.\n");

    int wikitotal
            = 4324
            + 37260
            + 224848
            + 3473184
            + 4047644
            + 6180020
            + 6461620
            + 31433400
            + 58627800
            + 23294460
            ;

    printf("Wiki-Total %d.\n", wikitotal);

    evalgame();

    return 0;
}