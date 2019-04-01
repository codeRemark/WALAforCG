package com.ibm.wala.cast.java.ecj.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.client.impl.ZeroOneContainerCFABuilderFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.warnings.Warnings;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.cast.java.ecj.util.ExampleUtil;

/**
 * Driver that constructs a call graph for an application specified as a directory of source code.
 * Example of using the JDT front-end based on ECJ.    
 * Useful for getting some code to copy-paste.    
 */
public class SourceDirCallGraph {

  /**
   * Usage: ScopeFileCallGraph -sourceDir file_path -mainClass class_name
   * 
   * If given -mainClass, uses main() method of class_name as entrypoint.  Class
   * name should start with an 'L'.
   * 
   * Example args: -sourceDir /tmp/srcTest -mainClass LFoo
   * 
   */
	
  public static final String DOT_EXE = "I:/tool/graphviz-2.38/release/bin/dot";	
  public final static String DOT_FILE = "H:\\data\\dotfiles\\temp.dt";
  public final static String PDF_FILE = "H:\\data\\dotfiles\\temp.pdf";
	
  public static void main(String[] args) throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException, WalaException {
    long start = System.currentTimeMillis();
    Properties p = CommandLine.parse(args);
    String sourceDir = p.getProperty("sourceDir");
    String mainClass = p.getProperty("mainClass");
    
    //add 路径
    sourceDir = "I:\\ideaPjt\\WALA\\com.ibm.wala.cast.java.test.data\\src\\foo\\bar\\hello\\world";
    mainClass = "Lfoo/bar/hello/world/InnerClasses";  //Simple1 Lfoo/bar/hello/world/InnerClasses
    
    AnalysisScope scope = new JavaSourceAnalysisScope();
    // add standard libraries to scope
    String[] stdlibs = WalaProperties.getJ2SEJarFiles();
    for (String stdlib : stdlibs) {
      scope.addToScope(ClassLoaderReference.Primordial, new JarFile(stdlib));
    }
    
    ExampleUtil.addDefaultExclusions(scope);	//增加无效lib库的裁减
    ExampleUtil.addTailExclusions(scope);	//增加无效类的剪切
    
    // add the source directory
    scope.addToScope(JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(sourceDir)));
    
    // build the class hierarchy
    IClassHierarchy cha = ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));
    System.out.println(cha.getNumberOfClasses() + " classes");
    System.out.println(Warnings.asString());
    Warnings.clear();
    AnalysisOptions options = new AnalysisOptions();
    Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE, cha, new String[] { mainClass } );
    options.setEntrypoints(entrypoints);
    // you can dial down reflection handling if you like
//    options.setReflectionOptions(ReflectionOptions.NONE);
    IAnalysisCacheView cache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory());
    //CallGraphBuilder builder = new ZeroCFABuilderFactory().make(options, cache, cha, scope, false);
    CallGraphBuilder<?> builder = new ZeroOneContainerCFABuilderFactory().make(options, cache, cha, scope);
    System.out.println("building call graph...");
    CallGraph cg = builder.makeCallGraph(options, null);
    //System.out.println(cg.toString());
    long end = System.currentTimeMillis();
    System.out.println("done");
    System.out.println("took " + (end-start) + "ms");
    System.out.println(CallGraphStats.getStats(cg));    
     
    DotUtil.dotify(cg, null, DOT_FILE, PDF_FILE, DOT_EXE);	//生成dot+pdf文件
    //DotUtil.dotify(cg, null, DOT_FILE, null, DOT_EXE);	//单生成dot文件
  }

}
