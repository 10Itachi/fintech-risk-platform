FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/user-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

#1FROM → base runtime (Java 17)
 #WORKDIR → sets container working dir
 #COPY → puts your built JAR into image
 #EXPOSE 8080 → documents container port
 #ENTRYPOINT → command to run app

#2mvn clean package -- target/user-service.jar

#3docker build -t user-service .  --Dockerfile → Image  Reads Dockerfile
                                                       #Copies JAR
                                                       #Creates a local image

#4docker images

#5 cp .env.example .env  -->Edit .env:
                            #
                            #DB URL
                            #Redis
                            #Keycloak
                            #
                            #👉 Use localhost for standalone mode

#6STEP 5 — Run container
  #docker run -p 8081:8080 --env-file .env user-service
  #What this does
  #Image → Container (running app)
  #-p 8081:8080 → maps host → container
  #--env-file → injects config
  #starts your service

  #🧱 STEP 6 — Verify
  #curl http://localhost:8081/actuator/health
  #
  #Expect:
  #
  #{ "status": "UP" }


  #🧱 STEP 7 — Fix common issues (quick checklist)
  #❌ DB connection fails → check .env
  #❌ Keycloak error → issuer URI
  #❌ Redis error → host/port
  #❌ Port conflict → change 8081


  #🧱 STEP 8 — Prepare for Git
  #Add .gitignore
  #target/
  #.env
  #Ensure repo contains
  #✔ src/
  #✔ pom.xml
  #✔ Dockerfile
  #✔ .env.example
  #✔ README.md
  #🧱 STEP 9 — Commit & Push


  #git add .
  #git commit -m "Add Docker support for user-service"
  #git push