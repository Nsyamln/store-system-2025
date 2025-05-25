package tokoibuelin.storesystem.model;

public record Response<T>(String code, String message, T data) {

    public static Response<Object> create(String serviceCode, String responseCode, String message, Object data) {
        return new Response<>(serviceCode + responseCode, message, data);
    }

    public static Response<Object> unauthenticated() {
        return new Response<>("0101", "unauthenticated", null);
    }

    public static Response<Object> unauthorized() {
        return new Response<>("0201", "unauthorized", null);
    }


    public static Response<Object> badRequest() {
        return new Response<>("0301", "bad request", null);
    }
}
