instaface.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffff88");

    var ic = instaface;

    ic.topDiv = WebLibSimple.createAnyAppend("div", document.body);
    WebLibSimple.setFontSpecs(ic.topDiv, 24, "bold", "#666666");
    ic.topDiv.style.padding = "20px";
}

instaface.createFrame();

  window.fbAsyncInit = function()
  {
    alert("pupsi");

    FB.init({
      appId      : '610331582448824',
      xfbml      : false,
      version    : 'v2.5'
    });

FB.getLoginStatus(function(response)
{
    alert("getLoginStatus");
});


  };

/*
  (function(d, s, id)
  {
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "//connect.facebook.net/en_US/sdk/debug.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));
*/