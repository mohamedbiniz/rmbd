package at.ainf.asp.interactive.input;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * Simple extension of the standard listener architecture
 * to find out if listener got results
 */
public interface ASPListener extends ParseTreeListener {

    boolean hasResult();
}
