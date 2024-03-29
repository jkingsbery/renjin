package r.compiler.ir.ssa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import r.compiler.CompilerTestCase;
import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.CfgPredicates;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.cfg.DominanceTree;
import r.compiler.ir.tac.IRBody;
import r.compiler.ir.tac.expressions.EnvironmentVariable;

import com.google.common.collect.Iterables;

public class SsaTransformTest extends CompilerTestCase {

  @Test
  public void cytronSsa() throws IOException {
    IRBody block = parseCytron();
    ControlFlowGraph cfg = new ControlFlowGraph(block);

    Iterable<BasicBlock> assignmentsToK = Iterables.filter(cfg.getBasicBlocks(), 
        CfgPredicates.containsAssignmentTo(new EnvironmentVariable("K")));
    
    assertThat(Iterables.size(assignmentsToK), equalTo(3));
    
    
    DominanceTree dtree = new DominanceTree(cfg);
    System.out.println(dtree);
    
    SsaTransformer transformer = new SsaTransformer(cfg, dtree);
    transformer.transform();
    
    // See Figure 6 in
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf
    
    
    // just before branching in basic block #2,
    // we need phi functions for all 4 variables
    
    BasicBlock bb2 = cfg.getBasicBlocks().get(1);
    assertThat(bb2.getStatements().size(), equalTo(5));
   
    System.out.println(cfg);
  }
  

  @Test
  public void forLoop() throws IOException {
    IRBody block = buildScope("for(i in 1:10) { n<-x[i]; print(n); }");
    
    System.out.println(block);
    
    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
   
    DominanceTree dtree = new DominanceTree(cfg);
    
    System.out.println("CFG:");
    System.out.println(cfg.getGraph());
    
    System.out.println("Dominance Tree:");  
    System.out.println(dtree);
    
    SsaTransformer transformer = new SsaTransformer(cfg, dtree);
    transformer.transform();
     
    System.out.println(cfg);
  }
  
}
