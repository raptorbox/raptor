FROM nginx:alpine

MAINTAINER Luca Capra <lcapra@fbk.eu>

COPY ./raptor-nginx.conf /etc/nginx/conf.d/default.conf
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

EXPOSE 80
CMD nginx
