package share.model.entity;

public class Question {
    private String question;
    // Usiamo una stringa o un enum per la risposta come suggerito dal VOPC
    private String possibleAnswer;

    public Question(String question) {
        this.question = question;
    }
}