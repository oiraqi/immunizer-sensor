package org.immunizer.instrumentation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.instrument.Instrumentation;
import java.time.Duration;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.google.gson.Gson;

import org.immunizer.instrumentation.Invocation;
import org.immunizer.instrumentation.monitoring.InvocationProducer;
import org.immunizer.instrumentation.response.InvocationConsumer;

public class ImmunizerAgent {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Instrumenter MicroAgent Launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		AgentBuilder builder = new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."));

		/*
		 * builder.type(named("org.apache.ofbiz.webapp.control.ControlFilter"))
		 * .transform(new InterceptTransformer()).installOn(inst);
		 */
		try {
			String configPath = System.getProperty("config");
			BufferedReader br = new BufferedReader(new FileReader(configPath));
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while ((line = br.readLine()) != null)
				buffer.append(line);
			br.close();

			Gson gson = new Gson();
			Config config = gson.fromJson(new String(buffer), Config.class);
			for (String ignore : config.ignore) {
				builder = builder.ignore(nameStartsWith(ignore + '.'));
			}
			for (String pkg : config.apply.packages) {
				builder.type(nameStartsWith(pkg + '.')).transform(new MonitoringTransformer(any())).installOn(inst);
			}
			for (Config.Apply.Class clazz : config.apply.classes) {
				Junction<? super MethodDescription> any = any(), matcher = any;
				for (Config.Apply.Class.Method method : clazz.methods) {
					Junction<? super MethodDescription> mtc = named(method.name);
					if (method.parameters != 0) {
						mtc = mtc.and(takesArguments(method.parameters));
					}
					if (matcher == any) {
						matcher = matcher.and(mtc);
					} else {
						matcher = matcher.or(mtc);
					}
				}
				builder.type(named(clazz.name)).transform(new MonitoringTransformer(matcher)).installOn(inst);
			}
		} catch (Exception ex) {
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				InvocationConsumer consumer = new InvocationConsumer();
				ConsumerRecords<String, Invocation> records;
				int i;
				while (true) {
					records = consumer.poll(Duration.ofSeconds(60));
					i = 0;
					for (ConsumerRecord<String, Invocation> record : records) {
						System.out.println(record.value().getFullyQualifiedMethodName());
						if (i++ == 10)
							break;
					}
				}
			}
		}).start();
	}

	public static class Config {
		public String[] ignore = {};
		public Apply apply;

		public static class Apply {
			public String[] packages = {};
			public Class[] classes = {};

			public class Class {
				public String name;
				public Method[] methods = {};

				public class Method {
					public String name;
					public int parameters = 0;
				}
			}
		}
	}

	private static class MonitoringTransformer implements Transformer {

		Junction<? super MethodDescription> matcher;

		public MonitoringTransformer(Junction<? super MethodDescription> matcher) {
			ElementMatcher<Iterable<? extends ParameterDescription>> parameterMatcher = parameterDescriptions -> {
				return (parameterDescriptions != null && parameterDescriptions.iterator().hasNext());
			};

			this.matcher = matcher.and(isPublic()).and(hasParameters(parameterMatcher));
		}

		@Override
		public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
				final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {

			// .and(named("update")) is enough for our effectiveness evaluation scenario
			// (the invoice update form)
			// should keep just .method(isPublic()) for general scenarios and efficiency
			// evaluation
			/*
			 * return builder.method(isPublic().and(named("doFilter"))).intercept(Advice.to(
			 * ControllerMethodAdvice.class))
			 * .method(isPublic().and(named("update"))).intercept(Advice.to(
			 * ModelMethodAdvice.class));
			 */

			return builder.method(matcher).intercept(Advice.to(MonitoringAdvice.class));
		}
	}

	public static class ControllerMethodAdvice {

		@Advice.OnMethodEnter
		public static Invocation onEnter(
				/* @Advice.This Object object, */@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			String userAgent = null;
			String type = "Genuine";
			try {
				userAgent = (String) params[0].getClass().getMethod("getHeader", java.lang.String.class)
						.invoke(params[0], "User-Agent");
				if (userAgent == null || !userAgent.equals("JMeter")) {
					type = "Malicious"; /**
										 * Label it as true positive, just for automatic evaluation of intrusion/oulier
										 * detection results
										 */
				}
			} catch (Exception ex) {
			}
			long threadTag = Math.abs(new Random().nextLong());
			Thread currentThread = Thread.currentThread();
			int index = currentThread.getName().indexOf("#");
			if (index > 0) {
				String threadBasicName = currentThread.getName().substring(0, index + 1);
				currentThread.setName(threadBasicName + threadTag + ' ' + type);
			} else
				currentThread.setName(currentThread.getName() + "#" + threadTag + ' ' + type);

			System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
			System.out.println(currentThread.getName());
			System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result) {
			// Do nothing here
		}
	}

	public static class MonitoringAdvice {

		public static InvocationProducer producer = InvocationProducer.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result, @Advice.Thrown Throwable thrown) {
			
			if (invocation.returns()) {
				if (thrown != null) {
					if (invocation.returnsNumber())
						invocation.update(Integer.valueOf("0"), true);
					else
						invocation.update("NULL", true);
				} else if (result == null)
					invocation.update("NULL", false);
				else
					invocation.update(result, false);
			} else if (thrown != null)
				invocation.update(null, true);
			else
				invocation.update(null, false);

			producer.send(invocation);
		}
	}
}
