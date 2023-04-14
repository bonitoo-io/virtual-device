package io.bonitoo.qa.plugin.util;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarTool {
    private Manifest manifest = new Manifest();

    public void startManifest() {
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    }

    public void setMainClass(String mainClass) {
        if (mainClass != null && !mainClass.equals("")) {
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        }
    }

    public void addToManifest(String key, String value) {
        manifest.getMainAttributes().put(new Attributes.Name(key), value);
    }

    public JarOutputStream openJar(String jarFile) throws IOException {
        return new JarOutputStream(new FileOutputStream(jarFile), manifest);
    }

    public void addFile(JarOutputStream target, String rootPath, String source)
      throws FileNotFoundException, IOException {

        System.out.println("DEBUG rootPath " + rootPath);

        String remaining = "";
        if (rootPath.endsWith(File.separator)) {
            remaining = source.substring(rootPath.length());
        } else {
            remaining = source.substring(rootPath.length() + 1);
        }

        System.out.println("DEBUG remaining " + remaining);
        String name = remaining.replace("\\","/");
        System.out.println("DEBUG name " + name);
        JarEntry entry = new JarEntry(name);
        entry.setTime(new File(source).lastModified());
        target.putNextEntry(entry);

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        byte[] buffer = new byte[1024];
        while (true) {
            int count = in.read(buffer);
            if (count == -1) {
                break;
            }
            target.write(buffer, 0, count);
        }
        target.closeEntry();
        in.close();
    }

    public void addRenamedFile(JarOutputStream target, String rootPath, String source, String jarPath)
      throws FileNotFoundException, IOException {
        System.out.println("DEBUG rootPath " + rootPath);

        String remaining = "";
        if (rootPath.endsWith(File.separator)) {
            remaining = source.substring(rootPath.length());
        } else {
            remaining = source.substring(rootPath.length() + 1);
        }

        System.out.println("DEBUG remaining " + remaining);
        String name = remaining.replace("\\","/");
        System.out.println("DEBUG name " + name);
        JarEntry entry = new JarEntry(jarPath);
        entry.setTime(new File(source).lastModified());
        target.putNextEntry(entry);

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        byte[] buffer = new byte[1024];
        while (true) {
            int count = in.read(buffer);
            if (count == -1) {
                break;
            }
            target.write(buffer, 0, count);
        }
        target.closeEntry();
        in.close();

    }

}