.PHONY: docker/build docker/push build

name := raptorbox/standalone

gittag := $(shell git describe --tag --always)
tag := $(shell echo ${gittag} | cut -d'-' -f 1)
basetag := $(shell echo ${gittag} | cut -d'.' -f 1)

build:
	mvn clean package -DskipTests=true

docker/build: build
	echo "Building ${tag}"
	cd raptor-api/raptor-standalone && docker build . -t "raptorbox/standalone:${tag}"
	docker tag ${name}:${tag} ${name}:${basetag}

docker/push: docker/build
	docker push ${name}:${tag}
	docker push ${name}:${basetag}
