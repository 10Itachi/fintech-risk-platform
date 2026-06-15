# ==========================================
# STAGE 1: Build the application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 1. Copy only the pom.xml first to cache dependencies
COPY pom.xml .

# 2. Go offline to download dependencies (makes future builds much faster)
RUN mvn dependency:go-offline -B

# 3. Copy the source code
COPY src ./src

# 4. Package the application (skipping tests for faster builds)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 5. Copy the compiled JAR from the builder stage into this fresh stage
COPY --from=builder /build/target/user-service-*.jar app.jar

EXPOSE 8081

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