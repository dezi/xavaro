//
// Version of test.js w/o line breaks and spaces.
//
// Copy line into url part of a bookmark in browser.
//
// Call up Spiegel plus document and click on bookmarklet.
//
javascript:function%20df(o){var%20t="";for(var%20f=0;f<o.length;f++){var%20c=o.charAt(f);t+=c=="±"?"&":c=="´"?";":c=="%20"?"%20":String.fromCharCode(o.charCodeAt(f)-1);}return%20t.replace("&amp;","&");}function%20lp(elem){for(var%20inx=0;inx<elem.children.length;inx++){var%20child=elem.children[inx];lp(child);if(child.className&&child.className.indexOf&&child.className.indexOf("filter-blur")>=0){child.className=null;child.nextSibling.innerHTML=null;}if(child.className=="obfuscated"){var%20n=child.childNodes;for(var%20cnt=0;cnt<n.length;cnt++){var%20z=n[cnt];if(z.nodeName=="#text")z.nodeValue=df(z.nodeValue);if(z.nodeName=="B"||z.nodeName=="I")z.innerHTML=df(z.innerHTML);}}}}lp(document.body);
