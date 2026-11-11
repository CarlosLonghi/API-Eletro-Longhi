package br.com.carloslonghi.eletrolonghi.exception;

/**
 * Nome em português das entidades citadas em mensagens de erro, com o gênero
 * gramatical para concordar artigo e particípio ("a marca ... removida",
 * "o aparelho ... removido").
 */
public enum DomainEntity {
    BRAND("marca", "marcas", true),
    ACCESSORY("acessório", "acessórios", false),
    CUSTOMER("cliente", "clientes", false),
    DEVICE("aparelho", "aparelhos", false),
    REPAIR_ORDER("ordem de reparo", "ordens de reparo", true),
    PAYMENT("pagamento", "pagamentos", false);

    private final String singular;
    private final String plural;
    private final boolean feminine;

    DomainEntity(String singular, String plural, boolean feminine) {
        this.singular = singular;
        this.plural = plural;
        this.feminine = feminine;
    }

    String singular() {
        return singular;
    }

    String plural() {
        return plural;
    }

    /** Artigo definido no singular: "a" ou "o". */
    String article() {
        return feminine ? "a" : "o";
    }

    /** Flexiona um radical no singular: "removid" → "removida"/"removido". */
    String agree(String stem) {
        return stem + (feminine ? "a" : "o");
    }

    /** Flexiona um radical no plural: "vinculad" → "vinculadas"/"vinculados". */
    String agreePlural(String stem) {
        return stem + (feminine ? "as" : "os");
    }

    static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
