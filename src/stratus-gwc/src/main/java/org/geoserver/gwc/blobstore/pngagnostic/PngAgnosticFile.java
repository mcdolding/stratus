package org.geoserver.gwc.blobstore.pngagnostic;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Utility class used to creste  Blob Stores that do not care if the underlying png file has "png" or "png8" suffix.
 *
 */
public class PngAgnosticFile {

    /**
     * Given a non exisiting "png" or "png8" file, will return path to alternate "png8", "png" file respectively.
     * Returns the same file if the file exists or in not "pn2g or "png8"
     * @param file
     * @return
     */
    public static File getPngOrPng8File(File file) {
        if (!file.exists()) {
            String extension = FilenameUtils.getExtension(file.getName());
            if ("png".equals(extension)) {
                return new File(file.getParent(), updateExtension(file.getName(), "png8"));
            } else if ("png8".equals(extension)) {
                return new File(file.getParent(), updateExtension(file.getName(), "png"));
            }
        }
        return file;
    }

    private static String updateExtension(String filename, String extension) {
        return String.format("%s.%s", FilenameUtils.getBaseName(filename), extension);
    }

    public static void main(String arg[]) {

        System.out.println(PngAgnosticFile.getPngOrPng8File(new File("/a/b/c/test.png")).getAbsolutePath());
        System.out.println(PngAgnosticFile.getPngOrPng8File(new File("/a/b/c/test.png8")).getAbsolutePath());
    }
}
