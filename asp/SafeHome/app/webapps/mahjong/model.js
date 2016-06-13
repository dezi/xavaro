mahjong.tilePackWid = 81;
mahjong.tilePackHei = 110;
mahjong.tileRealWid = 89;
mahjong.tileRealHei = 118;

mahjong.boards =
[
    "cat", "crab", "dragon", "fortress", "spider", "turtle"
];

mahjong.faces =
[
    "coin_1", "coin_2", "coin_3", "coin_4", "coin_5",
    "coin_6", "coin_7", "coin_8", "coin_9",
    "bamboo_1", "bamboo_2", "bamboo_3", "bamboo_4", "bamboo_5",
    "bamboo_6", "bamboo_7", "bamboo_8", "bamboo_9",
    "numeral_1",  "numeral_2", "numeral_3", "numeral_4", "numeral_5",
    "numeral_6", "numeral_7", "numeral_8", "numeral_9",
    "wind_n", "wind_s", "wind_e", "wind_w",
    "dragon_r", "dragon_w", "dragon_g",
    "flower_1", "flower_2", "flower_3", "flower_4",
    "season_1", "season_2", "season_3", "season_4"
];

mahjong.canBePairedWith = function(face1, face2)
{
    var isFlower1 = face1.startsWith("flower");
    var isSeason1 = face1.startsWith("season");

    var isFlower2 = face2.startsWith("flower");
    var isSeason2 = face2.startsWith("season");

    return (face1 == face2) ||
           (isFlower1 && isFlower2) ||
           (isSeason1 && isSeason2);
}

mahjong.createPoolAddTiles = function(max)
{
    for (var inx = 0; inx < max; inx++)
    {
        mahjong.tilePool.push(mahjong.faces[ inx ]);
    }
}

mahjong.createPool = function()
{
    var xx = mahjong;

    xx.tilePool = [];

    xx.createPoolAddTiles(42);
    xx.createPoolAddTiles(34);
    xx.createPoolAddTiles(34);
    xx.createPoolAddTiles(34);
}

mahjong.getRandomTile = function()
{
    var xx = mahjong;

    if (xx.tilePool.length == 0) return null;

    var rnd = Math.floor(Math.random() * xx.tilePool.length);
    var sub = xx.tilePool.splice(rnd, 1);

    return sub[ 0 ];
}

mahjong.createBoard = function()
{
    var xx = mahjong;

    var url = "boards/" + xx.boards[ xx.boardIndex - 1 ] + ".txt";
    var boarddata = WebAppRequest.loadSync(url);
    var boardlines = boarddata.trim().replace("\r","").split("\n");
    if (! boardlines.length) return;

    var magic = boardlines.shift();
    if (magic != "BRD") return;

    console.log("mahjong.createBoard: magic=" + magic);

    var tiles = boardlines.shift();
    xx.numTiles = parseInt(tiles);
    if (! xx.numTiles) return;

    console.log("mahjong.createBoard: numTiles=" + xx.numTiles);

    xx.createPool();

    var dimensions = boardlines.shift().trim().split(",");
    if (dimensions.length != 3) return;
    xx.zSize = parseInt(dimensions[ 0 ]);
    xx.ySize = parseInt(dimensions[ 1 ]);
    xx.xSize = parseInt(dimensions[ 2 ]);
    if (! (xx.zSize && xx.ySize && xx.xSize)) return;
    if ((xx.ySize % 2) || (xx.xSize % 2)) return;

    console.log("mahjong.createBoard: z=" + xx.zSize + " y=" + xx.ySize + " x=" + xx.xSize);

    xx.panelRealWid = xx.xSize * xx.tilePackWid;
    xx.panelRealHei = xx.ySize * xx.tilePackHei;

    console.log("mahjong.createBoard: wid=" + xx.panelRealWid + " hei=" + xx.panelRealHei);

    xx.gamePanel.innerHTML = null;
    xx.matrix = [];

    for (var zinx = 0; zinx < xx.zSize; zinx++)
    {
        xx.matrix[ zinx ] = [];

        for (var yinx = 0; yinx < xx.ySize; yinx++)
        {
            if (boardlines.length == 0)
            {
                alert("Lines missing in def.....");

                return;
            }

            var line = boardlines.shift().trim();

            while (boardlines.length && ! line.length)
            {
                line = boardlines.shift().trim();
            }

            xx.matrix[ zinx ][ yinx ] = [];

            for (var xinx = 0; xinx < xx.xSize; xinx++)
            {
                var div = null;

                if (line[ xinx ] == "1")
                {
                    var name = xx.getRandomTile();
                    console.log("==================xxxxxx====" + name);

                    if (name != null)
                    {
                        div = WebLibSimple.createDivWidHei(0, 0, 0, 0, null, xx.gamePanel);

                        div.tileName = name;

                        div.tileKey = zinx + ":" + yinx + ":" + xinx;
                        div.tilePosition = { z: zinx, y: yinx, x: xinx };
                        div.tileSelected = false;

                        div.tileBack = WebLibSimple.createAnyAppend("img", div);
                        div.tileBack.style.position = "absolute";
                        div.tileBack.style.left     = "0px";
                        div.tileBack.style.top      = "0px";
                        div.tileBack.style.width    = "100%";
                        div.tileBack.style.height   = "100%";
                        div.tileBack.src = "tile_neutral_89x117.png";

                        div.tileFace = WebLibSimple.createAnyAppend("img", div);
                        div.tileFace.style.position = "absolute";
                        div.tileFace.style.left     = "0px";
                        div.tileFace.style.top      = "0px";
                        div.tileFace.style.width    = "100%";
                        div.tileFace.style.height   = "100%";
                        div.tileFace.src = "tiles/classic/" + div.tileName + ".png";

                        div.onTouchClick = mahjong.onTileClick;
                    }
                }

                xx.matrix[ zinx ][ yinx ][ xinx ] = div;
            }
        }
    }

    xx.selectedTile1 = null;
    xx.selectedTile2 = null;

    xx.computeBorder();
}

mahjong.computeBorder = function()
{
    var xx = mahjong;

    xx.border = {};
    xx.tilesLeft = 0;
    xx.combinations = [];

    for (var zinx = 0; zinx < xx.zSize; zinx++)
    {
        for (var yinx = 0; yinx < xx.ySize; yinx++)
        {
            for (var xinx = 0; xinx < xx.xSize; xinx++)
            {
                var div = xx.matrix[ zinx ][ yinx ][ xinx ];
                if (! div) continue;

                xx.tilesLeft++;

                div.isBorder = xx.canTileBelongToBorder(xinx, yinx, zinx);

                if (div.isBorder)
                {
                    if (xx.hintLevel > 0)
                    {
                        div.tileBack.src = "tile_border_89x117.png";
                    }
                    else
                    {
                        div.tileBack.src = "tile_neutral_89x117.png";
                    }

                    for (var key in xx.border)
                    {
                        var div1 = xx.border[ key ];

                        if (xx.canBePairedWith(div.tileName, div1.tileName))
                        {
                            var combi = {};
                            combi.tile1 = div;
                            combi.tile2 = div1;

                            xx.combinations.push(combi);
                        }
                    }

                    xx.border[ div.tileKey ] = div;
                }
                else
                {
                    div.tileBack.src = "tile_neutral_89x117.png";
                }
            }
        }
    }

    console.log("mahjong.computeBorder: tiles=" + xx.tilesLeft + " combinations=" + xx.combinations.length);
}

mahjong.canTileBelongToBorder = function(x, y, z)
{
    var xx = mahjong;

    var zL = (z == xx.zSize - 1);
    var x0 = (x == 0);
    var xL = (x == xx.xSize - 1);
    var y0 = (y == 0);
    var yL = (y == xx.ySize - 1);
    var x1 = (x < 2);
    var xP = (x > xx.xSize - 3);

    var matrix = xx.matrix;

    if (zL || (matrix[ z + 1 ][ y ][ x ] == null
            && (x0 || matrix[ z + 1 ][ y ][ x - 1 ] == null)
            && (xL || matrix[ z + 1 ][ y ][ x + 1 ] == null)
            && (y0 || matrix[ z + 1 ][ y - 1 ][ x ] == null)
            && (yL || matrix[ z + 1 ][ y + 1 ][ x ] == null)
            && (x0 || y0 || matrix[ z + 1 ][ y - 1 ][ x - 1 ] == null)
            && (x0 || yL || matrix[ z + 1 ][ y + 1 ][ x - 1 ] == null)
            && (xL || y0 || matrix[ z + 1 ][ y - 1 ][ x + 1 ] == null)
            && (xL || yL || matrix[ z + 1 ][ y + 1 ][ x + 1 ] == null)))
    {
        return ((x1 || matrix[ z ][ y ][ x - 2 ] == null)
                && (x1 || y0 || matrix[ z ][ y - 1 ][ x - 2 ] == null)
                && (x1 || yL || matrix[ z ][ y + 1 ][ x - 2 ] == null))
                || ((xP || matrix[ z ][ y ][ x + 2 ] == null)
                && (xP || y0 || matrix[ z ][ y - 1 ][ x + 2 ] == null)
                && (xP || yL || matrix[ z ][ y + 1 ][ x + 2 ] == null));
    }

    return false;
}