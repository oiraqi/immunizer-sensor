cd ../../ofbiz-framework
java -javaagent:../immunizer-monitoring/framework/build/libs/immunizer-monitoring-agent.jar -javaagent:../immunizer-response/framework/build/libs/immunizer-response-agent.jar -Dconfig=../immunizer-monitoring/config/ofbiz-config.json -jar build/libs/ofbiz.jar
