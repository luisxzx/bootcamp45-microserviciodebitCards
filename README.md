cambiar las propiedades del application.properties para levantar en local reemplazar por lo siguiente que es descomentar el configServer y activar el cloud config


server.port=8088
spring.application.name=microservicios-DebiCard2
spring.config.import=configserver:http://localhost:8888
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
ntt.data.bootcamp.s01-account-service = http://localhost:8081
spring.cloud.config.enabled=true
