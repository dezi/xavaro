instaface.createTest = function()
{
    var ic = instaface;

    ic.pre = WebLibSimple.createAnyAppend("pre", document.body);

    ic.targets = JSON.parse(WebAppFacebook.getTargets());

    ic.pre.innerHTML += "===============>targets\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.targets)) + "\n";

    ic.feed = JSON.parse(WebAppFacebook.getFeed(ic.targets[ 0 ].id));

    if (ic.feed.length > 0)
    {
        var postid = ic.feed[ 0 ].id;
        ic.post = JSON.parse(WebAppFacebook.getPost(postid));

        ic.pre.innerHTML += "===============>post\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.post)) + "\n";
    }

    ic.pre.innerHTML += "===============>feed\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feed)) + "\n";

    if (ic.target[ 0 ].type == "owner")
    {
        ic.userfeeds = JSON.parse(WebAppFacebook.getUserFeeds());

        ic.pre.innerHTML += "===============>userfeeds\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.userfeeds)) + "\n";
    }
}

instaface.createTest();

