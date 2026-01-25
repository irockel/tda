package de.grimmfrost.tda.parser;

import de.grimmfrost.tda.model.Category;
import de.grimmfrost.tda.model.ThreadDumpInfo;
import de.grimmfrost.tda.model.ThreadInfo;
import org.junit.jupiter.api.Test;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JCmdJSONParserTest {

    @Test
    public void testJSONDumpParsing() throws Exception {
        System.out.println("testJSONDumpParsing");
        InputStream dumpFileStream = new FileInputStream("src/test/resources/intellij_dump.json");
        Map threadStore = new HashMap();
        DumpParser instance = DumpParserFactory.get().getDumpParserForLogfile(dumpFileStream, threadStore, false, 0);
        
        assertTrue(instance instanceof JCmdJSONParser);
        
        DefaultMutableTreeNode result = (DefaultMutableTreeNode) instance.parseNext();
        assertNotNull(result);
        
        ThreadDumpInfo tdi = (ThreadDumpInfo) result.getUserObject();
        assertEquals("Dump No. 1", tdi.getName());
        assertEquals("2026-01-25T15:46:04.439828Z", tdi.getStartTime());
        
        // Check threads
        Category threadsCat = tdi.getThreads();
        assertNotNull(threadsCat);
        // The intellij_dump.json has 8 threads in <root> container
        assertEquals(8, threadsCat.getNodeCount());
        
        DefaultMutableTreeNode firstThreadNode = (DefaultMutableTreeNode) threadsCat.getNodeAt(0);
        ThreadInfo firstThread = (ThreadInfo) firstThreadNode.getUserObject();
        assertTrue(firstThread.getName().contains("Reference Handler"));
        assertTrue(firstThread.getContent().contains("java.base/java.lang.ref.Reference.waitForReferencePendingList(Native Method)"));
        
        // Check tid mapping
        String[] tokens = firstThread.getTokens();
        assertNotNull(tokens);
        assertEquals("Reference Handler", tokens[0]);
        assertEquals("9", tokens[3]);
        
        assertFalse(instance.hasMoreDumps());
    }
}
