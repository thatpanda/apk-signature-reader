import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ApkFilter extends FileFilter
{
    public boolean accept(File f) {
        if(f.isDirectory()) {
            return true;
        }
        
        String extension = getFileExtension(f);
        return extension.equalsIgnoreCase(".apk") ||
                extension.equalsIgnoreCase(".bin");
    }

    public String getDescription() {
        return "Android Application Package (*.apk; *.bin)";
    }

    private static String getFileExtension(File f) {
        String filename = f.getName();
        int dotIndex = filename.lastIndexOf(".");
        if(dotIndex != -1) {
            return filename.substring(dotIndex);
        }
        return "";
    }
}