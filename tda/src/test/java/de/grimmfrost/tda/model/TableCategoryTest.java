package de.grimmfrost.tda.model;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TableCategoryTest {

    @Test
    public void testAddToCatNodesPreservation() {
        TableCategory cat = new TableCategory("Test", 1);
        ThreadInfo ti = new ThreadInfo("Thread1", "info", "content", 10, new String[]{"Thread1", "ID", "State"});
        DefaultMutableTreeNode nodeForCat = new DefaultMutableTreeNode(ti);
        DefaultMutableTreeNode nodeForTree = new DefaultMutableTreeNode(ti);
        
        cat.addToCatNodes(nodeForCat);
        assertEquals(1, cat.getNodeCount(), "Node should be in category");
        
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
        treeRoot.add(nodeForTree);
        
        assertEquals(1, cat.getNodeCount(), "Node should still be in category after adding DIFFERENT node with same user object to tree");
        
        // The bug was:
        // cat.addToCatNodes(node);
        // treeRoot.add(node); // This removes node from cat.rootNode!
    }

    @Test
    public void testBugReproductionBehavior() {
        TableCategory cat = new TableCategory("Test", 1);
        ThreadInfo ti = new ThreadInfo("Thread1", "info", "content", 10, new String[]{"Thread1", "ID", "State"});
        DefaultMutableTreeNode sharedNode = new DefaultMutableTreeNode(ti);
        
        cat.addToCatNodes(sharedNode);
        assertEquals(1, cat.getNodeCount());
        
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
        treeRoot.add(sharedNode); // This should remove sharedNode from cat's rootNode
        
        // This assertion confirms the behavior of DefaultMutableTreeNode which caused the bug
        assertEquals(0, cat.getNodeCount(), "Node was removed from category because it was added to another parent");
    }
}
