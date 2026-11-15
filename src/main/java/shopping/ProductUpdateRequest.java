package shopping;

public record ProductUpdateRequest(
        String name,
        Integer price,
        String imageUrl) {
}
