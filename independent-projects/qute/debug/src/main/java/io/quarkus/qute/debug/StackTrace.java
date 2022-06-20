package io.quarkus.qute.debug;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qute.debug.agent.RemoteStackFrame;

public class StackTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<StackFrame> frames;

    public StackTrace(List<RemoteStackFrame> frames) {
        super();
        this.frames = frames //
                .stream() //
                .map(f -> f.getData()) //
                .collect(Collectors.toList());
    }

    /**
     * The frames of the stackframe. If the array has length zero, there are no
     * stackframes available.
     * <p>
     * This means that there is no location information available.
     */
    public List<StackFrame> getStackFrames() {
        return frames;
    }

}
