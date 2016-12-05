FROM nginx:alpine

MAINTAINER Luca Capra <lcapra@create-net.org>

COPY ./raptor-nginx.conf /etc/nginx/conf.d/default.conf
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

EXPOSE 80
CMD nginx
