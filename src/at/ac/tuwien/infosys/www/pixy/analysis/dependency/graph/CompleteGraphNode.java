package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

/**
 * Special node for approximating SCCs in the string graph.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CompleteGraphNode extends AbstractNode {
    CompleteGraphNode() {
    }

    /**
     * Returns a name that can be used in dot file representation.
     *
     * @return
     */
    public String dotName() {
        return "SCC";
    }

    public String comparableName() {
        return dotName();
    }

    public String dotNameShort() {
        return dotName();
    }

    public String dotNameVerbose(boolean isModelled) {
        return dotName();
    }
}