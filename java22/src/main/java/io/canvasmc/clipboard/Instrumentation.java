package io.canvasmc.clipboard;

import java.util.jar.JarFile;

public class Instrumentation {
	public static java.lang.instrument.Instrumentation INSTRUMENTATION = null;

	public static void premain(final String arguments, final java.lang.instrument.Instrumentation instrumentation) {
		Instrumentation.agentmain(arguments, instrumentation);
	}

	public static void agentmain(final String arguments, final java.lang.instrument.Instrumentation instrumentation) {
		if (Instrumentation.INSTRUMENTATION == null) Instrumentation.INSTRUMENTATION = instrumentation;
		if (Instrumentation.INSTRUMENTATION == null)
			throw new NullPointerException("Unable to get instrumentation instance!");
	}

	public static void tryAppend(final JarFile jarFile) {
		if (!Boolean.getBoolean("paperclip.patchonly")) {
			INSTRUMENTATION.appendToSystemClassLoaderSearch(jarFile);
		}
	}
}
