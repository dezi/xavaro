instaface.createTest = function()
{
    var ic = instaface;

    ic.pre = WebLibSimple.createAnyAppend("pre", document.body);

    ic.target = JSON.parse(WebAppFacebook.getTarget());

    ic.pre.innerHTML += "===============>target\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.target)) + "\n";

    ic.feed = JSON.parse(WebAppFacebook.getFeed(ic.target.id));

    if (ic.feed.length > 0)
    {
        var postid = ic.feed[ 0 ].id;
        ic.post = JSON.parse(WebAppFacebook.getPost(postid));

        ic.pre.innerHTML += "===============>post\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.post)) + "\n";
    }

    ic.pre.innerHTML += "===============>feed\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feed)) + "\n";

    if (ic.target.type == "owner")
    {
        ic.userfeeds = JSON.parse(WebAppFacebook.getUserFeeds());

        ic.pre.innerHTML += "===============>userfeeds\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.userfeeds)) + "\n";
    }
}

instaface.createTest();

