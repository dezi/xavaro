<!DOCTYPE html>
<HTML>
   <HEAD>
      <TITLE>
         Instagram Landing Page.
      </TITLE>
	  <script type="text/javascript" src="https://raw.githubusercontent.com/stevenschobert/instafeed.js/master/instafeed.js"></script>
   </HEAD>
<BODY>
<center>
   <H1>Instagram Landing Page.</H1>
<div id="instafeed"></div>
<script type="text/javascript">
    var feed = new Instafeed({
        get: 'tagged',
        tagName: 'awesome',
        clientId: '63afb3307ec24f4886230f44d2fda884'
    });
    //feed.run();
</script>
<h1><a href="https://www.instagram.com/oauth/authorize?client_id=63afb3307ec24f4886230f44d2fda884&redirect_uri=http://www.xavaro.de/instagram&scope=basic+public_content+follower_list+likes&response_type=code"> Authorize Xavaro for Instagram</a></h1>
</center>
</BODY>
</HTML>
