/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.examples.drivers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.examples.properties.WalaExamplesProperties;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.warnings.Warnings;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.shrikeCT.ClassReader.AttrIterator;

/**
 * Driver that constructs a call graph for an application specified via a scope file.  
 * Useful for getting some code to copy-paste.    
 */
public class ScopeFileCallGraph2 {

  /**
   * Usage: ScopeFileCallGraph -scopeFile file_path [-entryClass class_name |
   * -mainClass class_name]
   * 
   * If given -mainClass, uses main() method of class_name as entrypoint. If
   * given -entryClass, uses all public methods of class_name.
   * 
   * @throws IOException
   * @throws ClassHierarchyException
   * @throws CallGraphBuilderCancelException
   * @throws IllegalArgumentException
   */
  public static void main(String[] args) throws IOException, ClassHierarchyException, IllegalArgumentException,
      CallGraphBuilderCancelException {
    long start = System.currentTimeMillis();
    Properties p = CommandLine.parse(args);
    //String scopeFile = p.getProperty("scopeFile");
    String scopeDir = p.getProperty("scopeDir");
    //String entryClass = p.getProperty("entryClass"); xxx
    //String mainClass = p.getProperty("mainClass");
    String dump = p.getProperty("dump");
    
    //��ȡ���е�info�ļ�
    Collection<String> scopeFileList = new ArrayList<>();
    getAllFileName(scopeDir, scopeFileList);

    if (scopeFileList.isEmpty())
    {
      System.out.println("scope file is empty! exit.");
      return;
    }
    
    for (String scopeFile : scopeFileList) {
      try {
        
        //support additional parse
        Properties q = null;
        try {
          q = WalaExamplesProperties.loadProperties();
          q.putAll(WalaProperties.loadProperties());
        } catch (WalaException e) {
          e.printStackTrace();
          Assertions.UNREACHABLE();
        }
        //create dot dir 
        String subdir = scopeFile.replaceAll("\\\\","/").split("/")[3];
        //create subdir
        String path_str = q.getProperty(WalaProperties.OUTPUT_DIR) + File.separatorChar + subdir;
        File path = new File(path_str);
        if (!path.exists()) { //目录不存在
          path.mkdirs();  //创建一个
        }
        else {  //目录存在
          System.out.println("dt dir exist, jump!");
          continue;  //跳过，处理下一个scope文件
        }
        
        // use exclusions to eliminate certain library packages
        File exclusionsFile = new File("I:\\project\\WALA1\\WALA\\com.ibm.wala.core.tests\\dat\\Java60RegressionExclusions.txt");   //xxx
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFile, exclusionsFile, ScopeFileCallGraph.class.getClassLoader());
        IClassHierarchy cha = ClassHierarchyFactory.make(scope);
        
        for (IClass klass : cha) {

          if (klass.getClassLoader().getReference().equals(scope.getApplicationLoader())) {
            for (IField field : klass.getDeclaredInstanceFields()) {
              FieldReference fieldReference = field.getReference();
//              IAnalysisCacheView cache = new AnalysisCacheImpl(options.getSSAOptions());
//              cache.getDefUse(ir)
              System.out.println(fieldReference.getSignature());
              AttrIterator iter = new AttrIterator();
              
              
            }
          }
      }
        
        System.out.println(cha.getNumberOfClasses() + " classes");
        System.out.println(Warnings.asString());
        Warnings.clear();

        AnalysisOptions options = new AnalysisOptions();
        //��������class
        Iterable<IClass> Classes = com.ibm.wala.ipa.callgraph.impl.Util.makeAllClasses(scope, cha);
        for (IClass entryClass : Classes) {
          //���ÿ��class����һ����ڵ�
          //����options����ڵ�
          //��������
          //xxx �޸ĳɵ�class����entrypoint
          Iterable<Entrypoint> entrypoints = makePublicEntrypoints(cha, entryClass);
          if (entrypoints.toString().length() == 2) {  //Ϊʲô��2��
            continue;
          }
          System.out.println(entrypoints.toString() + " entry point.");
          options.setEntrypoints(entrypoints);
      
          PDF_FILE = entryClass.getReference().getName().toString().replace('/', '_') + ".pdf";
          DOT_FILE = entryClass.getReference().getName().toString().replace('/', '_') + ".dot";
          // you can dial down reflection handling if you like
          options.setReflectionOptions(ReflectionOptions.NONE);
          IAnalysisCacheView cache = new AnalysisCacheImpl();
          // other builders can be constructed with different Util methods
          CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, cache, cha, scope);
          //    CallGraphBuilder builder = Util.makeNCFABuilder(2, options, cache, cha, scope);
          //    CallGraphBuilder builder = Util.makeVanillaNCFABuilder(2, options, cache, cha, scope);
          System.out.println("building call graph...");
          CallGraph cg = builder.makeCallGraph(options, null);
          long end = System.currentTimeMillis();
          System.out.println("done");
          if (dump != null) {
            System.err.println(cg);
          }
          System.out.println("took " + (end-start) + "ms");
          System.out.println(CallGraphStats.getStats(cg));
          if(cg.getNumberOfNodes() > 5000 || cg.getNumberOfNodes() < 5) {  //number too large or too small, blocked in creating dot file, so jump
            System.out.println("classes overload, jump to next one.");
            System.out.println();
            continue;
          }    

          //Graph<CGNode> g = pruneForAppLoader(cg);
          Graph<CGNode> g = pruneForMe(cg);
          System.out.println("pruned node " + g.getNumberOfNodes());
          if(g.getNumberOfNodes() > 1000 || g.getNumberOfNodes() < 5) {  //number too large or too small, blocked in creating dot file, so jump
            System.out.println("pruned node overload, jump to next one.");
            System.out.println();
            continue;
          }
         

          String pdfFile = path_str + File.separatorChar + PDF_FILE;
          String dotFile = path_str + File.separatorChar + DOT_FILE;

          String dotExe = q.getProperty(WalaExamplesProperties.DOT_EXE);
          DotUtil.dotify(g, null, dotFile, null, dotExe); //do not create pdf

          //String gvExe = q.getProperty(WalaExamplesProperties.PDFVIEW_EXE);
          //PDFViewUtil.launchPDFView(pdfFile, gvExe);
        }

      } catch (WalaException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch(NullPointerException e) {
        e.printStackTrace();      
      } catch(ArrayIndexOutOfBoundsException e) {
        e.printStackTrace();
    }
      
    }
    System.out.println("process finished!");
   }

  private static String PDF_FILE = "cg.pdf";
  public static String DOT_FILE = "temp.dt";
  private static Iterable<Entrypoint> makePublicEntrypoints(IClassHierarchy cha, String entryClass) {
    Collection<Entrypoint> result = new ArrayList<>();
    IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application,
        StringStuff.deployment2CanonicalTypeString(entryClass)));
    for (IMethod m : klass.getDeclaredMethods()) {
      if (m.isPublic()) {
        result.add(new DefaultEntrypoint(m, cha));
      }
    }
    return result;
  }
  //xxx
  private static Iterable<Entrypoint> makePublicEntrypoints(IClassHierarchy cha, IClass klass) {
    Collection<Entrypoint> result = new ArrayList<>();
    //IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application,
    //    StringStuff.deployment2CanonicalTypeString(entryClass)));
    for (IMethod m : klass.getDeclaredMethods()) {
      if (m.isPublic() && !m.isInit() && !m.isClinit()) {  //xxx  public and not initial
        result.add(new DefaultEntrypoint(m, cha));
      }
    }
    System.out.println(result.size() + " entry point size.");
    return result;
  }
  
  public static Graph<CGNode> pruneForMe(CallGraph g) {
    Collection<CGNode> mySlice = sliceForMe(g);
    return GraphSlicer.prune(g, new CollectionFilter<>(mySlice));
  }
  
  public static Set<CGNode> sliceForMe(Graph<CGNode> g){
    if (g == null) {
      throw new IllegalArgumentException("g is null");
    }
    HashSet<CGNode> roots = HashSetFactory.make();
    for (CGNode o : g) {
      boolean predAppflag = false;
      boolean currAppflag = false;
      
      //if initial method, jump
      if (o.getMethod().isInit()|| o.getMethod().isClinit()) {
        continue;
      }
      currAppflag = o.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
      // if application type method， save and jump
      if (currAppflag) {
        roots.add(o);
        continue;
      }
      // now is not application type methods
      Iterator<CGNode> predNodes = g.getPredNodes(o);
      // traverse pred nodes 
      while(predNodes.hasNext()) {
        CGNode pred = predNodes.next();
        // flaged if pred node is true
        predAppflag = predAppflag || pred.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
      }
      // pred is application type, and curr is not application type
      if (predAppflag) {
        roots.add(o);
      }
    }

    Set<CGNode> result = DFS.getReachableNodes(GraphInverter.invert(g), roots);

    return result;
  }
  
  public static Graph<CGNode> pruneForAppLoader(CallGraph g) {
    return PDFTypeHierarchy.pruneGraph(g, new ApplicationLoaderFilter());
  }
  /**
   * A filter that accepts WALA objects that "belong" to the application loader.
   * 
   * Currently supported WALA types include
   * <ul>
   * <li> {@link CGNode}
   * <li> {@link LocalPointerKey}
   * </ul>
   */
  private static class ApplicationLoaderFilter implements Predicate<CGNode> {

    @Override public boolean test(CGNode o) {  //
      if (o == null)
        return false;
      
      return o.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
    }
  }
  /**
  * ��ȡһ���ļ����µ������ļ�ȫ·��
  * @param path
  * @param listFileName
  */
  public static void getAllFileName(String path,Collection<String> listFileName){
    File file = new File(path);
    File[] files = file.listFiles();
    for(int i = 0; i < files.length; i++) {
      String fileName = files[i].getName();
      if (files[i].isFile() && fileName.substring(fileName.lastIndexOf(".") + 1).equals("txt")) {
        listFileName.add(files[i].toString());
      }
      else if (files[i].isDirectory()) {
        getAllFileName(files[i].toString(), listFileName);
      }
    }
 }

}
