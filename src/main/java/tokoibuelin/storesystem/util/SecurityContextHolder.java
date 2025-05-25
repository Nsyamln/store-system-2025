package tokoibuelin.storesystem.util;

import tokoibuelin.storesystem.model.Authentication;

public final class SecurityContextHolder {

    private static final Authentication UNAUTHENTICATED = new Authentication(null, null, false);

    private static final Authentication UNAUTHORIZED = new Authentication(null, null, true);


    private static final ThreadLocal<Authentication> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Get authentication object.
     *
     * @return return {@link Authentication}.
     */
    public static Authentication getAuthentication() {
        final Authentication authentication = THREAD_LOCAL.get();
        System.out.println("CEK THREADLOCAL.get() : "+THREAD_LOCAL.get());
        System.out.println("CEK THREAD_LOCAL : "+authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            return UNAUTHENTICATED;
        }
        if (authentication.id() == null || authentication.role() == null) {
            return UNAUTHORIZED;
        }
        return authentication;
    }

    /**
     * Set authentication.
     *
     * @param principal authentication.
     */

    public static void setAuthentication(Authentication principal) {
        System.out.println("CEK PRINCIPAL : "+principal);
        THREAD_LOCAL.set(principal);
        System.out.println("CEK PRINCIPAL AFTER SET: "+THREAD_LOCAL.get());
    }

    /**
     * Clear authentication.
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }
}

