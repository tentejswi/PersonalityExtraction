from os.path import abspath, dirname, basename, join


DEBUG = True
TEMPLATE_DEBUG = DEBUG

ROOT_PATH = abspath(dirname(__file__))
PROJECT_NAME = basename(ROOT_PATH)

ADMINS = (
    # ('Your Name', 'your_email@domain.com'),
)
MANAGERS = ADMINS

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'mysitedb',
	'USER': 'root',
	'PASSWORD': '',
	'HOST': '',
	'PORT': '',
    }
}

TIME_ZONE = 'America/Los_Angeles'
LANGUAGE_CODE = 'en-us'
SITE_ID = 1
APP_VERSION = 0.1
USE_I18N = True
USE_L10N = True

MEDIA_ROOT = ''
ADMIN_MEDIA_PREFIX = '/admin-media/'
MEDIA_ROOT = join(ROOT_PATH, 'media')
MEDIA_URL = '/media/'

SECRET_KEY = 't2eo^kd%k+-##ml3@_x__$j0(ps4p0q6eg*c4ttp9d2n(t!iol'

TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.Loader',
    'django.template.loaders.app_directories.Loader',
)

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
)

ROOT_URLCONF = 'urls'

TEMPLATE_DIRS = (
    join(ROOT_PATH, 'templates')
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.admin',
    'social_auth',
    'app',
    'accounts',
    'privatebeta',
#    'registration', 
)

AUTHENTICATION_BACKENDS = (
    'social_auth.backends.twitter.TwitterBackend',
    'social_auth.backends.facebook.FacebookBackend',
    'social_auth.backends.google.GoogleOAuthBackend',
    'social_auth.backends.google.GoogleOAuth2Backend',
    'social_auth.backends.google.GoogleBackend',
    'social_auth.backends.yahoo.YahooBackend',
    'social_auth.backends.contrib.linkedin.LinkedinBackend',
    'social_auth.backends.OpenIDBackend',
    'social_auth.backends.contrib.livejournal.LiveJournalBackend',
    'django.contrib.auth.backends.ModelBackend',
)

try:
    from local_settings import *
except:
    pass


LOGIN_REDIRECT_URL = '/'
SOCIAL_AUTH_LOGIN_REDIRECT_URL = '/'
SOCIAL_AUTH_CHANGE_SIGNAL_ONLY = True
SOCIAL_AUTH_COMPLETE_URL_NAME = 'my_complete'

FACEBOOK_EXTENDED_PERMISSIONS = ['email', 'read_stream', 'offline_access',]

ACCOUNT_ACTIVATION_DAYS = 7


GOOGLE_SREG_EXTRA_DATA = [('http://axschema.org/namePerson/first', 'first_name'),
                          ('http://axschema.org/contact/email', 'email'),
                          ('http://axschema.org/namePerson/last', 'last_name')]
GOOGLE_AX_EXTRA_DATA = [('http://axschema.org/namePerson/first', 'first_name'),
                        ('http://axschema.org/contact/email', 'email'),
                        ('http://axschema.org/namePerson/last', 'last_name')]

YAHOO_SREG_EXTRA_DATA = [('http://axschema.org/namePerson/friendly', 'friendly_name'),
                         ('http://axschema.org/contact/email', 'email'),
                         ('http://axschema.org/namePerson', 'fullname')]
YAHOO_AX_EXTRA_DATA = [('http://axschema.org/namePerson/friendly', 'friendly_name'),
                       ('http://axschema.org/contact/email', 'email'),
                       ('http://axschema.org/namePerson', 'fullname')]

OPENID_SREG_EXTRA_DATA = [('nickname', 'nickname'),
                         ('email', 'email'),
                         ('fullname', 'fullname')]
OPENID_AX_EXTRA_DATA = [('nickname', 'nickname'),
                        ('email', 'email'),
                        ('fullname', 'fullname')]

SOCIAL_AUTH_PIPELINE = (
        'social_auth.backends.pipeline.social.social_auth_user',
        'app.views.get_existing_user',
        #~ 'social_auth.backends.pipeline.associate.associate_by_email',
        'social_auth.backends.pipeline.user.get_username',
        #~ 'app.views.pipe_dump',
        'social_auth.backends.pipeline.user.create_user',
        'social_auth.backends.pipeline.social.associate_user',
        'social_auth.backends.pipeline.social.load_extra_data',
        'social_auth.backends.pipeline.user.update_user_details',
   )

# testing with
# python -m smtpd -n -c DebuggingServer localhost:1025

EMAIL_HOST = 'localhost'
EMAIL_PORT = 1025

# Private Beta Settings
PRIVATEBETA_ENABLE_BETA = True
PRIVATEBETA_NEVER_ALLOW_VIEWS = 'app.views.home', 'app.views.accounts'
PRIVATEBETA_ALWAYS_ALLOW_VIEWS = 'app.views.index',  'app.views.invite_sent'
PRIVATEBETA_ALWAYS_ALLOW_MODULES = 'app.views', 'django.contrib.admin.sites', 'social_auth.views'
PRIVATEBETA_REDIRECT_URL = '/'
