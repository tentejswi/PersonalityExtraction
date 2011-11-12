from django.db import models
from django.contrib.auth.models import User


User.add_to_class('tw', models.CharField(max_length=250, blank=True, null=True))
User.add_to_class('fb', models.CharField(max_length=250, blank=True, null=True))
User.add_to_class('secret_key', models.CharField(max_length=50, blank=True, null=True))
User.add_to_class('email_term', models.DateField(blank=True, null=True))

# email check states:
#secret_key = None, email_term = None - valid email address
#secret_key = 'uud', email_term = None - before sending test email
#secret_key = 'uud', email_term = date+7 - after sending test email

class UserLog(models.Model):
    """data for deleted users"""
    username = models.CharField(max_length=120)
    email = models.CharField(max_length=250)
    when = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        return u'%s %s' % (self.username, self.email)
