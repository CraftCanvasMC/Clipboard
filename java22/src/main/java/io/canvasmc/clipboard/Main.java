package io.canvasmc.clipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public final class Main {

	public static void main(String[] arguments) {
		(new Main()).run(arguments);
	}

	private void run(String[] arguments) {
		try {
			String repoDir = System.getProperty("bundlerRepoDir", "");
			Path outputDir = Paths.get(repoDir);
			Files.createDirectories(outputDir);
			Provider<String> versionProvider = () -> {
				InputStream inputStream = Main.class.getResourceAsStream("/version.json");

				if (inputStream == null) {
					throw new IOException("Unable to locate version resource!");
				}

				String jsonContent;
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
					jsonContent = sb.toString();
				}

				return jsonContent.split("\"id\": \"")[1].split("\"")[0];
			};
			JarFile patched = new PatcherBuilder().start(versionProvider);
			new LibraryLoader().start(versionProvider);
			// run after lib loading
			if (Boolean.getBoolean("paperclip.patchonly")) {
				System.exit(0);
			}
			String defaultMainClassName = this.readMainClass(patched);
			String mainClassName = System.getProperty("bundlerMainClass", defaultMainClassName);
			if (mainClassName == null || mainClassName.isEmpty()) {
				System.out.println("Empty main class specified, exiting");
				System.exit(0);
			}

			System.out.println("Starting " + mainClassName);
			Thread runThread = new Thread(() -> {
				try {
					Class<?> mainClass = Class.forName(mainClassName);
					MethodHandle mainHandle = MethodHandles.lookup().findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class)).asFixedArity();
					mainHandle.invoke((Object) arguments);
				} catch (Throwable var5x) {
					Thrower.INSTANCE.sneakyThrow(var5x);
				}
			}, "main");
			runThread.start();
		} catch (Exception var10) {
			var10.printStackTrace(System.out);
			System.out.println("Failed to extract server libraries, exiting");
		}
	}

	private String readMainClass(JarFile patched) throws Exception {
		return patched.getManifest().getMainAttributes().getValue("Main-Class");
	}

	@FunctionalInterface
	private interface ResourceParser<T> {
		T parse(BufferedReader var1) throws Exception;
	}

	public interface Provider<T> {
		T get() throws IOException;
	}

	private static class Thrower<T extends Throwable> {
		private static final Thrower<RuntimeException> INSTANCE = new Thrower<>();

		public void sneakyThrow(Throwable exception) throws T {
			throw new RuntimeException(exception);
		}
	}
}
