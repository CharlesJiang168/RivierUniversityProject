from django.conf.urls import patterns, include, url
from django.contrib import admin
from enroll import views

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'school.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    url(r'^$', views.index, name = 'index'),
    url(r'^create/$', views.create, name = 'create'),
    url(r'^(?P<student_id>\d+)/$', views.detail, name = 'detail'),
    
)
