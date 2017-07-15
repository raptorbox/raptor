#!/bin/sh

mkdir -p `pwd`/tmp/nginx/nginx_client_body
mkdir -p `pwd`/tmp/nginx/nginx_fastcgi_temp
mkdir -p `pwd`/tmp/nginx/nginx_proxy_temp
mkdir -p `pwd`/tmp/nginx/nginx_scgi_temp
mkdir -p `pwd`/tmp/nginx/nginx_uwsgi_temp

mkdir -p `pwd`/tmp/nginx/sites

cp `pwd`/config/raptor.test.conf `pwd`/tmp/nginx/sites/raptor.test.conf
cp `pwd`/travis/nginx.conf `pwd`/tmp/nginx/nginx.conf
