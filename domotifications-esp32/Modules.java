public class Module {

    private int idModule;
    private String nomModule;
    private TypeModule typeModule;
    private boolean etatActivation;
    private boolean etatModification;

    public enum TypeModule {
        BoiteAuxLettres,
        Machine,
        Poubelle
    }


}