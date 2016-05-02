//
// API Test...
//

instaface.pre = WebLibSimple.createAnyAppend("pre", document.body);

instaface.target = WebAppFacebook.getTarget();
instaface.pre.innerHTML += "\n===============>target";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(instaface.target);

var fields = [ "id", "name", "currency" ];
var params = { "fields": fields };

var info = WebAppFacebook.getGraphSync("/me", JSON.stringify(params));
instaface.pre.innerHTML += "\n===============>user";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(info);

var data = WebAppFacebook.getUserFeedFriends();
instaface.pre.innerHTML += "\n===============>friends";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);

instaface.pre.innerHTML += "\n===============>likes";
var data = WebAppFacebook.getUserFeedLikes();
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);

var likes = JSON.parse(data);
var likeid = likes[ 0 ].id;

var fields = [ "id", "message", "link", "place", "privacy" ];
var params = { "fields": fields };

var data = WebAppFacebook.getGraphSync("/" + likeid + "/feed", JSON.stringify(params));
instaface.pre.innerHTML += "\n===============>feed";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);

var feed = JSON.parse(data);
var storyid = feed.data[ 0 ].id;

var fields =
[
    "id", "caption", "description", "icon",
    "link", "message", "message_tags", "picture",
    "place", "shares", "source", "type"
];

var params = { "fields": fields };

var data = WebAppFacebook.getGraphSync("/" + storyid, JSON.stringify(params));
instaface.pre.innerHTML += "\n===============>post";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);

var data = WebAppFacebook.getGraphSync("/" + storyid + "/comments");
instaface.pre.innerHTML += "\n===============>post/comments";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);

var data = WebAppFacebook.getGraphSync("/" + storyid + "/attachments");
instaface.pre.innerHTML += "\n===============>post/attachments";
instaface.pre.innerHTML += "\n" + WebAppUtility.getPrettyJson(data);
