#!/usr/bin/env bash
mv build.gradle.kts build.gradle.kts.1
sed 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' build.gradle.kts.1 > build.gradle.kts
mv app/build.gradle.kts app/build.gradle.kts.1
sed 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' app/build.gradle.kts.1 > app/build.gradle.kts
mv buildSrc/build.gradle.kts buildSrc/build.gradle.kts.1
sed 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' buildSrc/build.gradle.kts.1 > buildSrc/build.gradle.kts
mv settings.gradle.kts settings.gradle.kts.1
sed 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' settings.gradle.kts.1 > settings.gradle.kts