package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallPreparation extends AbstractTransferFunction {
    private List<List<Variable>> cbrParams;
    private TacFunction caller;
    private AliasAnalysis aliasAnalysis;
    private AbstractCfgNode cfgNode;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public CallPreparation(TacFunction caller, AliasAnalysis aliasAnalysis, at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode) {

        this.cfgNode = cfgNode;
        this.cbrParams = cfgNode.getCbrParams();
        this.caller = caller;
        this.aliasAnalysis = aliasAnalysis;
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        AliasLatticeElement in = (AliasLatticeElement) inX;
        AliasLatticeElement out = new AliasLatticeElement(in);

        // see alias analysis tutorial for an explanation how this works

        if (!this.cbrParams.isEmpty()) {

            // note: alias analysis does not have to care about default params

            // note: at this point, it should already be ensured that there
            // are at least as many formal params as actual params

            SymbolTable placeHolderSymTab = new SymbolTable("_placeHolder");
            // placeholder -> real formal
            Map<Variable, Variable> replacements = new HashMap<>();

            // for all cbr-params...
            for (List<Variable> pairList : this.cbrParams) {
                Iterator<Variable> pairListIterator = pairList.iterator();
                Variable actualVar = pairListIterator.next();
                Variable formalVar = pairListIterator.next();

                Variable formalPlaceHolder = new Variable(formalVar.getName(), placeHolderSymTab);
                replacements.put(formalPlaceHolder, formalVar);

                // add the formal's placeholder to the actual's must-alias-group
                out.addToGroup(formalPlaceHolder, actualVar);

                // see the function in MayAliases.java for an explanation
                out.createAdjustedPairCopies(actualVar, formalPlaceHolder);
            }

            // remove all local variables that belong to the symbol table of the
            // caller; shortcut: if the caller is main, we don't have to do
            // this (since there are no real local variables in the main function)
            SymbolTable callerSymTab = this.caller.getSymbolTable();
            if (!callerSymTab.isMain()) {
                out.removeVariables(callerSymTab);
            }

            // replace the placeholders by the formals of the callee
            out.replace(replacements);
        } else {
            // there are no cbr params; hence, we can simply remove
            // all local variables of the caller
            // (note: at this point, the only locals that can appear
            // in the analysis info are those of the caller, so this
            // reduces to removing all local variables)
            out.removeLocals();
        }

        // recycle
        out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

        return out;
    }
}