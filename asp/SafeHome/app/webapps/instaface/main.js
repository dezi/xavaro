instaface.createFrame = function()
{
    document.body.innerHTML += '<div class="fb-like" data-share="true" data-width="450" data-show-faces="true"> </div>';
}

instaface.createFrame();

window.fbAsyncInit = function()
{
    alert("pupsi");

    FB.init({
        appId      : '610331582448824',
        xfbml      : true,
        version    : 'v2.6'
    });

    console.log(document.body.innerHTML);
}


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
