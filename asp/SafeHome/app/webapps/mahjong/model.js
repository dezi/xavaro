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

mahjong.createBoard = function()
{
    var xx = mahjong;

    var url = "boards/" + xx.boards[ xx.boardIndex ] + ".txt";
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

    var dimensions = boardlines.shift().trim().split(",");
    if (dimensions.length != 3) return;
    xx.zSize = parseInt(dimensions[ 0 ]);
    xx.ySize = parseInt(dimensions[ 1 ]);
    xx.xSize = parseInt(dimensions[ 2 ]);
    if (! (xx.zSize && xx.ySize && xx.xSize)) return;
    if ((xx.ySize % 2) || (xx.xSize % 2)) return;

    console.log("mahjong.createBoard: z=" + xx.zSize + " y=" + xx.ySize + " x=" + xx.xSize);

    xx.panelRealWid = xx.xSize * xx.tileRealWid;
    xx.panelRealHei = xx.ySize * xx.tileRealHei;

    console.log("mahjong.createBoard: wid=" + xx.panelRealWid + " hei=" + xx.panelRealHei);
}
