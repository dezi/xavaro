// javascript:(function(){document.getElementsByTagName('head')[0].appendChild(document.createElement('script')).src='http://192.168.2.103/weblibs/test.js?'+Math.random();}());
// javascript:(function(){document.getElementsByTagName('head')[0].appendChild(document.createElement('script')).src='http://192.168.1.150/weblibs/test.js?'+Math.random();}());

function df(o)
{
    var t="";

    for (var f=0;f<o.length;f++)
    {
        var c=o.charAt(f);

        t+=c=="±"?"&":c=="´"?";":c==" "?" ":String.fromCharCode(o.charCodeAt(f)-1);
    }

    return t.replace("&amp;","&");
}

function lp(elem)
{
    for (var inx = 0; inx < elem.children.length; inx++)
    {
        var child = elem.children[ inx ];

        lp(child);

        if (child.className && child.className.indexOf && child.className.indexOf("filter-blur") >= 0)
        {
            child.className = null;
            child.nextSibling.innerHTML = null;
        }

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
