texaspoker.scoreSeven = function(deck, vals, cols)
{
    var xx = texaspoker;

    xx.wcvals = [];
    
    //
    // Royal flush.
    //

    for (var inx = 51; inx >= 48; inx--)
    {
        if (deck[ inx - 0 ] && deck[ inx - 4 ] && deck[ inx - 8 ] && deck[ inx - 12 ] && deck[ inx - 16 ])
        {
            wcvals[ 0 ] = 9;
            wcvals[ 1 ] = xx.card2val[ inx -  0 ];
            wcvals[ 2 ] = xx.card2val[ inx -  4 ];
            wcvals[ 3 ] = xx.card2val[ inx -  8 ];
            wcvals[ 4 ] = xx.card2val[ inx - 12 ];
            wcvals[ 5 ] = xx.card2val[ inx - 16 ];

            return wcvals[ 0 ];
        }
    }

    //
    // Straight flush.
    //

    for (var inx = 47; inx >= 16; inx--)
    {
        if (deck[ inx ] && deck[ inx - 4 ] && deck[ inx - 8 ] && deck[ inx - 12 ] && deck[ inx - 16 ])
        {
            wcvals[ 0 ] = 8;
            wcvals[ 1 ] = xx.card2val[ inx - 0 ];
            wcvals[ 2 ] = xx.card2val[ inx - 4 ];
            wcvals[ 3 ] = xx.card2val[ inx - 8 ];
            wcvals[ 4 ] = xx.card2val[ inx - 12 ];
            wcvals[ 5 ] = xx.card2val[ inx - 16 ];

            return wcvals[ 0 ];
        }
    }

    //
    // Straight flush ace at end.
    //

    for (var inx = 0; inx <= 3; inx++)
    {
        if (deck[ 12 + inx ] && deck[ 8 + inx ] && deck[ 4 + inx ] && deck[ 0 + inx ] && deck[ 48 + inx ])
        {
            wcvals[ 0 ] = 8;
            wcvals[ 1 ] = xx.card2val[ 12 + inx ];
            wcvals[ 2 ] = xx.card2val[  8 + inx ];
            wcvals[ 3 ] = xx.card2val[  4 + inx ];
            wcvals[ 4 ] = xx.card2val[  0 + inx ];
            wcvals[ 5 ] = xx.card2val[ 48 + inx ];

            return wcvals[ 0 ];
        }
    }

    //
    // Compute same cards values.
    //

    var twos = 0;
    var threes = 0;
    var fours = 0;

    var fourval1 = -1;

    var threeval1 = -1;
    var threeval2 = -1;

    var twoval1 = -1;
    var twoval2 = -1;
    var twoval3 = -1;

    for (var inx = 12; inx >= 0; inx--)
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

    for (var col = 0; col <= 3; col++)
    {
        if (cols[ col ] >= 5)
        {
            wcvals[ 0 ] = 5;

            var cnt = 1;

            for (var inx = 51; inx >= 0; inx--)
            {
                if (deck[ inx ] && ((inx % 4) == col))
                {
                    wcvals[ cnt++ ] = xx.card2val[ inx ];

                    if (cnt == 6) return wcvals[ 0 ];
                }
            }

            alert("Something wrong Flush...\n");
            exit(1);
        }
    }

    //
    // Straight.
    //

    for (var inx = 12; inx >= 4; inx--)
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
