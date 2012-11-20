package org.pm4j.util.scaffold;

import java.io.File;
import java.io.IOException;

import org.pm4j.core.util.reflection.ClassUtil;
import org.pm4j.testdomains.user.Domain;
import org.pm4j.testdomains.user.User;
import org.pm4j.tools.scaffold.BeanPmGenerator;

public class BeanPmGeneratorMain {

  public static void main(String[] args) throws IOException {

    File prjRootDir = ClassUtil.getClassDir(BeanPmGeneratorMain.class)
                                    .getParentFile()
                                    .getParentFile()
                                    .getParentFile()
                                    .getParentFile()
                                    .getParentFile()
                                    .getParentFile();
	  File targetDir = new File(prjRootDir, "test/java");

    System.out.println(targetDir.getAbsolutePath());

    BeanPmGenerator gen = new BeanPmGenerator();

    gen.setGenSrcDir(targetDir);
    gen.setPmPostfix("PmTemplate");

    gen.beanClassesToPmClassFiles(User.class, Domain.class);

    // some visual console output...
    System.out.println(gen.beanClassToPmClassText(Domain.class));
    System.out.println(gen.beanClassToPmClassText(User.class));
  }
}
