package krankenhaus.model;

public record ZimmerAenderung(Zimmer zimmer, Patient vorher, Patient nachher) {
}
