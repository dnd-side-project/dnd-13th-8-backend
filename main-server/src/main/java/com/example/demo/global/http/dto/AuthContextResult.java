package com.example.demo.global.http.dto;


public final class AuthContextResult {
    public enum Reason { MISSING_REFRESH, MISSING_SESSION, MISSING_USER }

    private final boolean ok;
    private final AuthContext context;
    private final Reason reason;

    private AuthContextResult(boolean ok, AuthContext context, Reason reason) {
        this.ok = ok; this.context = context; this.reason = reason;
    }

    public static AuthContextResult ok(AuthContext ctx) { return new AuthContextResult(true, ctx, null); }
    public static AuthContextResult fail(Reason reason) { return new AuthContextResult(false, null, reason); }

    public boolean isOk() { return ok; }
    public AuthContext context() { return context; }
    public Reason reason() { return reason; }
}
