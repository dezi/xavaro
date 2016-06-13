sudoku.buildHints = function()
{
    var xx = sudoku;

    xx.horzhave = [];
    xx.verthave = [];
    xx.cellhave = [];

    xx.horzpositions = [];
    xx.vertpositions = [];
    xx.cellpositions = [];

    for (var inx = 0; inx < 9; inx++)
    {
        xx.horzhave[ inx ] = [];
        xx.verthave[ inx ] = [];
        xx.cellhave[ inx ] = [];

        xx.horzpositions[ inx ] = [];
        xx.vertpositions[ inx ] = [];
        xx.cellpositions[ inx ] = [];

        for (var dig = 1; dig <= 9; dig++)
        {
            xx.horzhave[ inx ][ dig ] = false;
            xx.verthave[ inx ][ dig ] = false;
            xx.cellhave[ inx ][ dig ] = false;
        }
    }

    for (var pos = 0; pos < 81; pos++)
    {
        var dig = xx.game[ pos ];

        xx.horzhave[ xx.horz[ pos ] ][ dig ] = true;
        xx.verthave[ xx.vert[ pos ] ][ dig ] = true;
        xx.cellhave[ xx.cell[ pos ] ][ dig ] = true;

        xx.horzpositions[ xx.horz[ pos ] ].push(pos);
        xx.vertpositions[ xx.vert[ pos ] ].push(pos);
        xx.cellpositions[ xx.cell[ pos ] ].push(pos);
    }
}

sudoku.recurseGame = function(pos)
{
    var xx = sudoku;

    if (pos == 81)
    {
        xx.solved++;

        return;
    }

    if (xx.solved >= xx.maxsolu) return;

    if (xx.game[ pos ] > 0)
    {
        sudoku.recurseGame(pos + 1);

        return;
    }

    var horz = xx.horzhave[ xx.horz[ pos ] ];
    var vert = xx.verthave[ xx.vert[ pos ] ];
    var cell = xx.cellhave[ xx.cell[ pos ] ];

    for (var dig = 1; dig <= 9; dig++)
    {
        if (horz[ dig ] || vert[ dig ] || cell[ dig ]) continue;

        horz[ dig ] = true;
        vert[ dig ] = true;
        cell[ dig ] = true;

        xx.game[ pos ] = dig;
        xx.recurseGame(pos + 1);
        if (xx.maxsolu > 1) xx.game[ pos ] = 0;

        horz[ dig ] = false;
        vert[ dig ] = false;
        cell[ dig ] = false;

        if (xx.solved >= xx.maxsolu) return;
    }

    xx.game[ pos ] = 0;
}

sudoku.fillGame = function()
{
    var xx = sudoku;

    xx.solved  = 0;
    xx.maxsolu = 1;

    sudoku.buildHints();
    sudoku.recurseGame(0);
}

sudoku.getNumberSolutions = function()
{
    var xx = sudoku;

    xx.solved  = 0;
    xx.maxsolu = 2;

    sudoku.buildHints();
    sudoku.recurseGame(0);

    return xx.solved;
}

sudoku.generateGame = function()
{
    var xx = sudoku;

    //
    // Random value translate array.
    //

    xx.rval = [];

    for (var inx = 0; inx < 9; inx++) xx.rval[ inx ] = inx + 1;

    for (var inx = 0; inx < 9; inx++)
    {
        var inx1 = Math.floor(Math.random() * 9);
        var inx2 = Math.floor(Math.random() * 9);

        var tmp = xx.rval[ inx1 ];
        xx.rval[ inx1 ] = xx.rval[ inx2 ];
        xx.rval[ inx2 ] = tmp;
    }

    //
    // Random position translate array.
    //

    xx.rpos = [];

    for (var inx = 0; inx < 81; inx++) xx.rpos[ inx ] = inx;

    for (var inx = 0; inx < 81; inx++)
    {
        var inx1 = Math.floor(Math.random() * 81);
        var inx2 = Math.floor(Math.random() * 81);

        var tmp = xx.rpos[ inx1 ];
        xx.rpos[ inx1 ] = xx.rpos[ inx2 ];
        xx.rpos[ inx2 ] = tmp;
    }

    //
    // Blank game.
    //

    xx.game = [];
    xx.horz = [];
    xx.vert = [];
    xx.cell = [];

    for (var inx = 0; inx < 81; inx++)
    {
        xx.game[ inx ] = 0;

        xx.horz[ inx ] = inx % 9;
        xx.vert[ inx ] = Math.floor(inx / 9);
        xx.cell[ inx ] = Math.floor(xx.horz[ inx ] / 3) + (Math.floor(xx.vert[ inx ] / 3) * 3);
    }

    //
    // Fill first cell with random values.
    //

    var inx = 0;

    for (var vinx = 0; vinx < 3; vinx++)
    {
        for (var hinx = 0; hinx < 3; hinx++)
        {
            xx.game[ (vinx * 9) + hinx ] = xx.rval[ inx++ ];
        }
    }

    //
    // Recursivly fill in rest.
    //

    sudoku.fillGame();

    //
    // Remove some cells.
    //

    for (var inx = 0; inx < 81; inx++)
    {
        var pos = xx.rpos[ inx ];

        var olddig = xx.game[ pos ];
        xx.game[ pos ] = 0;

        var solutions = xx.getNumberSolutions();

        if (solutions > 1)
        {
            //
            // Removing this position makes game
            // ambigous. Put it back.
            //

            xx.game[ pos ] = olddig;
        }

        if ((xx.level == 1) && (inx >  5)) break;
        if ((xx.level == 2) && (inx > 10)) break;
        if ((xx.level == 3) && (inx > 20)) break;
        if ((xx.level == 4) && (inx > 25)) break;
        if ((xx.level == 5) && (inx > 30)) break;
        if ((xx.level == 6) && (inx > 40)) break;
        if ((xx.level == 7) && (inx > 50)) break;
        if ((xx.level == 8) && (inx > 60)) break;
    }
}
