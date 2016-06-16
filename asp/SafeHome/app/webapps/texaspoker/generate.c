#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

//
// 12 = Ace
// 11 = King
// 10 = Queen
//  9 = Jack
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
char card2num[ 52 ][ 2 ];

char deck  [ 52 ];
char vals  [ 13 ];
char cols  [  4 ];

char wcvals[  6 ];

int  scores[ 10 ];
int  allsco[ 13 * 13 * 13 * 13 * 13 * 13 ];

int  wikitotal
    = 4324
    + 37260
    + 224848
    + 3473184
    + 4047644
    + 6180020
    + 6461620
    + 31433400
    + 58627800
    + 23294460;

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

    printf("Something wrong getBestVal1...\n");
    exit(1);
}

int wcvals2int()
{
    int intval = 0;

    intval = (intval * 13) + wcvals[ 0 ];
    intval = (intval * 13) + wcvals[ 1 ];
    intval = (intval * 13) + wcvals[ 2 ];
    intval = (intval * 13) + wcvals[ 3 ];
    intval = (intval * 13) + wcvals[ 4 ];
    intval = (intval * 13) + wcvals[ 5 ];

    return intval;
}

int score2hex2(int scoreval)
{
    int hexval = 0;

    hexval |= (scoreval % 13) << (4 * 0);
    scoreval /= 13;
    hexval |= (scoreval % 13) << (4 * 1);
    scoreval /= 13;
    hexval |= (scoreval % 13) << (4 * 2);
    scoreval /= 13;
    hexval |= (scoreval % 13) << (4 * 3);
    scoreval /= 13;
    hexval |= (scoreval % 13) << (4 * 4);
    scoreval /= 13;
    hexval |= (scoreval % 13) << (4 * 5);

    return hexval;
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
            wcvals[ 1 ] = card2val[ inx - 0 ];
            wcvals[ 2 ] = card2val[ inx - 4 ];
            wcvals[ 3 ] = card2val[ inx - 8 ];
            wcvals[ 4 ] = card2val[ inx - 12 ];
            wcvals[ 5 ] = card2val[ inx - 16 ];

            return wcvals[ 0 ];
        }
    }

    //
    // Straight flush ace at end.
    //

    for (int inx = 0; inx <= 3; inx++)
    {
        if (deck[ 12 + inx ] && deck[ 8 + inx ] && deck[ 4 + inx ] && deck[ 0 + inx ] && deck[ 48 + inx ])
        {
            wcvals[ 0 ] = 8;
            wcvals[ 1 ] = card2val[ 12 + inx ];
            wcvals[ 2 ] = card2val[  8 + inx ];
            wcvals[ 3 ] = card2val[  4 + inx ];
            wcvals[ 4 ] = card2val[  0 + inx ];
            wcvals[ 5 ] = card2val[ 48 + inx ];

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

    for (int inx = 12; inx >= 0; inx--)
    {
        if (vals[ inx ] == 4)
        {
            if (fours == 0) fourval1 = inx;

            fours++;
        }
        else
        {
            if (vals[ inx ] == 3)
            {
                if (threes == 0) threeval1 = inx;
                if (threes == 1) threeval2 = inx;

                threes++;
            }
            else
            {
                if (vals[ inx ] == 2)
                {
                    if (twos == 0) twoval1 = inx;
                    if (twos == 1) twoval2 = inx;
                    if (twos == 2) twoval3 = inx;

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
            wcvals[ 1 ] = inx - 0;
            wcvals[ 2 ] = inx - 1;
            wcvals[ 3 ] = inx - 2;
            wcvals[ 4 ] = inx - 3;
            wcvals[ 5 ] = inx - 4;

            return wcvals[ 0 ];
        }
    }

    //
    // Straight ace at end.
    //

    if (vals[ 3 ] && vals[ 2 ] && vals[ 1 ] && vals[ 0 ] && vals[ 12 ])
    {
        wcvals[ 0 ] = 4;
        wcvals[ 1 ] = 3;
        wcvals[ 2 ] = 2;
        wcvals[ 3 ] = 1;
        wcvals[ 4 ] = 0;
        wcvals[ 5 ] = 12;

        return wcvals[ 0 ];
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
        wcvals[ 5 ] = 0;

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
        wcvals[ 4 ] = twoval2;
        wcvals[ 5 ] = getBestVal2(twoval1, twoval2);

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
        wcvals[ 4 ] = 0;
        wcvals[ 5 ] = 0;

        return wcvals[ 0 ];
    }

    //
    // High card.
    //

    wcvals[ 0 ] = 0;
    wcvals[ 1 ] = getBestVal0();
    wcvals[ 2 ] = 0;
    wcvals[ 3 ] = 0;
    wcvals[ 4 ] = 0;
    wcvals[ 5 ] = 0;

    return 0;
}

void setup()
{
    for (int inx = 0; inx < 52; inx++)
    {
        card2val[ inx ] = inx / 4;
        card2col[ inx ] = inx % 4;

        card2num[ inx ][ 0 ] = '0' + inx / 10;
        card2num[ inx ][ 1 ] = '0' + inx % 10;
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
}

void evalhand()
{
    printf("Hallo=1\n");
    memset(allsco, 0, sizeof(allsco));
    printf("Hallo=2\n");

    int a1, a2, b1, b2, c1, c2, c3, c4, c5;
    int a1v, a2v, as, b1v, b2v, bs, akey, bkey, gkey, rkey, ascore, bscore;
    printf("Hallo=%d\n", akey);

    for (a1 = 51; a1 >= 0; a1--)
    {
        a1v = card2val[ a1 ];

        for (a2 = a1 - 1; a2 >= 0; a2--)
        {
            a2v = card2val[ a2 ];
            as  = (card2col[ a1 ] == card2col[ a2 ]);

            akey = (((a1v * 13) + a2v) << 1) + (as ? 1 : 0);

            int count = 0;

            for (b1 = 51; b1 >= 0; b1--)
            {
                if ((b1 == a1) || (b1 == a2)) continue;

                b1v = card2val[ b1 ];

                for (b2 = b1 - 1; b2 >= 0; b2--)
                {
                    if ((b2 == a1) || (b2 == a2)) continue;

                    b2v = card2val[ b2 ];
                    bs = (card2col[ b1 ] == card2col[ b2 ]);

                    bkey = (((b1v * 13) + b2v) << 1) + (bs ? 1 : 0);

                    gkey = ((akey * 512) + bkey) << 2;
                    rkey = ((bkey * 512) + akey) << 2;

                    if (allsco[ gkey ]) continue;

                    if (allsco[ rkey ])
                    {
                        allsco[ gkey + 1 ] = allsco[ rkey + 1 ];
                        allsco[ gkey + 2 ] = allsco[ rkey + 3 ];
                        allsco[ gkey + 3 ] = allsco[ rkey + 2 ];
                    }
                    else
                    {
                        allsco[ gkey ] = -1;

                        for (c1 = 51; c1 >= 0; c1--)
                        {
                            if ((c1 == a1) || (c1 == a2) || (c1 == b1) || (c1 == b2)) continue;

                            deck[ c1 ] = 1;

                            vals[ card2val[ c1 ]]++;
                            cols[ card2col[ c1 ]]++;

                            for (c2 = c1 - 1; c2 >= 0; c2--)
                            {
                                if ((c2 == a1) || (c2 == a2) || (c2 == b1) || (c2 == b2)) continue;

                                deck[ c2 ] = 1;

                                vals[ card2val[ c2 ]]++;
                                cols[ card2col[ c2 ]]++;

                                for (c3 = c2 - 1; c3 >= 0; c3--)
                                {
                                    if ((c3 == a1) || (c3 == a2) ||
                                        (c3 == b1) || (c3 == b2))
                                        continue;

                                    deck[ c3 ] = 1;

                                    vals[ card2val[ c3 ]]++;
                                    cols[ card2col[ c3 ]]++;

                                    for (c4 = c3 - 1; c4 >= 0; c4--)
                                    {
                                        if ((c4 == a1) || (c4 == a2) ||
                                            (c4 == b1) || (c4 == b2))
                                            continue;

                                        deck[ c4 ] = 1;

                                        vals[ card2val[ c4 ]]++;
                                        cols[ card2col[ c4 ]]++;

                                        for (c5 = c4 - 1; c5 >= 0; c5--)
                                        {
                                            if ((c5 == a1) || (c5 == a2) ||
                                                (c5 == b1) || (c5 == b2))
                                                continue;

                                            deck[ c5 ] = 1;

                                            vals[ card2val[ c5 ]]++;
                                            cols[ card2col[ c5 ]]++;

                                            count++;

                                            deck[ a1 ] = 1;
                                            vals[ card2val[ a1 ]]++;
                                            cols[ card2col[ a1 ]]++;

                                            deck[ a2 ] = 1;
                                            vals[ card2val[ a2 ]]++;
                                            cols[ card2col[ a2 ]]++;

                                            scoreSeven();
                                            ascore = wcvals2int();

                                            vals[ card2val[ a1 ]]--;
                                            cols[ card2col[ a1 ]]--;
                                            deck[ a1 ] = 0;

                                            vals[ card2val[ a2 ]]--;
                                            cols[ card2col[ a2 ]]--;
                                            deck[ a2 ] = 0;

                                            deck[ b1 ] = 1;
                                            vals[ card2val[ b1 ]]++;
                                            cols[ card2col[ b1 ]]++;

                                            deck[ b2 ] = 1;
                                            vals[ card2val[ b2 ]]++;
                                            cols[ card2col[ b2 ]]++;

                                            scoreSeven();
                                            bscore = wcvals2int();

                                            vals[ card2val[ b1 ]]--;
                                            cols[ card2col[ b1 ]]--;
                                            deck[ b1 ] = 0;

                                            vals[ card2val[ b2 ]]--;
                                            cols[ card2col[ b2 ]]--;
                                            deck[ b2 ] = 0;

                                            //
                                            // Sum up result.
                                            //

                                            if (ascore == bscore)
                                            {
                                                //
                                                // Split.
                                                //

                                                allsco[ gkey + 1 ]++;
                                            }
                                            else
                                            {
                                                if (ascore > bscore)
                                                {
                                                    //
                                                    // Player a wins.
                                                    //

                                                    allsco[ gkey + 2 ]++;
                                                }
                                                else
                                                {
                                                    //
                                                    // Player b wins.
                                                    //

                                                    allsco[ gkey + 3 ]++;
                                                }
                                            }

                                            vals[ card2val[ c5 ]]--;
                                            cols[ card2col[ c5 ]]--;

                                            deck[ c5 ] = 0;
                                        }

                                        vals[ card2val[ c4 ]]--;
                                        cols[ card2col[ c4 ]]--;

                                        deck[ c4 ] = 0;
                                    }

                                    vals[ card2val[ c3 ]]--;
                                    cols[ card2col[ c3 ]]--;

                                    deck[ c3 ] = 0;
                                }

                                vals[ card2val[ c2 ]]--;
                                cols[ card2col[ c2 ]]--;

                                deck[ c2 ] = 0;
                            }

                            vals[ card2val[ c1 ]]--;
                            cols[ card2col[ c1 ]]--;

                            deck[ c1 ] = 0;
                        }
                    }

                    printf("%x%x%c - %x%x%c %10d %10d %10d\n",
                           a1v, a2v, (as ? 's' : ' '), b1v, b2v, (bs ? 's' : ' '),
                           allsco[ gkey + 1 ], allsco[ gkey + 2 ], allsco[ gkey + 3 ]);
                }
            }

            printf("%x%x%c - %d\n", a1v, a2v, (as ? 's' : ' '), count);
        }
    }

    int maxscores = sizeof(allsco) / sizeof(int);

    FILE *fdhands = fopen("./handstable.txt", "w");

    int awins, bwins, splits;

    for (int inx = 0; inx < maxscores; inx += 4)
    {
        if (allsco[ inx ] == -1)
        {
            splits = allsco[ inx + 1 ];
            awins  = allsco[ inx + 2 ];
            bwins  = allsco[ inx + 3 ];

            int hexval = score2hex2(inx >> 2);

            fprintf(fdhands,"%06x=%d=%d=%d\n", hexval, awins, bwins, splits);
        }
    }

    fclose(fdhands);
}

void evalgame()
{
    memset(allsco, 0, sizeof(allsco));

    int total = 0;

    int c1, c2, c3, c4, c5, c6, c7, score;

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

                                allsco[ wcvals2int() ]++;

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

    int maxscores = sizeof(allsco) / sizeof(int);

    int checksumm = 0;

    FILE *fdscore = fopen("./scoretable.txt", "w");

    for (int inx = 0; inx < maxscores; inx++)
    {
        if (allsco[ inx ])
        {
            int hexval = score2hex2(inx);
            float wins = checksumm / (float) total;

            fprintf(fdscore, "%06x=%0.5f=%d\n", hexval, wins, allsco[ inx ]);

            checksumm += allsco[ inx ];
        }
    }

    fclose(fdscore);

    printf("Wiki-Total %d total=%d check=%d\n", wikitotal, total, checksumm);

    printf("%d\n", total);

    for (int inx = 0; inx < 10; inx++)
    {
        printf("score: %d=%d\n", inx, scores[ inx ]);
    }
}

int main()
{
    printf("Texas poker generator.\n");

    printf("Wiki-Total %d.\n", wikitotal);

    setup();

    //evalgame();

    evalhand();

    return 0;
}