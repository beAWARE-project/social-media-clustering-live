FROM openjdk:8

COPY resources /usr/src/social-media-clustering-live/resources

COPY target/social-media-clustering-live-1.0-jar-with-dependencies.jar /usr/src/social-media-clustering-live

WORKDIR /usr/src/social-media-clustering-live

#CMD ["java", "-jar", "social-media-clustering-live-1.0-jar-with-dependencies.jar"]
