package io.quarkus.qute.debug;

import java.rmi.RemoteException;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.Variable;

public interface Debugger {

    /**
     * Returns the remote debugger state.
     *
     * @return the remote debugger state.
     */
    DebuggerState getState(long threadId) ;

    void pause(long threadId) ;

    void resume(long threadId) ;

    Breakpoint getBreakpoint(String templateId, int line) ;

    Breakpoint setBreakpoint(String templateId, int line) ;

    Thread[] getThreads() ;

    Thread getThread(long threadId) ;

    StackTrace getStackTrace(long threadId) ;

    /**
     * Returns the variable scopes for the given stackframe ID <code>frameId</code>.
     *
     * @param frameId the stackframe ID
     *
     *
     * @return the variable scopes for the given stackframe ID <code>frameId</code>.
     * @
     */
    Scope[] getScopes(int frameId) ;

    /**
     * Retrieves all child variables for the given variable reference.
     *
     * @param variablesReference the Variable reference.
     * @return all child variables for the given variable reference.
     *
     * @
     */
    Variable[] getVariables(int variablesReference) ;

    void terminate() ;

    void stepIn(long threadId) ;

    void stepOut(long threadId) ;

    void stepOver(long threadId) ;

    void addDebuggerListener(DebuggerListener listener) ;

    void removeDebuggerListener(DebuggerListener listener) ;

}
