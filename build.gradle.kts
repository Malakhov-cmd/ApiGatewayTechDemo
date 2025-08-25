plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ru.tech.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    //Spring
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")

    //Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-annotations")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
        exclude(group = "com.fasterxml.jackson.datatype", module = "jackson-datatype-jsr310")
        exclude(group = "com.fasterxml.jackson.datatype", module = "jackson-datatype-jdk8")
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-parameter-names")
        exclude(group = "com.fasterxml.jackson.dataformat", module = "jackson-dataformat-yaml")
    }

    //DB
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.flywaydb:flyway-core:11.11.1")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.11.1")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    //Test
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
