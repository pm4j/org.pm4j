package org.pm4j.tools.scaffold;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.util.reflection.PrefixUtil;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrImpl;
import org.pm4j.core.pm.impl.PmAttrListImpl;
import org.pm4j.core.pm.impl.PmBeanBase;

/**
 * Helper class that may be used to generate default resource file content.
 * <p>
 * Uses the names of public field definitions.
 *
 * @author olaf boede
 */
public class BeanPmGenerator {

  private static final Class<?>[] DEFAULT_NAME_PATTERN_ATTR_CLASSES = {
      String.class,
      Boolean.class,
      Date.class,
      Double.class,
      Integer.class,
      Long.class,
  };

  private Set<Class<?>> namePatternClassSet = new HashSet<Class<?>>(Arrays.asList(DEFAULT_NAME_PATTERN_ATTR_CLASSES));

  private File genSrcDir;

  /**
   * Postfix for PM class names.
   */
  private String pmPostfix = "Pm";
  private String idAttr = null;

  public void beanClassesToPmClassFiles(Class<?>... beanCls) {
    for (Class<?> cls : beanCls) {
      beanClassToPmClassFile(cls);
    }
  }

  public static String makePmClassText(Class<?> beanCls) {
    BeanPmGenerator beanPmGenerator = new BeanPmGenerator();
    return beanPmGenerator.beanClassToPmClassText(beanCls);
  }

  public File beanClassToPmClassFile(Class<?> beanCls) {
    assert genSrcDir != null;

    String relPkgDir = beanCls.getPackage().getName().replace(".", "/");
    File dir = new File(genSrcDir, relPkgDir);
    File srcFile = new File(dir, ClassUtils.getShortClassName(beanCls)+pmPostfix+".java");

    dir.mkdirs();
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(srcFile);
      fileWriter.write(beanClassToPmClassText(beanCls));
      fileWriter.close();
    } catch (IOException e) {
      throw new CheckedExceptionWrapper(e);
    }
    finally {
      if (fileWriter != null) {
        try {
          fileWriter.close();
        }
        catch (Exception e) {
          // nothing.
        }
      }
    }

    return srcFile;
  }

  public String beanClassToPmClassText(Class<?> beanCls) {
    List<AttrInfo> attrs = new ArrayList<AttrInfo>();
    Ctxt ctxt = new Ctxt();

    for (Method m : beanCls.getMethods()) {
      if (PrefixUtil.isGetter(m) && !m.getDeclaringClass().equals(Object.class)) {
        attrs.add(new AttrInfo(PrefixUtil.propNameForGetter(m.getName()), m));
      }
    }

    StringBuilder sb = new StringBuilder(4000);

    String beanClassName = ClassUtils.getShortClassName(beanCls);
    String pmClassName = beanClassName + pmPostfix;

    ctxt.addUsedClass(PmBeanBase.class);
    ctxt.addUsedClass(PmBeanCfg.class);

    sb.append("@PmBeanCfg(beanClass="+beanClassName+".class");
    if (StringUtils.isNotBlank(idAttr)) {
      sb.append(", key=\""+idAttr+"\"");
    }
    sb.append(")\n");

    sb.append("public class "+pmClassName+" extends PmBeanBase<"+beanClassName+"> {\n\n");

//    sb.append("  public "+pmClassName+"(PmObject pmParent, "+beanClassName+" bean) {\n");
//    sb.append("    super(pmParent, bean);\n");
//    sb.append("  }\n\n");

    for (AttrInfo attrInfo : attrs) {
      addAttrInfo(sb, attrInfo, ctxt);
    }

    sb.append("\n}");

    return makeHeader(beanCls, ctxt) + sb.toString();
  }

  private String makeHeader(Class<?> beanCls, Ctxt ctxt) {
    String ownPackageName = beanCls.getPackage().getName();
    StringBuilder sb = new StringBuilder(1000);

    sb.append("package ").append(beanCls.getPackage().getName()).append(";\n\n");

    for (String clsName : ctxt.usedClassNames) {
      // XXX: check inner class logic. It's not correct!
      int dollarPos = clsName.indexOf('$');
      if (dollarPos != -1) {
        clsName = clsName.substring(0, dollarPos);
      }

      // No include for Java default imports.
      if (clsName.startsWith("java.lang"))
        continue;

      // No includes for own package.
      // But members of sub packages and inner classes are included.
      if (clsName.startsWith(ownPackageName)) {
        String afterPackage = clsName.substring(ownPackageName.length()+1);
        if ((afterPackage.indexOf('.') == -1) && (afterPackage.indexOf('$') == -1))
          continue;
      }

      sb.append("import ").append(clsName).append(";\n");
    }
    sb.append("\n");

    return sb.toString();
  }

  private void addAttrInfo(StringBuilder sb, AttrInfo attrInfo, Ctxt ctxt) {
    Class<?> type = attrInfo.getNonPrimitiveReturnType();
    String typeName = attrInfo.getNonPrimitiveReturnTypeName();
    String attrName = attrInfo.getName();
    String pmAttrTypeName;

    // general PM field annotations
    if ((!attrInfo.hasSetterMethod()) && (!attrInfo.isCollection())) {
      sb.append("  @PmAttrCfg(readOnly=true)\n");
    }

    // For all classes that use a simple class name based pattern (E.g. Long, String...)
    if (namePatternClassSet.contains(type)) {
      String attrDeclPkgName = PmAttr.class.getPackage().getName();
      pmAttrTypeName = "PmAttr"+typeName;
      String implClassName = pmAttrTypeName+"Impl";

      ctxt.addUsedClass(attrDeclPkgName+".PmAttr"+typeName);
      ctxt.addUsedClass(type);

      sb.append("  public final "+pmAttrTypeName+" "+attrName+" = new "+implClassName+"(this);\n");

      String attrImplPkgName = PmAttrBase.class.getPackage().getName();
      ctxt.addUsedClass(attrImplPkgName+"."+implClassName);
    }
    else
    // Enum attribues have a special constructor that passes the enum class reference.
    if (attrInfo.isEnum()) {
      pmAttrTypeName = "PmAttrEnum<"+typeName+">";
      ctxt.addUsedClasses(PmAttrEnum.class, PmAttrEnumImpl.class, type);
      sb.append("  public final "+pmAttrTypeName+" "+attrName+
                " = new PmAttrEnumImpl<"+typeName+">(this, "+typeName+".class);\n");
    }
    else
    if (attrInfo.isCollection()) {
      Class<?> genType = attrInfo.getGenericsArgOfReturnType();
      ctxt.addUsedClasses(genType, PmAttrList.class, PmAttrListImpl.class);

      String genTypeName = ClassUtils.getShortClassName(genType);
      pmAttrTypeName = "PmAttrList<"+genTypeName+">";

      sb.append("  public final "+pmAttrTypeName+" "+attrName+" = new PmAttrListImpl<"+genTypeName+">(this);\n");
    }
    else {
      ctxt.addUsedClasses(type, PmAttr.class, PmAttrImpl.class);

      pmAttrTypeName = "PmAttr<"+typeName+">";

      sb.append("  public final "+pmAttrTypeName+" "+attrName+" = new PmAttrImpl<"+typeName+">(this);\n");
    }

    // public getter for frameworks that do not support public field access:
//    sb.append("  public "+pmAttrTypeName+" get"+StringUtils.capitalize(attrName)+"() { return this."+attrName+"; }\n");

    sb.append("\n");
  }

  private class Ctxt {
    Set<String> usedClassNames = new TreeSet<String>();

    public void addUsedClass(Class<?> cls) {
      usedClassNames.add(cls.getName());
    }

    public void addUsedClasses(Class<?>... classes) {
      for (Class<?> c : classes) {
        usedClassNames.add(c.getName());
      }
    }

    public void addUsedClass(String clsName) {
      usedClassNames.add(clsName);
    }
  }

  public void setGenSrcDir(File p_genSrcDir) {
    this.genSrcDir = p_genSrcDir;
  }

  public void setPmPostfix(String p_pmPostfix) {
    this.pmPostfix = p_pmPostfix;
  }


}
