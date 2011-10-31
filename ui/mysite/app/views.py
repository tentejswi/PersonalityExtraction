from django import forms
from django.conf import settings
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth import login, REDIRECT_FIELD_NAME
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.core.exceptions import ObjectDoesNotExist
import tweepy
import urllib2
import traceback
from django.utils import simplejson
from django.contrib.auth import authenticate, login, logout
from social_auth.models import UserSocialAuth
from facebook import GraphAPI, GraphAPIError
from lib import process_tweet, process_feed, create_context

def index(request):
    """Index view, displays login mechanism"""
    if request.user.is_authenticated():
        #if loggedin - go to home page and display the interest graph
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
    except GraphAPIError,e:
        ctx['facebook_error'] = 'App revoked?'
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

def transfer(request, backend, *args, **kwargs):
    """Authentication complete process"""
    from social_auth import views as sav
    backend = sav.get_backend(backend, request, request.path)
    # copy + changes from social_auth.views.complete_process()
    user = sav.auth_complete(request, backend, *args, **kwargs)

    if user and not getattr(user, 'is_active', True):
        be = backend.AUTH_BACKEND
        login_backend = '%s.%s' % (be.__module__, be.__name__)
        request.session['SEEKINME_NEW_USER_ID'] = user.id
        request.session['SEEKINME_NEW_USER_BACKEND'] = login_backend
        url = '/user/'
    elif user and getattr(user, 'is_active', True):
        login(request, user)
        # user.social_user is the used UserSocialAuth instance defined
        # in authenticate process
        social_user = user.social_user

        if sav.SESSION_EXPIRATION :
            # Set session expiration date if present and not disabled by
            # setting. Use last social-auth instance for current provider,
            # users can associate several accounts with a same provider.
            if social_user.expiration_delta():
                request.session.set_expiry(social_user.expiration_delta())

        # store last login backend name in session
        request.session[sav.SOCIAL_AUTH_LAST_LOGIN] = social_user.provider

        # Remove possible redirect URL from session, if this is a new account,
        # send him to the new-users-page if defined.
        url = sav.NEW_USER_REDIRECT if sav.NEW_USER_REDIRECT and \
                                   getattr(user, 'is_new', False) else \
              request.session.pop(REDIRECT_FIELD_NAME, '') or \
              sav.DEFAULT_REDIRECT
    else:
        url = sav.LOGIN_ERROR_URL
    return HttpResponseRedirect(url)

class NewUserForm(forms.ModelForm):
    first_name = forms.CharField()
    last_name = forms.CharField()
    email = forms.EmailField()
    class Meta:
        model = User
        fields = ('first_name', 'last_name', 'email')
        
def user_check(this_id, **kwa):
    for other_user in User.objects.filter(**kwa):
        if other_user.id == this_id:
            continue
        provider = 'unknown'
        for sa_user in UserSocialAuth.objects.filter(user=other_user):
            provider = sa_user.provider
        return provider
    
#@login_required
def new_user(request):
    user = request.user
    if not user.is_authenticated():
        user = User.objects.get(pk=request.session['SEEKINME_NEW_USER_ID'])
    restart = False
    if request.method == 'POST':
        if 'restart' in request.POST:
            user.delete()
            return HttpResponseRedirect('/login/' + request.session['provider'])
        form = NewUserForm(request.POST, instance=user)
        if form.is_valid():
            #~ fnm = form.cleaned_data['first_name']
            #~ lnm = form.cleaned_data['last_name']
            eml = form.cleaned_data['email']
            provider = user_check(user.id, #first_name=fnm, last_name=lnm,
                                            email=eml)
            if provider:
                request.session['provider'] = provider
                form._errors['__all__'] = "A %s account has already been registered for this email address." % provider
                restart = True
            else:
                form.save()
                if not user.is_active:
                    user.is_active = True
                    user.save()
                    user.backend = request.session['SEEKINME_NEW_USER_BACKEND']
                    login(request, user)
                return HttpResponseRedirect('/home/')
    else:
        form = NewUserForm(instance=user)
        provider = user_check(user.id, email=user.email)
        if provider:
            request.session['provider'] = provider
            form._errors = dict(__all__="A %s account has already been registered for this email address." % provider)
            restart = True
    return render_to_response('register.html', dict(form=form, restart=restart),
        context_instance=RequestContext(request),
    )

def pipe_dump(*arg, **kwargs):
    print sorted(kwargs)
    print 'u:', kwargs['user'], 'so_u:', kwargs['social_user'], kwargs['details']

def get_existing_user(social_user=None, *arg, **kwargs):
    if social_user is not None:
        return {'user':social_user.user}

def on_new_user(sender, user, response, details, **kwargs):
    user.is_active = False
    user.is_new = True
    user.first_name = details.get('first_name')
    user.last_name = details.get('last_name')
    user.email = details.get('email')
    return True  # necessary user.save()

def on_user_change(sender, user, response, details, **kwargs):
    for so in UserSocialAuth.objects.filter(user=user, provider='twitter'):
        user.tw = so.uid + repr(so.extra_data)
    for so in UserSocialAuth.objects.filter(user=user, provider='facebook'):
        user.fb = so.uid + repr(so.extra_data)
    return True  # necessary user.save()

from social_auth.signals import socialauth_registered, pre_update
socialauth_registered.connect(on_new_user)
pre_update.connect(on_user_change)
