//
// Social utility library.
//

WebLibSocial = {};

WebLibSocial.getPostId = function(platform, post)
{
    if (platform == "twitter")
    {
        if (post.id_str) return post.id_str;
    }

    if (platform == "facebook")
    {
        if (post.id) return post.id;
    }

    if (platform == "googleplus")
    {
        if (post.id) return post.id;
    }

    if (platform == "instagram")
    {
        if (post.id) return post.id;
    }

    return null;
}

WebLibSocial.getPostDate = function(platform, post)
{
    if (platform == "twitter")
    {
        if (post.created_at) return new Date(post.created_at).getTime();
    }

    if (platform == "facebook")
    {
        if (post.created_time) return new Date(post.created_time).getTime();
    }

    if (platform == "googleplus")
    {
        if (post.updated) return new Date(post.updated).getTime();
        if (post.published) return new Date(post.published).getTime();
    }

    if (platform == "instagram")
    {
        if (post.created_time) return new Date(parseInt(post.created_time) * 1000).getTime();
    }

    return 0;
}

WebLibSocial.getPostName = function(platform, post)
{
    if (platform == "twitter")
    {
        if (post.user && post.user.name) return post.user.name;
    }

    if (platform == "facebook")
    {
        if (post.from && post.from.name) return post.from.name;
    }

    if (platform == "googleplus")
    {
        if (post.actor && post.actor.displayName) return post.actor.displayName;
    }

    if (platform == "instagram")
    {
        if (post.user && post.user.full_name) return post.user.full_name;
    }

    return "Unknown Name";
}

WebLibSocial.getPostUserid = function(platform, post)
{
    if (platform == "twitter")
    {
        if (post.user && post.user.id_str) return post.user.id_str;
    }

    if (platform == "facebook")
    {
        if (post.from && post.from.id) return post.from.id;
    }

    if (platform == "googleplus")
    {
        if (post.actor && post.actor.id) return post.actor.id;
    }

    if (platform == "instagram")
    {
        if (post.user && post.user.id) return post.user.id;
    }

    return "Unknown Name";
}

WebLibSocial.getPostUsername = function(platform, post)
{
    if (platform == "instagram")
    {
        //
        // On instagram public profiles are accessably only
        // by the screen user name, which functions as an id.
        //

        if (post.user && post.user.username) return post.user.username;
    }

    return null;
}

WebLibSocial.getPostText = function(platform, post)
{
    if (platform == "twitter")
    {
        if (post.text) return post.text;
    }

    if (platform == "facebook")
    {
        if (post.story) return post.story;
        if (post.message) return post.message;
    }

    if (platform == "googleplus")
    {
        if (post.title) return post.title;
    }

    if (platform == "instagram")
    {
        if (post.caption && post.caption.text) return post.caption.text;
    }

    return "Unknown Story";
}

WebLibSocial.getPostImgs = function(platform, post)
{
    var images = [];

    if (platform == "twitter")
    {
        if (post.entities && post.entities.media)
        {
           for (var inx = 0; inx < post.entities.media.length; inx++)
            {
                var media = post.entities.media[ inx ];

                if (media.media_url && media.sizes && media.sizes.large)
                {
                    var image = {};

                    image.src = media.media_url;
                    image.width = media.sizes.large.w;
                    image.height = media.sizes.large.h;

                    images.push(image);
                }
            }
        }
    }
    
    if (platform == "facebook")
    {
        if (post.attachments && post.attachments.data)
        {
           for (var inx = 0; inx < post.attachments.data.length; inx++)
            {
                var media = post.attachments.data[ inx ].media;

                if (media && media.image)
                {
                    images.push(media.image);
                }
                else
                {
                    var subattachments = post.attachments.data[ inx ].subattachments

                    if (subattachments && subattachments.data)
                    {
                        for (var sinx = 0; sinx < subattachments.data.length; sinx++)
                        {
                            var media = subattachments.data[ sinx ].media;
                            if (media && media.image)

                            if (media && media.image)
                            {
                                images.push(media.image);
                            }
                        }
                    }
                }
            }
        }
    }

    if (platform == "googleplus")
    {
        if (post.object && post.object.attachments)
        {
           for (var inx = 0; inx < post.object.attachments.length; inx++)
            {
                var media = post.object.attachments[ inx ];
                if (media.image) images.push(media.image);
            }
        }
    }

    if (platform == "instagram")
    {
        if (post.images && post.images.standard_resolution)
        {
            images.push(post.images.standard_resolution);
        }
    }

    return images;
}

WebLibSocial.getPostSuitable = function(platform, post)
{
    return WebAppSocial.getPostSuitable(platform, JSON.stringify(post));
}

WebLibSocial.getPlatformIcon = function(platform)
{
    return "/weblibs/social/social_" + platform + "_400x400.png";
}

WebLibSocial.getUserIcon = function(platform, pfid, pfname)
{
    var iconpath = WebAppSocial.getUserIcon(platform, pfid);

    if (iconpath && iconpath.length)
    {
        return ("local://" + iconpath);
    }

    if (pfname)
    {
        iconpath = WebAppSocial.getUserIcon(platform, pfname);

        if (iconpath && iconpath.length)
        {
            return ("local://" + iconpath);
        }
    }

    return "/weblibs/social/social_anon_300x300.png";
}



