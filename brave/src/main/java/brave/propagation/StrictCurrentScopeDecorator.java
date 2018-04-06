package brave.propagation;

import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext.Scope;

/** Useful when developing instrumentation as state is enforced more strictly. */
public final class StrictCurrentScopeDecorator implements CurrentTraceContext.ScopeDecorator {

  /** Identifies problems by throwing assertion errors when a scope is closed on a different thread. */
  @Override public Scope decorateScope(@Nullable TraceContext currentSpan, Scope scope) {
    return new StrictScope(scope, new Error(String.format("Thread %s opened scope for %s here:",
        Thread.currentThread().getName(), currentSpan)));
  }

  class StrictScope implements Scope {
    final Scope delegate;
    final Throwable caller;
    final long threadId = Thread.currentThread().getId();

    StrictScope(Scope delegate, Throwable caller) {
      this.delegate = delegate;
      this.caller = caller;
    }

    @Override public void close() {
      if (Thread.currentThread().getId() != threadId) {
        throw new IllegalStateException(
            "scope closed in a different thread: " + Thread.currentThread().getName(),
            caller);
      }
      delegate.close();
    }

    @Override public String toString() {
      return caller.toString();
    }
  }
}
