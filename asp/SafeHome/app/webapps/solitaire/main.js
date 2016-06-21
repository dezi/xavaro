solitaire.createFrame = function()
{
    var xx = solitaire;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";
    xx.topdiv.style.backgroundImage = "url('walls/green-wool.jpg')";;

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    //
    // Game panel.
    //

    xx.gamesize    = Math.floor(Math.min(wid, hei) * 95 / 100);
    xx.fieldwidth  = xx.gamesize / 7;
    xx.fieldheight = xx.gamesize / 4;

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    //
    // Buttons.
    //

    xx.buttonTop1div = solitaire.createButton("–");
    xx.buttonTop2div = solitaire.createButton("$");
    xx.buttonTop3div = solitaire.createButton("+");

    xx.buttonBot1div = solitaire.createButton("!");
    xx.buttonBot2div = solitaire.createButton("=");
    xx.buttonBot3div = solitaire.createButton("?");

    xx.buttonTop3div.onTouchClick = solitaire.onNewGame;
    xx.buttonBot1div.onTouchClick = solitaire.onRedealOpen;
    xx.buttonBot2div.onTouchClick = solitaire.onExecuteMove;
    xx.buttonBot3div.onTouchClick = solitaire.onHintPlayer;

    //
    // Create dummy display card stack.
    //

    var dummyStack = solitaire.createCard(0, true);
    dummyStack.style.top  = (xx.fieldheight * 0) + "px";
    dummyStack.style.left = (xx.fieldwidth  * 0) + "px";

    addEventListener("resize", solitaire.onWindowResize);
}

solitaire.createGame = function()
{
    var xx = solitaire;

    xx.gamePanel.innerHTML = null;

    xx.allCards  = [];
    xx.dealCards = [];
    xx.doneHeaps = [];
    xx.playHeaps = [];
    xx.heapCards = [];
    xx.openCards = [];
    xx.nextCards = [];

    //
    // Create cards.
    //

    WebLibCards.newDeck(52);

    xx.heapToken = solitaire.createCard(-1, false);
    xx.heapToken.style.top  = "0px";
    xx.heapToken.style.left = "0px";
    xx.allCards.pop();

    xx.doneHeaps[ 0 ] = [];
    xx.doneHeaps[ 1 ] = [];
    xx.doneHeaps[ 2 ] = [];
    xx.doneHeaps[ 3 ] = [];

    xx.doneHeaps[ 3 ][ 0 ] = solitaire.createCard(-51, false);
    xx.doneHeaps[ 3 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 3 ][ 0 ].style.left = (xx.fieldwidth  * 3) + "px";

    xx.doneHeaps[ 2 ][ 0 ] = solitaire.createCard(-50, false);
    xx.doneHeaps[ 2 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 2 ][ 0 ].style.left = (xx.fieldwidth  * 4) + "px";

    xx.doneHeaps[ 1 ][ 0 ] = solitaire.createCard(-49, false);
    xx.doneHeaps[ 1 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 1 ][ 0 ].style.left = (xx.fieldwidth  * 5) + "px";

    xx.doneHeaps[ 0 ][ 0 ] = solitaire.createCard(-48, false);
    xx.doneHeaps[ 0 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 0 ][ 0 ].style.left = (xx.fieldwidth  * 6) + "px";

    xx.dealCards.push(xx.doneHeaps[ 3 ][ 0 ]);
    xx.dealCards.push(xx.doneHeaps[ 2 ][ 0 ]);
    xx.dealCards.push(xx.doneHeaps[ 1 ][ 0 ]);
    xx.dealCards.push(xx.doneHeaps[ 0 ][ 0 ]);

    for (var inx = 0; inx < 7; inx++)
    {
        xx.playHeaps[ inx ] = [];

        var ph = xx.playHeaps[ inx ];

        ph[ 0 ] = solitaire.createCard(-1, false);
        ph[ 0 ].style.top  = (xx.fieldheight *   1) + "px";
        ph[ 0 ].style.left = (xx.fieldwidth  * inx) + "px";

        for (var cnt = 0; cnt <= inx; cnt++)
        {
            ph[ cnt + 1 ] = solitaire.createCard(WebLibCards.getCard(), (cnt != inx));
            ph[ cnt + 1 ].style.top  = (xx.fieldheight * (1 + (0.2 * cnt))) + "px";
            ph[ cnt + 1 ].style.left = (xx.fieldwidth  * inx) + "px";

            xx.dealCards.push(ph[ cnt + 1 ]);
        }
   }

    //
    // Heap cards.
    //

    while (WebLibCards.getCount())
    {
        var heapCard = solitaire.createCard(WebLibCards.getCard(), true);
        heapCard.style.top  = (xx.fieldheight * 0) + "px";
        heapCard.style.left = (xx.fieldwidth  * 0) + "px";

        xx.heapCards.push(heapCard);
    }

    for (var inx = 0; inx < xx.dealCards.length; inx++)
    {
        xx.dealCards[ inx ].style.display = "none";
    }

    xx.displayTimeout = setTimeout(xx.displayGame, 100);
}

solitaire.displayGame = function()
{
    var xx = solitaire;

    xx.displayTimeout = null;

    if (xx.dealCards.length > 0)
    {
        var cardDiv = xx.dealCards.shift();
        cardDiv.style.display = "block";

        xx.displayTimeout = setTimeout(xx.displayGame, 100);
    }
    else
    {
        xx.redealTimeout = setTimeout(xx.dealOpen, 500);
    }
}

solitaire.removeOpen = function()
{
    var xx = solitaire;

    xx.redealTimeout = null;

    if (xx.openCards.length > 0)
    {
        var cardDiv = xx.openCards.shift();

        cardDiv.style.display = "none";
        cardDiv.style.top     = (xx.fieldheight * 0) + "px";
        cardDiv.style.left    = (xx.fieldwidth  * 0) + "px";

        xx.showCard(cardDiv, false);

        xx.nextCards.push(cardDiv);

        if (xx.openCards.length > 0)
        {
            xx.redealTimeout = setTimeout(xx.removeOpen, 100);

            return;
        }
    }

    xx.redealTimeout = setTimeout(xx.dealOpen, 100);
}

solitaire.dealOpen = function()
{
    var xx = solitaire;

    xx.redealTimeout = null;

    if ((xx.openCards.length == 0) && (xx.heapCards.length == 0))
    {
        while (xx.nextCards.length > 0)
        {
            var rnd = Math.floor(Math.random() * xx.nextCards.length);
            var cardDiv = xx.nextCards.splice(rnd, 1)[ 0 ];
            cardDiv.style.display = "block";
            xx.heapCards.push(cardDiv);
        }
    }

    if ((xx.openCards.length < 3) && (xx.heapCards.length > 0))
    {
        var cardDiv = xx.heapCards.pop();

        cardDiv.style.top    = (xx.fieldheight * 0) + "px";
        cardDiv.style.left   = (xx.fieldwidth  * (1 + (xx.openCards.length * 0.3))) + "px";
        cardDiv.style.zIndex = "" + (xx.openCards.length + 1);

        xx.showCard(cardDiv, true);

        xx.openCards.push(cardDiv);

        if ((xx.openCards.length < 3) && (xx.heapCards.length > 0))
        {
            xx.redealTimeout = setTimeout(xx.dealOpen, 100);
        }

        return;
    }
}

solitaire.createCard = function(card, back)
{
    var xx = solitaire;

    var marginx = Math.floor(xx.fieldwidth  * 0.1);
    var marginy = Math.floor(xx.fieldheight * 0.175);

    var wid = xx.fieldwidth  - marginx * 2;
    var hei = xx.fieldheight - marginy * 2;

    cardDiv = WebLibSimple.createAnyAppend("center", xx.gamePanel);
    cardDiv.style.position = "absolute";
    cardDiv.style.width    = wid + "px";
    cardDiv.style.height   = hei + "px";
    cardDiv.style.marginLeft   = marginx + "px";
    cardDiv.style.marginRight  = marginx + "px";
    cardDiv.style.marginTop    = marginy + "px";
    cardDiv.style.marginBottom = marginy + "px";

    cardDiv.cardValue = Math.abs(card);
    cardDiv.isBlank   = (card == -1);
    cardDiv.isToken   = (card < 0);
    cardDiv.isBack    = back;

    cardDiv.onTouchClick = solitaire.onClickCard;

    cardDiv.backImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.backImg.style.position = "absolute";
    cardDiv.backImg.style.left     = "0px";
    cardDiv.backImg.style.top      = "0px";
    cardDiv.backImg.style.width    = "auto";
    cardDiv.backImg.style.height   = "100%";
    cardDiv.backImg.src = cardDiv.isToken
        ? WebLibCards.getCardDimmUrl()
        : WebLibCards.getCardBackgroundUrl(false);

    cardDiv.cardImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.cardImg.style.position = "absolute";
    cardDiv.cardImg.style.left     = "0px";
    cardDiv.cardImg.style.top      = "0px";
    cardDiv.cardImg.style.width    = "auto";
    cardDiv.cardImg.style.height   = "100%";
    cardDiv.cardImg.src = WebLibCards.getCardImageUrl(cardDiv.cardValue);

    cardDiv.flipImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.flipImg.style.position = "absolute";
    cardDiv.flipImg.style.display  = "none";
    cardDiv.flipImg.style.left     = "0px";
    cardDiv.flipImg.style.top      = "0px";
    cardDiv.flipImg.style.width    = "auto";
    cardDiv.flipImg.style.height   = "100%";
    cardDiv.flipImg.src = WebLibCards.getCardBacksideUrl();

    if (cardDiv.isBack)
    {
        cardDiv.cardImg.style.display = "none";
        cardDiv.flipImg.style.display = "block";
    }

    if (cardDiv.isBlank)
    {
        cardDiv.cardImg.style.display = "none";
        cardDiv.flipImg.style.display = "none";
    }

    if (xx.allCards) xx.allCards.push(cardDiv);

    return cardDiv;
}

solitaire.showCard = function(cardDiv, show)
{
    if (show)
    {
        cardDiv.cardImg.style.display = "block";
        cardDiv.flipImg.style.display = "none";
        cardDiv.isBack = false;
    }
    else
    {
        cardDiv.cardImg.style.display = "none";
        cardDiv.flipImg.style.display = "block";
        cardDiv.isBack = true;
    }
}

solitaire.checkValidSelect = function(cardDiv, select)
{
    var xx = solitaire;

    if (xx.checkTimeout)
    {
        clearTimeout(xx.checkTimeout);
        xx.checkTimeout = null;
    }

    //
    // Check open cards heap.
    //

    for (var inx = 0; inx < xx.openCards.length - 1; inx++)
    {
        if (xx.openCards[ inx ].select)
        {
            xx.selectCard(xx.openCards[ inx ], false);
        }
    }
}

solitaire.unselectAll = function()
{
    var xx = solitaire;

    for (var inx = 0; inx < xx.allCards.length; inx++)
    {
        if (xx.allCards[ inx ].select)
        {
            //
            // Unselect previous selection.
            //

            xx.allCards[ inx ].backImg.src = xx.allCards[ inx ].isToken
                ? WebLibCards.getCardDimmUrl()
                : WebLibCards.getCardBackgroundUrl(false);

            xx.allCards[ inx ].select = false;
        }
    }
}

solitaire.selectCard = function(cardDiv, select, noauto)
{
    var xx = solitaire;

    if (cardDiv.isBack) return;

    if (select)
    {
        //
        // Check for more than two selected.
        //

        var numSelected = 0;

        for (var inx = 0; inx < xx.allCards.length; inx++)
        {
            if (xx.allCards[ inx ].select) numSelected++;
        }

        if (numSelected >= 2)
        {
            xx.unselectAll();
        }
    }

    //
    // Perform selection on clicked card.
    //

    if (cardDiv.isToken && ! select)
    {
        cardDiv.backImg.src = WebLibCards.getCardDimmUrl();
    }
    else
    {
        cardDiv.backImg.src = WebLibCards.getCardBackgroundUrl(select);
    }

    cardDiv.select = select;

    if (cardDiv.select && ! noauto)
    {
        if (xx.checkTimeout) clearTimeout(xx.checkTimeout);
        xx.checkTimeout = setTimeout(xx.executeMove, 250);
    }
}

solitaire.toggleCard = function(cardDiv)
{
    if (! cardDiv.isBack)
    {
        solitaire.selectCard(cardDiv, ! cardDiv.select);
    }
}

solitaire.createButton = function(text)
{
    var xx = solitaire;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var butsiz = Math.floor((Math.max(wid, hei) - xx.gamesize) * 75 / 200);
    var fntsiz = Math.floor(butsiz * 70 / 100);

    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, butsiz, butsiz, null, solitaire.topdiv);
    WebLibSimple.setFontSpecs(buttonDiv, fntsiz, "bold");

    buttonImg = WebLibSimple.createAnyAppend("img", buttonDiv);
    buttonImg.style.width  = "100%";
    buttonImg.style.height = "100%";
    buttonImg.src = "black_button_256x256.png";

    buttonTab = WebLibSimple.createDivWidHei(0, 0, "100%", "100%", null, buttonDiv);
    buttonTab.style.display = "table";

    buttonCen = WebLibSimple.createAnyAppend("div", buttonTab);
    buttonCen.style.display = "table-cell";
    buttonCen.style.width   = "100%";
    buttonCen.style.height  = "100%";
    buttonCen.style.color   = "#ffffff";
    buttonCen.style.verticalAlign = "middle";
    buttonCen.style.textAlign     = "center";
    buttonCen.innerHTML = text;

    buttonDiv.buttonCen = buttonCen;

    return buttonDiv;
}

solitaire.onWindowResize = function()
{
    var xx = solitaire;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var spacehorz = (wid - xx.gamesize) >> 1;
    var spacevert = (hei - xx.gamesize) >> 1;

    if (spacehorz < spacevert)
    {
        var spacevert = (spacevert - xx.buttonTop1div.clientHeight) >> 1;
        var spacemiddle = (wid >> 1) - (xx.buttonTop1div.clientWidth >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacemiddle + "px";
        xx.buttonTop2div.style.top    = spacevert + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = null;
        xx.buttonTop3div.style.top    = spacevert + "px";
        xx.buttonTop3div.style.right  = spacehorz + "px";
        xx.buttonTop3div.style.bottom = null;

        xx.buttonBot1div.style.left   = spacehorz + "px";
        xx.buttonBot1div.style.top    = null;
        xx.buttonBot1div.style.right  = null;
        xx.buttonBot1div.style.bottom = spacevert + "px";

        xx.buttonBot2div.style.left   = spacemiddle + "px";
        xx.buttonBot2div.style.top    = null;
        xx.buttonBot2div.style.right  = null;
        xx.buttonBot2div.style.bottom = spacevert + "px";

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
    else
    {
        var spacehorz = (spacehorz - xx.buttonTop1div.clientWidth) >> 1;
        var spacemiddle = (hei >> 1) - (xx.buttonTop1div.clientHeight >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacehorz + "px";
        xx.buttonTop2div.style.top    = spacemiddle + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = spacehorz + "px";
        xx.buttonTop3div.style.top    = null;
        xx.buttonTop3div.style.right  = null;
        xx.buttonTop3div.style.bottom = spacevert + "px";

        xx.buttonBot1div.style.left   = null;
        xx.buttonBot1div.style.top    = spacevert + "px";
        xx.buttonBot1div.style.right  = spacehorz + "px";
        xx.buttonBot1div.style.bottom = null;

        xx.buttonBot2div.style.left   = null;
        xx.buttonBot2div.style.top    = spacemiddle + "px";
        xx.buttonBot2div.style.right  = spacehorz + "px";
        xx.buttonBot2div.style.bottom = null;

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
}

solitaire.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    //
    // Check number of selects.
    //

    var numSelected = 0;

    for (var inx = 0; inx < xx.allCards.length; inx++)
    {
        if (xx.allCards[ inx ].select) numSelected++;
    }

    if (numSelected == 2)
    {
        xx.executeMove();
    }

    xx.unselectAll();

    //
    // Check for play stack move.
    //

    for (var inx1 = 0; inx1 < xx.playHeaps.length; inx1++)
    {
        for (var cnt1 = 0; cnt1 < xx.playHeaps[ inx1 ].length; cnt1++)
        {
            var card1 = xx.playHeaps[ inx1 ][ cnt1 ];
            var val1 = Math.floor(card1.cardValue / 4);
            var col1 = card1.cardValue % 4;

            if (card1.isBack || card1.isToken || (val1 == 12)) continue;

            for (var inx2 = 0; inx2 < xx.playHeaps.length; inx2++)
            {
                if (inx1 == inx2) continue;

                for (var cnt2 = 0; cnt2 < xx.playHeaps[ inx2 ].length; cnt2++)
                {
                    var card2 = xx.playHeaps[ inx2 ][ cnt2 ];
                    var val2 = Math.floor(card2.cardValue / 4);
                    var col2 = card2.cardValue % 4;

                    if (card2.isBack || card2.isToken || (val2 == 12)) continue;

                    if (! (((col1 < 2) && (col2 >= 2)) || ((col1 >= 2) && (col2 < 2)))) continue;
                    if (! (((val1 + 1) == val2) || (val2 + 1) == val1)) continue;

                    if (val1 > val2)
                    {
                        if ((cnt1 + 1) != xx.playHeaps[ inx1 ].length)
                        {
                            //
                            // Invalid move.
                            //

                            continue;
                        }

                        if (! (xx.playHeaps[ inx2 ][ cnt2 - 1 ].isBack ||
                               xx.playHeaps[ inx2 ][ cnt2 - 1 ].isToken))
                        {
                            //
                            // Pointless move.
                            //

                            continue;
                        }
                    }

                    if (val2 > val1)
                    {
                        if ((cnt2 + 1) != xx.playHeaps[ inx2 ].length)
                        {
                            //
                            // Invalid move.
                            //

                            continue;
                        }

                        if (! (xx.playHeaps[ inx1 ][ cnt1 - 1 ].isBack ||
                               xx.playHeaps[ inx1 ][ cnt1 - 1 ].isToken))
                        {
                            //
                            // Pointless move.
                            //

                            continue;
                        }
                    }

                    xx.selectCard(card1, true, true);
                    xx.selectCard(card2, true, true);

                    return;
                }
            }
        }
    }

    //
    // Check for king to free play stack move.
    //

    for (var inx1 = 0; inx1 < xx.playHeaps.length; inx1++)
    {
        for (var cnt1 = 0; cnt1 < xx.playHeaps[ inx1 ].length; cnt1++)
        {
            var card1 = xx.playHeaps[ inx1 ][ cnt1 ];
            var val1 = Math.floor(card1.cardValue / 4);
            var col1 = card1.cardValue % 4;

            if (card1.isBack || card1.isToken) continue;
            if (val1 != 11) continue;

            if (cnt1 == 1)
            {
                //
                // Pointless move.
                //

                continue;
            }

            for (var inx2 = 0; inx2 < xx.playHeaps.length; inx2++)
            {
                if (inx1 == inx2) continue;

                if (xx.playHeaps[ inx2 ].length == 1)
                {
                    var card2 = xx.playHeaps[ inx2 ][ 0 ];

                    xx.selectCard(card1, true, true);
                    xx.selectCard(card2, true, true);

                    return;
                }
            }
        }
    }

    //
    // Check for play stack to done move.
    //

    for (var inx1 = 0; inx1 < xx.playHeaps.length; inx1++)
    {
        var cnt1 = xx.playHeaps[ inx1 ].length - 1;
        var card1 = xx.playHeaps[ inx1 ][ cnt1 ];
        var val1 = Math.floor(card1.cardValue / 4);
        var col1 = card1.cardValue % 4;

        if (card1.isBack || card1.isToken) continue;

        for (var inx2 = 0; inx2 < xx.doneHeaps.length; inx2++)
        {
            var cnt2 = xx.doneHeaps[ inx2 ].length - 1;
            var card2 = xx.doneHeaps[ inx2 ][ cnt2 ];
            var val2 = Math.floor(card2.cardValue / 4);
            var col2 = card2.cardValue % 4;

            if (col1 != col2) continue;

            if (((val2 == 12) && (val1 ==  0) && ! card2.isToken) ||
                ((val2 == 12) && (val1 == 12)) ||
                ((val2 != 12) && ((val1 - 1) == val2)))
            {
                xx.selectCard(card1, true, true);
                xx.selectCard(card2, true, true);

                return;
            }
        }
    }

    //
    // Check for open stack to done move.
    //

    if (xx.openCards.length)
    {
        var inx1 = xx.openCards.length - 1;
        var card1 = xx.openCards[ inx1 ];
        var val1 = Math.floor(card1.cardValue / 4);
        var col1 = card1.cardValue % 4;

        for (var inx2 = 0; inx2 < xx.doneHeaps.length; inx2++)
        {
            var cnt2 = xx.doneHeaps[ inx2 ].length - 1;
            var card2 = xx.doneHeaps[ inx2 ][ cnt2 ];
            var val2 = Math.floor(card2.cardValue / 4);
            var col2 = card2.cardValue % 4;

            if (col1 != col2) continue;

            if (((val2 == 12) && (val1 ==  0) && ! card2.isToken) ||
                ((val2 == 12) && (val1 == 12)) ||
                ((val2 != 12) && ((val1 - 1) == val2)))
            {
                xx.selectCard(card1, true, true);
                xx.selectCard(card2, true, true);

                return;
            }
        }
    }

    //
    // Check for open stack to play stack move.
    //

    if (xx.openCards.length)
    {
        var inx1 = xx.openCards.length - 1;
        var card1 = xx.openCards[ inx1 ];
        var val1 = Math.floor(card1.cardValue / 4);
        var col1 = card1.cardValue % 4;

        for (var inx2 = 0; inx2 < xx.playHeaps.length; inx2++)
        {
            var cnt2 = xx.playHeaps[ inx2 ].length - 1;
            var card2 = xx.playHeaps[ inx2 ][ cnt2 ];
            var val2 = Math.floor(card2.cardValue / 4);
            var col2 = card2.cardValue % 4;

            if ((cnt2 == 0) && (val1 == 11))
            {
                //
                // King to empty play stack.
                //

                xx.selectCard(card1, true, true);
                xx.selectCard(card2, true, true);

                return;
            }

            if (! (((col1 < 2) && (col2 >= 2)) || ((col1 >= 2) && (col2 < 2)))) continue;

            if (((val1 == 12) && (val2 ==  0)) ||
                ((val1 == 12) && (val2 == 12)) ||
                ((val1 + 1) == val2))
            {
                xx.selectCard(card1, true, true);
                xx.selectCard(card2, true, true);

                return;
            }
        }
    }
    
    //
    // Redeal.
    //

    if (xx.allCards && xx.allCards.length)
    {
        xx.removeOpen();
    }
}

solitaire.onRedealOpen = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    if (xx.redealTimeout || xx.displayTimeout) return;

    if (xx.allCards && xx.allCards.length)
    {
        xx.removeOpen();
    }
    else
    {
        xx.createGame();
    }
}

solitaire.isDoneStack = function(card)
{
    var xx = solitaire;

    for (var inx = 0; inx < 4; inx++)
    {
        if (xx.doneHeaps[ inx ][ xx.doneHeaps[ inx ].length - 1 ] == card)
        {
            return xx.doneHeaps[ inx ];
        }
    }

    return false;
}

solitaire.isOpenStack = function(card)
{
    var xx = solitaire;

    for (var inx = 0; inx < xx.openCards.length; inx++)
    {
        if (xx.openCards[ inx ] == card)
        {
            return xx.openCards;
        }
    }

    return false;
}

solitaire.isPlayStack = function(card)
{
    var xx = solitaire;

    for (var inx = 0; inx < xx.playHeaps.length; inx++)
    {
        for (var cnt = 0; cnt < xx.playHeaps[ inx ].length; cnt++)
        {
            if (xx.playHeaps[ inx ][ cnt ] == card)
            {
                return xx.playHeaps[ inx ];
            }
        }
    }

    return false;
}

solitaire.getPlayStackIndex = function(card)
{
    var xx = solitaire;

    for (var inx = 0; inx < xx.playHeaps.length; inx++)
    {
        for (var cnt = 0; cnt < xx.playHeaps[ inx ].length; cnt++)
        {
            if (xx.playHeaps[ inx ][ cnt ] == card)
            {
                return inx;
            }
        }
    }

    return -1;
}

solitaire.detachCard = function(card)
{
    var xx = solitaire;

    if (xx.openCards[ xx.openCards.length - 1 ] == card)
    {
        xx.openCards.pop();

        return;
    }

    for (var inx = 0; inx < 7; inx++)
    {
        for (var cnt = 0; cnt < xx.playHeaps[ inx ].length; cnt++)
        {
            if (xx.playHeaps[ inx ][ cnt ] == card)
            {
                xx.playHeaps[ inx ].splice(cnt, 1);

                return;
            }
        }
    }
}

solitaire.doZindex = function(cardStack)
{
    for (var inx = 0; inx < cardStack.length; inx++)
    {
        cardStack[ inx ].style.zIndex = "" + inx;
    }
}

solitaire.checkMatch = function(card1, card2)
{
    var xx = solitaire;

    //
    // Card 1 is the lower card.
    //

    var val1 = Math.floor(card1.cardValue / 4);
    var col1 = card1.cardValue % 4;
    var val2 = Math.floor(card2.cardValue / 4);
    var col2 = card2.cardValue % 4;

    console.log("solitaire.checkMatch: card1=" + card1.cardValue + " " + val1 + ":" + col1 + ":" + card1.isToken);
    console.log("solitaire.checkMatch: card2=" + card2.cardValue + " " + val2 + ":" + col2 + ":" + card2.isToken);

    if (xx.isDoneStack(card2))
    {
        //
        // Higher card is done stack.
        //

        console.log("solitaire.checkMatch: exit1");

        return false;
    }

    if (xx.isDoneStack(card1))
    {
        console.log("solitaire.checkMatch: doneStack");

        if (col1 != col2) return false;

        if (! (((val1 == 12) && (val2 ==  0)) ||
               ((val1 == 12) && (val2 == 12)) ||
               ((val1 + 1) == val2)))
        {
            //
            // Cards do not match.
            //

            return false;
        }

        xx.detachCard(card2);

        xx.doneHeaps[ col1 ].push(card2);

        card2.style.top  = (xx.fieldheight * 0) + "px";
        card2.style.left = (xx.fieldwidth  * (6 - col1)) + "px";

        xx.doZindex(xx.doneHeaps[ col1 ]);

        return true;
    }

    var stack1 = xx.isOpenStack(card1);
    var stack2 = xx.isPlayStack(card2);

    if (stack1 && stack2)
    {
        console.log("solitaire.checkMatch: openStack => playStack");

        if (((val1 + 1) == val2) && (((col1 < 2) && (col2 >= 2)) || ((col1 >= 2) && (col2 < 2))))
        {
            //
            // Cards match.
            //

            xx.detachCard(card1);

            stack2.push(card1);

            var toppos = stack2.length - 2;

            card1.style.top  = (xx.fieldheight * (1 + (0.2 * toppos))) + "px";
            card1.style.left = (xx.fieldwidth * xx.getPlayStackIndex(card2)) + "px";

            xx.doZindex(stack2);

            return true;
        }


        return false;
   }

    var stack1 = xx.isPlayStack(card1);
    var stack2 = xx.isOpenStack(card2);

    if (stack1 && stack2)
    {
        if (card1.isToken && (val2 == 11))
        {
            //
            // King matches open slot.
            //

            xx.detachCard(card2);

            stack1.push(card2);

            var toppos = stack1.length - 2;

            card2.style.top  = (xx.fieldheight * (1 + (0.2 * toppos))) + "px";
            card2.style.left = (xx.fieldwidth * xx.getPlayStackIndex(card1)) + "px";

            xx.doZindex(stack1);

            return true;
        }
    }

    var stack1 = xx.isPlayStack(card1);
    var stack2 = xx.isPlayStack(card2);

    if (stack1 && stack2)
    {
        console.log("solitaire.checkMatch: playStack => playStack");

        if ((stack1.length == 1) && card1.isToken)
        {
            //
            // Stack 1 ist empty slot with token card so switch cards.
            //

            var tmp = card1;
            card1 = card2;
            card2 = tmp;

            stack1 = xx.isPlayStack(card1);
            stack2 = xx.isPlayStack(card2);

            val2 = Math.floor(card2.cardValue / 4);
            col2 = card2.cardValue % 4;
        }

        var moveStart = stack1.length;

        for (var inx = stack1.length - 1; inx >= 0; inx--)
        {
            card1 = stack1[ inx ];

            if (card1.isBack || card1.isToken) break;

            val1 = Math.floor(card1.cardValue / 4);
            col1 = card1.cardValue % 4;

            if (((val1 + 1) == val2) && (((col1 < 2) && (col2 >= 2)) || ((col1 >= 2) && (col2 < 2))))
            {
                //
                // Normal cards do match.
                //

                moveStart = inx;

                break;
            }

            if (card2.isToken && (val1 == 11))
            {
                //
                // King matches placeholder token card.
                //

                moveStart = inx;

                break;
            }
        }

        while (stack1.length > moveStart)
        {
            card1 = stack1[ moveStart ];

            xx.detachCard(card1);

            stack2.push(card1);

            var toppos = stack2.length - 2;

            card1.style.top  = (xx.fieldheight * (1 + (0.2 * toppos))) + "px";
            card1.style.left = (xx.fieldwidth * xx.getPlayStackIndex(card2)) + "px";
        }

        xx.doZindex(stack2);

        return true;
    }

    return false;
}

solitaire.executeMove = function()
{
    var xx = solitaire;

    xx.checkValidSelect();

    var selected = [];

    for (var inx = 0; inx < xx.allCards.length; inx++)
    {
        if (xx.allCards[ inx ].select) selected.push(xx.allCards[ inx ]);
    }

    if (selected.length != 2) return;

    var lowcard;
    var higcard;

    if ((selected[ 0 ].cardValue >= 48) ||
        ((selected[ 0 ].cardValue < selected[ 1 ].cardValue) && (selected[ 1 ].cardValue < 48)) ||
         selected[ 0 ].isToken)
    {
        lowcard = selected[ 0 ];
        higcard = selected[ 1 ];
    }
    else
    {
        lowcard = selected[ 1 ];
        higcard = selected[ 0 ];
    }

    if (xx.checkMatch(lowcard, higcard))
    {
        //
        // Flip last card on play stacks.
        //

        for (var inx = 0; inx < xx.playHeaps.length; inx++)
        {
            var lastCard = xx.playHeaps[ inx ][ xx.playHeaps[ inx ].length - 1 ];
            var lastCard = xx.playHeaps[ inx ][ xx.playHeaps[ inx ].length - 1 ];

            if (lastCard.isBack)
            {
                xx.showCard(lastCard, true);
            }
        }
    }

    xx.unselectAll();
}

solitaire.onExecuteMove = function(target, ctarget)
{
    WebAppUtility.makeClick();

    solitaire.executeMove();
}

solitaire.onNewGame = function(target, ctarget)
{
    solitaire.createGame();
}

solitaire.onClickCard = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    if (xx.redealTimeout || xx.displayTimeout) return;

    if (xx.allCards && xx.allCards.length)
    {
        if (target == xx.heapToken)
        {
            if (! xx.redealTimeout)
            {
                xx.redealNow = true;
                xx.removeOpen();
            }

            return;
        }

        //
        // Check if heap card.
        //

        for (var inx = 0; inx < xx.heapCards.length; inx++)
        {
            if (target == xx.heapCards[ inx ])
            {
                if (! xx.redealTimeout) xx.removeOpen();

                return;
            }
        }

        xx.toggleCard(target);
    }
    else
    {
        xx.createGame();
    }
}

solitaire.createFrame();
solitaire.onWindowResize();
