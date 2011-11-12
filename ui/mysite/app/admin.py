from . import models
from django.contrib import admin
from django.contrib.auth.admin import UserAdmin

UserAdmin.list_display += ('secret_key', 'email_term')
UserAdmin.fieldsets += (('Email check', {'fields': ['secret_key', 'email_term']}),)

admin.site.register(models.UserLog)
