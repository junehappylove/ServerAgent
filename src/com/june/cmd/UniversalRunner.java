package com.june.cmd;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public final class UniversalRunner {
	
	private static final String CLASSPATH_SEPARATOR = System.getProperty("path.separator");
	private static final String OS_NAME = System.getProperty("os.name");
	private static final String OS_NAME_LC = OS_NAME.toLowerCase(Locale.ENGLISH);
	private static final String JAVA_CLASS_PATH = "java.class.path";
	private static final String jarDirectory;
	private static final String ADDITIONAL_CP = "additional.classpath";
	private static final FilenameFilter jarFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	static {
		System.setProperty("java.awt.headless", "true");
		List<URL> jars = new LinkedList<URL>();
		StringBuffer classpath = new StringBuffer();

		String initial_classpath = System.getProperty(JAVA_CLASS_PATH);
		String additional = System.getProperty(ADDITIONAL_CP);
		if (additional != null) {
			initial_classpath = initial_classpath + CLASSPATH_SEPARATOR
					+ additional;
			String[] parts = additional.split(CLASSPATH_SEPARATOR);
			for (int n = 0; n < parts.length; n++) {
				File[] f = { new File(parts[n]) };
				if (f[0].isDirectory()) {
					f = f[0].listFiles(jarFilter);
				}
				addFiles(f, jars, classpath);
			}
		}
		jarDirectory = getJarDirectory(initial_classpath);

		buildUpdatedClassPath(jarDirectory, jars, classpath);

		String cp = classpath.toString();
		System.setProperty(JAVA_CLASS_PATH, cp);

		URL[] urls = (URL[]) jars.toArray(new URL[0]);
		URLClassLoader loader = new URLClassLoader(urls);
		Thread.currentThread().setContextClassLoader(loader);
	}

	private static String getJarDirectory(String initial_classpath) {
		String tmpDir = null;
		StringTokenizer tok = new StringTokenizer(initial_classpath,
				File.pathSeparator);
		if ((tok.countTokens() == 1)
				|| ((tok.countTokens() == 2) && (OS_NAME_LC.startsWith("mac os x")))) {
			File jar = new File(tok.nextToken());
			try {
				tmpDir = jar.getCanonicalFile().getParent();
			} catch (IOException e) {
			}
		} else {
			File userDir = new File(System.getProperty("user.dir"));
			tmpDir = userDir.getAbsolutePath();
		}
		return tmpDir;
	}

	private static StringBuffer buildUpdatedClassPath(String jarDir, List<URL> jars, StringBuffer classpath) {
		List<File> libDirs = new LinkedList<File>();
		File f = new File(jarDir);
		while (f != null) {
			libDirs.add(f.getAbsoluteFile());
			f = f.getParentFile();
		}
		f = new File(jarDir + File.separator + "lib");
		if (f != null) {
			libDirs.add(f.getAbsoluteFile());
		}
		Iterator<File> it = libDirs.iterator();
		while (it.hasNext()) {
			File libDir = (File) it.next();
			File[] libJars = libDir.listFiles(jarFilter);
			if (libJars == null) {
				new Throwable("Could not access " + libDir)
						.printStackTrace(System.err);
			} else {
				addFiles(libJars, jars, classpath);
			}
		}
		return classpath;
	}

	private static void addFiles(File[] libJars, List<URL> jars,
			StringBuffer classpath) {
		boolean usesUNC = OS_NAME_LC.startsWith("windows");
		for (int i = 0; i < libJars.length; i++) {
			try {
				String s = libJars[i].getPath();
				if (usesUNC) {
					if ((s.startsWith("\\\\")) && (!s.startsWith("\\\\\\"))) {
						s = "\\\\" + s;
					} else if ((s.startsWith("//")) && (!s.startsWith("///"))) {
						s = "//" + s;
					}
				}
				jars.add(new File(s).toURI().toURL());
				classpath.append(CLASSPATH_SEPARATOR);
				classpath.append(s);
			} catch (MalformedURLException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	public static String getJARLocation() {
		return jarDirectory;
	}

	public static void main(String[] args) throws Throwable {
		try {
			args = new String[]{"--tool","PerfMonAgent"};
			Class<?> initialClass = Thread.currentThread().getContextClassLoader().loadClass("com.june.cmdtools.PluginsCMD");
			Object instance = initialClass.newInstance();
			Method startup = initialClass.getMethod("processParams", new Class[] { new String[0].getClass() });
			Object res = startup.invoke(instance, new Object[] { args });
			int rc = ((Integer) res).intValue();
			if (rc != 0) {
				System.exit(rc);
			}
		} catch (Throwable e) {
			if (e.getCause() != null) {
				System.err.println("ERROR: " + e.getCause().toString());
				System.err.println("*** Problem's technical details go below ***");
				System.err.println("Home directory was detected as: " + jarDirectory);
				throw e.getCause();
			}
			System.err.println("Home directory was detected as: " + jarDirectory);
			throw e;
		}
	}
}
