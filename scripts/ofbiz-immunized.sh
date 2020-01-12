cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/immunizer-monitoring-agent.jar ../scripts/manifest.mf
cd ../../ofbiz-framework
java -javaagent:../immunizer-instrumentation/framework/build/libs/immunizer-monitoring-agent.jar -Dconfig=../immunizer-instrumentation/config/ofbiz-config.json -jar build/libs/ofbiz.jar
