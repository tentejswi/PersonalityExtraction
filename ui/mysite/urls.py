from django.conf.urls.defaults import patterns, url, include
from django.contrib import admin

from app.views import home, index, tryfirst 


admin.autodiscover()

urlpatterns = patterns('',
    url(r'^$', index, name='index'),
    url(r'^home/$', home, name='home'),
    url(r'^admin/', include(admin.site.urls)),
    #url(r'^accounts/', include('registration.urls')),
    url(r'^accounts/', include('accounts.urls')),
    url(r'^tryfirst/', tryfirst, name='tryfirst'),
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
