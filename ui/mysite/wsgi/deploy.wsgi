ALLDIRS = ['/home/ubuntu/virtualenvs/seekinme/lib/python2.6/site-packages',
'/home/ubuntu/virtualenvs/seekinme/mysite']

import os
import sys
import site

# redirect sys.stdout to sys.stderr for bad libraries like geopy that uses
# print statements for optional import exceptions.
sys.stdout = sys.stderr
prev_sys_path = list(sys.path)

for directory in ALLDIRS:
  site.addsitedir(directory)

# Reorder sys.path so new directories at the front.
new_sys_path = []
for item in list(sys.path):
    if item not in prev_sys_path:
        new_sys_path.append(item)
        sys.path.remove(item)
sys.path[:0] = new_sys_path 

activate_this = '/home/ubuntu/virtualenvs/seekinme/bin/activate_this.py'
execfile(activate_this, dict(__file__=activate_this))
from os.path import abspath, dirname, join
from site import addsitedir

sys.path.insert(0, abspath(join(dirname(__file__), "../../")))

from django.conf import settings
os.environ["DJANGO_SETTINGS_MODULE"] = "mysite.settings"

from django.core.handlers.wsgi import WSGIHandler
application = WSGIHandler()
