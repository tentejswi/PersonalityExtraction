from django.conf.urls.defaults import patterns, url, include
from django.contrib import admin

from app.views import home, index, tryfirst, transfer, new_user, email_callback, invite_sent


admin.autodiscover()

urlpatterns = patterns('',
    url(r'^$', index, name='index'),
    url(r'^home/$', home, name='home'),
    url(r'^my/(?P<backend>[^/]+)/$', transfer, name='my_complete'),
    url(r'^verify/(?P<key>[^/]+)/?$', email_callback),
    url(r'^user/$', new_user),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^accounts/', include('accounts.auth_urls')),
    url(r'^tryfirst/(?P<username>[^/]+)', tryfirst, name='tryfirst'),
    url(r'^invites/queued', invite_sent, name='privatebeta_sent'),
    url(r'', include('social_auth.urls')),
)

from django.conf import settings
if settings.DEBUG:
    urlpatterns += patterns(
        '',
       (r'^site_media/(?P<path>.*)$', 'django.views.static.serve',
         { 'document_root': settings.MEDIA_ROOT, }
        ),
        
    )
