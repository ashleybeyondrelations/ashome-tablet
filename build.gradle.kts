
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.TreeMap

plugins {
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"	// ¶ the spring boot framework is an easy way to make fat jars by simply including the following plugin
	`maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
	id("org.springframework.boot") version "2.6.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
}
//springBoot {
//	mainClassName = "com.ashome.core.servlet.InternalApplicationKt"
//}

group = "com.ashome.tablet"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
		jcenter()
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	mavenLocal()
}

dependencies {
	compileOnly(kotlin("reflect"))
	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc-218")

	implementation("com.fasterxml.jackson.core:jackson-core:2.13.0")
	implementation("io.github.microutils:kotlin-logging:2.1.20")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.slf4j:slf4j-api:1.7.32")
//	implementation("org.springframework.boot:spring-boot-starter")
//	implementation("org.springframework:spring-web")
	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("org.springframework.boot:spring-boot-starter")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

/*
val jarManifest = TreeMap<String, String>()
//jarManifest.put( "Built-By", mv_username )
//jarManifest.put( "Built-When", ZonedDateTime.now( timeZoneId ).toString() )
//jarManifest.put( "Build-Revision", microworxClientVersion )
jarManifest.put( "Automatic-Module-Name", "com.beyondrelations.builder.devutils" )
jarManifest.put( "Main-Class", "com.ashome.core.servlet.InternalApplicationKt" )

tasks.withType<Jar> {
	manifest {
		attributes( jarManifest )
	}
}
*/

val sourcesJar by tasks.registering(Jar::class) {
	classifier = "sources"
	from(sourceSets.main.get().allSource)
}

publishing {
	repositories {
		mavenLocal()
	}
	publications {
		register("mavenJava", MavenPublication::class) {
			from(components["java"])
			artifact(sourcesJar.get())
		}
	}
}














