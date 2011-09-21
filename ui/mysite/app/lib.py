import re
def process_tweet(whole_tweet):
    """
    Process twitter feeds and create links out of 
    @mentions, has_tags and embedded url's - create the HTMl to be
    displayed on screen
    """
    tweet = whole_tweet.text

    links = re.findall(r'(http://.*?)[ |$]', tweet+'  ')
    for link in links:
        tweet = tweet.replace(link, "<a href='%s'>%s</a>" % (link, link))

    hash_tags = re.findall(r'#([^ ]+)', tweet)
    for hash_tag in hash_tags:
        tweet = tweet.replace( \
            '#'+hash_tag, "<a href='%s'>#%s</a>" % \
                ('http://twitter.com/#!/search?q=%23'+hash_tag, hash_tag))

    at_mentions = re.findall(r'@[a-zA-Z0-9]+',tweet)
    for at_mention in at_mentions:
        tweet = tweet.replace(at_mention, 
                              "<a href='http://twitter.com/%s'>%s</a>" % \
                                  (at_mention.strip('@'), at_mention))
    whole_tweet.text = tweet
    return whole_tweet


import datetime
def process_feed(feed):
    """
    Process the facebook feed item to have html links and process/parse timestamp 
    to correct format to be displyed.
    """
    feed['created_time'] = datetime.datetime.strptime(
        feed['created_time'].split('+')[0],
        '%Y-%m-%dT%H:%M:%S')
    if feed.get('message'):
        links = re.findall(r'(http://.*?)[ |$]', feed['message']+'  ')
        for link in links:
            feed['message'] = feed['message'].replace(link, "<a href='%s'>%s</a>" % (link, link))
    return feed

from urlparse import urlparse
def create_context(providers):
    """
    creates the context dictionary for /home
    to know which all social networks are connected.
    the dictionary {'twitter': True, 'facebook': True,...}
    """
    ctx = {}
    for p in providers:
        if p[0] in ['google', 'twitter', 'facebook', 'yahoo']:
            ctx[p[0]] = True
        elif  p[0] == 'openid':
            src_parts = urlparse(p[1]).netloc.split('.')
            if 'wordpress' in src_parts:
                ctx['wordpress'] = True
            elif 'blogspot' in src_parts:
                ctx['blogger'] = True
    print(ctx)
    return ctx
