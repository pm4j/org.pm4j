package org.pm4j.util.scaffold;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.pm4j.testdomains.user.User;
import org.pm4j.tools.scaffold.ResKeyReporter;

public class ResKeyReporterMain {

  public static void main(String[] args) throws IOException {
    String pmPostfix = "Pm_";
    File genSrcDir = new File("src/generated");
    String pkgName = User.class.getPackage().getName();
    File dir = new File(genSrcDir, pkgName.replace(".", "/"));
    FileWriter resFileWriter = new FileWriter(new File(dir, "resources_en.properties"));
    
    ResKeyReporter reporter = new ResKeyReporter();
    reporter.setPmPostfix(pmPostfix);
    
    resFileWriter.write(reporter.reportResKeysToString(
        pkgName+".User"+pmPostfix, pkgName+".Domain"+pmPostfix));

    resFileWriter.close();
  }
}
