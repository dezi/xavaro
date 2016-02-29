//
// Touch events support.
//

WebLibTouch = {};

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

    if (! (touch.starget || touch.ctarget))
    {
        console.log("not for me...");

        return;
    }

    touch.moves = 0;
    touch.initial = true;

    touch.startX = touchobj.clientX;
    touch.startY = touchobj.clientY;

    touch.clientX = touchobj.clientX;
    touch.clientY = touchobj.clientY;

    if (touch.starget)
    {
        touch.offsetTop  = touch.starget.offsetTop;
        touch.offsetLeft = touch.starget.offsetLeft;

        touch.clientWidth  = touch.starget.clientWidth;
        touch.clientHeight = touch.starget.clientHeight;

        touch.parentWidth  = touch.starget.parentElement.clientWidth;
        touch.parentHeight = touch.starget.parentElement.clientHeight;
    }

    if (touch.ctarget)
    {
        touch.bgcolor = touch.ctarget.style.backgroundColor;
        touch.ctarget.style.backgroundColor = "#dddddd";
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

        if ((Math.abs(touch.deltaX) > 10) || (Math.abs(touch.deltaY) > 10) || ! touch.initial)
        {
            touch.initial = false;

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
        }
    }

    if (touch.ctarget)
    {
        if ((Math.abs(touch.deltaX) > 10) || (Math.abs(touch.deltaY) > 10))
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
        if (touch.newX > 0) touch.newX = 0;
        if (touch.newY > 0) touch.newY = 0;

        if ((touch.newX + touch.clientWidth) < touch.parentWidth)
        {
            touch.newX = touch.parentWidth - touch.clientWidth;
        }

        if ((touch.newY + touch.clientHeight) < touch.parentHeight)
        {
            touch.newY = touch.parentHeight - touch.clientHeight;
        }

        WebLibTouch.setOffsets();
    }

    if (touch.ctarget)
    {
        touch.ctarget.style.backgroundColor = touch.bgcolor;
        touch.ctarget.onTouchClick(touch.ctarget);
    }

    event.preventDefault();
}

WebLibTouch.setOffsets = function()
{
    var touch = WebLibTouch.touch;

    if (touch.starget)
    {
        if (touch.starget.scrollVertical || touch.starget.scrollBoth)
        {
            touch.starget.style.top = touch.newY + "px";
        }

        if (touch.starget.scrollHorizontal || touch.starget.scrollBoth)
        {
            touch.starget.style.left = touch.newX + "px";
        }
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

window.addEventListener("touchstart", WebLibTouch.onTouchStart);
window.addEventListener("touchmove", WebLibTouch.onTouchMove);
window.addEventListener("touchend", WebLibTouch.onTouchEnd);
