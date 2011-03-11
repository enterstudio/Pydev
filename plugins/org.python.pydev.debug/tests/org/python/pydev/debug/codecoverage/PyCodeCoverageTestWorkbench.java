/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.debug.codecoverage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.python.pydev.core.TestCaseUtils;
import org.python.pydev.editor.codecompletion.revisited.javaintegration.AbstractWorkbenchTestCase;
import org.python.pydev.plugin.nature.PythonNature;

public class PyCodeCoverageTestWorkbench extends AbstractWorkbenchTestCase{

    public static Test suite() {
        TestSuite suite = new TestSuite(PyCodeCoverageTestWorkbench.class.getName());
        
        suite.addTestSuite(PyCodeCoverageTestWorkbench.class); 
        
        if (suite.countTestCases() == 0) {
            throw new Error("There are no test cases to run");
        } else {
            return suite;
        }
    }

    private IFolder sourceFolder;
    
    /* (non-Javadoc)
     * @see org.python.pydev.editor.codecompletion.revisited.javaintegration.AbstractWorkbenchTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        closeWelcomeView();
        configureInterpreters();
        
        IProgressMonitor monitor = new NullProgressMonitor();
        IProject project = createProject(monitor, "coverage_test_project");
        sourceFolder = createSourceFolder(monitor, project, true, false);
        IFile initFile = createPackageStructure(sourceFolder, "pack_cov", monitor);
        IFile modCov = initFile.getParent().getFile(new Path("mod_cov.py"));
        setFileContents(modCov, getModCovContents());


        PythonNature nature = PythonNature.getPythonNature(project);
        waitForNatureToBeRecreated(nature);
    }
    
    /**
     * @return
     */
    private String getModCovContents() {
        return "" +
        		"import unittest\n" +
        		"\n" +
        		"class TestCase(unittest.TestCase):\n" +
        		"    \n" +
        		"    def testCovered1(self):\n" +
        		"        print('t1')\n" +
        		"        print('t2')\n" +
        		"        print('t3')\n" +
        		"    \n" +
        		"    def testCovered2(self):\n" +
        		"        print('t1')\n" +
        		"        print('t2')\n" +
        		"        print('t3')\n" +
        		"        \n" +
        		"    def testNotCovered(self):\n" +
        		"        if False:\n" +
        		"            print('t1')\n" +
        		"            print('t2')\n" +
        		"            print('t3')\n" +
        		"        \n" +
        		"if __name__ == '__main__':\n" +
        		"    unittest.main()\n" +
        		"";
    }

    public void testPyCodeCoverageView() throws Exception {
        
        PyCodeCoverageView view = PyCodeCoverageView.getView(true);
        //At this point it should have no folder selected and the option to run things in coverage should be
        //set to false.
        assertTrue(!PyCodeCoverageView.getAllRunsDoCoverage());
        assertTrue(PyCodeCoverageView.getChosenDir() == null);
        
        assertTrue(!view.allRunsGoThroughCoverage.getSelection());
        assertTrue(!PyCodeCoverageView.allRunsDoCoverage);
        view.allRunsGoThroughCoverage.setSelection(true);
        view.allRunsGoThroughCoverage.notifyListeners(SWT.Selection, new Event());
        
        assertTrue(PyCodeCoverageView.allRunsDoCoverage);
        assertTrue(!PyCodeCoverageView.getAllRunsDoCoverage());

        view.setSelectedContainer(sourceFolder, new NullProgressMonitor());
        TreeViewer treeViewer = view.getTreeViewer();
        ITreeContentProvider cp = (ITreeContentProvider) treeViewer.getContentProvider();
        Object[] elements = cp.getElements(treeViewer.getInput());
        assertEquals(1, elements.length);
        ILabelProvider labelProvider = (ILabelProvider) treeViewer.getLabelProvider();
        assertEquals("pack_cov", labelProvider.getText(elements[0]));
        
        TestCaseUtils.assertContentsEqual(getInitialCoverageText(), view.getCoverageText());

        Object[] expandedElements = treeViewer.getExpandedElements();
        assertEquals(0, expandedElements.length);
        treeViewer.expandAll();
        expandedElements = treeViewer.getExpandedElements();
        assertEquals(1, expandedElements.length);
        
        view.executeRefreshAction(new NullProgressMonitor());
        expandedElements = treeViewer.getExpandedElements();
        assertEquals(1, expandedElements.length);
        
//        final IWorkbench workBench = PydevPlugin.getDefault().getWorkbench();
//        Display display = workBench.getDisplay();
//
//
//        // Make sure to run the UI thread.
//        display.syncExec( new Runnable(){
//            public void run(){
//                JythonLaunchShortcut launchShortcut = new JythonLaunchShortcut();
//                launchShortcut.launch(debugEditor, "debug");
//            }
//        });

        goToManual();
          
    }

    /**
     * @return
     */
    private String getInitialCoverageText() {
        return "" +
        		"Name                                      Stmts     Exec     Cover  Missing\n" +
        		"-----------------------------------------------------------------------------\n" +
        		"pack_cov\\mod_cov.py                          17        0         0%  1-22\n" +
        		"pack_cov\\__init__.py                          0        0         0%  \n" +
        		"-----------------------------------------------------------------------------\n" +
        		"TOTAL                                        17        0         0%  \n" +
        		"";
    }
}
