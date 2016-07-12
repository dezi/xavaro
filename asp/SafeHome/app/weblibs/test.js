//
// Laterpay defuscator.
//
// javascript:(function(){document.getElementsByTagName('head')[0].appendChild(document.createElement('script')).src='http://192.168.1.150/weblibs/test.js?'+Math.random();}());

// Defuscate text string.

function df(o)
{
    var t="";

    for (var f=0;f<o.length;f++)
    {
        var c=o.charAt(f);

        // Subtract 1 from UTF-8 character code with a few exceptions.

        t+=c=="±"?"&":c=="´"?";":c==" "?" ":String.fromCharCode(o.charCodeAt(f)-1);
    }

    return t.replace("&amp;","&");
}

// Recurse DOM HTML elements.

function lp(elem)
{
    for (var inx = 0; inx < elem.children.length; inx++)
    {
        var child = elem.children[ inx ];

        lp(child);

        // Identify Laterpay popup and blur.

        if (child.className && child.className.indexOf && child.className.indexOf("laterpay-under-overlay") >= 0)
        {
            // Remove blur css style.

            child.nextSibling.className = null;
        }

        // Defuscate a paragraph

        if (child.className == "obfuscated")
        {
            var n=child.childNodes;

            for (var cnt = 0; cnt < n.length; cnt++)
            {
                var z=n[ cnt ];

                if (z.nodeName=="#text") z.nodeValue = df(z.nodeValue);
                if (z.nodeName=="B"||z.nodeName=="I") z.innerHTML = df(z.innerHTML);
            }
        }
    }
}

lp(document.body);
