//
// Touch events support.
//

WebLibTouch = {};

WebLibTouch.minDist = 20;

WebLibTouch.onTouchStart = function(event)
{
    var touchobj = event.changedTouches[ 0 ];
    var touch = WebLibTouch.touch = {};
    var target = touchobj.target;

    while (target)
    {
        if (target.scrollHorizontal || target.scrollVertical || target.scrollBoth)
        {
            touch.starget = target;
        }

        if (target.onTouchClick)
        {
            touch.ctarget = target;
        }

        target = target.parentElement;
    }

    if (! (touch.starget || touch.ctarget)) return;

    touch.moves = 0;
    touch.initial = true;
    touch.target  = touchobj.target;

    touch.startX = touchobj.clientX;
    touch.startY = touchobj.clientY;

    touch.clientX = touchobj.clientX;
    touch.clientY = touchobj.clientY;

    if (touch.starget)
    {
        touch.scrollVertical   = touch.starget.scrollVertical   || touch.starget.scrollBoth;
        touch.scrollHorizontal = touch.starget.scrollHorizontal || touch.starget.scrollBoth;

        touch.offsetTop  = touch.starget.offsetTop;
        touch.offsetLeft = touch.starget.offsetLeft;

        touch.clientWidth  = touch.starget.clientWidth;
        touch.clientHeight = touch.starget.clientHeight;

        touch.parentWidth  = touch.starget.parentElement.clientWidth;
        touch.parentHeight = touch.starget.parentElement.clientHeight;

        if (touch.scrollVertical && (touch.clientHeight < touch.parentHeight))
        {
            touch.scrollVertical = false;
        }

        if (touch.scrollHorizontal && (touch.clientWidth < touch.parentWidth))
        {
            touch.scrollHorizontal = false;
        }

        if (! (touch.scrollVertical || touch.scrollHorizontal))
        {
            touch.starget = null;
        }
    }

    if (touch.ctarget)
    {
        touch.bgcolor = touch.ctarget.style.backgroundColor;
        //touch.ctarget.style.backgroundColor = "#dddddd";
    }

    event.preventDefault();
}

WebLibTouch.onTouchMove = function(event)
{
    var touch = WebLibTouch.touch;
    var touchobj = event.changedTouches[ 0 ];

    if (! (touch.starget || touch.ctarget)) return;

    touch.moves += 1;

    WebLibTouch.computeOffsets(touchobj);

    if (touch.starget)
    {
        //
        // Ignore small initial movements.
        //

        if ((Math.abs(touch.deltaX) > WebLibTouch.minDist) ||
            (Math.abs(touch.deltaY) > WebLibTouch.minDist) || ! touch.initial)
        {
            if (touch.initial)
            {
                if (Math.abs(touch.deltaX) > WebLibTouch.minDist)
                {
                    touch.disableVertical = true;
                }
                else
                {
                    if (Math.abs(touch.deltaY) > WebLibTouch.minDist)
                    {
                        touch.disableHorizontal = true;
                    }
                }
            }

            if (touch.newX > 0) touch.newX = Math.log(touch.newX) * 10;
            if (touch.newY > 0) touch.newY = Math.log(touch.newY) * 10;

            if ((touch.newX + touch.clientWidth) < touch.parentWidth)
            {
                var overX = touch.newX - (touch.parentWidth - touch.clientWidth);
                touch.newX = (touch.parentWidth - touch.clientWidth) - Math.log(-overX) * 10;
            }

            if ((touch.newY + touch.clientHeight) < touch.parentHeight)
            {
                var overY = touch.newY - (touch.parentHeight - touch.clientHeight);
                touch.newY = (touch.parentHeight - touch.clientHeight) - Math.log(-overY) * 10;
            }

            WebLibTouch.setOffsets();

            touch.initial = false;
       }
    }

    if (touch.ctarget)
    {
        if ((Math.abs(touch.deltaX) > WebLibTouch.minDist) ||
            (Math.abs(touch.deltaY) > WebLibTouch.minDist))
        {
            //
            // Discard click touch events.
            //

            touch.ctarget.style.backgroundColor = touch.bgcolor;
            touch.ctarget = null;
        }
    }

    event.preventDefault();
}

WebLibTouch.onTouchEnd = function(event)
{
    var touch = WebLibTouch.touch;
    var touchobj = event.changedTouches[ 0 ];

    if (! (touch.starget || touch.ctarget)) return;

    WebLibTouch.computeOffsets(touchobj);

    if (touch.starget)
    {
        if ((touch.newX + touch.clientWidth) < touch.parentWidth)
        {
            touch.newX = touch.parentWidth - touch.clientWidth;
        }

        if ((touch.newY + touch.clientHeight) < touch.parentHeight)
        {
            touch.newY = touch.parentHeight - touch.clientHeight;
        }

        if (touch.newX > 0) touch.newX = 0;
        if (touch.newY > 0) touch.newY = 0;

        WebLibTouch.setOffsets();
    }

    if (touch.ctarget)
    {
        touch.ctarget.style.backgroundColor = touch.bgcolor;
        touch.ctarget.onTouchClick(touch.ctarget, touch.target);
    }

    event.preventDefault();
}

WebLibTouch.setOffsets = function()
{
    var touch = WebLibTouch.touch;

    if (touch.starget)
    {
        var callbackNewX = null;
        var callbackNewY = null;

        if (touch.scrollHorizontal && ! touch.disableHorizontal)
        {
            callbackNewX = touch.newX;
            touch.starget.style.left = touch.newX + "px";
        }

        if (touch.scrollVertical && ! touch.disableVertical)
        {
            callbackNewY = touch.newY;
            touch.starget.style.top = touch.newY + "px";
        }

        if (touch.starget.onTouchScroll) touch.starget.onTouchScroll(callbackNewX, callbackNewY);
    }
}

WebLibTouch.computeOffsets = function(touchobj)
{
    var touch = WebLibTouch.touch;

    touch.clientX = touchobj.clientX;
    touch.clientY = touchobj.clientY;

    touch.speedX = Math.abs((touchobj.clientX - touch.startX) / touch.moves);
    touch.speedY = Math.abs((touchobj.clientY - touch.startY) / touch.moves);

    if (touch.speedX < 1.0) touch.speedX = 1.0;
    if (touch.speedY < 1.0) touch.speedY = 1.0;
    if (touch.speedX > 5.0) touch.speedX = 5.0;
    if (touch.speedY > 5.0) touch.speedY = 5.0;

    touch.speedX = 2.0;
    touch.speedY = 2.0;

    touch.deltaX = (touchobj.clientX - touch.startX) * touch.speedX;
    touch.deltaY = (touchobj.clientY - touch.startY) * touch.speedY;

    if (touch.starget)
    {
        touch.newX = touch.offsetLeft + touch.deltaX;
        touch.newY = touch.offsetTop  + touch.deltaY;
    }
}

addEventListener("touchstart", WebLibTouch.onTouchStart);
addEventListener("touchmove", WebLibTouch.onTouchMove);
addEventListener("touchend", WebLibTouch.onTouchEnd);
