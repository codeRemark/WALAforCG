package com.ibm.wala.cast.java.ipa.callgraph;

import java.util.HashSet;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * Includes all source methods in an analysis scope as entrypoints.
 */
public class AllSourceEntryPoints extends HashSet<Entrypoint> {
  
  private static final long serialVersionUID = 6541081454519490100L;
  private final static boolean DEBUG = false;

  /**
   * @param scope governing analyais scope
   * @param cha governing class hierarchy
   * @throws IllegalArgumentException if cha is null
   */
  public AllSourceEntryPoints(JavaSourceAnalysisScope scope, final IClassHierarchy cha) {

    if (cha == null) {
      throw new IllegalArgumentException("cha is null");
    }
    for (IClass klass : cha) {
      if (!klass.isInterface()) {
        if (isApplicationClass(scope, klass)) {
          for (IMethod method : klass.getDeclaredMethods()) {
            if (!method.isAbstract()) {
              add(new ArgumentTypeEntrypoint(method, cha));
            }
          }
        }
        if (isSourceClass(scope, klass)) {  //用于分析源代码得到的类
          for (IMethod method : klass.getDeclaredMethods()) {
            if (!method.isAbstract()) {
              add(new ArgumentTypeEntrypoint(method, cha));
            }
          }         
        }
      }
    }
    if (DEBUG) {
      System.err.println((getClass() + "Number of EntryPoints:" + size()));
    }

  }

  /**
   * @return true iff klass is loaded by the application loader.
   */
  private static boolean isApplicationClass(AnalysisScope scope, IClass klass) {
    return scope.getApplicationLoader().equals(klass.getClassLoader().getReference());
  }
  
  /**
   * @return true iff klass is loaded by the source loader.
   */
  private static boolean isSourceClass(JavaSourceAnalysisScope scope, IClass klass) {
    return scope.getSourceLoader().equals(klass.getClassLoader().getReference());
  }
}
