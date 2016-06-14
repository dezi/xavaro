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

int wcvals[ 6 ];

int getBestVal0()
{
    for (int inx = 51; inx >= 0; inx--)
    {
        if (deck[ inx ])
        {
            return card2val[ inx ];
        }
    }

    printf("Something wrong getBestVal0...\n");
    exit(1);
}

int getBestVal1(ex1)
{
    for (int inx = 51; inx >= 0; inx--)
    {
        if (deck[ inx ] && (card2val[ inx ] != ex1))
        {
            return card2val[ inx ];
        }
    }

    printf("Something wrong getBestVal1...\n");
    exit(1);
}

int getBestVal2(ex1, ex2)
{
    for (int inx = 51; inx >= 0; inx--)
    {
        if (deck[ inx ] && (card2val[ inx ] != ex1) && (card2val[ inx ] != ex2))
        {
            return card2val[ inx ];
        }
    }

    printf("Something wrong getBestVal2...\n");
    exit(1);
}

int getBestVal3(ex1, ex2, ex3)
{
    for (int inx = 51; inx >= 0; inx--)
    {
        if (deck[ inx ] && (card2val[ inx ] != ex1) &&
                (card2val[ inx ] != ex2) && (card2val[ inx ] != ex3))
        {
            return card2val[ inx ];
        }
    }

    printf("Something wrong getBestVal3...\n");
    exit(1);
}

int getBestVal4(ex1, ex2, ex3, ex4)
{
    for (int inx = 51; inx >= 0; inx--)
    {
        if (deck[ inx ] &&
                (card2val[ inx ] != ex1) && (card2val[ inx ] != ex2) &&
                (card2val[ inx ] != ex3) && (card2val[ inx ] != ex4))
        {
            return card2val[ inx ];
        }
    }

    printf("Something wrong getBestVal4...\n");
    exit(1);
}

int scoreSeven()
{
    //
    // Royal flush.
    //

    for (int inx = 51; inx >= 48; inx--)
    {
        if (deck[ inx - 0 ] && deck[ inx - 4 ] && deck[ inx - 8 ] && deck[ inx - 12 ] && deck[ inx - 16 ])
        {
            wcvals[ 0 ] = 9;
            wcvals[ 1 ] = card2val[ inx -  0 ];
            wcvals[ 2 ] = card2val[ inx -  4 ];
            wcvals[ 3 ] = card2val[ inx -  8 ];
            wcvals[ 4 ] = card2val[ inx - 12 ];
            wcvals[ 5 ] = card2val[ inx - 16 ];

            return wcvals[ 0 ];
        }
    }

    //
    // Straight flush.
    //

    for (int inx = 47; inx >= 16; inx--)
    {
        if (deck[ inx ] && deck[ inx - 4 ] && deck[ inx - 8 ] && deck[ inx - 12 ] && deck[ inx - 16 ])
        {
            wcvals[ 0 ] = 8;
            wcvals[ 1 ] = card2val[ inx -  0 ];
            wcvals[ 2 ] = card2val[ inx -  4 ];
            wcvals[ 3 ] = card2val[ inx -  8 ];
            wcvals[ 4 ] = card2val[ inx - 12 ];
            wcvals[ 5 ] = card2val[ inx - 16 ];

            return wcvals[ 0 ];
        }
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
        wcvals[ 0 ] = 7;
        wcvals[ 1 ] = fourval1;
        wcvals[ 2 ] = fourval1;
        wcvals[ 3 ] = fourval1;
        wcvals[ 4 ] = fourval1;
        wcvals[ 5 ] = getBestVal1(fourval1);

        return wcvals[ 0 ];
    }

    //
    // Full house.
    //

    if (threes == 2)
    {
        wcvals[ 0 ] = 6;
        wcvals[ 1 ] = threeval1;
        wcvals[ 2 ] = threeval1;
        wcvals[ 3 ] = threeval1;
        wcvals[ 4 ] = threeval2;
        wcvals[ 5 ] = threeval2;

        return wcvals[ 0 ];
    }

    if ((threes == 1) && (twos >= 1))
    {
        wcvals[ 0 ] = 6;
        wcvals[ 1 ] = threeval1;
        wcvals[ 2 ] = threeval1;
        wcvals[ 3 ] = threeval1;
        wcvals[ 4 ] = twoval1;
        wcvals[ 5 ] = twoval1;

        return wcvals[ 0 ];
    }

    //
    // Flush.
    //

    for (int col = 0; col <= 3; col++)
    {
        if (cols[ col ] >= 5)
        {
            wcvals[ 0 ] = 5;

            int cnt = 1;

            for (int inx = 51; inx >= 0; inx--)
            {
                if (deck[ inx ] && ((inx % 4) == col))
                {
                    wcvals[ cnt++ ] = card2val[ inx ];

                    if (cnt == 6) return wcvals[ 0 ];
                }
            }

            printf("Something wrong Flush...\n");
            exit(1);
        }
    }

    //
    // Straight.
    //

    for (int inx = 12; inx >= 4; inx--)
    {
        if (vals[ inx - 0 ] && vals[ inx - 1 ] && vals[ inx - 2 ] && vals[ inx - 3 ] && vals[ inx - 4 ])
        {
            wcvals[ 0 ] = 4;
            wcvals[ 1 ] = vals[ inx - 0 ];
            wcvals[ 2 ] = vals[ inx - 1 ];
            wcvals[ 3 ] = vals[ inx - 2 ];
            wcvals[ 4 ] = vals[ inx - 3 ];
            wcvals[ 5 ] = vals[ inx - 4 ];

            return wcvals[ 0 ];
        }
    }

    //
    // Three of a kind.
    //

    if (threes >= 1)
    {
        wcvals[ 0 ] = 3;
        wcvals[ 1 ] = threeval1;
        wcvals[ 2 ] = threeval1;
        wcvals[ 3 ] = threeval1;
        wcvals[ 4 ] = getBestVal1(threeval1);
        wcvals[ 5 ] = getBestVal2(threeval1, wcvals[ 4 ]);

        return wcvals[ 0 ];
    }

    //
    // Two pairs.
    //

    if (twos >= 2)
    {
        wcvals[ 0 ] = 2;
        wcvals[ 1 ] = twoval1;
        wcvals[ 2 ] = twoval1;
        wcvals[ 3 ] = twoval2;
        wcvals[ 3 ] = twoval2;
        wcvals[ 3 ] = getBestVal2(twoval1, twoval2);

        return wcvals[ 0 ];
    }

    //
    // Two of a kind.
    //

    if (twos >= 1)
    {
        wcvals[ 0 ] = 1;
        wcvals[ 1 ] = twoval1;
        wcvals[ 2 ] = twoval1;
        wcvals[ 3 ] = getBestVal1(twoval1);
        wcvals[ 4 ] = getBestVal2(twoval1, wcvals[ 3 ]);
        wcvals[ 5 ] = getBestVal3(twoval1, wcvals[ 3 ], wcvals[ 4 ]);

        return wcvals[ 0 ];
    }

    wcvals[ 0 ] = 0;
    wcvals[ 1 ] = getBestVal0();
    wcvals[ 2 ] = getBestVal1(wcvals[ 1 ]);
    wcvals[ 3 ] = getBestVal2(wcvals[ 1 ], wcvals[ 2 ]);
    wcvals[ 4 ] = getBestVal3(wcvals[ 1 ], wcvals[ 2 ], wcvals[ 3 ]);
    wcvals[ 5 ] = getBestVal4(wcvals[ 1 ], wcvals[ 2 ], wcvals[ 3 ], wcvals[ 4 ]);

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