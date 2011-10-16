package edu.washington.cs.detector;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.graph.Graph;

/**
 * A few special cases of method that touches GUI
 * 1. does not change its state (access is OK)
 *    like:  if(uifield != null) ....
 *           { uifield = null; }
 *            uifield.fieldname = 
 * */
public class UIMethodSummarizer {

	public static final String WIDGET_SIG = "Lorg/eclipse/swt/widgets/Widget";
	//Display.wake(),
	public static final String[] THREAD_SAFE_SIGS = {"org.eclipse.swt.widgets.Display.wake()V"};
	
	public final Graph<CGNode> cg;
	public final ClassHierarchy cha;
	public final IClass widgetClass;
	
	private Map<CGNode, List<SSAInstruction>> uiNodes = new LinkedHashMap<CGNode, List<SSAInstruction>>();
	
	public UIMethodSummarizer(Graph<CGNode> cg, ClassHierarchy cha) {
		this.cg = cg;
		this.cha = cha;
		this.widgetClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, WIDGET_SIG));
		assert this.widgetClass != null;
	}
	
	public List<SSAInstruction> lookupUIInstructions(CGNode node) {
		return uiNodes.get(node);
	}
	
	public Set<CGNode> getUINodes() {
		for(CGNode node : this.cg) {
			//check if node touches UI element in an unsafe way
			List<SSAInstruction> uiElements = this.getAccessedUIElements(node);
			if(!uiElements.isEmpty()) {
				uiNodes.put(node, uiElements);
			}
		}
		return uiNodes.keySet();
	}
	
	private List<SSAInstruction> getAccessedUIElements(CGNode node) {
		//System.out.println("CGNode: " + node.getMethod());
		List<SSAInstruction> uiInstrList = new LinkedList<SSAInstruction>();
		IR ir = node.getIR();
		if(ir == null || ir.isEmptyIR()) {
			return uiInstrList;
		}
		Iterator<SSAInstruction> ssaIt = ir.iterateAllInstructions();
		//a visitor to check if the current instruction involves UI instructions or not
		UIElementVisitor visitor = new UIElementVisitor();
		while(ssaIt.hasNext()) {
			SSAInstruction instr = ssaIt.next();
			visitor.clear();
			instr.visit(visitor);
			if(visitor.hasUIElement()) {
				uiInstrList.add(instr);
			}
		}
		return uiInstrList;
	}
	
	class UIElementVisitor extends Visitor {
		boolean touchUIElement = false;
		
		public boolean hasUIElement() {
			return this.touchUIElement;
		}
		public void touchUIElement() {
			this.touchUIElement = true;
		}
		
		public void clear() {
			this.touchUIElement = false;
		}
		//check if a referred type is a UI widget
		private boolean isWidget(TypeReference r) {
			IClass kclass = cha.lookupClass(r);
	    	//c2 is a subclass of c1?
	    	if(kclass == null) { //it is possible kclass is null, such as <Primordial, C>
	    		return false;
	    	}
 	    	return cha.isAssignableFrom(widgetClass, kclass);
		}
		
		/** All the visitor methods below */
		/** Instructions that may be relevant to UI elements*/
	    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
	    	TypeReference r = instruction.getElementType();
	    	if(isWidget(r)) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
	    	if(isWidget(instruction.getElementType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitConversion(SSAConversionInstruction instruction) {
	    	if(this.isWidget(instruction.getFromType()) || this.isWidget(instruction.getToType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
	    	if(this.isWidget(instruction.getType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitGet(SSAGetInstruction instruction) {
	    	if(this.isWidget(instruction.getDeclaredFieldType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitPut(SSAPutInstruction instruction) {
	    	if(this.isWidget(instruction.getDeclaredFieldType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitNew(SSANewInstruction instruction) {
	    	if(this.isWidget(instruction.getConcreteType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
	    	if(this.isWidget(instruction.getType())) {
	    		this.touchUIElement = true;
	    	}
	    }
	    public void visitInvoke(SSAInvokeInstruction instruction) {
	    	//TODO exclude a few thread-safe methods
	    }

	    /** Instructions irrelevant to UI elements */
	    public void visitComparison(SSAComparisonInstruction instruction) {    }
	    public void visitBinaryOp(SSABinaryOpInstruction instruction) {   }
	    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {   }
	    public void visitGoto(SSAGotoInstruction instruction) {  }
	    public void visitSwitch(SSASwitchInstruction instruction) {    }
	    public void visitReturn(SSAReturnInstruction instruction) {    }
	    public void visitArrayLength(SSAArrayLengthInstruction instruction) {    }
	    public void visitThrow(SSAThrowInstruction instruction) {    }
	    public void visitMonitor(SSAMonitorInstruction instruction) {    }
	    public void visitCheckCast(SSACheckCastInstruction instruction) {    }
	    public void visitInstanceof(SSAInstanceofInstruction instruction) {    }
	    public void visitPhi(SSAPhiInstruction instruction) {    }
	    public void visitPi(SSAPiInstruction instruction) {    }
	    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {    }
	    
	}
	
}