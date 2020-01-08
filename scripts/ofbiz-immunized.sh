cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/ofbiz-immunizer-agent.jar ../scripts/manifest-ofbiz.mf
cd ../../ofbiz-framework
java -javaagent:../immunizer-instrumentation/framework/build/libs/ofbiz-immunizer-agent.jar -jar build/libs/ofbiz.jar
