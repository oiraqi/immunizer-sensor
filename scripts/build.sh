cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/immunizer-monitoring-agent.jar ../scripts/manifest.mf
