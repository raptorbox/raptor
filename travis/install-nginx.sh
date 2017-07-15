#!/bin/sh

mkdir -p /tmp/nginx/nginx_client_body
mkdir -p /tmp/nginx/nginx_fastcgi_temp
mkdir -p /tmp/nginx/nginx_proxy_temp
mkdir -p /tmp/nginx/nginx_scgi_temp
mkdir -p /tmp/nginx/nginx_uwsgi_temp

mkdir -p /tmp/nginx/sites

cp `pwd`/config/raptor.test.conf /tmp/nginx/sites/raptor.test.conf
cp `pwd`/travis/nginx.conf /tmp/nginx/nginx.conf
