package org.hotswap.agent.plugin.proxy.java;

import org.hotswap.agent.javassist.ClassPool;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.plugin.proxy.ProxyTransformer;
import org.hotswap.agent.plugin.proxy.signature.ClassfileSignatureComparer;

/**
 * Redefines Java proxy classes. One-step process. Uses CtClasses from the ClassPool.
 * 
 * @author Erki Ehtla
 * 
 */
public class JavassistProxyTransformer implements ProxyTransformer {
	private static AgentLogger LOGGER = AgentLogger.getLogger(JavassistProxyTransformer.class);
	private final Class<?> classBeingRedefined;
	private final byte[] classfileBuffer;
	private final CtClass cc;
	private final ClassPool cp;
	
	/**
	 * 
	 * @param classBeingRedefined
	 * @param classfileBuffer
	 *            new definition of Class<?>
	 * @param cc
	 *            CtClass from classfileBuffer
	 * @param cp
	 *            Classpool of the classloader
	 * @return classfileBuffer or new Proxy defition if there are signature changes
	 */
	public JavassistProxyTransformer(Class<?> classBeingRedefined, byte[] classfileBuffer, CtClass cc, ClassPool cp) {
		super();
		this.classBeingRedefined = classBeingRedefined;
		this.classfileBuffer = classfileBuffer;
		this.cc = cc;
		this.cp = cp;
	}
	
	/**
	 * 
	 * @param classBeingRedefined
	 * @param classfileBuffer
	 *            new definition of Class<?>
	 * @param cc
	 *            CtClass from classfileBuffer
	 * @param cp
	 *            Classpool of the classloader
	 * @return classfileBuffer or new Proxy defition if there are signature changes
	 * @throws Exception
	 */
	public static byte[] transform(final Class<?> classBeingRedefined, byte[] classfileBuffer, CtClass cc, ClassPool cp)
			throws Exception {
		return new JavassistProxyTransformer(classBeingRedefined, classfileBuffer, cc, cp).transformRedefine();
	}
	
	@Override
	public byte[] transformRedefine() throws Exception {
		try {
			if (!ClassfileSignatureComparer.isNonSyntheticPoolClassOrParentDifferent(classBeingRedefined, cp)) {
				return classfileBuffer;
			}
			byte[] generateProxyClass = CtClassJavaProxyGenerator.generateProxyClass(classBeingRedefined.getName(),
					cc.getInterfaces(), cp);
			LOGGER.reload("Class '{}' has been reloaded.", classBeingRedefined.getName());
			return generateProxyClass;
		} catch (Exception e) {
			LOGGER.error("Error transforming a Java reflect Proxy", e);
			return classfileBuffer;
		}
	}
}