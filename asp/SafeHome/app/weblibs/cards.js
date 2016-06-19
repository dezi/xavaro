//
// Card games support.
//

WebLibCards = {};

WebLibCards.cardPoker =
[
    "/weblibs/cards/set1/ace_of_clubs.png",
    "/weblibs/cards/set1/ace_of_spades.png",
    "/weblibs/cards/set1/ace_of_hearts.png",
    "/weblibs/cards/set1/ace_of_diamonds.png",
    "/weblibs/cards/set1/king_of_clubs2.png",
    "/weblibs/cards/set1/king_of_spades2.png",
    "/weblibs/cards/set1/king_of_hearts2.png",
    "/weblibs/cards/set1/king_of_diamonds2.png",
    "/weblibs/cards/set1/queen_of_clubs2.png",
    "/weblibs/cards/set1/queen_of_spades2.png",
    "/weblibs/cards/set1/queen_of_hearts2.png",
    "/weblibs/cards/set1/queen_of_diamonds2.png",
    "/weblibs/cards/set1/jack_of_clubs2.png",
    "/weblibs/cards/set1/jack_of_spades2.png",
    "/weblibs/cards/set1/jack_of_hearts2.png",
    "/weblibs/cards/set1/jack_of_diamonds2.png",
    "/weblibs/cards/set1/10_of_clubs.png",
    "/weblibs/cards/set1/10_of_spades.png",
    "/weblibs/cards/set1/10_of_hearts.png",
    "/weblibs/cards/set1/10_of_diamonds.png",
    "/weblibs/cards/set1/9_of_clubs.png",
    "/weblibs/cards/set1/9_of_spades.png",
    "/weblibs/cards/set1/9_of_hearts.png",
    "/weblibs/cards/set1/9_of_diamonds.png",
    "/weblibs/cards/set1/8_of_clubs.png",
    "/weblibs/cards/set1/8_of_spades.png",
    "/weblibs/cards/set1/8_of_hearts.png",
    "/weblibs/cards/set1/8_of_diamonds.png",
    "/weblibs/cards/set1/7_of_clubs.png",
    "/weblibs/cards/set1/7_of_spades.png",
    "/weblibs/cards/set1/7_of_hearts.png",
    "/weblibs/cards/set1/7_of_diamonds.png",
    "/weblibs/cards/set1/6_of_clubs.png",
    "/weblibs/cards/set1/6_of_spades.png",
    "/weblibs/cards/set1/6_of_hearts.png",
    "/weblibs/cards/set1/6_of_diamonds.png",
    "/weblibs/cards/set1/5_of_clubs.png",
    "/weblibs/cards/set1/5_of_spades.png",
    "/weblibs/cards/set1/5_of_hearts.png",
    "/weblibs/cards/set1/5_of_diamonds.png",
    "/weblibs/cards/set1/4_of_clubs.png",
    "/weblibs/cards/set1/4_of_spades.png",
    "/weblibs/cards/set1/4_of_hearts.png",
    "/weblibs/cards/set1/4_of_diamonds.png",
    "/weblibs/cards/set1/3_of_clubs.png",
    "/weblibs/cards/set1/3_of_spades.png",
    "/weblibs/cards/set1/3_of_hearts.png",
    "/weblibs/cards/set1/3_of_diamonds.png",
    "/weblibs/cards/set1/2_of_clubs.png",
    "/weblibs/cards/set1/2_of_spades.png",
    "/weblibs/cards/set1/2_of_hearts.png",
    "/weblibs/cards/set1/2_of_diamonds.png"
];

WebLibCards.newDeck = function(cards)
{
    WebLibCards.playDeck = [];

    for (var inx = 0; inx < cards; inx++) WebLibCards.playDeck[ inx ] = inx;
}

WebLibCards.getCard = function()
{
    var rnd = Math.floor(Math.random() * WebLibCards.playDeck.length);

    return WebLibCards.playDeck.splice(rnd,1)[ 0 ];
}

WebLibCards.putCard = function(card)
{
    return WebLibCards.playDeck.push(card);
}

WebLibCards.getCount = function()
{
    return WebLibCards.playDeck.length;
}

WebLibCards.getCardImageUrl = function(card)
{
    return WebLibCards.cardPoker[ 51 - card ];
}

WebLibCards.getCardBacksideUrl = function()
{
    return "/weblibs/cards/backside_red_320x460.png";
}

WebLibCards.getCardBackgroundUrl = function(selected)
{
    if (selected)
    {
        return "/weblibs/cards/set1/0_background_green.png";
    }

    return "/weblibs/cards/set1/0_background_white.png";
}

WebLibCards.getCardDimmUrl = function()
{
    return "/weblibs/cards/set1/0_background_dimm.png";
}
