from django.conf import settings
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.core.exceptions import ObjectDoesNotExist
import tweepy
import urllib2
import traceback
from django.utils import simplejson
from django.contrib.auth import authenticate, login, logout
from facebook import GraphAPI
from lib import process_tweet, process_feed, create_context

def index(request):
    """Index view, displays login mechanism"""
    if request.user.is_authenticated():
        #if loggedin - go to home page and display feeds
        return HttpResponseRedirect('home')
    else:
        #else just display the main page with login
        return render_to_response('index.html', {'version': settings.APP_VERSION},
                                  RequestContext(request))

def tryfirst(request, username):
   """This is for when the user tries out by giving the twitter handle directly"""
   ctx = RequestContext(request, {
		'version' : settings.APP_VERSION,
		'username': username,
	})
   return render_to_response('home.html', ctx,
                             RequestContext(request))

@login_required
def home(request):
    """Home complete view, displays user data"""
    providers = request.user.social_auth.values_list('provider', 'uid')
    ctx = create_context(providers) 
    """
    creates a dictionary like {'twitter': True, 'facebook': True,...}
    and passed as a context variable to the template so that the template can use
    it to determine which all social platforms are authorized.
    """
    ctx['version'] = settings.APP_VERSION
    try:
        #getting twitter access token from database, if already authenticated, else raise exception
        twitter_user = request.user.social_auth.get(provider='twitter')
        access_token = twitter_user.extra_data['access_token']
        access_token = dict([each.split('=') for each in 
                             access_token.split('&')])
        #uses the tweepy library to establish a connection with Twitter
        twitter_auth = tweepy.OAuthHandler(settings.TWITTER_CONSUMER_KEY,
                                           settings.TWITTER_CONSUMER_SECRET)
        #set in te authorized OAuth token
        twitter_auth.set_access_token(\
            access_token['oauth_token'],
            access_token['oauth_token_secret'])
        api = tweepy.API(twitter_auth)#create an api instance
        tweets = [process_tweet(tweet) 
                  for tweet in api.user_timeline()]
        #get the home timeline
        ctx['tweets'] = tweets
    except ObjectDoesNotExist,e:
        ctx['twitter_error'] = 'No access token'
    except tweepy.TweepError,e:
        ctx['twitter_error'] = "Access token failure"
    except AttributeError,e:
        ctx['twitter_error'] = "Twitter API Key not set up properly"

    try:
        facebook_user = request.user.social_auth.get(provider='facebook')
        #get access token from db, if already authorized, else raise exception
        access_token = facebook_user.extra_data['access_token']
        #use facebook python sdk and the OAuth access token to create a graph API instance
        #and request the /me/home path to get the home timeline.
        home_dict = GraphAPI(access_token = access_token).request("/me/feed")
        #parse the feeds
        ctx['feeds'] = [process_feed(each) 
                        for each in home_dict['data']
                        if each['from']['id'] == facebook_user.uid]
    except ObjectDoesNotExist,e:
        ctx['facebook_error'] = 'No access token'
    except urllib2.HTTPError,e:
        ctx['facebook_error'] = "Access token failure"
    except urllib2.URLError,e:
        ctx['facebook_error'] = "Access token failure"
        
    return render_to_response('home.html', ctx, RequestContext(request))

ALT_NEXT = '/'

def seekinme_logout(request, template_name):
    """custom logout"""
    logout(request)
    return HttpResponseRedirect('/')
