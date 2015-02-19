package org.pm4j.tools.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.ComparisonFailure;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.util.io.FileUtil;
import org.pm4j.common.util.io.SrcFileAccessor;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmObject.PmMatcher;
import org.pm4j.core.pm.impl.PmMatcherBuilder;
import org.pm4j.core.xml.visibleState.VisibleStateAspect;
import org.pm4j.core.xml.visibleState.VisibleStateAspectMatcher;
import org.pm4j.core.xml.visibleState.VisibleStateUtil;

/**
 * A test tool that allows comparing the visible state of a PM tree against an XML file
 * containing the the expected state.
 * <p>
 * You may configure the content to compare by defining the set of checked PMs and (see:
 * {@link VisibleStateAspect})s.<br>
 * Please have a look at the <code>exclude</code> methods for that.
 * <p>
 * You can configure several test modes for this tool by setting JVM parameter
 * {@code pmSnapshotTestMode} or by calling {@link PmSnapshotTestTool#setTestMode(TestMode)}
 * method.<br>
 * 
 * @see TestMode
 * 
 * @author Olaf Boede
 * @author Aleksander Lech
 */
public class PmSnapshotTestTool {

    /**
     * Represents a test mode of the {@link PmSnapshotTestTool}.
     */
    public static enum TestMode {
        /**
         * Performs strict compare test run. No file can be changed or generated at this point.
         * This mode is intended to be used in build scripts. This mode is assumed as
         * <b>DEFAULT</b>.
         */
        STRICT,

        /**
         * Recommended for development use, once the initial snapshot file is generated behaves
         * like {@link TestMode#STRICT} performing normal tests.
         */
        AUTO_CREATE,

        /**
         * Recommended only for intended WRITE of existing snapshot files. No compare tests are
         * executed in this mode.
         */
        WRITE
    }

    private static final char CLASSLOADER_RESOURCE_SEPARATOR = '/';
    private static final Logger LOG = LoggerFactory.getLogger(PmSnapshotTestTool.class);
    private static final String SYS_PROP_TEST_MODE = "pmSnapshotTestMode";

    private final Class<?> testCtxtClass;
    private SrcFileAccessor srcFileAccessor;
    private TestMode testMode;

    private Collection<PmMatcher> excludedPms = new ArrayList<PmMatcher>();
    private Collection<VisibleStateAspectMatcher> excludedAspects = new ArrayList<VisibleStateAspectMatcher>();

    /**
     * @param testCtxtClass
     *            A class that is used to determine the file location from. This is usually the
     *            test class that uses this snapshot test instance.
     */
    public PmSnapshotTestTool(Class<?> testCtxtClass) {
        assert testCtxtClass != null;
        this.testCtxtClass = testCtxtClass;

        try {
            this.testMode = TestMode.valueOf(System.getProperty(SYS_PROP_TEST_MODE, TestMode.STRICT.toString()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pmSnapshotTestMode value. Please check allowed values for org.pm4j.tools.test.PmSnapshotTestTool.TestMode.", e);
        }
    }

    /**
     * Configures PM items to hide.
     * 
     * @param hideMatchers
     * @return the tool for inline usage.
     */
    public PmSnapshotTestTool excludePms(PmMatcher... hideMatchers) {
        excludedPms.addAll(Arrays.asList(hideMatchers));
        return this;
    }

    /**
     * Configures PM aspects to hide.
     * 
     * @param hideMatchers
     * @return the tool for inline usage.
     */
    public PmSnapshotTestTool exclude(VisibleStateAspectMatcher... hideMatchers) {
        excludedAspects.addAll(Arrays.asList(hideMatchers));
        return this;
    }

    /**
     * Excludes all instances of the given class from XML snapshot test.
     * 
     * @param pmClass
     *            The PM class to exclude.
     */
    public void exclude(Class<?> pmClass) {
        PmMatcher pmMatcher = new PmMatcherBuilder().pmClass(pmClass).build();
        excludePms(pmMatcher);
    }

    /**
     * Excludes PM fields of the given class from XML snapshot test.
     * 
     * @param pmClass
     *            The PM class to exclude PM fields for.
     * @param names
     *            The field names to be excluded.<br>
     *            RegExpressions like <code>cmd*</code> are supported.
     */
    public void exclude(Class<?> pmClass, String... names) {
        for (String name : names) {
            PmMatcher pmMatcher = new PmMatcherBuilder().pmClass(pmClass).name(name).build();
            excludePms(pmMatcher);
        }
    }

    /**
     * Excludes some {@link VisibleStateAspect}s from XML snapshot tests.
     * 
     * @param pmMatcher
     *            Rule used to identify matching PMs.
     * @param aspects
     *            The aspects to exclude for all matching PMs.
     */
    public void exclude(PmMatcher pmMatcher, VisibleStateAspect... aspects) {
        exclude(new VisibleStateAspectMatcher(pmMatcher, aspects));
    }

    /**
     * Excludes some {@link VisibleStateAspect}s from XML snapshot tests.
     * 
     * @param pmClass
     *            The PM class to apply this exclude for.
     * @param aspects
     *            The aspects to exclude for all matching PM(s).
     */
    public void exclude(Class<?> pmClass, VisibleStateAspect... aspects) {
        exclude(new VisibleStateAspectMatcher(pmClass, aspects));
    }

    /**
     * Excludes {@link VisibleStateAspect}s of the specified PM's from XML snapshot test.
     * 
     * @param pmClass
     *            The PM class.
     * @param name
     *            Name of the PM field(s) of the given class.<br>
     *            RegExpressions like <code>cmd*</code> are supported.
     * @param aspects
     *            The aspects to exclude for all matching PM(s).
     */
    public void exclude(Class<?> pmClass, String name, VisibleStateAspect... aspects) {
        PmMatcher pmMatcher = new PmMatcherBuilder().pmClass(pmClass).name(name).build();
        exclude(pmMatcher, aspects);
    }

    /**
     * Performs the snapshot logic. It has the following execution modi:
     * <ol>
     * <li>Snapshot file does not exist: The snapshot file will be generated.</li>
     * <li>Snapshot file exists: The current PM state will be compared to the documented state
     * by {@link #compare(PmObject, String, File)} method.</li>
     * <ol>
     * 
     * @param rootPm
     *            The root of the PM tree to verify.
     * @param fileNameBase
     *            Name of the XML file. Without '.xml' post fix. E.g.
     *            'myTest_afterEnteringData'.
     * @return a reference to the used or generated XML file.
     */
    public final File snapshot(PmObject rootPm, String fileNameBase) {
        File expectedFile = getExpectedStateFile(fileNameBase);
        switch (testMode) {
        case STRICT:
            return compare(rootPm, fileNameBase, expectedFile);
        case WRITE:
            return writeSnapshot(rootPm, fileNameBase);
        case AUTO_CREATE:
            if (expectedFile != null && expectedFile.exists()) {
                return compare(rootPm, fileNameBase, expectedFile);
            } else {
                return writeSnapshot(rootPm, fileNameBase);
            }
        default:
            throw new UnsupportedOperationException("Unsupported TestMode: " + testMode);
        }
    }

    /**
     * Compares the current PM state will be compared to the documented state stored in
     * {@code expectedFile}.
     * 
     * @param rootPm
     *            The root of the PM tree to verify.
     * @param fileNameBase
     *            Name of the XML file. Without '.xml' post fix. E.g.
     *            'myTest_afterEnteringData'.
     * @param expectedFile
     *            File handle for stored documented state
     * @return The expected file again for inline usage.
     * 
     * @throws ComparisonFailure
     *             in case of any encountered difference
     */
    protected File compare(PmObject rootPm, String fileNameBase, File expectedFile) {
        File actualStateFile = getActualStateFile(fileNameBase);
        String actualStateXmlString = VisibleStateUtil.toXmlString(rootPm, excludedPms, excludedAspects);

        if (expectedFile == null || !expectedFile.exists()) {
            writeActualStateFile(actualStateFile, actualStateXmlString);
            fail("Expectation file not found for '" + fileNameBase + "'.\n" + "The actual state is reported in file: " + actualStateFile + "'.\n" + "After reviewing its content you may consider using it as your expectation file that should be located at:\n" + "\t" + getExpectedStateSrcFile(fileNameBase) + "'.\n" + "Alternatively you may consider configuring PmSnapshotTestTool to AUTO_CREATE or WRITE by calling setTestMode() or defining -DpmSnapshotTestMode=AUTO_CREATE");
        }

        try {
            // Disturbing Windows carriage return characters need to be removed.
            String expectedStateXmlString = FileUtil.fileToString(expectedFile).replaceAll("\r\n", "\n");
            assertEquals("Compare " + fileNameBase + "\nExpected: " + expectedFile + "\nCurrent: " + actualStateFile, expectedStateXmlString, actualStateXmlString);
            return expectedFile;
        } catch (ComparisonFailure e) {
            writeActualStateFile(actualStateFile, actualStateXmlString);
            throw e;
        }
    }

    /**
     * Writes snapshot of {@code rootPm} replacing existing one if any.
     * 
     * @param rootPm
     *            PM Object
     * @param fileNameBase
     *            Name of the XML file. Without '.xml' post fix. E.g.
     *            'myTest_afterEnteringData'.
     * 
     * @return generated snapshot {@link File} handle
     */
    protected File writeSnapshot(PmObject rootPm, String fileNameBase) {
        File expectedFileSrc = getExpectedStateSrcFile(fileNameBase);
        try {
            FileUtil.createFile(expectedFileSrc);
            VisibleStateUtil.toXmlFile(rootPm, expectedFileSrc, excludedPms, excludedAspects);
            LOG.info("Created snapshot file: " + expectedFileSrc + ", if using Eclipse please refresh your workspace!!!");
        } catch (Exception e) {
            throw new PmRuntimeException(rootPm, "Unable to write file " + expectedFileSrc, e);
        }
        return expectedFileSrc;
    }

    /**
     * Writes a snapshot of {@code rootPm} to an file having a name that ends with
     * '.actual.xml'.
     * 
     * @param actualStateFile
     *            is file to write to.
     * @param actualStateXmlString
     *            provides the file content.
     */
    protected void writeActualStateFile(File actualStateFile, String actualStateXmlString) {
        try {
            LOG.info("Create actual state file " + actualStateFile);
            FileUtil.createFile(actualStateFile);
            FileUtil.stringToFile(actualStateFile, actualStateXmlString);
        } catch (RuntimeException e2) {
            // If the test gets executed for a compiled .jar it will not be
            // possible to write the actual state file.
            // The programmer will usually repeat the test in a development
            // environment.
            // Then this error will not occur.
            LOG.error("Unable to write actual state file " + actualStateFile, e2);
        }
    }

    /**
     * Provides the directory the actual PM state files will be written to.
     * <p>
     * The default implementation provides the sub directory {@link #xmlSubDirName()} within the
     * folder the '.class' file of the test class (testCtxtClass) if located in.
     * <p>
     * This location may be customized by overriding this method.
     * 
     * @return the directory to write actual state files to.
     */
    protected File getActualStateDir() {
        return new File(getSrcFileAccessor().getSrcPkgDir(), xmlSubDirName());
    }

    /**
     * Provides the directory the expected PM state files will be read from.<br>
     * The initial test run will also use it to write the initial expected files.
     * <p>
     * The default implementation provides the sub directory {@link #xmlSubDirName()} within the
     * folder the '.java' file of the test class (testCtxtClass) if located in.
     * <p>
     * This location may be customized by overriding this method.
     * 
     * @return the directory to handle the expected state files in or {@code null} if not exist.
     */
    protected File getExpectedStateDir() {
        Class<?> testClass = getSrcFileAccessor().getCtxtClass();

        String expectedDirUrlString = new StringBuilder(testClass.getPackage().getName().replace('.', CLASSLOADER_RESOURCE_SEPARATOR)).append(CLASSLOADER_RESOURCE_SEPARATOR).append(xmlSubDirName()).toString();
        URL expectedDirUrl = testClass.getClassLoader().getResource(expectedDirUrlString);

        try {
            return expectedDirUrl == null ? null : new File(expectedDirUrl.toURI());
        } catch (URISyntaxException e) {
            throw new CheckedExceptionWrapper(e);
        }
    }

    /**
     * Provides the {@link File} the expected PM state will be read from.
     * 
     * @param fileNameBase
     *            The file name base string, provided as argument of
     *            {@link #snapshot(PmObject, String)}.
     * @return the file to use or {@code null} if not exist
     */
    protected File getExpectedStateFile(String fileNameBase) {
        File expectedStateDir = getExpectedStateDir();
        return expectedStateDir == null ? null : new File(getExpectedStateDir(), fileNameBase + ".xml");
    }

    protected File getExpectedStateSrcDir() {
        return new File(getSrcFileAccessor().getSrcPkgDir(), xmlSubDirName());
    }

    /**
     * Provides the {@link File} the actual PM state will be written to.
     * 
     * @param fileNameBase
     *            The file name base string, provided as argument of
     *            {@link #snapshot(PmObject, String)}.
     * @return the file to write.
     */
    protected File getActualStateFile(String fileNameBase) {
        return new File(getActualStateDir(), fileNameBase + ".actual.xml");
    }

    /**
     * Provides the {@link File} the expected PM state will be written to in write mode.
     * 
     * @param fileNameBase
     *            The file name base string, provided as argument of
     *            {@link #snapshot(PmObject, String)}.
     * @return the file to use.
     */
    protected File getExpectedStateSrcFile(String fileNameBase) {
        return new File(getExpectedStateSrcDir(), fileNameBase + ".xml");
    }

    /**
     * @return the name of the XML sub directory to use (in src as well as in temp dir).<br>
     *         The default implementation provides the uncapitalized test class name.
     */
    protected String xmlSubDirName() {
        return StringUtils.uncapitalize(testCtxtClass.getSimpleName());
    }

    /**
     * @return the {@link SrcFileAccessor} that provides access to source and bin path
     *         information.
     */
    protected SrcFileAccessor getSrcFileAccessor() {
        if (srcFileAccessor == null) {
            srcFileAccessor = makeSrcFileAccessor(testCtxtClass);
            assert srcFileAccessor != null;
        }
        return srcFileAccessor;
    }

    /**
     * Creates a source file structure accessor that matches your specific source/target
     * directory structure.
     * 
     * @param testCtxtClass
     *            The test class context. Used to get directory information.
     * @return the {@link SrcFileAccessor} to use.
     */
    protected SrcFileAccessor makeSrcFileAccessor(Class<?> testCtxtClass) {
        return new SrcFileAccessor(testCtxtClass);
    }

    /**
     * @return the current tool test mode
     */
    public final TestMode getTestMode() {
        return testMode;
    }

    /**
     * Sets the test mode of {@link PmSnapshotTestTool}. <br/>
     * <br/>
     * <b>Note: This method is for development use only, do not commit any code that uses it<br/>
     * consider using system param <code>-DpmSnapshotTestMode</code></b>
     * 
     * @param testMode
     *            the testMode to set
     * 
     * @see TestMode
     */
    public final void setTestMode(TestMode testMode) {
        this.testMode = testMode;
    }

}
