instaface.createTest = function()
{
    var ic = instaface;

    ic.pre = WebLibSimple.createAnyAppend("pre", document.body);

    ic.targets = JSON.parse(WebAppSocial.getTargets());

    ic.pre.innerHTML += "===============>targets\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.targets)) + "\n";

    if (ic.targets[ 0 ].type == "owner")
    {
        ic.userfeeds = JSON.parse(WebAppSocial.getUserFeeds());

        ic.pre.innerHTML += "===============>userfeeds\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.userfeeds)) + "\n";
    }

    ic.feed = JSON.parse(WebAppSocial.getFeed(ic.targets[ 0 ].plat, ic.targets[ 0 ].id));

    if (ic.feed.length > 0)
    {
        var postid = ic.feed[ 0 ].id;
        var platform = ic.targets[ 0 ].plat;
        ic.post = JSON.parse(WebAppSocial.getPost(platform, postid));

        ic.pre.innerHTML += "===============>post\n";
        ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.post)) + "\n";
    }

    ic.pre.innerHTML += "===============>feed\n";
    ic.pre.innerHTML += WebAppUtility.getPrettyJson(JSON.stringify(ic.feed)) + "\n";

}

instaface.createTest();

