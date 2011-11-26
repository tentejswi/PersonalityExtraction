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
import datetime, uuid
import smtplib
from django.core.mail import send_mail
from django.core.urlresolvers import reverse
from .models import UserLog
from privatebeta.forms import InviteRequestForm
from privatebeta.models import InviteRequest

def user_invited_check(**kwa):
    for inv_user in InviteRequest.objects.filter(**kwa):
        if inv_user.invited == True:
            return True
        return False
    
def index(request):
    """Index view, displays login mechanism"""
    if request.user.is_authenticated():
        #if loggedin - go to home page and display the interest graph
        return HttpResponseRedirect('home')
    # if form has been submitted, show the next steps, or show the form
    else:
        if request.method == 'POST':    
            form = InviteRequestForm(request.POST)
            ctx = RequestContext(request, {
                                           'version' : settings.APP_VERSION,
                                           'form': form,
                                           })
            if form.is_valid():
                """ Check if the user has been invited or needs to be invited"""
                eml = form.cleaned_data['email']
                # if invited then continue to login
                if user_invited_check(email=eml):
                    ctx['invited']=True
                    return render_to_response('index.html', ctx,
                                              RequestContext(request))
                    # else email is in the invitation queue
                else:
                    if not InviteRequest.objects.filter(email=eml):
                        form.save()
                    return HttpResponseRedirect(reverse('privatebeta_sent'))
            else:
                # email address already exists
                # TO-DO handle the case when email address is not valid
                print "form is invalid"
                print form
                return HttpResponseRedirect(reverse('privatebeta_sent'))
        else:
            form = InviteRequestForm(request.POST)
            ctx = RequestContext(request, {
                                           'version' : settings.APP_VERSION,
                                           'form': form,
                                           })
            ctx['invited']=False
            return render_to_response('index.html', ctx,
                                      RequestContext(request))

def invite_sent(request):
    return render_to_response('invites/queued.html', {'version' : settings.APP_VERSION},
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
        try:
            user = User.objects.get(pk=request.session['SEEKINME_NEW_USER_ID'])
        except (User.DoesNotExist, KeyError): # double click on 'restart'
            return HttpResponseRedirect('/')
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
                u = form.save()
                assert u is user, [u, user]
                if request.session['original_email'] != user.email: # send email to verify that email belongs to you
                    send_test_email(user, request.build_absolute_uri)
                if not user.is_active:
                    user.is_active = True
                    user.save()
                    user.backend = request.session['SEEKINME_NEW_USER_BACKEND']
                    login(request, user)
                return HttpResponseRedirect('/home/')
    else:
        form = NewUserForm(instance=user)
        request.session['original_email'] = user.email
        provider = user_check(user.id, email=user.email)
        if provider:
            request.session['provider'] = provider
            form._errors = dict(__all__="A %s account has already been registered for this email address." % provider)
            restart = True
    return render_to_response('registration/register.html', dict(form=form, restart=restart),
        context_instance=RequestContext(request),
    )

@login_required
def update_user(request):
    user = request.user
    if not user.is_authenticated():
        try:
            user = User.objects.get(pk=request.session['SEEKINME_NEW_USER_ID'])
        except (User.DoesNotExist, KeyError): # double click on 'restart'
            return HttpResponseRedirect('/')
        s_networks = ()
    elif not user.is_active: # before activating user can't delete networks
        s_networks = ()
    else:
        s_networks = UserSocialAuth.objects.filter(user=user)
    #~ print sorted(request.session.keys())
    if request.method == 'POST':
        if 'cancel' in request.POST:
            return HttpResponseRedirect('/home/')
        for_delete = request.POST.getlist('delete')
        # delete user when all networks are deleted
        if len(s_networks) == len(for_delete) > 0:
            UserLog.objects.get_or_create(username=user.username, email=user.email)
            user.delete()
            logout(request)
            if 'provider' in request.session:
                go_to = '/login/' + request.session['provider']
            else:
                go_to = '/'
            return HttpResponseRedirect(go_to)
        # check whick networks are selected and delete them
        for so in s_networks:
            if so.provider in for_delete:
                so.delete()
        form = NewUserForm(request.POST, instance=user)
        if form.is_valid():
            #~ fnm = form.cleaned_data['first_name']
            #~ lnm = form.cleaned_data['last_name']
            eml = form.cleaned_data['email']
            provider = user_check(user.id, email=eml)
            if provider:
                request.session['provider'] = provider
                form._errors['__all__'] = "This email address is already being used for another account. Please try a different email address."
            else:
                u = form.save()
                assert u is user, [u, user]
                if request.session['original_email'] != user.email: # send email to verify that email belongs to you
                    send_test_email(user, request.build_absolute_uri)
                if not user.is_active:
                    user.is_active = True
                    user.save()
                    user.backend = request.session['SEEKINME_NEW_USER_BACKEND']
                    login(request, user)
                return HttpResponseRedirect('/home/')
    else: # when the form is first rendered
        form = NewUserForm(instance=user)
        request.session['original_email'] = user.email
        provider = user_check(user.id, email=user.email)
        if provider:
            request.session['provider'] = provider
            form._errors = dict(__all__="This email address is already being used for another account. Please try a different email address.")
    return render_to_response('registration/update.html', dict(form=form, networks=s_networks),
        context_instance=RequestContext(request),
    )

def send_test_email(user, build):
    user.secret_key = str(uuid.uuid4())
    url = reverse(email_callback, args=(user.secret_key,))
    try:
        msg = u'Hello %s\nplease use\n%s to confirm' % (user.username, build(url))
        send_mail('Welcome to seekinme!', msg, 'admin@seekinme.com',
            [user.email], fail_silently=False)
    except smtplib.SMTPException:
        # can't send - retry on next login
        user.email_term = None
    except Exception:
        # can't send - retry on next login
        user.email_term = None
    else:
        user.email_term = datetime.date.today() + datetime.timedelta(days=7)
    user.save()

def email_callback(request, key):
    try:
        user = User.objects.get(secret_key=key)
    except User.DoesNotExist:
        user = None
        tname = 'registration/verify_bad.html'
    else:
        user.secret_key = None
        user.email_term = None
        user.save()
        tname = 'registration/verify_good.html'
    return render_to_response(tname, dict(user=user), context_instance=RequestContext(request))

def pipe_dump(*arg, **kwargs):
    print sorted(kwargs)
    print 'u:', kwargs['user'], 'so_u:', kwargs['social_user'], kwargs['details']

def get_existing_user(request, backend, uid, user=None, social_user=None, *arg, **kwargs):
    try:
        social_user = UserSocialAuth.objects.select_related('user').get(provider=backend.name, uid=uid)
    except UserSocialAuth.DoesNotExist:
        return {'social_user': None}
    else: #merging here. Be careful when adding new attributes
        if user and social_user and social_user.user != user:
            extra_user = social_user.user
            extra_user.delete()
            social_user.user = user
            social_user.save()
        user = social_user.user
        if user.secret_key:
            if user.email_term is None: # test email has not been sent
                send_test_email(user, request.build_absolute_uri)
            elif user.email_term < datetime.date.today(): # no repsonse after 7 days
                user.delete()
                return {'social_user':None, 'user':None}
        return {'social_user': social_user, 'user':user}

def on_new_user(sender, user, response, details, **kwargs):
    user.is_active = False
    user.is_new = True
    user.first_name = details.get('first_name')
    user.last_name = details.get('last_name')
    user.email = details.get('email')
    return True  # necessary user.save()

def on_user_change(sender, user, response, details, **kwargs):
    for so in UserSocialAuth.objects.filter(user=user, provider='twitter'):
        user.tw = so.provider + so.uid + repr(so.extra_data)
    for so in UserSocialAuth.objects.filter(user=user, provider='facebook'):
        user.fb = so.provider + so.uid + repr(so.extra_data)
    return True  # necessary user.save()

from social_auth.signals import socialauth_registered, pre_update
socialauth_registered.connect(on_new_user)
pre_update.connect(on_user_change)
